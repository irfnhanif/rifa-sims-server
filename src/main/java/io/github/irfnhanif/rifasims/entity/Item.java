package io.github.irfnhanif.rifasims.entity;

import io.github.irfnhanif.rifasims.validation.MustBeString;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(indexes = {
        @Index(name = "idx_item_barcode", columnList = "barcode")
})
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @NotNull
    @NotBlank
    @MustBeString
    private String name;

    @Column(nullable = false)
    @NotNull
    @NotBlank
    @Pattern(regexp = "^[0-9A-Z\\-]{4,30}$", message = "Barcode harus 4-30 karakter dan hanya berisi angka, huruf kapital, dan tanda hubung")
    private String barcode;

    private String description;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
