package com.poplavok.data.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
@Table(name = "levels")
public class Level {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poplavok_id", nullable = false)
    @Nullable
    private Poplavok poplavok;

    @Column(name = "is_active", nullable = false)
    @Nullable
    private boolean isActive;

    @Column(name = "creation_date", nullable = false)
    @Nullable
    private Date creationDate;

    @Column(name = "close_date")
    @Nullable
    private Date closeDate;

    @Column(name = "notes", nullable = false)
    @Nullable
    private String notes;

    @Column(name = "available_amount_base", nullable = false, precision = 20, scale = 8)
    @Nullable
    private BigDecimal availableAmountBase;

    @Column(name = "available_amount_quote", nullable = false, precision = 20, scale = 8)
    @Nullable
    private BigDecimal availableAmountQuote;

    @Column(name = "lent_amount_base", nullable = false, precision = 20, scale = 8)
    @Nullable
    private BigDecimal lentAmountBase;

    @Column(name = "lent_amount_quote", nullable = false, precision = 20, scale = 8)
    @Nullable
    private BigDecimal lentAmountQuote;

    @Column(name = "debt_base", nullable = false, precision = 20, scale = 8)
    @Nullable
    private BigDecimal debtBase;

    @Column(name = "debt_quote", nullable = false, precision = 20, scale = 8)
    @Nullable
    private BigDecimal debtQuote;

    @Column(name = "projected_price", nullable = false, precision = 20, scale = 8)
    @Nullable
    private BigDecimal projectedPrice;

    @OneToMany(mappedBy = "destinationLevel", cascade = CascadeType.ALL)
    private List<Loan> loans = new ArrayList<>();

    @OneToMany(mappedBy = "level", cascade = CascadeType.ALL)
    private List<LevelTrade> levelTrades = new ArrayList<>();

    public Level() {
    }

    public Level(Poplavok poplavok,
                 BigDecimal availableAmountBase, BigDecimal availableAmountQuote,
                 BigDecimal lentAmountBase, BigDecimal lentAmountQuote,
                 BigDecimal debtBase, BigDecimal debtQuote,
                 BigDecimal projectedPrice, Date creationDate) {
        this.poplavok = poplavok;

        this.availableAmountBase = availableAmountBase;
        this.availableAmountQuote = availableAmountQuote;
        this.lentAmountBase = lentAmountBase;
        this.lentAmountQuote = lentAmountQuote;
        this.debtBase = debtBase;
        this.debtQuote = debtQuote;

        this.projectedPrice = projectedPrice;
        this.creationDate = creationDate;
        this.isActive = true;
    }

    public Long getId() {
        return checkNotNull(id);
    }

    public Poplavok getPoplavok() {
        return checkNotNull(poplavok);
    }

    void setPoplavok(@Nullable Poplavok poplavok) {
        this.poplavok = poplavok;
    }

    @Nullable
    public BigDecimal getAvailableAmountBase() {
        return availableAmountBase;
    }

    public void setAvailableAmountBase(@Nullable BigDecimal availableAmountBase) {
        this.availableAmountBase = availableAmountBase;
    }

    @Nullable
    public BigDecimal getAvailableAmountQuote() {
        return availableAmountQuote;
    }

    public void setAvailableAmountQuote(@Nullable BigDecimal availableAmountQuote) {
        this.availableAmountQuote = availableAmountQuote;
    }

    @Nullable
    public BigDecimal getLentAmountBase() {
        return lentAmountBase;
    }

    public void setLentAmountBase(@Nullable BigDecimal lentAmountBase) {
        this.lentAmountBase = lentAmountBase;
    }

    @Nullable
    public BigDecimal getLentAmountQuote() {
        return lentAmountQuote;
    }

    public void setLentAmountQuote(@Nullable BigDecimal lentAmountQuote) {
        this.lentAmountQuote = lentAmountQuote;
    }

    @Nullable
    public BigDecimal getDebtBase() {
        return debtBase;
    }

    public void setDebtBase(@Nullable BigDecimal debtBase) {
        this.debtBase = debtBase;
    }

    @Nullable
    public BigDecimal getDebtQuote() {
        return debtQuote;
    }

    public void setDebtQuote(@Nullable BigDecimal debtQuote) {
        this.debtQuote = debtQuote;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public BigDecimal getProjectedPrice() {
        return checkNotNull(projectedPrice);
    }

    public void setProjectedPrice(BigDecimal projectedPrice) {
        this.projectedPrice = projectedPrice;
    }

    public Date getCreationDate() {
        return checkNotNull(creationDate);
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getCloseDate() {
        return checkNotNull(closeDate);
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public String getNotes() {
        return checkNotNull(notes);
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<Loan> getLoans() {
        return loans;
    }

    public List<LevelTrade> getLevelTrades() {
        return levelTrades;
    }

    public void addLevelTrade(LevelTrade levelTrade) {
        levelTrades.add(levelTrade);
        levelTrade.setLevel(this);
    }

    public void addLoan(Loan loan) {
        loans.add(loan);
        loan.setDestinationLevel(this);
    }
}
