package com.charginghive.admin.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adminUsername;

    /**
     * The specific action performed by the admin.
     * Examples: "APPROVE_STATION", "BLOCK_USER", "GET_ALL_USERS".
     */
    @Column(nullable = false)
    private String action;

    /**
     * The type of entity that was the target of the action.
     */
    private String targetEntity;

    /**
     * The ID of the entity that was affected.
     */
    private Long targetId;

    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Lob
    private String details;
}
