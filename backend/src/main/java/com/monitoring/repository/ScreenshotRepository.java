package com.monitoring.repository;

import com.monitoring.entity.Screenshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ScreenshotRepository extends JpaRepository<Screenshot, Long> {
    List<Screenshot> findBySessionIdOrderByCapturedAtAsc(UUID sessionId);
}
