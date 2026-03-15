package com.poplavok.data.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import javax.annotation.Nullable;

@Entity
@Table(name = "chains")
public class Chain {

    @Id
    @Nullable
    private String chain;

    @Column
    @Nullable
    private String chainName;

    public Chain() {
    }

    public static Chain ofNew(@Nullable String chainName, @Nullable String chain) {
        Chain c = new Chain();
        c.setChainName(chainName);
        c.setChain(chain);
        return c;
    }

    public static Chain ofNew(@Nullable String chain) {
        Chain c = new Chain();
        c.setChain(chain);
        return c;
    }

    @Nullable
    public String chainName() {
        return chainName;
    }

    @Nullable
    public String chain() {
        return chain;
    }

    // Getters and Setters

    @Nullable
    public String getChainName() {
        return chainName;
    }

    public void setChainName(@Nullable String chainName) {
        this.chainName = chainName;
    }

    @Nullable
    public String getChain() {
        return chain;
    }

    public void setChain(@Nullable String chain) {
        this.chain = chain;
    }
}

