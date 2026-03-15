package com.poplavok.kucoin;

import com.KyKu4.MogeJlb.response.ApiCurrencyDetailChainPropertyResponse;
import com.KyKu4.MogeJlb.response.CurrencyExtendedInfoResponse;
import com.KyKu4.MogeJlb.response.CurrencyResponse;
import com.KyKu4.MogeJlb.response.ImmutableCurrencyExtendedInfoResponse;
import com.KyKu4.MogeJlb.response.MarketTickerResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.poplavok.data.model.Currency;
import com.poplavok.data.model.CurrencyChain;
import com.poplavok.data.model.CurrencyExtendedInfo;
import com.poplavok.data.model.MarketTicker;

import javax.annotation.Nullable;
import java.math.BigDecimal;

public class EntityConverter {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    static {
        OBJECT_MAPPER.registerModules(new GuavaModule());
    }

    public static Currency fromResponse(CurrencyResponse currencyResponse) {
        return of(currencyResponse.currency(), currencyResponse.fullName(), currencyResponse.name(),
                currencyResponse.precision(), currencyResponse.withdrawalMinSize(), currencyResponse.withdrawalMinFee(),
                currencyResponse.isWithdrawEnabled(), currencyResponse.isDepositEnabled(), currencyResponse.isMarginEnabled(),
                currencyResponse.isDebitEnabled());
    }

    private static Currency of(String currency, String fullName, String name, Integer precision,
                               BigDecimal withdrawalMinSize, BigDecimal withdrawalMinFee,
                               Boolean isWithdrawEnabled, Boolean isDepositEnabled,
                               Boolean isMarginEnabled, Boolean isDebitEnabled) {
        Currency c = new Currency(currency);
        if (currency != null) {
            c.setCurrency(currency);
        }
        c.setFullName(fullName);
        if (name != null) {
            c.setName(name);
        }
        c.setPrecision(precision);
        c.setWithdrawalMinSize(withdrawalMinSize != null ? withdrawalMinSize.toString() : null);
        c.setWithdrawalMinFee(withdrawalMinFee != null ? withdrawalMinFee.toString() : null);
        c.setIsWithdrawEnabled(isWithdrawEnabled);
        c.setIsDepositEnabled(isDepositEnabled);
        c.setIsMarginEnabled(isMarginEnabled);
        c.setIsDebitEnabled(isDebitEnabled);
        return c;
    }

    // --------------------------------------------

    public static MarketTicker fromResponse(String baseCurrency, String quoteCurrency, MarketTickerResponse marketTickerResponse) {
        return fromResponse(null, baseCurrency, quoteCurrency, marketTickerResponse);
    }

    public static MarketTicker fromResponse(@Nullable Long marketTickerId, String baseCurrency, String quoteCurrency, MarketTickerResponse marketTickerResponse) {
        return of(marketTickerId, baseCurrency, quoteCurrency, marketTickerResponse.symbol(),
                marketTickerResponse.symbolName(), marketTickerResponse.takerFeeRate(), marketTickerResponse.makerFeeRate(),
                marketTickerResponse.takerCoefficient(), marketTickerResponse.makerCoefficient());
    }

    private static MarketTicker of(@Nullable Long marketTickerId, String baseCurrency, String quoteCurrency, String symbol,
                                   String symbolName, BigDecimal takerFeeRate, BigDecimal makerFeeRate,
                                   BigDecimal takerCoefficient, BigDecimal makerCoefficient) {
        MarketTicker ticker = new MarketTicker();
        ticker.setId(marketTickerId);
        Currency base = new Currency();
        base.setCurrency(baseCurrency);
        ticker.setBase(base);
        Currency quote = new Currency();
        quote.setCurrency(quoteCurrency);
        ticker.setQuote(quote);
        ticker.setSymbol(symbol);
        ticker.setSymbolName(symbolName);
        ticker.setTakerFeeRate(takerFeeRate != null ? takerFeeRate.toString() : null);
        ticker.setMakerFeeRate(makerFeeRate != null ? makerFeeRate.toString() : null);
        ticker.setTakerCoefficient(takerCoefficient != null ? takerCoefficient.toString() : null);
        ticker.setMakerCoefficient(makerCoefficient != null ? makerCoefficient.toString() : null);
        return ticker;
    }

    // --------------------------------------------

    public static CurrencyChain fromResponse(String currency, String chain, ApiCurrencyDetailChainPropertyResponse marketTickerResponse) {
        return fromResponse(null, currency, chain, marketTickerResponse);
    }

    public static CurrencyChain fromResponse(@Nullable Long currencyChainId, String currency, @Nullable String chain, ApiCurrencyDetailChainPropertyResponse marketTickerResponse) {
        return of(currencyChainId, currency, chain,
                marketTickerResponse.minWithdrawSize() != null ? marketTickerResponse.minWithdrawSize().toString() : null,
                marketTickerResponse.minWithdrawFee() != null ? marketTickerResponse.minWithdrawFee().toString() : null,
                marketTickerResponse.isWithdrawEnabled(), marketTickerResponse.isDepositEnabled(), marketTickerResponse.confirms(), marketTickerResponse.contractAddress());
    }

    private static CurrencyChain of(@Nullable Long id, String currency, @Nullable String chain_, @Nullable String withdrawalMinSize, @Nullable String withdrawalMinFee,
                                    @Nullable Boolean isWithdrawEnabled, @Nullable Boolean isDepositEnabled, @Nullable Integer confirms, @Nullable String contractAddress) {
        CurrencyChain chain = new CurrencyChain(currency, chain_);
        if (id != null && id != -1L) {
            chain.setId(id);
        }
        chain.setWithdrawalMinSize(withdrawalMinSize);
        chain.setWithdrawalMinFee(withdrawalMinFee);
        chain.setIsWithdrawEnabled(isWithdrawEnabled);
        chain.setIsDepositEnabled(isDepositEnabled);
        chain.setConfirms(confirms);
        chain.setContractAddress(contractAddress);
        return chain;
    }

    // --------------------------------------------

    public static CurrencyExtendedInfo fromResponse(String currency, CurrencyExtendedInfoResponse currencyExtendedInfoResponse) {
        try {
            String currencyExtendedInfoJson = OBJECT_MAPPER.writeValueAsString(currencyExtendedInfoResponse);
            return of(currency, currencyExtendedInfoJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static CurrencyExtendedInfo of(String currency, String currencyExtendedInfoJson) {
        CurrencyExtendedInfo info = new CurrencyExtendedInfo(currency);
        info.setCurrency(currency);
        info.setCurrencyExtendedInfoJson(currencyExtendedInfoJson);
        return info;
    }

    public static CurrencyExtendedInfoResponse fromCurrencyExtendedInfo(CurrencyExtendedInfo info) {
        try {
            String currencyExtendedInfoJson = info.getCurrencyExtendedInfoJson();
            return OBJECT_MAPPER.readValue(currencyExtendedInfoJson, ImmutableCurrencyExtendedInfoResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
