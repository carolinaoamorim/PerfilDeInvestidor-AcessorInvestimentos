package com.acessorinvestimentos.perfilinvestidor.service;

import com.acessorinvestimentos.perfilinvestidor.model.QuestionnaireResponse;
import com.acessorinvestimentos.perfilinvestidor.model.enums.ExperienceLevel;
import com.acessorinvestimentos.perfilinvestidor.model.enums.FinancialGoal;
import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestmentHorizon;
import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestorType;
import com.acessorinvestimentos.perfilinvestidor.model.enums.RiskTolerance;
import org.springframework.stereotype.Service;

/**
 * Responsável exclusivamente pela pontuação e classificação.
 *
 * Pontuação (máximo 11):
 *   financialGoal:       EMERGENCY_RESERVE 0 | PRESERVE_CAPITAL 0 | GENERATE_INCOME 1 | RETIREMENT 1 | BUILD_WEALTH 2
 *   riskTolerance:       LOW 0 | MEDIUM 2 | HIGH 4
 *   investmentHorizon:   SHORT_TERM 0 | MEDIUM_TERM 1 | LONG_TERM 2
 *   hasEmergencyReserve: false 0 | true 1
 *   experienceLevel:     BEGINNER 0 | INTERMEDIATE 1 | ADVANCED 2
 *
 * Classificação: 0-4 CONSERVATIVE | 5-8 MODERATE | 9+ AGGRESSIVE
 */
@Service
public class ProfileClassificationService {

    static final int MODERATE_MIN_SCORE = 5;
    static final int AGGRESSIVE_MIN_SCORE = 9;

    public int calculateScore(QuestionnaireResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("Questionnaire response is required");
        }
        return goalPoints(response.getFinancialGoal())
                + riskPoints(response.getRiskTolerance())
                + horizonPoints(response.getInvestmentHorizon())
                + (Boolean.TRUE.equals(response.getHasEmergencyReserve()) ? 1 : 0)
                + experiencePoints(response.getExperienceLevel());
    }

    public InvestorType classifyInvestor(int score) {
        if (score < 0) {
            throw new IllegalArgumentException("Score cannot be negative");
        }
        if (score >= AGGRESSIVE_MIN_SCORE) {
            return InvestorType.AGGRESSIVE;
        }
        if (score >= MODERATE_MIN_SCORE) {
            return InvestorType.MODERATE;
        }
        return InvestorType.CONSERVATIVE;
    }

    private int goalPoints(FinancialGoal goal) {
        return switch (goal) {
            case EMERGENCY_RESERVE, PRESERVE_CAPITAL -> 0;
            case GENERATE_INCOME, RETIREMENT -> 1;
            case BUILD_WEALTH -> 2;
        };
    }

    private int riskPoints(RiskTolerance tolerance) {
        return switch (tolerance) {
            case LOW -> 0;
            case MEDIUM -> 2;
            case HIGH -> 4;
        };
    }

    private int horizonPoints(InvestmentHorizon horizon) {
        return switch (horizon) {
            case SHORT_TERM -> 0;
            case MEDIUM_TERM -> 1;
            case LONG_TERM -> 2;
        };
    }

    private int experiencePoints(ExperienceLevel level) {
        return switch (level) {
            case BEGINNER -> 0;
            case INTERMEDIATE -> 1;
            case ADVANCED -> 2;
        };
    }
}
