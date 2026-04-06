package com.poplavok.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
@Table(name = "trades")
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Nullable
    private TradeOperation operation;

    @Column(name = "amount_base_in", precision = 20, scale = 8)
    @Nullable
    private BigDecimal amountBaseIn;

    @Column(name = "amount_base_out", precision = 20, scale = 8)
    @Nullable
    private BigDecimal amountBaseOut;

    @Column(name = "amount_quote_in", precision = 20, scale = 8)
    @Nullable
    private BigDecimal amountQuoteIn;

    @Column(name = "amount_quote_out", precision = 20, scale = 8)
    @Nullable
    private BigDecimal amountQuoteOut;

    @Column(name = "commission_base", precision = 20, scale = 8)
    @Nullable
    private BigDecimal commissionBase;

    @Column(name = "commission_quote", precision = 20, scale = 8)
    @Nullable
    private BigDecimal commissionQuote;

    @Column(nullable = false)
    @Nullable
    private Date date;

    public Trade() {
    }

    public Long getId() {
        return checkNotNull(id);
    }

    public TradeOperation getOperation() {
        return checkNotNull(operation);
    }

    public void setOperation(TradeOperation operation) {
        this.operation = operation;
    }

    public @Nullable BigDecimal getAmountBaseIn() {
        return amountBaseIn;
    }

    public void setAmountBaseIn(BigDecimal amountBaseIn) {
        this.amountBaseIn = amountBaseIn;
    }

    public @Nullable BigDecimal getAmountBaseOut() {
        return amountBaseOut;
    }

    public void setAmountBaseOut(BigDecimal amountBaseOut) {
        this.amountBaseOut = amountBaseOut;
    }

    public @Nullable BigDecimal getAmountQuoteIn() {
        return amountQuoteIn;
    }

    public void setAmountQuoteIn(BigDecimal amountQuoteIn) {
        this.amountQuoteIn = amountQuoteIn;
    }

    public @Nullable BigDecimal getAmountQuoteOut() {
        return amountQuoteOut;
    }

    public void setAmountQuoteOut(BigDecimal amountQuoteOut) {
        this.amountQuoteOut = amountQuoteOut;
    }

    public @Nullable BigDecimal getCommissionBase() {
        return commissionBase;
    }

    public void setCommissionBase(BigDecimal commissionBase) {
        this.commissionBase = commissionBase;
    }

    public @Nullable BigDecimal getCommissionQuote() {
        return commissionQuote;
    }

    public void setCommissionQuote(BigDecimal commissionQuote) {
        this.commissionQuote = commissionQuote;
    }

    public @Nullable Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
