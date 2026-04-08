package com.poplavok.data.utils;

import java.math.BigDecimal;

public class AmountAndCommission {
    public final BigDecimal commissionQuote;
    public final BigDecimal amount;

    public AmountAndCommission(BigDecimal commissionQuote, BigDecimal amount) {
        this.commissionQuote = commissionQuote;
        this.amount = amount;
    }
}
