package com.poplavok.data.utils.distributors;

import java.math.BigDecimal;
import java.util.List;

public interface Distributor {
    default List<BigDecimal> distribute(List<BigDecimal> amounts, BigDecimal distributeAmount, int scale) {
        return distribute(amounts, distributeAmount, scale, false);
    }

    List<BigDecimal> distribute(List<BigDecimal> amounts, BigDecimal distributeAmount, int scale, boolean allowOverdraft);
}
