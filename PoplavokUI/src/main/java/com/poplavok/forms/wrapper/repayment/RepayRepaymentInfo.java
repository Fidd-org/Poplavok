package com.poplavok.forms.wrapper.repayment;

import com.poplavok.data.model.Loan;
import com.poplavok.data.model.RepaymentType;
import java.math.BigDecimal;

public class RepayRepaymentInfo extends BaseRepaymentInfo {
    protected final Loan loanToRepay;

    public RepayRepaymentInfo(BigDecimal amount, Loan loanToRepay) {
        super(RepaymentType.REPAY, amount);
        this.loanToRepay = loanToRepay;
    }

    public Loan getLoanToRepay() {
        return loanToRepay;
    }
}

