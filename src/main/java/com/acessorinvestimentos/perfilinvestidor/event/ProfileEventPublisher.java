package com.acessorinvestimentos.perfilinvestidor.event;

import com.acessorinvestimentos.perfilinvestidor.model.enums.InvestorType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProfileEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String exchange;
    private final String routingKey;

    public ProfileEventPublisher(RabbitTemplate rabbitTemplate,
                                 @Value("${app.rabbitmq.exchange}") String exchange,
                                 @Value("${app.rabbitmq.routing-keys.profile-updated}") String routingKey) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
        this.routingKey = routingKey;
    }

    public void publishProfileUpdated(String userId, InvestorType investorType) {
        ProfileUpdatedEvent event = ProfileUpdatedEvent.of(userId, investorType);
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Published {} for user {} ({})", routingKey, userId, investorType);
        } catch (AmqpException ex) {
            // O perfil já foi salvo; uma falha no broker não deve derrubar a requisição.
            log.error("Could not publish {} for user {}: {}", routingKey, userId, ex.getMessage());
        }
    }
}
