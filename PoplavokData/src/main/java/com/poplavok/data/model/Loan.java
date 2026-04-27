package com.poplavok.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;

@Entity
@Table(name = "loans")
@PrimaryKeyJoinColumn(name = "transaction_id")
public class Loan extends Transaction {

    @Column(name = "is_active", nullable = false)
    @Nullable
    private boolean isActive;

    @Enumerated(EnumType.STRING)
    @Column(name = "loan_type", nullable = false, length = 20)
    @Nullable
    private LoanType loanType;

    @Column(name = "interest_rate", precision = 10, scale = 6)
    @Nullable
    private BigDecimal interestRate;

    @Column(name = "repaid_amount", precision = 10, scale = 6)
    @Nullable
    private BigDecimal repaidAmount;

    @Column(name = "lost_amount", precision = 10, scale = 6)
    @Nullable
    private BigDecimal lostAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_rate_type", length = 20)
    @Nullable
    private InterestRateType interestRateType;

    @Column(length = 500)
    @Nullable
    private String notes;

    public Loan() {
    }

    public Loan(Currency currency, BigDecimal amount, Level level,
                Date date, LoanType loanType) {
        super(currency, null, null, null, level, amount, date);
        this.loanType = loanType;
        this.isActive = true;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LoanType getLoanType() {
        return checkNotNull(loanType);
    }

    public void setLoanType(LoanType loanType) {
        this.loanType = loanType;
    }

    public BigDecimal getInterestRate() {
        return checkNotNull(interestRate);
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public InterestRateType getInterestRateType() {
        return checkNotNull(interestRateType);
    }

    public void setInterestRateType(InterestRateType interestRateType) {
        this.interestRateType = interestRateType;
    }

    public @Nullable String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public @Nullable BigDecimal getRepaidAmount() { return repaidAmount; }

    public void setRepaidAmount(@Nullable BigDecimal repaidAmount) { this.repaidAmount = repaidAmount; }

    public @Nullable BigDecimal getLostAmount() { return lostAmount; }

    public void setLostAmount(@Nullable BigDecimal lostAmount) { this.lostAmount = lostAmount; }

    public boolean isFullyRepaidOrLost() {
        BigDecimal repaid = nullToZero(getRepaidAmount());
        BigDecimal lost = nullToZero(getLostAmount());

        return (repaid.add(lost).compareTo(nullToZero(getAmount())) >= 0);
    }
}
