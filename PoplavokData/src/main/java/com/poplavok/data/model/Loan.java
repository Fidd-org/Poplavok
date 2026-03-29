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
}
