package com.monitoring.repository;

import com.monitoring.entity.WorkSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<WorkSession, UUID> {
    List<WorkSession> findByUserIdOrderByStartTimeDesc(String userId);

    List<WorkSession> findByStatusOrderByStartTimeDesc(WorkSession.SessionStatus status);

    List<WorkSession> findByUserIdAndStatus(String userId, WorkSession.SessionStatus status);
}
