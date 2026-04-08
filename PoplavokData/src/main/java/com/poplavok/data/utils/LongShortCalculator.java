package com.poplavok.data.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.poplavok.data.utils.BigDecimalUtil.SCALE;

public class LongShortCalculator {

    // --------------- BASE CALCULATIONS ---------------

    public static AmountAndCommission calculateBaseAmountToGetLong(BigDecimal giveQuoteBuy, BigDecimal price, BigDecimal fee) {
        BigDecimal commissionQuote = giveQuoteBuy.multiply(fee);
        BigDecimal getBaseBuy = giveQuoteBuy
                .subtract(commissionQuote)
                .divide(price, SCALE, RoundingMode.FLOOR);
        return new AmountAndCommission(commissionQuote, getBaseBuy);
    }

    public static AmountAndCommission calculateBaseAmountToGiveShort(BigDecimal getQuoteSell, BigDecimal price, BigDecimal fee) {
        BigDecimal withoutCommission = BigDecimal.ONE.subtract(fee);
        BigDecimal giveBaseSell = getQuoteSell.divide(
                price.multiply(withoutCommission), SCALE, RoundingMode.CEILING);
        BigDecimal commissionQuote = giveBaseSell.multiply(price).multiply(fee);
        return new AmountAndCommission(commissionQuote, giveBaseSell);
    }

    // --------------- QUOTE CALCULATIONS ---------------

    public static AmountAndCommission calculateQuoteAmountToGiveLong(BigDecimal getBaseBuy, BigDecimal price, BigDecimal fee) {
        BigDecimal withoutCommission = BigDecimal.ONE.subtract(fee);
        BigDecimal giveQuoteBuy = getBaseBuy.multiply(price).divide(withoutCommission, SCALE, RoundingMode.CEILING);
        BigDecimal commissionQuote = giveQuoteBuy.multiply(fee);

        return new AmountAndCommission(commissionQuote, giveQuoteBuy);
    }

    public static AmountAndCommission calculateQuoteAmountToGetShort(BigDecimal giveBaseSell, BigDecimal price, BigDecimal fee) {
        BigDecimal getQuoteSell = giveBaseSell.multiply(price);
        BigDecimal commissionQuote = giveBaseSell.multiply(fee).multiply(price);
        getQuoteSell = getQuoteSell.subtract(commissionQuote);

        return new AmountAndCommission(commissionQuote, getQuoteSell);
    }
}
