package com.example.cx_broker.ticket_ingestion.domain.ports.in;

import java.util.List;

public record IngestBatchCommand(
        String source,
        List<TicketItem> tickets
) {
    public record TicketItem(
            String externalId,
            String content
    ) {}
}
