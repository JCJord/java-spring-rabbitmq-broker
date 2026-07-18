package com.example.cx_broker.ticket_ingestion.infrastructure.persistance;

import com.example.cx_broker.ticket_ingestion.domain.model.TicketBatch;
import com.example.cx_broker.ticket_ingestion.domain.ports.out.BatchRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class PostgresBatchRepositoryAdapter implements BatchRepositoryPort {

    private final SpringDataBatchRepository springDataRepo;

    public PostgresBatchRepositoryAdapter(SpringDataBatchRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public void save(TicketBatch batch) {
        // 1. We receive the pure, smart Domain Object from the Chef

        // 2. We convert it into the "dumb" Database Entity
        TicketBatchEntity entity = new TicketBatchEntity(
                batch.getBatchId(),
                batch.getSource(),
                batch.getStatus().name(),
                batch.getProcessedCount(),
                batch.getCreatedAt(),
                batch.getUpdatedAt());

        springDataRepo.save(entity);
    }
}
