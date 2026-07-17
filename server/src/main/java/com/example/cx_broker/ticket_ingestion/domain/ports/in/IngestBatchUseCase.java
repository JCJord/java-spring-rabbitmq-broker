package com.example.cx_broker.ticket_ingestion.domain.ports.in;

import java.util.UUID;

public interface IngestBatchUseCase {
    UUID ingestBatch(IngestBatchCommand command);
}
