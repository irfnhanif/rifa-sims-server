package io.github.irfnhanif.rifasims.dto;

import io.github.irfnhanif.rifasims.entity.StockChangeType;

public class ScanStockChangeRequest {
    private StockChangeType changeType;
    private Integer amount;

    public StockChangeType getChangeType() {
        return changeType;
    }

    public void setChangeType(StockChangeType changeType) {
        this.changeType = changeType;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
