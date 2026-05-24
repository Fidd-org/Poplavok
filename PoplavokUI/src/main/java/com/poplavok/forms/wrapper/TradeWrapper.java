package com.poplavok.forms.wrapper;

import com.poplavok.data.model.Trade;
import com.poplavok.data.model.TradeOperation;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;

import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;
import static com.poplavok.data.utils.PriceCalculator.calculateBuyPriceFromTradeData;
import static com.poplavok.data.utils.PriceCalculator.calculateSellPriceFromTradeData;

public record TradeWrapper(Trade trade) {
    public BigDecimal getCalculatedTradePrice() {
        BigDecimal tradePrice = BigDecimal.ZERO;
        if (getOperation() == TradeOperation.BUY) {
            BigDecimal quoteIn = nullToZero(getAmountQuoteIn());
            BigDecimal commissionQuote = nullToZero(getCommissionQuote());
            BigDecimal baseOut = nullToZero(getAmountBaseOut());

            if (quoteIn.compareTo(BigDecimal.ZERO) > 0 && baseOut.compareTo(BigDecimal.ZERO) > 0) {
                tradePrice = calculateBuyPriceFromTradeData(quoteIn, commissionQuote, baseOut);
            }
        } else if (getOperation() == TradeOperation.SELL) {
            BigDecimal baseIn = nullToZero(getAmountBaseIn());
            BigDecimal commissionBase = nullToZero(getCommissionBase());
            BigDecimal quoteOut = nullToZero(getAmountQuoteOut());

            if (baseIn.compareTo(BigDecimal.ZERO) > 0 && quoteOut.compareTo(BigDecimal.ZERO) > 0) {
                tradePrice = calculateSellPriceFromTradeData(baseIn, commissionBase, quoteOut);
            }
        } else {
            throw new RuntimeException("Unknown trade direction");
        }

        return tradePrice;
    }

    public Long getId() {
        return trade.getId();
    }

    public TradeOperation getOperation() {
        return trade.getOperation();
    }

    public @Nullable BigDecimal getAmountBaseIn() {
        return trade.getAmountBaseIn();
    }

    public @Nullable BigDecimal getAmountBaseOut() {
        return trade.getAmountBaseOut();
    }

    public @Nullable BigDecimal getAmountQuoteIn() {
        return trade.getAmountQuoteIn();
    }

    public @Nullable BigDecimal getAmountQuoteOut() {
        return trade.getAmountQuoteOut();
    }

    public @Nullable BigDecimal getCommissionBase() {
        return trade.getCommissionBase();
    }

    public @Nullable BigDecimal getCommissionQuote() {
        return trade.getCommissionQuote();
    }

    public @Nullable Date getDate() {
        return trade.getDate();
    }
}
