package com.acessorinvestimentos.perfilinvestidor.integration;

import com.acessorinvestimentos.perfilinvestidor.controller.ProfileController;
import com.acessorinvestimentos.perfilinvestidor.repository.InvestorProfileRepository;
import com.acessorinvestimentos.perfilinvestidor.repository.QuestionnaireResponseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sobe a aplicação inteira com H2 (migrations do Flyway incluídas) e o RabbitMQ mockado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProfileFlowIntegrationTest {

    private static final String USER_ID = "auth0|integration";

    private static final String AGGRESSIVE_ANSWERS = """
            {"financialGoal":"BUILD_WEALTH","riskTolerance":"HIGH","investmentHorizon":"LONG_TERM",
             "hasEmergencyReserve":true,"experienceLevel":"ADVANCED"}
            """;

    private static final String CONSERVATIVE_ANSWERS = """
            {"financialGoal":"PRESERVE_CAPITAL","riskTolerance":"LOW","investmentHorizon":"SHORT_TERM",
             "hasEmergencyReserve":false,"experienceLevel":"BEGINNER"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private QuestionnaireResponseRepository questionnaireRepository;

    @Autowired
    private InvestorProfileRepository profileRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @BeforeEach
    void cleanDatabase() {
        profileRepository.deleteAll();
        questionnaireRepository.deleteAll();
    }

    @Test
    void fullQuestionnaireFlow() throws Exception {
        // 1. Responde o questionário -> perfil arrojado
        mockMvc.perform(post("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGGRESSIVE_ANSWERS))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.investorType").value("AGGRESSIVE"))
                .andExpect(jsonPath("$.score").value(11))
                .andExpect(jsonPath("$.id").exists());

        // 2. Responder de novo com POST não é permitido
        mockMvc.perform(post("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(AGGRESSIVE_ANSWERS))
                .andExpect(status().isConflict());

        // 3. Consulta o perfil (formato usado pelo Serviço de Recomendações)
        mockMvc.perform(get("/profiles/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.investorType").value("AGGRESSIVE"));

        // 4. Atualiza as respostas -> perfil conservador
        mockMvc.perform(put("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CONSERVATIVE_ANSWERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investorType").value("CONSERVATIVE"))
                .andExpect(jsonPath("$.score").value(0));

        // 5. Consulta as respostas salvas
        mockMvc.perform(get("/profiles/{userId}/questionnaire", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskTolerance").value("LOW"))
                .andExpect(jsonPath("$.hasEmergencyReserve").value(false));

        assertEquals(1, questionnaireRepository.count());
        assertEquals(1, profileRepository.count());

        // Um evento na criação e outro na mudança de perfil
        verify(rabbitTemplate, times(2))
                .convertAndSend(eq("investment.events"), eq("profile.updated"), any(Object.class));
    }

    @Test
    void incompleteQuestionnaire_returns400AndSavesNothing() throws Exception {
        mockMvc.perform(post("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"financialGoal\":\"RETIREMENT\",\"hasEmergencyReserve\":true}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.length()").value(3));

        assertEquals(0, questionnaireRepository.count());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void unknownUser_returns404() throws Exception {
        mockMvc.perform(get("/profiles/{userId}", "auth0|nobody"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/profiles/{userId}/questionnaire", "auth0|nobody"))
                .andExpect(status().isNotFound());
    }
}
