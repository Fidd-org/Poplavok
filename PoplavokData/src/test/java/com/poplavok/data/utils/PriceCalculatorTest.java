package com.poplavok.data.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceCalculatorTest {

    @Test
    void testCalculateSellPrice() {
        BigDecimal giveBaseSell = new BigDecimal("10");
        BigDecimal getQuoteSell = new BigDecimal("99");
        BigDecimal fee = new BigDecimal("0.01");

        PriceInfo result = PriceCalculator.calculateSellPrice(giveBaseSell, getQuoteSell, fee);

        assertEquals(new BigDecimal("10").setScale(result.price.scale(), RoundingMode.HALF_UP), result.price);
        assertEquals(new BigDecimal("1").setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateSellPriceHighFee() {
        BigDecimal giveBaseSell = new BigDecimal("10");
        BigDecimal getQuoteSell = new BigDecimal("95");
        BigDecimal fee = new BigDecimal("0.05");

        PriceInfo result = PriceCalculator.calculateSellPrice(giveBaseSell, getQuoteSell, fee);

        assertEquals(new BigDecimal("10").setScale(result.price.scale(), RoundingMode.HALF_UP), result.price);
        assertEquals(new BigDecimal("5").setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateBuyPrice() {
        BigDecimal giveQuoteBuy = new BigDecimal("101");
        BigDecimal getBaseBuy = new BigDecimal("10");
        BigDecimal fee = new BigDecimal("0.01");

        BuyPriceInfo result = PriceCalculator.calculateBuyPriceForEntry(giveQuoteBuy, getBaseBuy, fee);

        assertEquals(new BigDecimal("10").setScale(result.price.scale(), RoundingMode.HALF_UP), result.price);
        assertEquals(new BigDecimal("1").setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateBuyPrice2() {
        BigDecimal giveQuoteBuy = new BigDecimal("2002");
        BigDecimal getBaseBuy = new BigDecimal("200");
        BigDecimal fee = new BigDecimal("0.001");

        BuyPriceInfo result = PriceCalculator.calculateBuyPriceForEntry(giveQuoteBuy, getBaseBuy, fee);

        assertEquals(new BigDecimal("10").setScale(result.price.scale(), RoundingMode.HALF_UP), result.price);
        assertEquals(new BigDecimal("2").setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateBuyPriceHighFee() {
        BigDecimal giveQuoteBuy = new BigDecimal("105");
        BigDecimal getBaseBuy = new BigDecimal("10");
        BigDecimal fee = new BigDecimal("0.05");

        BuyPriceInfo result = PriceCalculator.calculateBuyPriceForEntry(giveQuoteBuy, getBaseBuy, fee);

        assertEquals(new BigDecimal("10").setScale(result.price.scale(), RoundingMode.HALF_UP), result.price);
        assertEquals(new BigDecimal("5").setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }
}
