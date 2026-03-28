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
