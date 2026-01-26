package com.monitoring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActivityLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, columnDefinition = "uuid")
    private UUID sessionId;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime loggedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityStatus activityStatus;
    
    @Column(length = 500)
    private String metadata;
    
    public enum ActivityStatus {
        ACTIVE,
        IDLE
    }
}
