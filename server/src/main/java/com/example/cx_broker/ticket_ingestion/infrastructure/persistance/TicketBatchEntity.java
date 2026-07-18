package com.example.cx_broker.ticket_ingestion.infrastructure.persistance;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ticket_batches")
public class TicketBatchEntity {

    @Id
    @Column(name = "batch_id", updatable = false, nullable = false)
    private UUID batchId;

    @Column(name = "source", nullable = false)
    private String source;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "processed_count", nullable = false)
    private int processedCount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected TicketBatchEntity() {
    }

    public TicketBatchEntity(UUID batchId, String source, String status, int processedCount, LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        this.batchId = batchId;
        this.source = source;
        this.status = status;
        this.processedCount = processedCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getBatchId() {
        return batchId;
    }

    public String getSource() {
        return source;
    }

    public String getStatus() {
        return status;
    }

    public int getProcessedCount() {
        return processedCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
