package com.example.cx_broker.ticket_ingestion.infrastructure.web;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.cx_broker.ticket_ingestion.domain.ports.in.IngestBatchCommand;
import com.example.cx_broker.ticket_ingestion.domain.ports.in.IngestBatchUseCase;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketIngestionController {

    private final IngestBatchUseCase ingestBatchUseCase;

    public TicketIngestionController(IngestBatchUseCase ingestBatchUseCase) {
        this.ingestBatchUseCase = ingestBatchUseCase;
    }

    @PostMapping("/batch")
    public ResponseEntity<BatchResponseDto> uploadBatch(@RequestBody IngestBatchCommand command) {
        UUID batchId = ingestBatchUseCase.ingestBatch(command);

        BatchResponseDto response = new BatchResponseDto(batchId, "Batch accepted and is processing");
        return ResponseEntity.ok(response);
    }

    public record BatchResponseDto(UUID batchId, String message) {
    }
}
