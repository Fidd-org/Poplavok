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

@Entity
@Table(name = "currency_chains")
public class CurrencyChain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "currency", nullable = false)
    @Nullable
    private Currency currency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chain", nullable = false)
    @Nullable
    private Chain chain;

    @Column
    @Nullable
    private String withdrawalMinSize;

    @Column
    @Nullable
    private String withdrawalMinFee;

    @Column
    @Nullable
    private Boolean isWithdrawEnabled;

    @Column
    @Nullable
    private Boolean isDepositEnabled;

    @Column
    @Nullable
    private Integer confirms;

    @Column
    @Nullable
    private String contractAddress;

    public CurrencyChain() {
    }

    public CurrencyChain(String currency, @Nullable String chain) {
        this.currency = new Currency(currency);
        this.chain = Chain.ofNew(chain);
    }

    // Accessors matching the interface request

    @Nullable
    public Long currencyChainId() {
        return id;
    }

    @Nullable
    public Currency currency() {
        return currency;
    }

    @Nullable
    public Chain chain() {
        return chain;
    }

    @Nullable
    public String withdrawalMinSize() {
        return withdrawalMinSize;
    }

    @Nullable
    public String withdrawalMinFee() {
        return withdrawalMinFee;
    }

    @Nullable
    public Boolean isWithdrawEnabled() {
        return isWithdrawEnabled;
    }

    @Nullable
    public Boolean isDepositEnabled() {
        return isDepositEnabled;
    }

    @Nullable
    public Integer confirms() {
        return confirms;
    }

    @Nullable
    public String contractAddress() {
        return contractAddress;
    }

    // Getters and Setters

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    @Nullable
    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @Nullable
    public Chain getChain() {
        return chain;
    }

    public void setChain(@Nullable Chain chain) {
        this.chain = chain;
    }

    @Nullable
    public String getWithdrawalMinSize() {
        return withdrawalMinSize;
    }

    public void setWithdrawalMinSize(@Nullable String withdrawalMinSize) {
        this.withdrawalMinSize = withdrawalMinSize;
    }

    @Nullable
    public String getWithdrawalMinFee() {
        return withdrawalMinFee;
    }

    public void setWithdrawalMinFee(@Nullable String withdrawalMinFee) {
        this.withdrawalMinFee = withdrawalMinFee;
    }

    @Nullable
    public Boolean getIsWithdrawEnabled() {
        return isWithdrawEnabled;
    }

    public void setIsWithdrawEnabled(@Nullable Boolean isWithdrawEnabled) {
        this.isWithdrawEnabled = isWithdrawEnabled;
    }

    @Nullable
    public Boolean getIsDepositEnabled() {
        return isDepositEnabled;
    }

    public void setIsDepositEnabled(@Nullable Boolean isDepositEnabled) {
        this.isDepositEnabled = isDepositEnabled;
    }

    @Nullable
    public Integer getConfirms() {
        return confirms;
    }

    public void setConfirms(@Nullable Integer confirms) {
        this.confirms = confirms;
    }

    @Nullable
    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(@Nullable String contractAddress) {
        this.contractAddress = contractAddress;
    }
}
