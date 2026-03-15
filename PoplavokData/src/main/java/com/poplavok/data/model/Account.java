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
    private BigDecimal available;

    @Column(nullable = false, precision = 20, scale = 8)
    @Nullable
    private BigDecimal borrowed;

    @Column(nullable = false, precision = 20, scale = 8)
    @Nullable
    public BigDecimal lentAmount;

    @Nullable
    public Date creationDate;

    public Account() {
    }

    public Account(Currency currency, BigDecimal available, BigDecimal borrowed) {
        this.currency = currency;
        this.available = available;
        this.borrowed = borrowed;
        this.lentAmount = BigDecimal.ZERO;
        this.creationDate = new Date();
    }

    public Long getId() {
        return checkNotNull(id);
    }

    public Currency getCurrency() {
        return checkNotNull(currency);
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getAvailable() {
        return checkNotNull(available);
    }

    public void setAvailable(BigDecimal available) {
        this.available = available;
    }

    public BigDecimal getBorrowed() {
        return checkNotNull(borrowed);
    }

    public void setBorrowed(BigDecimal borrowed) {
        this.borrowed = borrowed;
    }

    public @Nullable BigDecimal getLentAmount() {
        return lentAmount != null ? lentAmount : BigDecimal.ZERO;
    }

    public void setLentAmount(@Nullable BigDecimal lentAmount) {
        this.lentAmount = lentAmount;
    }

    public @Nullable Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }
}
