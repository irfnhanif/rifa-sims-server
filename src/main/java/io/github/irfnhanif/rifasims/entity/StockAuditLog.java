package io.github.irfnhanif.rifasims.entity;

import jakarta.persistence.*;

import java.util.UUID;

public class StockAuditLog {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    @Column(nullable = false)
    private Item item;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @Column(nullable = false)
    private User user;

    @Column(nullable = false)
    private StockChangeType type;

    @Column(nullable = false)
    private int oldStock;

    @Column(nullable = false)
    private int newStock;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String reason;
}
