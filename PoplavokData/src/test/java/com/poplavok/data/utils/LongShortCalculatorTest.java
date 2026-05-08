package com.poplavok.data.utils;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.poplavok.data.utils.BigDecimalUtil.fromString;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LongShortCalculatorTest {

    @Test
    void testCalculateBaseAmountToGetLong() {
        BigDecimal giveQuoteBuy = nullToZero(fromString("100"));
        BigDecimal price = nullToZero(fromString("10"));
        BigDecimal fee = nullToZero(fromString("0.01"));

        // getBaseBuy = (100 - 1) / 10 = 9.9
        AmountAndCommission result = LongShortCalculator.calculateBaseAmountToGetLong(giveQuoteBuy, price, fee);
        assertEquals(nullToZero(fromString("9.9")).setScale(result.amount.scale(), RoundingMode.HALF_UP), result.amount);
        assertEquals(nullToZero(fromString("1")).setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
        assertEquals(nullToZero(fromString("0.1")).setScale(result.commissionBase.scale(), RoundingMode.HALF_UP), result.commissionBase);
    }

    @Test
    void testCalculateBaseAmountToGetLong2() {
        BigDecimal giveQuoteBuy = nullToZero(fromString("100"));
        BigDecimal price = nullToZero(fromString("10"));
        BigDecimal fee = nullToZero(fromString("0.02"));

        // commission = 2
        // getBaseBuy = (100 - 2) / 10 = 9.8
        AmountAndCommission result = LongShortCalculator.calculateBaseAmountToGetLong(giveQuoteBuy, price, fee);
        assertEquals(nullToZero(fromString("9.8")).setScale(result.amount.scale(), RoundingMode.HALF_UP), result.amount);
        assertEquals(nullToZero(fromString("2")).setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
        assertEquals(nullToZero(fromString("0.2")).setScale(result.commissionBase.scale(), RoundingMode.HALF_UP), result.commissionBase);
    }

    @Test
    void testCalculateQuoteAmountToGetShort() {
        BigDecimal giveBaseSell = nullToZero(fromString("10"));
        BigDecimal price = nullToZero(fromString("10"));
        BigDecimal fee = nullToZero(fromString("0.01"));

        // getQuoteSell = 10 * 10 = 100
        // commission = 1
        // result = 99
        AmountAndCommission result = LongShortCalculator.calculateQuoteAmountToGetShort(giveBaseSell, price, fee);
        assertEquals(nullToZero(fromString("99.00")).setScale(result.amount.scale(), RoundingMode.HALF_UP), result.amount);
        assertEquals(nullToZero(fromString("1")).setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
        assertEquals(nullToZero(fromString("0.1")).setScale(result.commissionBase.scale(), RoundingMode.HALF_UP), result.commissionBase);
    }

    @Test
    void testCalculateBaseAmountToGiveShort() {
        BigDecimal getQuoteSell = nullToZero(fromString("99"));
        BigDecimal price = nullToZero(fromString("10"));
        BigDecimal fee = nullToZero(fromString("0.01"));

        // commission = 1
        // giveBaseSell = 101 / 10 = 10.9
        AmountAndCommission result = LongShortCalculator.calculateBaseAmountToGiveShort(getQuoteSell, price, fee);
        assertEquals(nullToZero(fromString("10")).setScale(result.amount.scale(), RoundingMode.HALF_UP), result.amount);
        assertEquals(nullToZero(fromString("1")).setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
        assertEquals(nullToZero(fromString("0.1")).setScale(result.commissionBase.scale(), RoundingMode.HALF_UP), result.commissionBase);
    }


    @Test
    void testCalculateQuoteAmountToGiveLong() {
        BigDecimal getBaseSell = nullToZero(fromString("9.9"));
        BigDecimal price = nullToZero(fromString("10"));
        BigDecimal fee = nullToZero(fromString("0.01"));

        // commission = 1
        // giveBaseSell = 101 / 10 = 10.9
        AmountAndCommission result = LongShortCalculator.calculateQuoteAmountToGiveLong(getBaseSell, price, fee);
        assertEquals(nullToZero(fromString("100")).setScale(result.amount.scale(), RoundingMode.HALF_UP), result.amount);
        assertEquals(nullToZero(fromString("1")).setScale(result.commissionQuote.scale(), RoundingMode.HALF_UP), result.commissionQuote);
        assertEquals(nullToZero(fromString("0.1")).setScale(result.commissionBase.scale(), RoundingMode.HALF_UP), result.commissionBase);
    }

}
