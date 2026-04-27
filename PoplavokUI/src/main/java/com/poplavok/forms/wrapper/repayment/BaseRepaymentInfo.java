package com.poplavok.forms.wrapper.repayment;

import com.poplavok.data.model.RepaymentType;

import java.math.BigDecimal;

abstract class BaseRepaymentInfo implements RepaymentInfo {
    private final BigDecimal amount;
    private final RepaymentType repaymentType;

    public BaseRepaymentInfo(RepaymentType repaymentType, BigDecimal amount) {
        this.amount = amount;
        this.repaymentType = repaymentType;
    }

    @Override
    public RepaymentType getRepaymentType() {
        return repaymentType;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }
}

