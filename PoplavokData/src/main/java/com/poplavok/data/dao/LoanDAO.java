package com.poplavok.data.dao;

import com.poplavok.data.model.Level;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.Repayment;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LoanDAO {
    public static void save(Session session, Loan loan) {
        session.persist(loan);
    }

    public static Loan update(Session session, Loan loan) {
        return session.merge(loan);
    }

    public static void delete(Session session, Loan loan) {
        session.remove(loan);
    }

    public static Optional<Loan> findById(Session session, Long id) {
        return Optional.ofNullable(session.find(Loan.class, id));
    }

    public static List<Loan> findAll(Session session) {
        return session.createQuery("from Loan", Loan.class).list();
    }

    public static List<Loan> findByDestinationLevel(Session session, Level level) {
        return session.createQuery("from Loan l " +
                        "left join fetch l.sourceAccount " +
                        "left join fetch l.sourceLevel sl " +
                        "left join fetch sl.poplavok p " +
                        "left join fetch p.marketTicker " +
                        "left join fetch l.destinationAccount " +
                        "left join fetch l.destinationLevel " +
                        "where l.destinationLevel = :level", Loan.class)
                .setParameter("level", level)
                .list();
    }

    protected static List<Object[]> findLoansAndRepaymentsByDestinationLevel(Session session, Level level) {
        return session.createQuery("select l, r from Loan l " +
                        "left join Repayment r on r.loan = l " +
                        "left join fetch l.sourceAccount " +
                        "left join fetch l.sourceLevel sl " +
                        "left join fetch sl.poplavok p " +
                        "left join fetch p.marketTicker " +
                        "left join fetch l.destinationAccount " +
                        "left join fetch l.destinationLevel " +
                        "where l.destinationLevel = :level", Object[].class)
                .setParameter("level", level)
                .list();
    }

    public static List<LoanInfo> getLoanInfosByDestinationLevel(Session session, Level level) {
        List<Object[]> results = findLoansAndRepaymentsByDestinationLevel(session, level);

        Map<Loan, List<Repayment>> map = new LinkedHashMap<>();
        for (Object[] row : results) {
            Loan loan = (Loan) row[0];
            Repayment repayment = (Repayment) row[1];

            map.computeIfAbsent(loan, k -> new ArrayList<>());
            if (repayment != null) {
                map.get(loan).add(repayment);
            }
        }

        List<LoanInfo> infos = new ArrayList<>();
        for (Map.Entry<Loan, List<Repayment>> entry : map.entrySet()) {
            infos.add(new LoanInfo(entry.getKey(), entry.getValue()));
        }

        return infos;
    }
}
