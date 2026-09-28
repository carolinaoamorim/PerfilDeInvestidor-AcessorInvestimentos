package com.acessorinvestimentos.perfilinvestidor.Service;

import br.insper.investorprofile.dto.InvestorProfileResponse;
import br.insper.investorprofile.dto.QuestionnaireDto;
import br.insper.investorprofile.dto.QuestionnaireRequest;
import br.insper.investorprofile.exception.IncompleteQuestionnaireException;
import br.insper.investorprofile.exception.QuestionnaireAlreadyExistsException;
import br.insper.investorprofile.exception.ResourceNotFoundException;
import br.insper.investorprofile.mapper.ProfileMapper;
import br.insper.investorprofile.model.QuestionnaireResponse;
import br.insper.investorprofile.model.enums.ExperienceLevel;
import br.insper.investorprofile.model.enums.FinancialGoal;
import br.insper.investorprofile.model.enums.InvestmentHorizon;
import br.insper.investorprofile.model.enums.InvestorType;
import br.insper.investorprofile.model.enums.RiskTolerance;
import br.insper.investorprofile.repository.QuestionnaireResponseRepository;
import br.insper.investorprofile.service.InvestorProfileService;
import br.insper.investorprofile.service.QuestionnaireService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionnaireServiceTest {

    private static final String USER_ID = "auth0|123";

    @Mock
    private QuestionnaireResponseRepository questionnaireRepository;

    @Mock
    private InvestorProfileService investorProfileService;

    @Spy
    private ProfileMapper mapper = new ProfileMapper();

    @InjectMocks
    private QuestionnaireService service;

    private QuestionnaireRequest aggressiveRequest() {
        return new QuestionnaireRequest(FinancialGoal.BUILD_WEALTH, RiskTolerance.HIGH,
                InvestmentHorizon.LONG_TERM, true, ExperienceLevel.ADVANCED);
    }

    private QuestionnaireRequest conservativeRequest() {
        return new QuestionnaireRequest(FinancialGoal.PRESERVE_CAPITAL, RiskTolerance.LOW,
                InvestmentHorizon.SHORT_TERM, false, ExperienceLevel.BEGINNER);
    }

    private InvestorProfileResponse profile(InvestorType type, int score) {
        LocalDateTime now = LocalDateTime.now();
        return new InvestorProfileResponse(UUID.randomUUID(), USER_ID, type, score, now, now);
    }

    private QuestionnaireResponse savedQuestionnaire() {
        return QuestionnaireResponse.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .financialGoal(FinancialGoal.BUILD_WEALTH)
                .riskTolerance(RiskTolerance.HIGH)
                .investmentHorizon(InvestmentHorizon.LONG_TERM)
                .hasEmergencyReserve(true)
                .experienceLevel(ExperienceLevel.ADVANCED)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void submitQuestionnaire_savesAnswersAndCalculatesProfile() {
        InvestorProfileResponse expected = profile(InvestorType.AGGRESSIVE, 11);
        when(questionnaireRepository.existsByUserId(USER_ID)).thenReturn(false);
        when(investorProfileService.updateProfile(USER_ID)).thenReturn(expected);

        InvestorProfileResponse result = service.submitQuestionnaire(USER_ID, aggressiveRequest());

        ArgumentCaptor<QuestionnaireResponse> captor = ArgumentCaptor.forClass(QuestionnaireResponse.class);
        verify(questionnaireRepository).save(captor.capture());
        QuestionnaireResponse saved = captor.getValue();
        assertEquals(USER_ID, saved.getUserId());
        assertEquals(FinancialGoal.BUILD_WEALTH, saved.getFinancialGoal());
        assertEquals(RiskTolerance.HIGH, saved.getRiskTolerance());
        assertEquals(InvestmentHorizon.LONG_TERM, saved.getInvestmentHorizon());
        assertTrue(saved.getHasEmergencyReserve());
        assertEquals(ExperienceLevel.ADVANCED, saved.getExperienceLevel());
        assertEquals(expected, result);
    }

    @Test
    void submitQuestionnaire_whenAlreadyAnswered_throwsConflict() {
        when(questionnaireRepository.existsByUserId(USER_ID)).thenReturn(true);

        assertThrows(QuestionnaireAlreadyExistsException.class,
                () -> service.submitQuestionnaire(USER_ID, aggressiveRequest()));

        verify(questionnaireRepository, never()).save(any());
        verifyNoInteractions(investorProfileService);
    }

    @Test
    void submitQuestionnaire_withMissingAnswers_listsMissingQuestions() {
        QuestionnaireRequest incomplete = new QuestionnaireRequest(
                FinancialGoal.RETIREMENT, null, InvestmentHorizon.LONG_TERM, true, null);

        IncompleteQuestionnaireException ex = assertThrows(IncompleteQuestionnaireException.class,
                () -> service.submitQuestionnaire(USER_ID, incomplete));

        assertEquals(List.of("riskTolerance", "experienceLevel"), ex.getMissingFields());
        verifyNoInteractions(questionnaireRepository, investorProfileService);
    }

    @Test
    void submitQuestionnaire_withNoAnswers_listsAllQuestions() {
        QuestionnaireRequest empty = new QuestionnaireRequest(null, null, null, null, null);

        IncompleteQuestionnaireException ex = assertThrows(IncompleteQuestionnaireException.class,
                () -> service.submitQuestionnaire(USER_ID, empty));

        assertEquals(5, ex.getMissingFields().size());
    }

    @Test
    void submitQuestionnaire_withBlankUserId_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> service.submitQuestionnaire("  ", aggressiveRequest()));
        assertThrows(IllegalArgumentException.class,
                () -> service.submitQuestionnaire(null, aggressiveRequest()));
        verifyNoInteractions(questionnaireRepository);
    }

    @Test
    void submitQuestionnaire_withNullRequest_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.submitQuestionnaire(USER_ID, null));
    }

    @Test
    void updateQuestionnaire_changesAnswersAndRecalculatesProfile() {
        QuestionnaireResponse existing = savedQuestionnaire();
        InvestorProfileResponse expected = profile(InvestorType.CONSERVATIVE, 0);
        when(questionnaireRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));
        when(investorProfileService.updateProfile(USER_ID)).thenReturn(expected);

        InvestorProfileResponse result = service.updateQuestionnaire(USER_ID, conservativeRequest());

        assertEquals(FinancialGoal.PRESERVE_CAPITAL, existing.getFinancialGoal());
        assertEquals(RiskTolerance.LOW, existing.getRiskTolerance());
        assertEquals(InvestmentHorizon.SHORT_TERM, existing.getInvestmentHorizon());
        assertFalse(existing.getHasEmergencyReserve());
        assertEquals(ExperienceLevel.BEGINNER, existing.getExperienceLevel());
        verify(questionnaireRepository).save(existing);
        assertEquals(expected, result);
    }

    @Test
    void updateQuestionnaire_whenNeverAnswered_throwsNotFound() {
        when(questionnaireRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateQuestionnaire(USER_ID, conservativeRequest()));
        verifyNoInteractions(investorProfileService);
    }

    @Test
    void updateQuestionnaire_withMissingAnswers_throwsBeforeTouchingDatabase() {
        QuestionnaireRequest incomplete = new QuestionnaireRequest(
                null, RiskTolerance.LOW, InvestmentHorizon.SHORT_TERM, false, ExperienceLevel.BEGINNER);

        assertThrows(IncompleteQuestionnaireException.class,
                () -> service.updateQuestionnaire(USER_ID, incomplete));
        verifyNoInteractions(questionnaireRepository);
    }

    @Test
    void getQuestionnaireByUserId_returnsAnswers() {
        QuestionnaireResponse existing = savedQuestionnaire();
        when(questionnaireRepository.findByUserId(USER_ID)).thenReturn(Optional.of(existing));

        QuestionnaireDto dto = service.getQuestionnaireByUserId(USER_ID);

        assertEquals(existing.getId(), dto.id());
        assertEquals(USER_ID, dto.userId());
        assertEquals(RiskTolerance.HIGH, dto.riskTolerance());
        assertEquals(ExperienceLevel.ADVANCED, dto.experienceLevel());
        assertNotNull(dto.createdAt());
    }

    @Test
    void getQuestionnaireByUserId_whenMissing_throwsNotFound() {
        when(questionnaireRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getQuestionnaireByUserId(USER_ID));
    }

    @Test
    void validateAnswers_completeRequest_doesNotThrow() {
        assertDoesNotThrow(() -> service.validateAnswers(aggressiveRequest()));
    }
}
