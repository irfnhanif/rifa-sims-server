package io.github.irfnhanif.rifasims.dto;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;

public class CreateItemRequest {
    @NotBlank(message = "Nama tidak boleh kosong")
    @Size(max = 255, message = "Nama tidak boleh lebih dari 255 karakter")
    private String name;

    @NotBlank(message = "Barcode tidak boleh kosong")
    @Pattern(regexp = "^[0-9A-Z\\-]{4,30}$", message = "Barcode harus 4-30 karakter dan hanya berisi angka, huruf kapital, dan tanda hubung")
    private String barcode;

    @Size(max = 1000, message = "Deskripsi tidak boleh lebih dari 1000 karakter")
    private String description;

    @NotNull(message = "Stok saat ini tidak boleh kosong")
    @Min(value = 0, message = "Stok saat ini tidak boleh negatif")
    private Integer currentStock;

    @NotNull(message = "Batas minimum stok tidak boleh kosong")
    @Min(value = 0, message = "Batas minimum stok tidak boleh negatif")
    private Integer threshold;

    @Column(nullable = false)
    @NotNull(message = "Harga beli tidak boleh kosong")
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

    public Integer getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(Integer currentStock) {
        this.currentStock = currentStock;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
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
}
