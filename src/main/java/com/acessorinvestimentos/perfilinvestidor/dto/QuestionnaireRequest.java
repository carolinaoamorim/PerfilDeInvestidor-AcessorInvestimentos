package com.acessorinvestimentos.perfilinvestidor.dto;

import com.acessorinvestimentos.perfilinvestidor.model.enums.ExperienceLevel;
import com.acessorinvestimentos.perfilinvestidor.model.enums.FinancialGoal;
import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestmentHorizon;
import com.acessorinvestimentos.perfilinvestidor.model.enums.RiskTolerance;

/**
 * Respostas do questionário. A validação de "todas as perguntas respondidas"
 * fica no QuestionnaireService, que devolve a lista das perguntas faltantes.
 */
public record QuestionnaireRequest(
        FinancialGoal financialGoal,
        RiskTolerance riskTolerance,
        InvestmentHorizon investmentHorizon,
        Boolean hasEmergencyReserve,
        ExperienceLevel experienceLevel
) {
}
