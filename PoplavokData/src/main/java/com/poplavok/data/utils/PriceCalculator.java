package com.poplavok.data.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.poplavok.data.utils.BigDecimalUtil.SCALE;

public class PriceCalculator {
    // --------------- PRICE CALCULATIONS ---------------

    /**
     * KuCoin cmomission mechanics: Selling
     *   - When we sell, commission is applied to the proceeds in QUOTE.
     *   - Thus, if we sell BASE worth $1000 at current price, it will first be converted to QUOTE, and then
     *      the commission will be applied to the proceeds.
     *     - In this case Seller will get $1000 - $1 = $999 with fee at 0.001.
     *   - I.e. Sell commission is charged in QUOTE _after_ operation.
     */
    public static PriceAndCommission calculateSellPrice(BigDecimal giveBaseSell, BigDecimal getQuoteSell, BigDecimal fee) {
        BigDecimal withoutCommission = BigDecimal.ONE.subtract(fee);
        BigDecimal price = getQuoteSell.divide(giveBaseSell.multiply(withoutCommission), SCALE, RoundingMode.CEILING);
        BigDecimal cleanQuote = giveBaseSell.multiply(price);
        BigDecimal commission = cleanQuote.multiply(fee);

        return new PriceAndCommission(commission, price);
    }

    /**
     * KuCoin commission mechanics: Buying
     *   - When we buy, KuCoin applies commission on top of entered QUOTE amount.
     *   - Thus, if we buy $1000 worth of BASE with a fee at 0.001, it will actually withdraw $1001,
     *      even though we specifically entered $1000.
     *   - I.e. Buy commission is charged in QUOTE _before_ operation.
     */
    public static PriceAndCommission calculateBuyPrice(BigDecimal giveQuoteBuy, BigDecimal getBaseBuy, BigDecimal fee) {
        BigDecimal withCommission = BigDecimal.ONE.add(fee);
        BigDecimal cleanQuote = giveQuoteBuy.divide(withCommission, SCALE, RoundingMode.FLOOR);
        BigDecimal price = cleanQuote.divide(getBaseBuy, SCALE, RoundingMode.CEILING);
        BigDecimal commission = cleanQuote.multiply(fee);

        return new PriceAndCommission(commission, price);
    }
}
