package com.poplavok.data.utils;

import java.math.BigDecimal;

public class AmountAndCommission {
    public final BigDecimal commissionQuote;
    public final BigDecimal commissionBase;
    public final BigDecimal amount;

    public AmountAndCommission(BigDecimal commissionQuote, BigDecimal commissionBase, BigDecimal amount) {
        this.commissionQuote = commissionQuote;
        this.commissionBase = commissionBase;
        this.amount = amount;
    }
}
