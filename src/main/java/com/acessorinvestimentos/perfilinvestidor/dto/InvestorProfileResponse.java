package com.acessorinvestimentos.perfilinvestidor.dto;

import br.insper.investorprofile.model.enums.InvestorType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Resposta de GET /profiles/{userId}. Contém userId e investorType,
 * que é o que o InvestorProfileClient do Serviço de Recomendações espera.
 */
public record InvestorProfileResponse(
        UUID id,
        String userId,
        InvestorType investorType,
        Integer score,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
