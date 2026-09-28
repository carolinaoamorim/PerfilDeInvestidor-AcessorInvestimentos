package com.acessorinvestimentos.perfilinvestidor.unit;

import com.acessorinvestimentos.perfilinvestidor.model.QuestionnaireResponse;
import com.acessorinvestimentos.perfilinvestidor.model.enums.ExperienceLevel;
import com.acessorinvestimentos.perfilinvestidor.model.enums.FinancialGoal;
import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestmentHorizon;
import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestorType;
import com.acessorinvestimentos.perfilinvestidor.model.enums.RiskTolerance;
import com.acessorinvestimentos.perfilinvestidor.service.ProfileClassificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfileClassificationServiceTest {

    private final ProfileClassificationService service = new ProfileClassificationService();

    private QuestionnaireResponse questionnaire(FinancialGoal goal, RiskTolerance risk, InvestmentHorizon horizon,
                                                Boolean reserve, ExperienceLevel experience) {
        return QuestionnaireResponse.builder()
                .userId("auth0|123")
                .financialGoal(goal)
                .riskTolerance(risk)
                .investmentHorizon(horizon)
                .hasEmergencyReserve(reserve)
                .experienceLevel(experience)
                .build();
    }

    /** Questionário com pontuação zero, usado como base para medir cada pergunta isoladamente. */
    private QuestionnaireResponse baseline() {
        return questionnaire(FinancialGoal.PRESERVE_CAPITAL, RiskTolerance.LOW, InvestmentHorizon.SHORT_TERM,
                false, ExperienceLevel.BEGINNER);
    }

    @Test
    void calculateScore_minimumAnswers_returnsZero() {
        assertEquals(0, service.calculateScore(baseline()));
    }

    @Test
    void calculateScore_moderateAnswers_returnsSix() {
        QuestionnaireResponse q = questionnaire(FinancialGoal.GENERATE_INCOME, RiskTolerance.MEDIUM,
                InvestmentHorizon.MEDIUM_TERM, true, ExperienceLevel.INTERMEDIATE);
        assertEquals(6, service.calculateScore(q));
    }

    @Test
    void calculateScore_maximumAnswers_returnsEleven() {
        QuestionnaireResponse q = questionnaire(FinancialGoal.BUILD_WEALTH, RiskTolerance.HIGH,
                InvestmentHorizon.LONG_TERM, true, ExperienceLevel.ADVANCED);
        assertEquals(11, service.calculateScore(q));
    }

    @ParameterizedTest
    @CsvSource({"EMERGENCY_RESERVE,0", "PRESERVE_CAPITAL,0", "GENERATE_INCOME,1", "RETIREMENT,1", "BUILD_WEALTH,2"})
    void calculateScore_financialGoalPoints(FinancialGoal goal, int expected) {
        QuestionnaireResponse q = baseline();
        q.setFinancialGoal(goal);
        assertEquals(expected, service.calculateScore(q));
    }

    @ParameterizedTest
    @CsvSource({"LOW,0", "MEDIUM,2", "HIGH,4"})
    void calculateScore_riskTolerancePoints(RiskTolerance risk, int expected) {
        QuestionnaireResponse q = baseline();
        q.setRiskTolerance(risk);
        assertEquals(expected, service.calculateScore(q));
    }

    @ParameterizedTest
    @CsvSource({"SHORT_TERM,0", "MEDIUM_TERM,1", "LONG_TERM,2"})
    void calculateScore_investmentHorizonPoints(InvestmentHorizon horizon, int expected) {
        QuestionnaireResponse q = baseline();
        q.setInvestmentHorizon(horizon);
        assertEquals(expected, service.calculateScore(q));
    }

    @ParameterizedTest
    @CsvSource({"BEGINNER,0", "INTERMEDIATE,1", "ADVANCED,2"})
    void calculateScore_experienceLevelPoints(ExperienceLevel level, int expected) {
        QuestionnaireResponse q = baseline();
        q.setExperienceLevel(level);
        assertEquals(expected, service.calculateScore(q));
    }

    @Test
    void calculateScore_withEmergencyReserve_addsOnePoint() {
        QuestionnaireResponse q = baseline();
        q.setHasEmergencyReserve(true);
        assertEquals(1, service.calculateScore(q));
    }

    @Test
    void calculateScore_nullResponse_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.calculateScore(null));
    }

    @ParameterizedTest
    @CsvSource({"0,CONSERVATIVE", "4,CONSERVATIVE", "5,MODERATE", "8,MODERATE", "9,AGGRESSIVE", "11,AGGRESSIVE"})
    void classifyInvestor_respectsScoreRanges(int score, InvestorType expected) {
        assertEquals(expected, service.classifyInvestor(score));
    }

    @Test
    void classifyInvestor_negativeScore_throws() {
        assertThrows(IllegalArgumentException.class, () -> service.classifyInvestor(-1));
    }
}
