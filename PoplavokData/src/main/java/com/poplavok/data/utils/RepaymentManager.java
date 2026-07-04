package com.poplavok.data.utils;

import com.poplavok.data.dao.AccountDAO;
import com.poplavok.data.dao.LevelDAO;
import com.poplavok.data.dao.LoanDAO;
import com.poplavok.data.dao.RepaymentDAO;
import com.poplavok.data.model.Account;
import com.poplavok.data.model.Currency;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.model.Repayment;
import com.poplavok.data.model.RepaymentType;

import java.math.BigDecimal;
import java.util.Date;

import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;

public class RepaymentManager {
    public static Repayment repay(Loan loan, Level sourceLevel, BigDecimal repaymentAmount, Date date, String notes) {
        return repayInternal(loan, sourceLevel, repaymentAmount, date, notes, true, true);
    }

    public static Repayment repayForLoanTransfer(Loan loan, Level sourceLevel, BigDecimal repaymentAmount, Date date, String notes) {
        return repayInternal(loan, sourceLevel, repaymentAmount, date, notes, false, false);
    }

    protected static void removeAmountFromSourceLevel(Level sourceLevel, String loanCurrency, BigDecimal repaymentAmount) {
        // Source level: remove amount from available, remove from debt amount
        String sourceQuote = sourceLevel.getPoplavok().getTicker().getQuote().getCurrency();
        String sourceBase = sourceLevel.getPoplavok().getTicker().getBase().getCurrency();
        if (loanCurrency.equals(sourceQuote)) {
            BigDecimal availableAmountQuote = nullToZero(sourceLevel.getAvailableAmountQuote());
            if (availableAmountQuote.compareTo(repaymentAmount) < 0) {
                throw new RuntimeException("Not enough available amount in the source level to cover the repayment "
                        + formatAmount(availableAmountQuote) + " < " + formatAmount(repaymentAmount));
            }
            sourceLevel.setAvailableAmountQuote(availableAmountQuote.subtract(repaymentAmount));

            BigDecimal debtAmountQuote = nullToZero(sourceLevel.getDebtQuote());
            sourceLevel.setDebtQuote(debtAmountQuote.subtract(repaymentAmount));
        } else if (loanCurrency.equals(sourceBase)) {
            BigDecimal availableAmountBase = nullToZero(sourceLevel.getAvailableAmountBase());
            if (availableAmountBase.compareTo(repaymentAmount) < 0) {
                throw new RuntimeException("Not enough available amount in the source level to cover the repayment "
                        + formatAmount(availableAmountBase) + " < " + formatAmount(repaymentAmount));
            }
            sourceLevel.setAvailableAmountBase(availableAmountBase.subtract(repaymentAmount));

            BigDecimal debtAmountBase = nullToZero(sourceLevel.getDebtBase());
            sourceLevel.setDebtBase(debtAmountBase.subtract(repaymentAmount));
        } else {
            throw new RuntimeException("Loan currency doesn't match either BASE or QUOTE of the source level's ticker");
        }
    }

    protected static Repayment repayInternal(Loan loan, Level sourceLevel, BigDecimal repaymentAmount, Date date, String notes,
                                             boolean removeAmountFromSource, boolean saveToDB) {
        String loanCurrency = loan.getCurrency().getCurrency();

        // 1. Process source
        // Source level: remove amount from available, remove from debt amount
        if (removeAmountFromSource) {
            removeAmountFromSourceLevel(sourceLevel, loanCurrency, repaymentAmount);
        }

        // 2. Process destination
        // We're repaying so loan's source and destination are flipped
        Account destinationAccount = loan.getSourceAccount();
        Level destinationLevel = loan.getSourceLevel();

        if (destinationAccount != null) {
            // Add amount to destination account's available amount
            BigDecimal currentAmount = destinationAccount.getAvailableAmount();
            destinationAccount.setAvailableAmount(currentAmount.add(repaymentAmount));
        } else if (destinationLevel != null) {
            // Destination level: add amount to available, remove from lent amount
            String destinationQuote = destinationLevel.getPoplavok().getTicker().getQuote().getCurrency();
            String destinationBase = destinationLevel.getPoplavok().getTicker().getBase().getCurrency();
            if (loanCurrency.equals(destinationQuote)) {
                BigDecimal availableAmountQuote = nullToZero(destinationLevel.getAvailableAmountQuote());
                destinationLevel.setAvailableAmountQuote(availableAmountQuote.add(repaymentAmount));

                BigDecimal lentAmountQuote = nullToZero(destinationLevel.getLentAmountQuote());
                destinationLevel.setLentAmountQuote(lentAmountQuote.subtract(repaymentAmount));
            } else if (loanCurrency.equals(destinationBase)) {
                BigDecimal availableAmountBase = nullToZero(destinationLevel.getAvailableAmountBase());
                destinationLevel.setAvailableAmountBase(availableAmountBase.add(repaymentAmount));

                BigDecimal lentAmountBase = nullToZero(destinationLevel.getLentAmountBase());
                destinationLevel.setLentAmountBase(lentAmountBase.subtract(repaymentAmount));
            } else {
                throw new RuntimeException("Loan currency doesn't match either BASE or QUOTE of the destination level's ticker");
            }
        }

        // 3. Update Loan info
        BigDecimal newRepaidAmount = nullToZero(loan.getRepaidAmount()).add(repaymentAmount);
        loan.setRepaidAmount(newRepaidAmount);
        if (loan.isFullyRepaidOrLost()) {
            loan.setActive(false);
        }

        // 4. Create Repayment
        Repayment dbRepayment = new Repayment(loan.getCurrency(), null, sourceLevel,
                destinationAccount, destinationLevel,
                repaymentAmount, date, RepaymentType.REPAY, loan, notes);

        // 5. Save everything to DB
        if (saveToDB) {
            DBUtil.connectCommitAndClose(sess -> {
                LevelDAO.update(sess, sourceLevel);
                if (destinationAccount != null) {
                    AccountDAO.update(sess, destinationAccount);
                } else if (destinationLevel != null) {
                    LevelDAO.update(sess, destinationLevel);
                }

                LoanDAO.update(sess, loan);
                RepaymentDAO.save(sess, dbRepayment);
            });
        }

        return dbRepayment;
    }

