package com.poplavok.data.utils;

import com.poplavok.data.model.Level;
import com.poplavok.data.model.LevelState;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.LoanTransfer;
import com.poplavok.data.model.LoanType;
import com.poplavok.data.model.Repayment;

import java.math.BigDecimal;
import java.util.Date;

import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;

public class LoanTransferManager {
    public static LoanTransfer transferLoan(Loan sourceLoan, Level sourceLevel, Level destinationLevel,
                                            BigDecimal loanTransferAmount, Date date) {
        String loanCurrency = sourceLoan.getCurrency().getCurrency();
        String destinationQuote = destinationLevel.getPoplavok().getTicker().getQuote().getCurrency();
        String destinationBase = destinationLevel.getPoplavok().getTicker().getBase().getCurrency();

        // 1. Create Repayment - repay source loan
        Repayment repayment = RepaymentManager.repayForLoanTransfer(sourceLoan, sourceLevel, loanTransferAmount, date, "Loan transfer repayment");

        // 2. Create new Loan - transfer loan to destination level
        Loan destinationLoan = new Loan(sourceLoan.getCurrency(), loanTransferAmount, destinationLevel, date, LoanType.LOAN_TRANSFER);
        destinationLoan.setSourceAccount(sourceLoan.getSourceAccount());
        destinationLoan.setSourceLevel(sourceLoan.getSourceLevel());
        destinationLoan.setActive(true);
        destinationLoan.setNotes("Loan transfer");

        // 3. Update destination level's debt (add debt)
        if (loanCurrency.equals(destinationQuote)) {
            destinationLevel.setDebtQuote(nullToZero(destinationLevel.getDebtQuote()).add(loanTransferAmount));
        } else if (loanCurrency.equals(destinationBase)) {
            destinationLevel.setDebtBase(nullToZero(destinationLevel.getDebtBase()).add(loanTransferAmount));
        } else {
            throw new RuntimeException("Loan currency doesn't match either BASE or QUOTE of the destination level's ticker");
        }

        // 4. Update source level's debt (remove debt)
        if (loanCurrency.equals(destinationQuote)) {
            sourceLevel.setDebtQuote(nullToZero(sourceLevel.getDebtQuote()).subtract(loanTransferAmount));
        } else if (loanCurrency.equals(destinationBase)) {
            sourceLevel.setDebtBase(nullToZero(sourceLevel.getDebtBase()).subtract(loanTransferAmount));
        } else {
            throw new RuntimeException("Loan currency doesn't match either BASE or QUOTE of the source level's ticker");
        }

        if (destinationLevel.getState() == LevelState.INCEPTION) {
            destinationLevel.setState(LevelState.TRADING);
        }

        return new LoanTransfer(repayment, destinationLoan, loanTransferAmount, date);
    }
}
