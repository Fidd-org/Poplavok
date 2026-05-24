package com.poplavok.data.dao;

import com.poplavok.data.model.Trade;
import org.hibernate.Session;

import java.util.List;

public class TradeDAO {
    public static void save(Session session, Trade trade) {
        session.persist(trade);
    }

    public static void update(Session session, Trade trade) {
        session.merge(trade);
    }

    public static void delete(Session session, Trade trade) {
        session.remove(trade);
    }

    public static List<Trade> findByLevel(Session session, Long levelId) {
        return session.createQuery("select lt.trade from LevelTrade lt where lt.level.id = :levelId", Trade.class)
                .setParameter("levelId", levelId)
                .getResultList();
    }
}