    public static Repayment takeProfitToAccount(Level sourceLevel, MarketTicker ticker, Account destinationAccount, BigDecimal repaymentAmount, String repaymentCurrency, Date date) {
        // 1. Process source

        // Source level: remove amount from available
        String sourceQuote = ticker.getQuote().getCurrency();
        String sourceBase = ticker.getBase().getCurrency();
        if (repaymentCurrency.equals(sourceQuote)) {
            BigDecimal availableAmountQuote = nullToZero(sourceLevel.getAvailableAmountQuote());
            if (availableAmountQuote.compareTo(repaymentAmount) < 0) {
                throw new RuntimeException("Not enough available amount in the source level to cover the repayment "
                        + formatAmount(availableAmountQuote) + " < " + formatAmount(repaymentAmount));
            }
            sourceLevel.setAvailableAmountQuote(availableAmountQuote.subtract(repaymentAmount));
        } else if (repaymentCurrency.equals(sourceBase)) {
            BigDecimal availableAmountBase = nullToZero(sourceLevel.getAvailableAmountBase());
            if (availableAmountBase.compareTo(repaymentAmount) < 0) {
                throw new RuntimeException("Not enough available amount in the source level to cover the repayment "
                        + formatAmount(availableAmountBase) + " < " + formatAmount(repaymentAmount));
            }
            sourceLevel.setAvailableAmountBase(availableAmountBase.subtract(repaymentAmount));
        } else {
            throw new RuntimeException("Loan currency doesn't match either BASE or QUOTE of the source level's ticker");
        }

        // 2. Process destination

        // Add amount to destination account's available amount
        BigDecimal currentAmount = destinationAccount.getAvailableAmount();
        destinationAccount.setAvailableAmount(currentAmount.add(repaymentAmount));

        // 3. Create Repayment
        Repayment dbRepayment = new Repayment(destinationAccount.getCurrency(), null, sourceLevel,
                destinationAccount, null, repaymentAmount, date, RepaymentType.PROFIT, null, null);

        // 4. Save everything to DB
        DBUtil.connectCommitAndClose(sess -> {
            LevelDAO.update(sess, sourceLevel);
            AccountDAO.update(sess, destinationAccount);
            RepaymentDAO.save(sess, dbRepayment);
        });

        return dbRepayment;
    }

    public static Repayment takeProfitToLevelAndSave(Level sourceLevel, MarketTicker ticker, Level destinationLevel, BigDecimal repaymentAmount, String repaymentCurrency, Date date) {
        Repayment dbRepayment = takeProfitToLevel(sourceLevel, ticker, destinationLevel, repaymentAmount, repaymentCurrency, date);

        // 4. Save everything to DB
        DBUtil.connectCommitAndClose(sess -> {
            LevelDAO.update(sess, sourceLevel);
            LevelDAO.update(sess, destinationLevel);
            RepaymentDAO.save(sess, dbRepayment);
        });

        return dbRepayment;
    }

