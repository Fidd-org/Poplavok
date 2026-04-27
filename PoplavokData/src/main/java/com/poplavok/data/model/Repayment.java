package com.poplavok.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
@Table(name = "repayments")
@PrimaryKeyJoinColumn(name = "transaction_id")
public class Repayment extends Transaction {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    @Nullable
    private Loan loan;

    @Column(length = 500)
    @Nullable
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_type", length = 50)
    @Nullable
    private RepaymentType type;

    protected Repayment() {
    }

    public Repayment(Currency currency, @Nullable Account sourceAccount, @Nullable Level sourceLevel,
                     @Nullable Account destinationAccount, @Nullable Level destinationLevel,
                     BigDecimal amount, Date date, RepaymentType type, @Nullable Loan loan, @Nullable String notes) {
        super(currency, sourceAccount, sourceLevel, destinationAccount, destinationLevel, amount, date);
        setRepaymentType(type);
        setLoan(loan);
        setNotes(notes);
    }

    public Loan getLoan() {
        return checkNotNull(loan);
    }

    void setLoan(@Nullable Loan loan) {
        this.loan = loan;
    }

    public @Nullable String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }

    public @Nullable RepaymentType getRepaymentType() {
        return type;
    }

    public void setRepaymentType(RepaymentType type) {
        this.type = type;
    }
}
