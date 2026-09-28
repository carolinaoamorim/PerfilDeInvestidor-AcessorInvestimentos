package com.acessorinvestimentos.perfilinvestidor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    /** Exchange compartilhada pelos serviços. O Serviço de Recomendações liga a fila dele aqui. */
    @Bean
    public TopicExchange investmentEventsExchange(@Value("${app.rabbitmq.exchange}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    /** Usa o ObjectMapper do Spring para mandar as datas em ISO-8601 (ex.: 2026-09-28T14:00:00Z). */
    @Bean
    public MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
