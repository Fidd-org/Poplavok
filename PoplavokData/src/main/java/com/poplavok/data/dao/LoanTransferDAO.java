package com.poplavok.data.dao;

import com.poplavok.data.model.LoanTransfer;
import org.hibernate.Session;

public class LoanTransferDAO {
    public static void save(Session session, LoanTransfer loanTransfer) {
        session.persist(loanTransfer);
    }

    public static void update(Session session, LoanTransfer loanTransfer) {
        session.merge(loanTransfer);
    }

    public static void delete(Session session, LoanTransfer loanTransfer) {
        session.remove(loanTransfer);
    }
}
