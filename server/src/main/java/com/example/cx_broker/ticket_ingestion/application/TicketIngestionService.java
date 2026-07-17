package com.example.cx_broker.ticket_ingestion.application;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.cx_broker.ticket_ingestion.domain.model.SupportTicket;
import com.example.cx_broker.ticket_ingestion.domain.model.TicketBatch;
import com.example.cx_broker.ticket_ingestion.domain.ports.in.IngestBatchCommand;
import com.example.cx_broker.ticket_ingestion.domain.ports.in.IngestBatchUseCase;
import com.example.cx_broker.ticket_ingestion.domain.ports.out.BatchRepositoryPort;
import com.example.cx_broker.ticket_ingestion.domain.ports.out.MessagePublisherPort;

@Service
public class TicketIngestionService implements IngestBatchUseCase {

    private final BatchRepositoryPort repositoryPort;
    private final MessagePublisherPort publisherPort;

    public TicketIngestionService(BatchRepositoryPort repositoryPort, MessagePublisherPort publisherPort) {
        this.repositoryPort = repositoryPort;
        this.publisherPort = publisherPort;
    }

    @Override
    @Transactional
    public UUID ingestBatch(IngestBatchCommand command) {

        List<SupportTicket> domainTickets = command.tickets().stream()
                .map(item -> SupportTicket.create(item.externalId(), item.content()))
                .collect(Collectors.toList());

        TicketBatch newBatch = TicketBatch.create(command.source(), domainTickets);

        repositoryPort.save(newBatch);
        publisherPort.publishBatchCreatedEvent(newBatch);

        return newBatch.getBatchId();
    }
}