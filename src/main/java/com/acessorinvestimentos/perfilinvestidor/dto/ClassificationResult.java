package com.acessorinvestimentos.perfilinvestidor.dto;

import br.insper.investorprofile.model.enums.InvestorType;

public record ClassificationResult(int score, InvestorType investorType) {
}
