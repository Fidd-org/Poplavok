package com.poplavok.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import javax.annotation.Nullable;

@Entity
@Table(name = "currency_extended_info")
@PrimaryKeyJoinColumn(name = "currency")
public class CurrencyExtendedInfo extends Currency {

    @Column(columnDefinition = "TEXT")
    @Nullable
    private String currencyExtendedInfoJson;

    public CurrencyExtendedInfo() {
    }

    public CurrencyExtendedInfo(String currency) {
        super(currency);
    }

    // Accessor methods matching the interface requested

    @Nullable
    public String getCurrencyExtendedInfoJson() {
        return currencyExtendedInfoJson;
    }

    public void setCurrencyExtendedInfoJson(@Nullable String currencyExtendedInfoJson) {
        this.currencyExtendedInfoJson = currencyExtendedInfoJson;
    }
}

