package com.acessorinvestimentos.perfilinvestidor.Service;

import br.insper.investorprofile.controller.ProfileController;
import br.insper.investorprofile.dto.InvestorProfileResponse;
import br.insper.investorprofile.dto.QuestionnaireDto;
import br.insper.investorprofile.exception.IncompleteQuestionnaireException;
import br.insper.investorprofile.exception.QuestionnaireAlreadyExistsException;
import br.insper.investorprofile.exception.ResourceNotFoundException;
import br.insper.investorprofile.model.enums.ExperienceLevel;
import br.insper.investorprofile.model.enums.FinancialGoal;
import br.insper.investorprofile.model.enums.InvestmentHorizon;
import br.insper.investorprofile.model.enums.InvestorType;
import br.insper.investorprofile.model.enums.RiskTolerance;
import br.insper.investorprofile.service.InvestorProfileService;
import br.insper.investorprofile.service.QuestionnaireService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    private static final String USER_ID = "auth0|123";
    private static final String BODY = """
            {
              "financialGoal": "BUILD_WEALTH",
              "riskTolerance": "HIGH",
              "investmentHorizon": "LONG_TERM",
              "hasEmergencyReserve": true,
              "experienceLevel": "ADVANCED"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionnaireService questionnaireService;

    @MockitoBean
    private InvestorProfileService investorProfileService;

    private InvestorProfileResponse profile(InvestorType type, int score) {
        LocalDateTime now = LocalDateTime.now();
        return new InvestorProfileResponse(UUID.randomUUID(), USER_ID, type, score, now, now);
    }

    @Test
    void submitQuestionnaire_returns201WithProfile() throws Exception {
        when(questionnaireService.submitQuestionnaire(eq(USER_ID), any()))
                .thenReturn(profile(InvestorType.AGGRESSIVE, 11));

        mockMvc.perform(post("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.investorType").value("AGGRESSIVE"))
                .andExpect(jsonPath("$.score").value(11));
    }

    @Test
    void submitQuestionnaire_withoutUserHeader_returns400() throws Exception {
        mockMvc.perform(post("/profiles/questionnaire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(questionnaireService);
    }

    @Test
    void submitQuestionnaire_incomplete_returns400WithMissingQuestions() throws Exception {
        when(questionnaireService.submitQuestionnaire(eq(USER_ID), any()))
                .thenThrow(new IncompleteQuestionnaireException(List.of("riskTolerance")));

        mockMvc.perform(post("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"financialGoal\": \"RETIREMENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("All questions must be answered"))
                .andExpect(jsonPath("$.details[0]").value("riskTolerance"));
    }

    @Test
    void submitQuestionnaire_alreadyAnswered_returns409() throws Exception {
        when(questionnaireService.submitQuestionnaire(eq(USER_ID), any()))
                .thenThrow(new QuestionnaireAlreadyExistsException(USER_ID));

        mockMvc.perform(post("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isConflict());
    }

    @Test
    void submitQuestionnaire_invalidAnswerValue_returns400() throws Exception {
        String invalid = BODY.replace("\"HIGH\"", "\"VERY_HIGH\"");

        mockMvc.perform(post("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verifyNoInteractions(questionnaireService);
    }

    @Test
    void submitQuestionnaire_blankUserId_returns400() throws Exception {
        when(questionnaireService.submitQuestionnaire(anyString(), any()))
                .thenThrow(new IllegalArgumentException("userId is required"));

        mockMvc.perform(post("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, " ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("userId is required"));
    }

    @Test
    void updateQuestionnaire_returns200WithProfile() throws Exception {
        when(questionnaireService.updateQuestionnaire(eq(USER_ID), any()))
                .thenReturn(profile(InvestorType.MODERATE, 6));

        mockMvc.perform(put("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.investorType").value("MODERATE"));
    }

    @Test
    void updateQuestionnaire_neverAnswered_returns404() throws Exception {
        when(questionnaireService.updateQuestionnaire(eq(USER_ID), any()))
                .thenThrow(new ResourceNotFoundException("Questionnaire not found for user " + USER_ID));

        mockMvc.perform(put("/profiles/questionnaire")
                        .header(ProfileController.USER_ID_HEADER, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isNotFound());
    }

    @Test
    void getProfile_returns200() throws Exception {
        when(investorProfileService.getProfileByUserId(USER_ID)).thenReturn(profile(InvestorType.CONSERVATIVE, 2));

        mockMvc.perform(get("/profiles/{userId}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.investorType").value("CONSERVATIVE"));
    }

    @Test
    void getProfile_unknownUser_returns404() throws Exception {
        when(investorProfileService.getProfileByUserId(USER_ID))
                .thenThrow(new ResourceNotFoundException("Profile not found for user " + USER_ID));

        mockMvc.perform(get("/profiles/{userId}", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void getQuestionnaire_returns200() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        when(questionnaireService.getQuestionnaireByUserId(USER_ID)).thenReturn(new QuestionnaireDto(
                UUID.randomUUID(), USER_ID, FinancialGoal.RETIREMENT, RiskTolerance.MEDIUM,
                InvestmentHorizon.LONG_TERM, true, ExperienceLevel.INTERMEDIATE, now, now));

        mockMvc.perform(get("/profiles/{userId}/questionnaire", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.financialGoal").value("RETIREMENT"))
                .andExpect(jsonPath("$.hasEmergencyReserve").value(true));
    }
}
