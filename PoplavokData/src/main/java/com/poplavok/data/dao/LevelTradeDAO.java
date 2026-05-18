package com.poplavok.data.dao;

import com.poplavok.data.model.LevelTrade;
import org.hibernate.Session;

public class LevelTradeDAO {
    public static void save(Session session, LevelTrade trade) {
        session.persist(trade);
    }

    public static void update(Session session, LevelTrade trade) {
        session.merge(trade);
    }

    public static void delete(Session session, LevelTrade trade) {
        session.remove(trade);
    }
}
