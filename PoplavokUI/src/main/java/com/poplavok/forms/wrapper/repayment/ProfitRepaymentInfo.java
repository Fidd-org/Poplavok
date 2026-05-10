package com.poplavok.forms.wrapper.repayment;

import com.poplavok.data.model.Account;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.RepaymentType;

import javax.annotation.Nullable;
import java.math.BigDecimal;

public class ProfitRepaymentInfo extends BaseRepaymentInfo {
    @Nullable protected final Account accountToMoveProfitTo;
    @Nullable protected final Level levelToMoveProfitTo;

    public ProfitRepaymentInfo(BigDecimal amount, String currency, Account accountToMoveProfitTo) {
        super(RepaymentType.PROFIT, amount, currency);
        this.accountToMoveProfitTo = accountToMoveProfitTo;
        this.levelToMoveProfitTo = null;
    }

    public ProfitRepaymentInfo(BigDecimal amount, String currency, Level levelToMoveProfitTo) {
        super(RepaymentType.PROFIT, amount, currency);
        this.levelToMoveProfitTo = levelToMoveProfitTo;
        this.accountToMoveProfitTo = null;
    }

    public @Nullable Account getAccountToMoveProfitTo() {
        return accountToMoveProfitTo;
    }

    public @Nullable Level getLevelToMoveProfitTo() {
        return levelToMoveProfitTo;
    }
}
