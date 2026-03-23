package com.poplavok.data.dao;

import com.poplavok.data.model.Poplavok;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class PoplavokDAO {
    public static void save(Session session, Poplavok poplavok) {
        session.persist(poplavok);
    }

    public static Poplavok update(Session session, Poplavok poplavok) {
        return session.merge(poplavok);
    }

    public static void delete(Session session, Poplavok poplavok) {
        session.remove(poplavok);
    }

    public static Optional<Poplavok> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(Poplavok.class, id));
    }

    public static List<Poplavok> findAll(Session session) {
        return session.createQuery("from Poplavok p left join fetch p.marketTicker", Poplavok.class).list();
    }
    
    public static List<Poplavok> findByActive(Session session, boolean isActive) {
        return session.createQuery("from Poplavok p left join fetch p.marketTicker where p.isActive = :isActive", Poplavok.class)
                .setParameter("isActive", isActive)
                .list();
    }
}

