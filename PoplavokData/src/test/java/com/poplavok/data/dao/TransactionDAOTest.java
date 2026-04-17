package com.poplavok.data.dao;

import com.poplavok.data.model.Currency;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.LoanType;
import com.poplavok.data.model.Repayment;
import com.poplavok.data.model.Transaction;
import com.poplavok.data.utils.HibernateUtil; // Assuming this exists from the context
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;

import static com.poplavok.data.utils.BigDecimalUtil.fromString;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TransactionDAOTest {

    private SessionFactory sessionFactory;
    private Session session;
    private org.hibernate.Transaction transaction;

    @BeforeEach
    public void setUp() {
        // Assuming HibernateUtil is available and configured for tests
        // Based on CurrencyDAOTest.java usage
        sessionFactory = HibernateUtil.getSessionFactory();
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();
        
        // Clean up or ensure clean state if possible, but transactions roll back usually
    }

    @AfterEach
    public void tearDown() {
        if (transaction != null && transaction.isActive()) {
            transaction.rollback();
        }
        if (session != null) {
            session.close();
        }
    }

    @Test
    public void testFindAllReturnsConcreteInstances() {
        // Create Currency
        if (session.find(Currency.class, "USD") == null) {
            Currency usd = new Currency("USD");
            usd.setName("US Dollar");
            session.persist(usd);
        }
        Currency usd = session.find(Currency.class, "USD");

        // Create Loan
        Loan loan = new Loan(usd, nullToZero(fromString("1000.00")), null,  DateUtils.addDays(new Date(), -10), LoanType.POPLAVOK_FUNDED);
        loan.setInterestRate(nullToZero(fromString("0.05")));
        TransactionDAO.save(session, loan);

        // Create Repayment
        Repayment repayment = new Repayment(loan, nullToZero(fromString("500.00")), new Date());
        repayment.setNotes("Partial repayment");
        TransactionDAO.save(session, repayment);

        session.flush();
        session.clear(); // Clear session to force fetch from DB

        // Find All Transactions
        List<Transaction> allTransactions = TransactionDAO.findAll(session);

        assertEquals(2, allTransactions.size());

        // Verify Loan
        Transaction t1 = allTransactions.stream()
                .filter(t -> t instanceof Loan)
                .findFirst()
                .orElse(null);
        assertNotNull(t1);
        assertInstanceOf(Loan.class, t1);
        Loan fetchedLoan = (Loan) t1;
        // BigDecimal equality can be tricky with scale, use compareTo
        assertEquals(0, nullToZero(fromString("0.05")).compareTo(fetchedLoan.getInterestRate()));

        // Verify Repayment
        Transaction t2 = allTransactions.stream()
                .filter(t -> t instanceof Repayment)
                .findFirst()
                .orElse(null);
        assertNotNull(t2);
        assertInstanceOf(Repayment.class, t2);
        Repayment fetchedRepayment = (Repayment) t2;
        assertEquals("Partial repayment", fetchedRepayment.getNotes());
        
        // Check Association
        // Note: Repayment.loan is lazy, but since session is open, we can access it
        assertNotNull(fetchedRepayment.getLoan());
        assertEquals(fetchedLoan.getId(), fetchedRepayment.getLoan().getId());
    }
}
