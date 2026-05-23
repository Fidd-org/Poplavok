package com.poplavok.data.dao;

import com.poplavok.data.model.Direction;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.model.Poplavok;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class PoplavokDAO {
    public static void save(Session session, Poplavok poplavok) {
        session.persist(poplavok);
    }

    public static Poplavok update(Session session, Poplavok poplavok) {
        Poplavok merged = session.merge(poplavok);
        org.hibernate.Hibernate.initialize(merged.getTicker());
        return merged;
    }

    public static void delete(Session session, Poplavok poplavok) {
        session.remove(poplavok);
    }

    public static Optional<Poplavok> findById(Session session, Long id) {
        return session.createQuery("from Poplavok p left join fetch p.marketTicker where p.id = :id", Poplavok.class)
                .setParameter("id", id)
                .uniqueResultOptional();
    }

    public static List<Poplavok> findAll(Session session) {
        return session.createQuery("from Poplavok p left join fetch p.marketTicker", Poplavok.class).list();
    }
    
    public static List<Poplavok> findByActive(Session session, boolean isActive) {
        return session.createQuery("from Poplavok p left join fetch p.marketTicker where p.isActive = :isActive", Poplavok.class)
                .setParameter("isActive", isActive)
                .list();
    }

    public static List<Poplavok> findInversePoplavoks(Session session, MarketTicker ticker, Direction direction) {
        Direction opposite = direction == Direction.LONG ? Direction.SHORT : Direction.LONG;
        return session.createQuery("from Poplavok p left join fetch p.marketTicker where p.marketTicker = :ticker and p.direction = :opposite and p.closeDate is null", Poplavok.class)
                .setParameter("ticker", ticker)
                .setParameter("opposite", opposite)
                .list();
    }
}
