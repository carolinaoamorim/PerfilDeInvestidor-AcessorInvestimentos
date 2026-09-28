package com.acessorinvestimentos.perfilinvestidor.dto;

import br.insper.investorprofile.model.enums.ExperienceLevel;
import br.insper.investorprofile.model.enums.FinancialGoal;
import br.insper.investorprofile.model.enums.InvestmentHorizon;
import br.insper.investorprofile.model.enums.RiskTolerance;

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
