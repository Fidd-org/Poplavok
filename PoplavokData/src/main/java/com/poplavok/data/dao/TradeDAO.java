package com.poplavok.data.dao;

import com.poplavok.data.model.Trade;
import org.hibernate.Session;

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
}
