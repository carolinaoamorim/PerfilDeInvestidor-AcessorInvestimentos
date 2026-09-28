package com.acessorinvestimentos.perfilinvestidor.unit;

import com.acessorinvestimentos.perfilinvestidor.dto.ClassificationResult;
import com.acessorinvestimentos.perfilinvestidor.dto.InvestorProfileResponse;
import com.acessorinvestimentos.perfilinvestidor.event.ProfileEventPublisher;
import com.acessorinvestimentos.perfilinvestidor.exception.ResourceNotFoundException;
import com.acessorinvestimentos.perfilinvestidor.mapper.ProfileMapper;
import com.acessorinvestimentos.perfilinvestidor.model.InvestorProfile;
import com.acessorinvestimentos.perfilinvestidor.model.QuestionnaireResponse;
import com.acessorinvestimentos.perfilinvestidor.model.enums.ExperienceLevel;
import com.acessorinvestimentos.perfilinvestidor.model.enums.FinancialGoal;
import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestmentHorizon;
import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestorType;
import com.acessorinvestimentos.perfilinvestidor.model.enums.RiskTolerance;
import com.acessorinvestimentos.perfilinvestidor.repository.InvestorProfileRepository;
import com.acessorinvestimentos.perfilinvestidor.repository.QuestionnaireResponseRepository;
import com.acessorinvestimentos.perfilinvestidor.service.InvestorProfileService;
import com.acessorinvestimentos.perfilinvestidor.service.ProfileClassificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestorProfileServiceTest {

    private static final String USER_ID = "auth0|123";

    @Mock
    private InvestorProfileRepository profileRepository;

    @Mock
    private QuestionnaireResponseRepository questionnaireRepository;

    @Spy
    private ProfileClassificationService classificationService = new ProfileClassificationService();

    @Mock
    private ProfileEventPublisher eventPublisher;

    @Spy
    private ProfileMapper mapper = new ProfileMapper();

    @InjectMocks
    private InvestorProfileService service;

    private QuestionnaireResponse aggressiveQuestionnaire() {   // 11 pontos
        return questionnaire(FinancialGoal.BUILD_WEALTH, RiskTolerance.HIGH,
                InvestmentHorizon.LONG_TERM, true, ExperienceLevel.ADVANCED);
    }

    private QuestionnaireResponse moderateQuestionnaire() {     // 6 pontos
        return questionnaire(FinancialGoal.GENERATE_INCOME, RiskTolerance.MEDIUM,
                InvestmentHorizon.MEDIUM_TERM, true, ExperienceLevel.INTERMEDIATE);
    }

    private QuestionnaireResponse questionnaire(FinancialGoal goal, RiskTolerance risk, InvestmentHorizon horizon,
                                                boolean reserve, ExperienceLevel experience) {
        return QuestionnaireResponse.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .financialGoal(goal)
                .riskTolerance(risk)
                .investmentHorizon(horizon)
                .hasEmergencyReserve(reserve)
                .experienceLevel(experience)
                .build();
    }

    private InvestorProfile existingProfile(InvestorType type, int score) {
        return InvestorProfile.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .investorType(type)
                .score(score)
                .createdAt(LocalDateTime.now().minusDays(1))
                .updatedAt(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void getProfileByUserId_returnsProfile() {
        InvestorProfile profile = existingProfile(InvestorType.MODERATE, 6);
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));

        InvestorProfileResponse response = service.getProfileByUserId(USER_ID);

        assertEquals(profile.getId(), response.id());
        assertEquals(USER_ID, response.userId());
        assertEquals(InvestorType.MODERATE, response.investorType());
        assertEquals(6, response.score());
    }

    @Test
    void getProfileByUserId_whenMissing_throwsNotFound() {
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getProfileByUserId(USER_ID));
    }

    @Test
    void calculateProfile_returnsScoreAndType() {
        when(questionnaireRepository.findByUserId(USER_ID)).thenReturn(Optional.of(aggressiveQuestionnaire()));

        ClassificationResult result = service.calculateProfile(USER_ID);

        assertEquals(11, result.score());
        assertEquals(InvestorType.AGGRESSIVE, result.investorType());
        verifyNoInteractions(profileRepository, eventPublisher);
    }

    @Test
    void calculateProfile_withoutQuestionnaire_throwsNotFound() {
        when(questionnaireRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.calculateProfile(USER_ID));
    }

    @Test
    void updateProfile_firstTime_createsProfileAndPublishesEvent() {
        when(questionnaireRepository.findByUserId(USER_ID)).thenReturn(Optional.of(moderateQuestionnaire()));
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
        when(profileRepository.save(any(InvestorProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        InvestorProfileResponse response = service.updateProfile(USER_ID);

        assertEquals(USER_ID, response.userId());
        assertEquals(InvestorType.MODERATE, response.investorType());
        assertEquals(6, response.score());
        verify(eventPublisher).publishProfileUpdated(USER_ID, InvestorType.MODERATE);
    }

    @Test
    void updateProfile_sameInvestorType_updatesScoreWithoutPublishing() {
        InvestorProfile profile = existingProfile(InvestorType.MODERATE, 5);
        when(questionnaireRepository.findByUserId(USER_ID)).thenReturn(Optional.of(moderateQuestionnaire()));
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);

        InvestorProfileResponse response = service.updateProfile(USER_ID);

        assertEquals(6, response.score());
        assertEquals(InvestorType.MODERATE, response.investorType());
        verify(eventPublisher, never()).publishProfileUpdated(anyString(), any());
    }

    @Test
    void updateProfile_investorTypeChanged_keepsSameProfileAndPublishesEvent() {
        InvestorProfile profile = existingProfile(InvestorType.CONSERVATIVE, 2);
        UUID originalId = profile.getId();
        when(questionnaireRepository.findByUserId(USER_ID)).thenReturn(Optional.of(aggressiveQuestionnaire()));
        when(profileRepository.findByUserId(USER_ID)).thenReturn(Optional.of(profile));
        when(profileRepository.save(profile)).thenReturn(profile);

        InvestorProfileResponse response = service.updateProfile(USER_ID);

        assertEquals(originalId, response.id());
        assertEquals(InvestorType.AGGRESSIVE, response.investorType());
        assertEquals(11, response.score());
        verify(eventPublisher).publishProfileUpdated(USER_ID, InvestorType.AGGRESSIVE);
    }

    @Test
    void updateProfile_withoutQuestionnaire_throwsAndSavesNothing() {
        when(questionnaireRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.updateProfile(USER_ID));
        verify(profileRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
