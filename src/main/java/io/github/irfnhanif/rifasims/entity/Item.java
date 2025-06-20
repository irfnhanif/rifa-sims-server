package io.github.irfnhanif.rifasims.entity;

import io.github.irfnhanif.rifasims.validation.MustBeString;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

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

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Harga kulakan tidak boleh kosong")
    @Min(value = 1, message = "Harga beli minimal 1 Rupiah")
    private Long wholesalePrice;

    @Column(nullable = false)
    @NotNull(message = "Persentase keuntungan tidak boleh kosong")
    @Min(value = 0, message = "Persentase keuntungan tidak boleh negatif")
    @Max(value = 100, message = "Persentase keuntungan maksimal 100%")
    private Double profitPercentage;

    @Column(nullable = false)
    @NotNull(message = "Harga jual tidak boleh kosong")
    @Min(value = 1, message = "Harga jual minimal 1 Rupiah")
    private Long retailPrice;

    private boolean deleted = false;

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

    public Long getWholesalePrice() {
        return wholesalePrice;
    }

    public void setWholesalePrice(Long wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }

    public Double getProfitPercentage() {
        return profitPercentage;
    }

    public void setProfitPercentage(Double profitPercentage) {
        this.profitPercentage = profitPercentage;
    }

    public Long getRetailPrice() {
        return retailPrice;
    }

    public void setRetailPrice(Long retailPrice) {
        this.retailPrice = retailPrice;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
