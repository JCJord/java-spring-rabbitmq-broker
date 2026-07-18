package com.example.cx_broker.ticket_ingestion.infrastructure.persistance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataBatchRepository extends JpaRepository<TicketBatchEntity, UUID> {

}
