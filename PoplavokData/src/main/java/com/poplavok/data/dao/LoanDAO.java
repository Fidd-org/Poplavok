package com.poplavok.data.dao;

import com.poplavok.data.model.Loan;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class LoanDAO {
    public static void save(Session session, Loan loan) {
        session.persist(loan);
    }

    public static Loan update(Session session, Loan loan) {
        return session.merge(loan);
    }

    public static void delete(Session session, Loan loan) {
        session.remove(loan);
    }

    public static Optional<Loan> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(Loan.class, id));
    }

    public static List<Loan> findAll(Session session) {
        return session.createQuery("from Loan", Loan.class).list();
    }
}

