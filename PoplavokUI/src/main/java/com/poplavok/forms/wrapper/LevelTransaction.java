package com.poplavok.forms.wrapper;

import com.poplavok.data.model.Account;
import com.poplavok.data.model.ExternalTransaction;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.Repayment;
import com.poplavok.data.model.Transaction;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

public class LevelTransaction {
    protected final Transaction transaction;
    protected final Level level;

    public LevelTransaction(Transaction transaction, Level level) {
        this.transaction = transaction;
        this.level = level;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Level getLevel() { return level; }

    public String getCurrencyCode() {
        return getTransaction().getCurrency().getCurrency();
    }

    static String getAccountOrLevelName(@Nullable Account account, @Nullable Level level) {
        if (account != null) {
            String name = checkNotNull(account).getAccountName();
            if (name == null) {
                name += "ID " + checkNotNull(account).getId();
            }
            return "Acc: " + name;
        } else if (level != null) {
            return "Lvl: " + level.getId().toString();
        } else {
            return "";
        }
    }

    public Long getId() {
        return getTransaction().getId();
    }

    public BigDecimal getAmount() {
        if (getTransaction().getSourceLevel() != null && getTransaction().getSourceLevel().getId().equals(getLevel().getId())) {
            return getTransaction().getAmount().negate();
        } else {
            return getTransaction().getAmount();
        }
    }

    public Date getDate() {
        return getTransaction().getDate();
    }

    public String getSourceName() {
        return getAccountOrLevelName(getTransaction().getSourceAccount(), getTransaction().getSourceLevel());
    }

    public String getDestinationName() {
        return getAccountOrLevelName(getTransaction().getDestinationAccount(), getTransaction().getDestinationLevel());
    }

    public String getTransactionType() {
        if (getTransaction() instanceof Loan) {
            return "Loan";
        } else if (getTransaction() instanceof Repayment) {
            return "Repayment";
        } else if (getTransaction() instanceof ExternalTransaction) {
            return "External";
        } else {
            return "Unknown";
        }
    }
}
