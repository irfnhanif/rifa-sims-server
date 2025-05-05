package io.github.irfnhanif.rifasims.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class ItemStock {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "item_id")
    private Item item;

    @Column(nullable = false)
    private int currentStock;

    private Integer threshold;
}
