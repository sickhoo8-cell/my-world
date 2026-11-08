
package com.myworld.app.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "direct_messages",
        indexes = {
                @Index(
                        name = "idx_dm_sender_receiver_id",
                        columnList = "sender_id, receiver_id, id"
                ),
                @Index(
                        name = "idx_dm_receiver_sender_id",
                        columnList = "receiver_id, sender_id, id"
                )
        }
)
public class DirectMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private Member receiver;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected DirectMessage() {
    }

    public DirectMessage(
            Member sender,
            Member receiver,
            String content
    ) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Member getSender() {
        return sender;
    }

    public Member getReceiver() {
        return receiver;
    }

    public String getContent() {
        return content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
