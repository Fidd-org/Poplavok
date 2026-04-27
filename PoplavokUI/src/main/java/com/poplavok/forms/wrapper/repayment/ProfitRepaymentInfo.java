package com.poplavok.forms.wrapper.repayment;

import com.poplavok.data.model.Account;
import com.poplavok.data.model.RepaymentType;
import java.math.BigDecimal;

public class ProfitRepaymentInfo extends BaseRepaymentInfo {
    protected final Account accountToMoveProfitTo;

    public ProfitRepaymentInfo(BigDecimal amount, Account accountToMoveProfitTo) {
        super(RepaymentType.PROFIT, amount);
        this.accountToMoveProfitTo = accountToMoveProfitTo;
    }

    public Account getAccountToMoveProfitTo() {
        return accountToMoveProfitTo;
    }
}

