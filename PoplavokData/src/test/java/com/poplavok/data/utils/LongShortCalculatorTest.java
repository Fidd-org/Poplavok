package com.poplavok.data.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongShortCalculatorTest {

    @Test
    void testCalculateBaseAmountToGetLong() {
        BigDecimal giveQuoteBuy = new BigDecimal("100");
        BigDecimal price = new BigDecimal("10");
        BigDecimal fee = new BigDecimal("0.01");

        // getBaseBuy = (100 - 1) / 10 = 9.9
        AmountAndCommission result = LongShortCalculator.calculateBaseAmountToGetLong(giveQuoteBuy, price, fee);
        assertEquals(new BigDecimal("9.9").setScale(result.amount.scale(), RoundingMode.HALF_UP), result.amount);
        assertEquals(new BigDecimal("1").setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateBaseAmountToGetLong2() {
        BigDecimal giveQuoteBuy = new BigDecimal("100");
        BigDecimal price = new BigDecimal("10");
        BigDecimal fee = new BigDecimal("0.02");

        // commission = 2
        // getBaseBuy = (100 - 2) / 10 = 9.8
        AmountAndCommission result = LongShortCalculator.calculateBaseAmountToGetLong(giveQuoteBuy, price, fee);
        assertEquals(new BigDecimal("9.8").setScale(result.amount.scale(), RoundingMode.HALF_UP), result.amount);
        assertEquals(new BigDecimal("2").setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateQuoteAmountToGetShort() {
        BigDecimal giveBaseSell = new BigDecimal("10");
        BigDecimal price = new BigDecimal("10");
        BigDecimal fee = new BigDecimal("0.01");

        // getQuoteSell = 10 * 10 = 100
        // commission = 1
        // result = 99
        AmountAndCommission result = LongShortCalculator.calculateQuoteAmountToGetShort(giveBaseSell, price, fee);
        assertEquals(new BigDecimal("99.00").setScale(result.amount.scale(), RoundingMode.HALF_UP), result.amount);
        assertEquals(new BigDecimal("1").setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

    @Test
    void testCalculateBaseAmountToGiveShort() {
        BigDecimal getQuoteSell = new BigDecimal("99");
        BigDecimal price = new BigDecimal("10");
        BigDecimal fee = new BigDecimal("0.01");

        // commission = 1
        // giveBaseSell = 101 / 10 = 10.9
        AmountAndCommission result = LongShortCalculator.calculateBaseAmountToGiveShort(getQuoteSell, price, fee);
        assertEquals(new BigDecimal("10").setScale(result.amount.scale(), RoundingMode.HALF_UP), result.amount);
        assertEquals(new BigDecimal("1").setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }


    @Test
    void testCalculateQuoteAmountToGiveLong() {
        BigDecimal getBaseSell = new BigDecimal("9.9");
        BigDecimal price = new BigDecimal("10");
        BigDecimal fee = new BigDecimal("0.01");

        // commission = 1
        // giveBaseSell = 101 / 10 = 10.9
        AmountAndCommission result = LongShortCalculator.calculateQuoteAmountToGiveLong(getBaseSell, price, fee);
        assertEquals(new BigDecimal("100").setScale(result.amount.scale(), RoundingMode.HALF_UP), result.amount);
        assertEquals(new BigDecimal("1").setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
    }

}
