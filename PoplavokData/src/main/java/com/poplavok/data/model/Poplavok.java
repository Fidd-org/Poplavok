package com.poplavok.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import javax.annotation.Nullable;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
@Table(name = "poplavoks")
public class Poplavok {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticker_id", nullable = false)
    @Nullable
    private MarketTicker marketTicker;

    @Enumerated(EnumType.STRING)
    @Column(name = "level_strategy", nullable = true, length = 30)
    @Nullable
    private LevelStrategy levelStrategy;

    @Column(name = "strategy_parameters", length = 5000)
    @Nullable
    private String strategyParameters;

    @Column
    @Nullable
    private String name;

    @Column(length = 2000)
    @Nullable
    private String notes;

    @Column(name = "is_active", nullable = false)
    @Nullable
    private boolean isActive;

    @Column(name = "creation_date", nullable = false)
    @Nullable
    private Date creationDate;

    @Column(name = "close_date")
    @Nullable
    private Date closeDate;

    public Poplavok() {
    }

    public Poplavok(MarketTicker marketTicker, LevelStrategy levelStrategy, String strategyParameters, Date creationDate) {
        this.marketTicker = marketTicker;
        this.levelStrategy = levelStrategy;
        this.strategyParameters = strategyParameters;
        this.creationDate = creationDate;
        this.isActive = true;
    }

    public Long getId() {
        return checkNotNull(id);
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    public MarketTicker getTicker() {
        return checkNotNull(marketTicker);
    }

    public void setTicker(MarketTicker marketTicker) {
        this.marketTicker = marketTicker;
    }

    public LevelStrategy getLevelStrategy() {
        return checkNotNull(levelStrategy);
    }

    public void setLevelStrategy(LevelStrategy levelStrategy) {
        this.levelStrategy = levelStrategy;
    }

    public String getStrategyParameters() {
        return checkNotNull(strategyParameters);
    }

    public void setStrategyParameters(String strategyParameters) {
        this.strategyParameters = strategyParameters;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(length = 10)
    @Nullable
    private Direction direction;

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Date getCreationDate() {
        return checkNotNull(creationDate);
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public @Nullable Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getTickerSymbol() {
        return marketTicker != null ? marketTicker.getSymbol() : "";
    }

    public String getStrategyStr() {
        return levelStrategy != null ? levelStrategy.toString() : "";
    }
}
