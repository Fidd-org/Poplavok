package com.poplavok.data.utils;

import java.math.BigDecimal;

public class PriceAndCommission {
    public final BigDecimal commissionQuote;
    public final BigDecimal price;

    public PriceAndCommission(BigDecimal commissionQuote, BigDecimal price) {
        this.commissionQuote = commissionQuote;
        this.price = price;
    }
}
