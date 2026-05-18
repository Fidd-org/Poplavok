package com.poplavok.data.utils.distributors;

import java.math.BigDecimal;
import java.util.List;

public class PreciseDistributor implements Distributor {
    @Override
    public List<BigDecimal> distribute(List<BigDecimal> amounts, BigDecimal distributeAmount, int scale, boolean allowOverdraft) {
        throw new UnsupportedOperationException();
    }
}
