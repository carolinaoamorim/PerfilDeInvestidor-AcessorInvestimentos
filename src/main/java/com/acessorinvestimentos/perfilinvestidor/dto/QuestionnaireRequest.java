package com.acessorinvestimentos.perfilinvestidor.dto;

import br.insper.investorprofile.model.enums.ExperienceLevel;
import br.insper.investorprofile.model.enums.FinancialGoal;
import br.insper.investorprofile.model.enums.InvestmentHorizon;
import br.insper.investorprofile.model.enums.RiskTolerance;

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
