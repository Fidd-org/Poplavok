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
@Table(name = "virtual_trades")
public class VirtualTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_level_id")
    @Nullable
    private Level quoteLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_level_id")
    @Nullable
    private Level baseLevel;

    @Column(name = "amount_quote", precision = 20, scale = 8)
    @Nullable
    private BigDecimal quoteAmount;

    @Column(name = "amount_base", precision = 20, scale = 8)
    @Nullable
    private BigDecimal baseAmount;

    @Column(nullable = false)
    @Nullable
    private Date date;

    public VirtualTrade() {
    }

    public Long getId() {
        return checkNotNull(id);
    }

    @Nullable
    public Level getQuoteLevel() {
        return quoteLevel;
    }

    public void setQuoteLevel(Level quoteLevel) {
        this.quoteLevel = quoteLevel;
    }

    @Nullable
    public Level getBaseLevel() {
        return baseLevel;
    }

    public void setBaseLevel(Level baseLevel) {
        this.baseLevel = baseLevel;
    }

    @Nullable
    public BigDecimal getQuoteAmount() {
        return quoteAmount;
    }

    public void setQuoteAmount(BigDecimal quoteAmount) {
        this.quoteAmount = quoteAmount;
    }

    @Nullable
    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public @Nullable Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
