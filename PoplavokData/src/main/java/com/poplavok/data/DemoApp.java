package com.poplavok.data;

import com.poplavok.data.utils.HibernateUtil;
import com.poplavok.data.model.Account;
import com.poplavok.data.model.Currency;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.LevelStrategy;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.LoanType;
import com.poplavok.data.model.Poplavok;
import com.poplavok.data.model.MarketTicker;
import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class DemoApp {
    public static void main(String[] args) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();

        // Create currencies
        Currency btc = new Currency("BTC");
        Currency usdt = new Currency("USDT");
        session.persist(btc);
        session.persist(usdt);

        // Create ticker BTC/USDT
        MarketTicker btcUsdt = new MarketTicker(btc, usdt, "BTCUSDT");
        btcUsdt.setTakerFeeRate("0.001");
        btcUsdt.setMakerFeeRate("0.001");
        session.persist(btcUsdt);

        // Create accounts
        Account btcAccount = new Account(btc, new BigDecimal("1.5"), BigDecimal.ZERO);
        Account usdtAccount = new Account(usdt, new BigDecimal("10000"), BigDecimal.ZERO);
        session.persist(btcAccount);
        session.persist(usdtAccount);

        // Create a Poplavok
        Poplavok poplavok = new Poplavok(
                btcUsdt,
                LevelStrategy.LINEAR,
                "{\"step\": 0.05, \"multiplier\": 1.5}",
                new Date()
        );
        session.persist(poplavok);

        // Create levels
        Level level1 = new Level(poplavok, new BigDecimal("0.1"), new BigDecimal("4500"),
                new BigDecimal("45000"), new BigDecimal("45000"), new BigDecimal("45000"), new BigDecimal("45000"), new BigDecimal("45000"), new Date());
        Level level2 = new Level(poplavok, new BigDecimal("0.15"), new BigDecimal("6412.5"),
                new BigDecimal("42750"), new BigDecimal("42750"), new BigDecimal("42750"), new BigDecimal("42750"), new BigDecimal("42750"), new Date());
        poplavok.addLevel(level1);
        poplavok.addLevel(level2);

        tx.commit();
        session.close();

        System.out.println("Demo data created successfully!");
        System.out.println("Created currencies: BTC, USDT");
        System.out.println("Created ticker: BTC/USDT");
        System.out.println("Created Poplavok with 2 levels");

        HibernateUtil.shutdown();
    }
}