    public static Repayment takeProfitToLevel(Level sourceLevel, MarketTicker ticker, Level destinationLevel, BigDecimal repaymentAmount, String repaymentCurrency, Date date) {
        if (destinationLevel.getNullableId() != null && sourceLevel.getId().equals(destinationLevel.getId())) {
            throw new RuntimeException("Cannot take profit to the same level");
        }

        // 1. Process source

        // Source level: remove amount from available
        Currency repaymentCurrencyObj;
        String sourceQuote = ticker.getQuote().getCurrency();
        String sourceBase = ticker.getBase().getCurrency();
        if (repaymentCurrency.equals(sourceQuote)) {
            repaymentCurrencyObj = ticker.getQuote();
            BigDecimal availableAmountQuote = nullToZero(sourceLevel.getAvailableAmountQuote());
            if (availableAmountQuote.compareTo(repaymentAmount) < 0) {
                throw new RuntimeException("Not enough available amount in the source level to cover the repayment "
                        + formatAmount(availableAmountQuote) + " < " + formatAmount(repaymentAmount));
            }
            sourceLevel.setAvailableAmountQuote(availableAmountQuote.subtract(repaymentAmount));
        } else if (repaymentCurrency.equals(sourceBase)) {
            repaymentCurrencyObj = ticker.getBase();
            BigDecimal availableAmountBase = nullToZero(sourceLevel.getAvailableAmountBase());
            if (availableAmountBase.compareTo(repaymentAmount) < 0) {
                throw new RuntimeException("Not enough available amount in the source level to cover the repayment "
                        + formatAmount(availableAmountBase) + " < " + formatAmount(repaymentAmount));
            }
            sourceLevel.setAvailableAmountBase(availableAmountBase.subtract(repaymentAmount));
        } else {
            throw new RuntimeException("Loan currency doesn't match either BASE or QUOTE of the source level's ticker");
        }

        // 2. Process destination

        // Add amount to destination level's available amount
        String destinationQuote = destinationLevel.getPoplavok().getTicker().getQuote().getCurrency();
        String destinationBase = destinationLevel.getPoplavok().getTicker().getBase().getCurrency();
        if (repaymentCurrency.equals(destinationQuote)) {
            BigDecimal availableAmountQuote = nullToZero(destinationLevel.getAvailableAmountQuote());
            destinationLevel.setAvailableAmountQuote(availableAmountQuote.add(repaymentAmount));
        } else if (repaymentCurrency.equals(destinationBase)) {
            BigDecimal availableAmountBase = nullToZero(destinationLevel.getAvailableAmountBase());
            destinationLevel.setAvailableAmountBase(availableAmountBase.add(repaymentAmount));
        } else {
            throw new RuntimeException("Loan currency doesn't match either BASE or QUOTE of the destination level's ticker");
        }

        // 3. Create Repayment
        Repayment dbRepayment = new Repayment(repaymentCurrencyObj, null, sourceLevel,
                null, destinationLevel, repaymentAmount, date, RepaymentType.PROFIT, null, null);

        return dbRepayment;
    }

    public static Repayment takeLoss(Loan loan, MarketTicker ticker, Level sourceLevel, BigDecimal lossAmount, Date date) {
        String loanCurrency = loan.getCurrency().getCurrency();

        // 1. Process source

        // Source level: writing off debt amount
        String sourceQuote = ticker.getQuote().getCurrency();
        String sourceBase = ticker.getBase().getCurrency();
        if (loanCurrency.equals(sourceQuote)) {
            BigDecimal debtAmountQuote = nullToZero(sourceLevel.getDebtQuote());
            sourceLevel.setDebtQuote(debtAmountQuote.subtract(lossAmount));
        } else if (loanCurrency.equals(sourceBase)) {
            BigDecimal debtAmountBase = nullToZero(sourceLevel.getDebtBase());
            sourceLevel.setDebtBase(debtAmountBase.subtract(lossAmount));
        } else {
            throw new RuntimeException("Loan currency doesn't match either BASE or QUOTE of the source level's ticker");
        }

        // 2. Process destination

        // We're repaying so source and destination are flipped
        Account destinationAccount = loan.getSourceAccount();
        Level destinationLevel = loan.getSourceLevel();

        if (destinationAccount != null) {
            // Nothing to do with the account
        } else if (destinationLevel != null) {
            // Destination level: lost funds removed from lent amount
            String destinationQuote = destinationLevel.getPoplavok().getTicker().getQuote().getCurrency();
            String destinationBase = destinationLevel.getPoplavok().getTicker().getBase().getCurrency();
            if (loanCurrency.equals(destinationQuote)) {
                BigDecimal lentAmountQuote = nullToZero(destinationLevel.getLentAmountQuote());
                destinationLevel.setLentAmountQuote(lentAmountQuote.subtract(lossAmount));
            } else if (loanCurrency.equals(destinationBase)) {
                BigDecimal lentAmountBase = nullToZero(destinationLevel.getLentAmountBase());
                destinationLevel.setLentAmountBase(lentAmountBase.subtract(lossAmount));
            } else {
                throw new RuntimeException("Loan currency doesn't match either BASE or QUOTE of the destination level's ticker");
            }
        }

        // 3. Update Loan info - we're considering lost amount repaid
        BigDecimal newLostAmount = nullToZero(loan.getLostAmount()).add(lossAmount);
        loan.setLostAmount(newLostAmount);
        if (loan.isFullyRepaidOrLost()) {
            loan.setActive(false);
        }

        // 4. Create Repayment
        Repayment dbRepayment = new Repayment(loan.getCurrency(), null, sourceLevel,
                destinationAccount, destinationLevel,
                lossAmount, new Date(), RepaymentType.LOSS, loan, null);

        // 5. Save everything to DB
        DBUtil.connectCommitAndClose(sess -> {
            LevelDAO.update(sess, sourceLevel);
            if (destinationAccount != null) {
                AccountDAO.update(sess, destinationAccount);
            } else if (destinationLevel != null) {
                LevelDAO.update(sess, destinationLevel);
            }

            LoanDAO.update(sess, loan);
            RepaymentDAO.save(sess, dbRepayment);
        });

        return dbRepayment;
    }
}
