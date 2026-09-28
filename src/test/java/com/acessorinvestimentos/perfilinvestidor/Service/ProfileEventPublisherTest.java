package com.acessorinvestimentos.perfilinvestidor.Service;

import br.insper.investorprofile.event.ProfileEventPublisher;
import br.insper.investorprofile.event.ProfileUpdatedEvent;
import br.insper.investorprofile.model.enums.InvestorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProfileEventPublisherTest {

    private static final String EXCHANGE = "investment.events";
    private static final String ROUTING_KEY = "profile.updated";

    @Mock
    private RabbitTemplate rabbitTemplate;

    private ProfileEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new ProfileEventPublisher(rabbitTemplate, EXCHANGE, ROUTING_KEY);
    }

    @Test
    void publishProfileUpdated_sendsEventToExchange() {
        publisher.publishProfileUpdated("auth0|123", InvestorType.MODERATE);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq(ROUTING_KEY), captor.capture());

        ProfileUpdatedEvent event = assertInstanceOf(ProfileUpdatedEvent.class, captor.getValue());
        assertEquals("PROFILE_UPDATED", event.eventType());
        assertEquals("auth0|123", event.userId());
        assertEquals(InvestorType.MODERATE, event.investorType());
        assertNotNull(event.occurredAt());
    }

    @Test
    void publishProfileUpdated_whenBrokerFails_doesNotPropagateError() {
        doThrow(new AmqpException("broker down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        assertDoesNotThrow(() -> publisher.publishProfileUpdated("auth0|123", InvestorType.AGGRESSIVE));
    }
}
