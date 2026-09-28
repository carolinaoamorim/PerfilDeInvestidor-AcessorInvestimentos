package com.acessorinvestimentos.perfilinvestidor.dto;

import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestorType;

public record ClassificationResult(int score, InvestorType investorType) {
}
