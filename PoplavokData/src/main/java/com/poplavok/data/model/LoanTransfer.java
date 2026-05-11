package com.poplavok.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
@Table(name = "loan_transfer")
public class LoanTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repayment_id")
    @Nullable
    private Repayment repayment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    @Nullable
    private Loan newLoan;

    @Column(name = "amount", precision = 20, scale = 8)
    @Nullable
    private BigDecimal transferAmount;

    @Column(nullable = false)
    @Nullable
    private Date date;

    public LoanTransfer() {
    }

    public Long getId() {
        return checkNotNull(id);
    }

    @Nullable
    public Repayment getRepayment() {
        return repayment;
    }

    public void setRepayment(@Nullable Repayment repayment) {
        this.repayment = repayment;
    }

    @Nullable
    public Loan getNewLoan() {
        return newLoan;
    }

    public void setNewLoan(@Nullable Loan newLoan) {
        this.newLoan = newLoan;
    }

    @Nullable
    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(@Nullable BigDecimal transferAmount) {
        this.transferAmount = transferAmount;
    }

    public @Nullable Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
