package com.poplavok.data.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.poplavok.data.utils.BigDecimalUtil.fromString;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorTest {

    @Test
    void testCalculateSellPrice() {
        BigDecimal giveBaseSell = nullToZero(fromString("10"));
        BigDecimal getQuoteSell = nullToZero(fromString("99"));
        BigDecimal fee = nullToZero(fromString("0.01"));

        PriceInfo result = PriceCalculator.calculateSellPrice(giveBaseSell, getQuoteSell, fee);

        assertEquals(nullToZero(fromString("10")).setScale(result.price.scale(), RoundingMode.HALF_UP), result.price);
        assertEquals(nullToZero(fromString("1")).setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateSellPriceHighFee() {
        BigDecimal giveBaseSell = nullToZero(fromString("10"));
        BigDecimal getQuoteSell = nullToZero(fromString("95"));
        BigDecimal fee = nullToZero(fromString("0.05"));

        PriceInfo result = PriceCalculator.calculateSellPrice(giveBaseSell, getQuoteSell, fee);

        assertEquals(nullToZero(fromString("10")).setScale(result.price.scale(), RoundingMode.HALF_UP), result.price);
        assertEquals(nullToZero(fromString("5")).setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateBuyPrice() {
        BigDecimal giveQuoteBuy = nullToZero(fromString("101"));
        BigDecimal getBaseBuy = nullToZero(fromString("10"));
        BigDecimal fee = nullToZero(fromString("0.01"));

        BuyPriceInfo result = PriceCalculator.calculateBuyPriceForEntry(giveQuoteBuy, getBaseBuy, fee);

        assertEquals(nullToZero(fromString("10")).setScale(result.price.scale(), RoundingMode.HALF_UP), result.price);
        assertEquals(nullToZero(fromString("1")).setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateBuyPrice2() {
        BigDecimal giveQuoteBuy = nullToZero(fromString("2002"));
        BigDecimal getBaseBuy = nullToZero(fromString("200"));
        BigDecimal fee = nullToZero(fromString("0.001"));

        BuyPriceInfo result = PriceCalculator.calculateBuyPriceForEntry(giveQuoteBuy, getBaseBuy, fee);

        assertEquals(nullToZero(fromString("10")).setScale(result.price.scale(), RoundingMode.HALF_UP), result.price);
        assertEquals(nullToZero(fromString("2")).setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateBuyPriceHighFee() {
        BigDecimal giveQuoteBuy = nullToZero(fromString("105"));
        BigDecimal getBaseBuy = nullToZero(fromString("10"));
        BigDecimal fee = nullToZero(fromString("0.05"));

        BuyPriceInfo result = PriceCalculator.calculateBuyPriceForEntry(giveQuoteBuy, getBaseBuy, fee);

        assertEquals(nullToZero(fromString("10")).setScale(result.price.scale(), RoundingMode.HALF_UP), result.price);
        assertEquals(nullToZero(fromString("5")).setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }
}
