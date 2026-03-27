package com.poplavok.data.dao;

import com.poplavok.data.model.Rate;
import com.poplavok.data.utils.DBUtil;
import org.hibernate.Session;
import java.util.List;
import java.util.Optional;

public class RateDAO {
    public static void save(Session session, Rate rate) {
        session.persist(rate);
    }
    public static Rate update(Session session, Rate rate) {
        return session.merge(rate);
    }
    public static void delete(Session session, Rate rate) {
        session.remove(rate);
    }
    public static Optional<Rate> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(Rate.class, id));
    }
    public static List<Rate> findAll(Session session) {
        return session.createQuery("from Rate", Rate.class).list();
    }
    public static Rate getLatestRateForTicker(Long tickerId) {
        return DBUtil.connectGetResultAndClose(session -> 
            session.createQuery("from Rate r where r.marketTicker.id = :tickerId order by r.timestamp desc", Rate.class)
                .setParameter("tickerId", tickerId)
                .setMaxResults(1)
                .uniqueResult()
        );
    }
}
