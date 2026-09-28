package com.acessorinvestimentos.perfilinvestidor.dto;

import com.acessorinvestimentos.perfilinvestidor.model.enums.ExperienceLevel;
import com.acessorinvestimentos.perfilinvestidor.model.enums.FinancialGoal;
import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestmentHorizon;
import com.acessorinvestimentos.perfilinvestidor.model.enums.RiskTolerance;

import java.time.LocalDateTime;
import java.util.UUID;

public record QuestionnaireDto(
        UUID id,
        String userId,
        FinancialGoal financialGoal,
        RiskTolerance riskTolerance,
        InvestmentHorizon investmentHorizon,
        Boolean hasEmergencyReserve,
        ExperienceLevel experienceLevel,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
