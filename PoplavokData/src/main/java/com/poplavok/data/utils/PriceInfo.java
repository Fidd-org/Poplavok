package com.poplavok.data.utils;

import java.math.BigDecimal;

public class PriceInfo {
    public final BigDecimal commissionQuote;
    public final BigDecimal price;

    public PriceInfo(BigDecimal commissionQuote, BigDecimal price) {
        this.commissionQuote = commissionQuote;
        this.price = price;
    }
}
