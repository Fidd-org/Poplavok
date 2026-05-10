package com.poplavok.data.dao;

import com.poplavok.data.model.Level;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class LevelDAO {
    public static void save(Session session, Level level) {
        session.persist(level);
    }

    public static Level update(Session session, Level level) {
        return session.merge(level);
    }

    public static void delete(Session session, Level level) {
        session.remove(level);
    }

    public static Optional<Level> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(Level.class, id));
    }

    public static List<Level> findAll(Session session) {
        return session.createQuery("from Level", Level.class).list();
    }

    public static List<Level> findAvailableByCurrency(Session session, String currency) {
        String hql = "select l from Level l join fetch l.poplavok p join fetch p.marketTicker t " +
                     "join fetch t.base b join fetch t.quote q " +
                     "where (b.currency = :currency and l.availableAmountBase > 0) " +
                     "or (q.currency = :currency and l.availableAmountQuote > 0)";
        return session.createQuery(hql, Level.class)
                .setParameter("currency", currency)
                .list();
    }

    public static List<Level> findByCurrencies(Session session, List<String> currencies) {
        String hql = "select l from Level l join fetch l.poplavok p join fetch p.marketTicker t " +
                     "join fetch t.base b join fetch t.quote q " +
                     "where l.state = com.poplavok.data.model.LevelState.TRADING " +
                     "and p.isActive = true " +
                     "and (b.currency in :currencies or q.currency in :currencies)";
        List<Level> levels = session.createQuery(hql, Level.class)
                .setParameter("currencies", currencies)
                .list();

        levels.forEach(l -> {
            l.getPoplavok().getTicker().getBase().getCurrency();
            l.getPoplavok().getTicker().getQuote().getCurrency();
        });
        return levels;
    }

    public static List<Level> findByPoplavokId(Session session, Long poplavokId, boolean includeClosed) {
        String hql = "from Level l where l.poplavok.id = :poplavokId";
        if (!includeClosed) {
            hql += " and l.state != com.poplavok.data.model.LevelState.CLOSED";
        }
        return session.createQuery(hql, Level.class)
                .setParameter("poplavokId", poplavokId)
                .list();
    }
}
