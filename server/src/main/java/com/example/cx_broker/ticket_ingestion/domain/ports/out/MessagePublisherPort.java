package com.example.cx_broker.ticket_ingestion.domain.ports.out;

import com.example.cx_broker.ticket_ingestion.domain.model.TicketBatch;

public interface MessagePublisherPort {
    void publishBatchCreatedEvent(TicketBatch batch);
}
