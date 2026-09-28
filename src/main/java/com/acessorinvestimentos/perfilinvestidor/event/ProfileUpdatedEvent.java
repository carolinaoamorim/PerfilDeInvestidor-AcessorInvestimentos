package com.acessorinvestimentos.perfilinvestidor.event;

import br.insper.investorprofile.model.enums.InvestorType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public record ProfileUpdatedEvent(
        String eventType,
        String userId,
        InvestorType investorType,
        Instant occurredAt
) {
    public static final String EVENT_TYPE = "PROFILE_UPDATED";

    public static ProfileUpdatedEvent of(String userId, InvestorType investorType) {
        return new ProfileUpdatedEvent(EVENT_TYPE, userId, investorType,
                Instant.now().truncatedTo(ChronoUnit.SECONDS));
    }
}
