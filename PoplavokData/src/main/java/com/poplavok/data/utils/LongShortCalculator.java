package com.poplavok.data.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LongShortCalculator {

    // --------------- BASE CALCULATIONS ---------------

    public static AmountAndCommission calculateBaseAmountToGetLong(BigDecimal giveQuoteBuy, BigDecimal price, BigDecimal fee) {
        BigDecimal commissionQuote = giveQuoteBuy.multiply(fee);
        BigDecimal getBaseBuy = giveQuoteBuy
                .subtract(commissionQuote)
                .divide(price, RoundingMode.DOWN);
        return new AmountAndCommission(commissionQuote, getBaseBuy);
    }

    public static AmountAndCommission calculateBaseAmountToGiveShort(BigDecimal getQuoteSell, BigDecimal price, BigDecimal fee) {
        BigDecimal commissionQuote = getQuoteSell.multiply(fee);

        BigDecimal giveBaseSell =
                getQuoteSell.add(commissionQuote)
                .divide(price, RoundingMode.UP);
        return new AmountAndCommission(commissionQuote, giveBaseSell);
    }

    // --------------- QUOTE CALCULATIONS ---------------

    public static AmountAndCommission calculateQuoteAmountToGiveLong(BigDecimal getBaseBuy, BigDecimal price, BigDecimal fee) {
        BigDecimal giveQuoteBuy = getBaseBuy.multiply(price);
        BigDecimal commissionQuote = giveQuoteBuy.multiply(fee);
        giveQuoteBuy = giveQuoteBuy.add(commissionQuote);

        return new AmountAndCommission(commissionQuote, giveQuoteBuy);
    }

    public static AmountAndCommission calculateQuoteAmountToGetShort(BigDecimal giveBaseSell, BigDecimal price, BigDecimal fee) {
        BigDecimal getQuoteSell = giveBaseSell.multiply(price);
        BigDecimal commissionQuote = getQuoteSell.multiply(fee);
        getQuoteSell = getQuoteSell.subtract(commissionQuote);

        return new AmountAndCommission(commissionQuote, getQuoteSell);
    }
}
