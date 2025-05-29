package io.github.irfnhanif.rifasims.dto;

import io.github.irfnhanif.rifasims.entity.Item;
import io.github.irfnhanif.rifasims.entity.ItemStock;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateItemRequest {
    @NotBlank(message = "Nama tidak boleh kosong")
    @Size(max = 255, message = "Nama tidak boleh lebih dari 255 karakter")
    private String name;

    @NotBlank(message = "Barcode tidak boleh kosong")
    @Size(max = 50, message = "Barcode tidak boleh lebih dari 50 karakter")
    private String barcode;

    @Size(max = 1000, message = "Deskripsi tidak boleh lebih dari 1000 karakter")
    private String description;

    @NotNull(message = "Stok saat ini tidak boleh kosong")
    @Min(value = 0, message = "Stok saat ini tidak boleh negatif")
    private Integer currentStock;

    @NotNull(message = "Batas minimum stok tidak boleh kosong")
    @Min(value = 0, message = "Batas minimum stok tidak boleh negatif")
    private Integer threshold;

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
}
