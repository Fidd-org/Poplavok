package com.poplavok.data.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import javax.annotation.Nullable;

@Entity
@Table(name = "level_trades")
public class LevelTrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "level_id", nullable = false)
    @Nullable
    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trade_id", nullable = false)
    @Nullable
    private Trade trade;

    public LevelTrade() {
    }

    public @Nullable Long getId() {
        return id;
    }

    public @Nullable Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public @Nullable Trade getTrade() {
        return trade;
    }

    public void setTrade(Trade trade) {
        this.trade = trade;
    }
}

