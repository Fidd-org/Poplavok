package com.poplavok.data.dao;

import com.poplavok.data.model.Loan;
import com.poplavok.data.model.Repayment;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.List;

import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;

public class LoanInfo {
    public final Loan loan;
    public final List<Repayment> repayments;

    public LoanInfo(Loan loan, List<Repayment> repayments) {
        this.loan = loan;
        this.repayments = repayments;
    }

    public Long getLoanId() {
        return loan.getId();
    }

    public @Nullable String getSource() {
        if (loan.getSourceAccount() != null) {
            return loan.getSourceAccount().getAccountString();
        } else if (loan.getSourceLevel() != null) {
            return loan.getSourceLevel().getLevelString();
        } else {
            return "External";
        }
    }

    public BigDecimal getRepaidAmount() {
        BigDecimal totalRepaid = BigDecimal.ZERO;
        for (Repayment repayment : repayments) {
            totalRepaid = totalRepaid.add(repayment.getAmount());
        }
        return totalRepaid;
    }

    public BigDecimal getRemainingOwedAmount() {
        return loan.getAmount().subtract(getRepaidAmount());
    }

    public String getRepaid() {
        BigDecimal totalRepaid = getRepaidAmount();
        return formatAmount(totalRepaid);
    }

    public String getTotalAmount() {
        return formatAmount(loan.getAmount());
    }

    public String getRemainingOwed() {
        return formatAmount(getRemainingOwedAmount());
    }

    public String getLoanCurrency() {
        return loan.getCurrency().getCurrency();
    }
}

