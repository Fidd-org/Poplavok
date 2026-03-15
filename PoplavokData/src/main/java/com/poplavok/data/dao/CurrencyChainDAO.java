package com.poplavok.data.dao;

import com.poplavok.data.model.CurrencyChain;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class CurrencyChainDAO {

    public static void save(Session session, CurrencyChain chain) {
        session.persist(chain);
    }

    public static void update(Session session, CurrencyChain chain) {
        session.merge(chain);
    }

    public static void delete(Session session, CurrencyChain chain) {
        session.remove(chain);
    }

    public static Optional<CurrencyChain> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(CurrencyChain.class, id));
    }

    public static List<CurrencyChain> getForCurrency(Session session, String currency) {
        return session.createQuery("from CurrencyChain cc left join fetch cc.chain left join fetch cc.currency where cc.currency.currency = :currency", CurrencyChain.class)
                .setParameter("currency", currency)
                .list();
    }

    public static List<CurrencyChain> getForCurrencyLazy(Session session, String currency) {
        return session.createQuery("from CurrencyChain where currency = :currency", CurrencyChain.class)
                .setParameter("currency", currency)
                .list();
    }
}
