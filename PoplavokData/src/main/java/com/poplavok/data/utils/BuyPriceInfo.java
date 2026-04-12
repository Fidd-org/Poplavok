package com.poplavok.data.utils;

import java.math.BigDecimal;

public class BuyPriceInfo extends PriceInfo {
    public final BigDecimal totalQuote;
    public final BigDecimal entryQuote;

    public BuyPriceInfo(BigDecimal totalQuote, BigDecimal entryQuote, BigDecimal commissionQuote, BigDecimal price) {
        super(commissionQuote, price);
        this.totalQuote = totalQuote;
        this.entryQuote = entryQuote;
    }
}
