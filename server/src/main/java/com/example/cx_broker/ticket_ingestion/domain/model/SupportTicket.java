package com.example.cx_broker.ticket_ingestion.domain.model;

import java.util.UUID;

public class SupportTicket {
    private final UUID ticketId;
    private final String externalId;
    private final String content;
    private String sentiment;
    private TicketStatus status;

    private SupportTicket(UUID ticketId, String externalId, String content) {
        this.ticketId = ticketId;
        this.externalId = externalId;
        this.content = content;
        this.status = TicketStatus.PENDING;
    }

    public static SupportTicket create(String externalId, String content) {
        if (externalId == null || externalId.isBlank()) {
            throw new IllegalArgumentException("External ID is required");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content is required");
        }
        return new SupportTicket(UUID.randomUUID(), externalId, content);
    }

    public void markAsProcessing() {
        if (this.status == TicketStatus.PENDING) {
            throw new IllegalStateException("Can only process a PENDING ticket.");
        }
        this.status = TicketStatus.PROCESSING;
    }

    public void markAsFailed() {
        this.status = TicketStatus.FAILED;
    }

    public void markAsCompleted(String sentiment) {
        this.sentiment = sentiment;
        this.status = TicketStatus.COMPLETED;
    }

    public UUID getTicketId() {
        return ticketId;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getContent() {
        return content;
    }

    public String getSentiment() {
        return sentiment;
    }

    public TicketStatus getStatus() {
        return status;
    }

}