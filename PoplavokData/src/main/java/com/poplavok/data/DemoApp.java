package com.poplavok.data;

import com.poplavok.data.model.LevelState;
import com.poplavok.data.utils.HibernateUtil;
import com.poplavok.data.model.Account;
import com.poplavok.data.model.Currency;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.LevelStrategy;
import com.poplavok.data.model.Poplavok;
import com.poplavok.data.model.MarketTicker;
import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.Session;
import org.hibernate.Transaction;

import static com.poplavok.data.utils.BigDecimalUtil.fromString;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;

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
        Account btcAccount = new Account(btc, nullToZero(fromString("1.5")), BigDecimal.ZERO);
        Account usdtAccount = new Account(usdt, nullToZero(fromString("10000")), BigDecimal.ZERO);
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

        tx.commit();
        session.close();

        System.out.println("Demo data created successfully!");
        System.out.println("Created currencies: BTC, USDT");
        System.out.println("Created ticker: BTC/USDT");
        System.out.println("Created Poplavok with 2 levels");

        HibernateUtil.shutdown();
    }
}

