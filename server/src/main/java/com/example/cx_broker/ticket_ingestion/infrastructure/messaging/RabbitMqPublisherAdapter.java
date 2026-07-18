package com.example.cx_broker.ticket_ingestion.infrastructure.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.example.cx_broker.ticket_ingestion.domain.model.TicketBatch;
import com.example.cx_broker.ticket_ingestion.domain.ports.out.MessagePublisherPort;

@Component
public class RabbitMqPublisherAdapter implements MessagePublisherPort {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMqPublisherAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishBatchCreatedEvent(TicketBatch batch) {
        String routingKey = "ticket.batch.created";

        rabbitTemplate.convertAndSend("cx-broker-exchange", routingKey, batch.getBatchId().toString());

    }

}
