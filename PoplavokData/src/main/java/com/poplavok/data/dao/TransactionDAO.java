package com.poplavok.data.dao;

import com.poplavok.data.model.ExternalTransaction;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.Repayment;
import com.poplavok.data.model.Transaction;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class TransactionDAO {

    public static void save(Session session, Transaction transaction) {
        session.persist(transaction);
    }

    public static void update(Session session, Transaction transaction) {
        session.merge(transaction);
    }

    public static void delete(Session session, Transaction transaction) {
        session.remove(transaction);
    }

    public static Optional<Transaction> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(Transaction.class, id));
    }

    public static Optional<Loan> findLoanById(Session session, Long id) {
        return Optional.ofNullable(session.find(Loan.class, id));
    }

    public static Optional<Repayment> findRepaymentById(Session session, Long id) {
        return Optional.ofNullable(session.find(Repayment.class, id));
    }

    public static Optional<ExternalTransaction> findExternalTransactionById(Session session, Long id) {
        return Optional.ofNullable(session.find(ExternalTransaction.class, id));
    }

    public static List<Transaction> findAll(Session session) {
        return session.createQuery(
                "select t from Transaction t " +
                        "left join fetch t.currency " +
                        "left join fetch t.sourceAccount " +
                        "left join fetch t.destinationAccount " +
                        "left join fetch t.sourceLevel " +
                        "left join fetch t.destinationLevel " +
                        "order by t.date desc",
                Transaction.class).list();
    }

    public static List<Loan> findAllLoans(Session session) {
        return session.createQuery(
                "select l from Loan l " +
                        "left join fetch l.currency " +
                        "left join fetch l.destinationLevel " + // Loans usually have a destination level (the borrowed assets)
                        "order by l.date desc",
                Loan.class).list();
    }

    public static List<Loan> findActiveLoans(Session session) {
        return session.createQuery(
                "select l from Loan l " +
                        "left join fetch l.currency " +
                        "where l.isActive = true " +
                        "order by l.date desc",
                Loan.class).list();
    }

    public static List<Repayment> findAllRepayments(Session session) {
        return session.createQuery(
                "select r from Repayment r " +
                        "left join fetch r.currency " +
                        "left join fetch r.loan " +
                        "order by r.date desc",
                Repayment.class).list();
    }

    public static List<Repayment> findRepaymentsByLoan(Session session, Loan loan) {
        return session.createQuery(
                "select r from Repayment r " +
                        "left join fetch r.currency " +
                        "where r.loan = :loan " +
                        "order by r.date desc",
                Repayment.class)
                .setParameter("loan", loan)
                .list();
    }

    public static List<ExternalTransaction> findAllExternalTransactions(Session session) {
        return session.createQuery(
                "select et from ExternalTransaction et " +
                        "left join fetch et.currency " +
                        "left join fetch et.sourceAccount " +
                        "left join fetch et.destinationAccount " +
                        "order by et.date desc",
                ExternalTransaction.class).list();
    }

    public static List<Transaction> findByAccount(Session session, Long accountId) {
        return session.createQuery(
                "select t from Transaction t " +
                        "left join fetch t.currency " +
                        "left join fetch t.sourceAccount " +
                        "left join fetch t.destinationAccount " +
                        "left join fetch t.sourceLevel " +
                        "left join fetch t.destinationLevel " +
                        "where t.sourceAccount.id = :accountId or t.destinationAccount.id = :accountId " +
                        "order by t.date desc",
                Transaction.class)
                .setParameter("accountId", accountId)
                .list();
    }
}
