package com.poplavok.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import javax.annotation.Nullable;

@Entity
@Table(name = "currency_extended_info")
public class CurrencyExtendedInfo {

    @Id
    @Column(name = "currency", length = 20)
    private String currency; // Same PK as Currency

    @OneToOne
    @MapsId
    @JoinColumn(name = "currency", foreignKey = @ForeignKey(name = "fk_currency_extended_info_currencies"))
    @Nullable
    private Currency currencyEntity;

    @Column(columnDefinition = "TEXT")
    @Nullable
    private String currencyExtendedInfoJson;

    public CurrencyExtendedInfo() {
        this.currency = "";
    }

    public CurrencyExtendedInfo(String currency) {
        this.currency = currency;
        this.currencyEntity = new Currency(currency);
    }

    // Accessor methods matching the interface requested

    @Nullable
    public String getCurrencyExtendedInfoJson() {
        return currencyExtendedInfoJson;
    }

    public void setCurrencyExtendedInfoJson(@Nullable String currencyExtendedInfoJson) {
        this.currencyExtendedInfoJson = currencyExtendedInfoJson;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Nullable
    public Currency getCurrencyEntity() {
        return currencyEntity;
    }

    public void setCurrencyEntity(@Nullable Currency currencyEntity) {
        this.currencyEntity = currencyEntity;
    }
}

