package com.poplavok.forms.wrapper.repayment;

import com.poplavok.data.model.RepaymentType;

import java.math.BigDecimal;

abstract class BaseRepaymentInfo implements RepaymentInfo {
    private final BigDecimal amount;
    private final String currency;
    private final RepaymentType repaymentType;

    public BaseRepaymentInfo(RepaymentType repaymentType, BigDecimal amount, String currency) {
        this.amount = amount;
        this.repaymentType = repaymentType;
        this.currency = currency;
    }

    @Override
    public RepaymentType getRepaymentType() {
        return repaymentType;
    }

    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String getCurrency() {
        return currency;
    }
}

