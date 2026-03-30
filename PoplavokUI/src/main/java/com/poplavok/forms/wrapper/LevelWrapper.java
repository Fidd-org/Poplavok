package com.poplavok.forms.wrapper;

import com.poplavok.data.model.Currency;
import com.poplavok.data.model.Level;

import javax.annotation.Nullable;

import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;

public class LevelWrapper {
    public final Level level;
    public final String currency;

    public LevelWrapper(Level level, String currency) {
        this.level = level;
        this.currency = currency;
    }

    public @Nullable String getPoplavokString() {
        return level.getPoplavokString();
    }

    public @Nullable String getLevelString() {
        return level.getLevelString();
    }

    public String getAvailableAmountStr() {
        Currency levelBase = level.getPoplavok().getTicker().getBase();
        Currency levelQuote = level.getPoplavok().getTicker().getQuote();

        if (levelBase.getCurrency().equals(currency)) {
            return formatAmount(level.getAvailableAmountBase());
        } else if (levelQuote.getCurrency().equals(currency)) {
            return formatAmount(level.getAvailableAmountQuote());
        } else {
            return "N/A";
        }
    }
}
