package com.poplavok.data.dao;

import com.poplavok.data.model.Repayment;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class RepaymentDAO {
    public static void save(Session session, Repayment repayment) {
        session.persist(repayment);
    }

    public static Repayment update(Session session, Repayment repayment) {
        return session.merge(repayment);
    }

    public static void delete(Session session, Repayment repayment) {
        session.remove(repayment);
    }

    public static Optional<Repayment> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(Repayment.class, id));
    }

    public static List<Repayment> findAll(Session session) {
        return session.createQuery("from Repayment", Repayment.class).list();
    }
}

