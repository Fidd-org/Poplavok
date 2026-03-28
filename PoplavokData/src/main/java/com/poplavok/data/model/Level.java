package com.poplavok.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;

@Entity
@Table(name = "levels")
public class Level {
    public static final String SEPARATOR = " / ";

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

    @Column(name = "notes")
    @Nullable
    private String notes;

    @Column(name = "projected_price", nullable = false, precision = 20, scale = 8)
    @Nullable
    private BigDecimal projectedPrice;

    @Column(name = "projected_amount_base", precision = 20, scale = 8)
    @Nullable
    private BigDecimal projectedAmountBase;

    @Column(name = "projected_amount_quote", precision = 20, scale = 8)
    @Nullable
    private BigDecimal projectedAmountQuote;

    @Column(name = "available_amount_base", precision = 20, scale = 8)
    @Nullable
    private BigDecimal availableAmountBase;

    @Column(name = "available_amount_quote", precision = 20, scale = 8)
    @Nullable
    private BigDecimal availableAmountQuote;

    @Column(name = "lent_amount_base", precision = 20, scale = 8)
    @Nullable
    private BigDecimal lentAmountBase;

    @Column(name = "lent_amount_quote", precision = 20, scale = 8)
    @Nullable
    private BigDecimal lentAmountQuote;

    @Column(name = "debt_base", precision = 20, scale = 8)
    @Nullable
    private BigDecimal debtBase;

    @Column(name = "debt_quote", precision = 20, scale = 8)
    @Nullable
    private BigDecimal debtQuote;

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

    public void setId(@Nullable Long id) { this.id = id; }

    public Poplavok getPoplavok() {
        return checkNotNull(poplavok);
    }

    public void setPoplavok(@Nullable Poplavok poplavok) {
        this.poplavok = poplavok;
    }

    @Nullable
    public BigDecimal getProjectedAmountBase() {
        return projectedAmountBase;
    }

    public void setProjectedAmountBase(@Nullable BigDecimal projectedAmountBase) {
        this.projectedAmountBase = projectedAmountBase;
    }

    @Nullable
    public BigDecimal getProjectedAmountQuote() {
        return projectedAmountQuote;
    }

    public void setProjectedAmountQuote(@Nullable BigDecimal projectedAmountQuote) {
        this.projectedAmountQuote = projectedAmountQuote;
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

    private String formatAmount(@Nullable BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        BigDecimal stripped = amount.stripTrailingZeros();
        if (stripped.scale() < 2) {
            // only pad with zeros to strictly ensure at least 2 decimal places, no actual rounding occurs
            stripped = stripped.setScale(2, java.math.RoundingMode.UNNECESSARY);
        }
        return stripped.toPlainString();
    }

    public String getProjectedAmount() {
        return formatAmount(projectedAmountBase) + SEPARATOR + formatAmount(projectedAmountQuote);
    }

    public String getAvailableAmount() {
        return formatAmount(availableAmountBase) + SEPARATOR + formatAmount(availableAmountQuote);
    }

    public String getDebt() {
        return formatAmount(debtBase) + SEPARATOR + formatAmount(debtQuote);
    }

    public String getLentAmount() {
        return formatAmount(lentAmountBase) + SEPARATOR + formatAmount(lentAmountQuote);
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

    public @Nullable Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public @Nullable String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
