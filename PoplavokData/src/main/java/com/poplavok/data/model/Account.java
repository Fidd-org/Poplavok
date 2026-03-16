package com.poplavok.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency", nullable = false)
    @Nullable
    private Currency currency;

    @Column(nullable = false, precision = 20, scale = 8)
    @Nullable
    private BigDecimal availableAmount;

    @Column(nullable = false, precision = 20, scale = 8)
    @Nullable
    public BigDecimal reservedAmount;

    @Nullable
    public Date creationDate;

    @Column(nullable = false)
    private boolean archived = false;

    @Column
    @Nullable
    private String accountName;

    public Account(Currency currency, BigDecimal availableAmount, BigDecimal reservedAmount) {
        this.currency = currency;
        this.availableAmount = availableAmount;
        this.reservedAmount = reservedAmount;
        this.creationDate = new Date();
        this.archived = false;
    }

    public Account() {
        this.archived = false;
    }

    public Long getId() {
        return checkNotNull(id);
    }

    public void setId(@Nullable Long accountId) { this.id = accountId; }

    public Currency getCurrency() {
        return checkNotNull(currency);
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getAvailableAmount() {
        return checkNotNull(availableAmount);
    }

    public void setAvailableAmount(BigDecimal availableAmount) {
        this.availableAmount = availableAmount;
    }

    public @Nullable BigDecimal getReservedAmount() {
        return reservedAmount != null ? reservedAmount : BigDecimal.ZERO;
    }

    public void setReservedAmount(@Nullable BigDecimal reservedAmount) {
        this.reservedAmount = reservedAmount;
    }

    public @Nullable Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    @Nullable
    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(@Nullable String accountName) {
        this.accountName = accountName;
    }

    public String getCurrencyStr() {
        return currency != null ? currency.getCurrency() : "N/A";
    }

    public String getAvailableAmountStr() {
        return availableAmount != null ? availableAmount.toPlainString() : "0";
    }

    public String getReservedAmountStr() {
        return reservedAmount != null ? reservedAmount.toPlainString() : "0";
    }
}
