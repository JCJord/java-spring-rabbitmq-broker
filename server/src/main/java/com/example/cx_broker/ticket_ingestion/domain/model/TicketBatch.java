package com.example.cx_broker.ticket_ingestion.domain.model;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Collections;

public class TicketBatch {
    private final UUID batchId;
    private final String source;
    private final List<SupportTicket> tickets;

    private BatchStatus status;
    private int processedCount;

    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private TicketBatch(UUID batchId, String source, LocalDateTime createdAt) {
        this.batchId = batchId;
        this.source = source;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;

        this.tickets = new ArrayList<>();
        this.status = BatchStatus.PENDING;
        this.processedCount = 0;
    }

    public static TicketBatch create(String source, List<SupportTicket> initialTickets) {
        if (source == null || source.isBlank()) {
            throw new IllegalArgumentException("Source cant be empty");
        }

        if (initialTickets == null || initialTickets.isEmpty()) {
            throw new IllegalArgumentException("Initial tickets cant be empty");
        }
        TicketBatch batch = new TicketBatch(UUID.randomUUID(), source, LocalDateTime.now());
        batch.tickets.addAll(initialTickets);
        return batch;
    }

    public void StartProcessing() {
        if (this.status != BatchStatus.PENDING) {
            throw new IllegalStateException("Cannot record progress unless PROCESSING.");
        }

        this.status = BatchStatus.PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    public void recordTicketProcessed() {
        if (this.status != BatchStatus.PROCESSING) {
            throw new IllegalStateException("Cannot record progress unless PROCESSING");
        }

        this.processedCount++;
        this.updatedAt = LocalDateTime.now();

        if (this.processedCount >= this.tickets.size()) {
            this.status = BatchStatus.COMPLETED;
        }
    }

    public void fail() {
        this.status = BatchStatus.FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getBatchId() {
        return batchId;
    }

    public String getSource() {
        return source;
    }

    public List<SupportTicket> getTickets() {
        return Collections.unmodifiableList(tickets);
    }

    public BatchStatus getStatus() {
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