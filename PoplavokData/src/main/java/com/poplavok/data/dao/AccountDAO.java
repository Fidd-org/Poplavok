package com.poplavok.data.dao;

import com.poplavok.data.model.Account;
import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public class AccountDAO {

    public static void save(Session session, Account account) {
        session.persist(account);
    }

    public static Account update(Session session, Account account) {
        return session.merge(account);
    }

    public static void delete(Session session, Account account) {
        session.remove(account);
    }

    public static Optional<Account> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(Account.class, id));
    }

    public static List<Account> findAll(Session session) {
        return session.createQuery("from Account a left join fetch a.currency", Account.class).list();
    }

    public static List<Account> findAllLazy(Session session) {
        return session.createQuery("from Account", Account.class).list();
    }

    public static List<Account> findByCurrency(Session session, String currency) {
        return session.createQuery("from Account a where a.currency.currency = :currency", Account.class)
                .setParameter("currency", currency)
                .list();
    }

    public static List<Account> findAvailableByCurrency(Session session, String currency) {
        return session.createQuery("from Account a left join fetch a.currency where a.currency.currency = :currency and a.availableAmount > 0", Account.class)
                .setParameter("currency", currency)
                .list();
    }

    public static void setArchived(Session session, Long accountId, boolean archived) {
        Account account = session.find(Account.class, accountId);
        if (account != null) {
            account.setArchived(archived);
            update(session, account);
        }
    }
}
