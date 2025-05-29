package io.github.irfnhanif.rifasims.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EditStockChangeRequest {
    @NotNull(message = "Stok saat ini tidak boleh kosong")
    @Min(value = 0, message = "Stok saat ini tidak boleh negatif")
    private Integer currentStock;

    @NotNull(message = "Batas minimum stok tidak boleh kosong")
    @Min(value = 0, message = "Batas minimum stok tidak boleh negatif")
    private Integer threshold;

    @NotBlank(message = "Alasan tidak boleh kosong")
    @Size(max = 1000, message = "Alasan tidak boleh lebih dari 1000 karakter")
    private String reason;

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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
