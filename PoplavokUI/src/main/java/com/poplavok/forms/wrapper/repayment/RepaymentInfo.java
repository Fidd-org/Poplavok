package com.poplavok.forms.wrapper.repayment;

import com.poplavok.data.model.RepaymentType;

import java.math.BigDecimal;

public interface RepaymentInfo {
    RepaymentType getRepaymentType();
    BigDecimal getAmount();
}
