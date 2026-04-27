package com.poplavok.forms.wrapper.repayment;

import com.poplavok.data.model.Loan;
import com.poplavok.data.model.RepaymentType;
import java.math.BigDecimal;

public class LossRepaymentInfo extends BaseRepaymentInfo {
    protected final Loan loanToWriteOff;

    public LossRepaymentInfo(BigDecimal amount, Loan loanToWriteOff) {
        super(RepaymentType.LOSS, amount);
        this.loanToWriteOff = loanToWriteOff;
    }

    public Loan loanToWriteOff() {
        return loanToWriteOff;
    }
}

