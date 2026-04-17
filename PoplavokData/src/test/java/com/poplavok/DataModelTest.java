package com.poplavok;

import com.poplavok.data.model.LevelState;
import com.poplavok.data.utils.HibernateUtil;
import com.poplavok.data.model.Account;
import com.poplavok.data.model.Currency;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.LevelStrategy;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.LoanType;
import com.poplavok.data.model.Poplavok;
import com.poplavok.data.model.Repayment;
import com.poplavok.data.model.MarketTicker;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.poplavok.data.utils.BigDecimalUtil.fromString;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DataModelTest {

    private Session session;

    @BeforeEach
    void setUp() {
        session = HibernateUtil.getSessionFactory().openSession();
    }

    @AfterEach
    void tearDown() {
        if (session != null && session.isOpen()) {
            session.close();
        }
        HibernateUtil.closeSessionFactory();
    }

    @Test
    public void testCurrencyAndTicker() {
        Transaction tx = session.beginTransaction();

        Currency btc = new Currency("BTC");
        Currency usdt = new Currency("USDT");
        session.persist(btc);
        session.persist(usdt);

        MarketTicker marketTicker = new MarketTicker(btc, usdt, "BTCUSDT");
        session.persist(marketTicker);

        tx.commit();
        session.clear();

        List<MarketTicker> marketTickers = session
                .createQuery("from MarketTicker", MarketTicker.class)
                .list();

        assertEquals(1, marketTickers.size());
        assertEquals("BTC", marketTickers.get(0).getBase().getCurrency());
        assertEquals("USDT", marketTickers.get(0).getQuote().getCurrency());
    }

    @Test
    public void testPoplavokWithLevels() {
        Transaction tx = session.beginTransaction();

        Currency btc = new Currency("BTC");
        Currency usdt = new Currency("USDT");
        session.persist(btc);
        session.persist(usdt);

        MarketTicker marketTicker = new MarketTicker(btc, usdt, "BTCUSDT");
        session.persist(marketTicker);

        Poplavok poplavok = new Poplavok(
                marketTicker,
                LevelStrategy.LINEAR,
                "{}",
                new Date()
        );
        session.persist(poplavok);

        tx.commit();
        session.clear();

        List<Poplavok> poplavoks = session
                .createQuery("from Poplavok", Poplavok.class)
                .list();

        assertEquals(1, poplavoks.size());
        assertTrue(poplavoks.get(0).isActive());
    }

    @Test
    public void testLoanAndRepayment() {
        Transaction tx = session.beginTransaction();

        Currency usdt = new Currency("USDT");
        session.persist(usdt);

        Loan loan = new Loan(usdt, nullToZero(fromString("1000")), null,
                new Date(), LoanType.EXTERNAL_CROSS_MARGIN);
        session.persist(loan);

        tx.commit();
        session.clear();

        List<Loan> loans = session
                .createQuery("from Loan", Loan.class)
                .list();

        assertEquals(1, loans.size());
    }

    @Test
    public void testAccountAndHistory() {
        Transaction tx = session.beginTransaction();

        Currency btc = new Currency("BTC");
        session.persist(btc);

        Account account = new Account(btc, nullToZero(fromString("10")), BigDecimal.ZERO);
        session.persist(account);

        tx.commit();
        session.clear();

        List<Account> accounts = session
                .createQuery("from Account", Account.class)
                .list();

        assertEquals(1, accounts.size());
        assertEquals("BTC", accounts.get(0).getCurrency().getCurrency());
    }
}
