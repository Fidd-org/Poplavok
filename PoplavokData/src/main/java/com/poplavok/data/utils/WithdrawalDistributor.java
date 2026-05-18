package com.poplavok.data.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

// TODO: dummy implementation, only stubs "100% withdrawal" use case, need to implement the proper distribution logic
public class WithdrawalDistributor {
    public static List<BigDecimal> distributeWithdrawal(List<BigDecimal> amounts, BigDecimal withdrawalAmount, int scale) {
        return distributeWithdrawal(amounts, withdrawalAmount, scale, false);
    }

    public static List<BigDecimal> distributeWithdrawal(List<BigDecimal> amounts, BigDecimal withdrawalAmount, int scale, boolean allowOverdraft) {
        List<BigDecimal> withdrawAmounts = new ArrayList<>();

        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal amount : amounts) { sum = sum.add(amount); }

        if (!allowOverdraft && withdrawalAmount.compareTo(sum) > 0) {
            throw new RuntimeException("Withdraw amount cannot be greater than the sum of all amounts.");
        }

        if (withdrawalAmount.compareTo(sum) == 0) {
            // 100% withdrawal, just return the original amounts
            withdrawAmounts.addAll(amounts);
        } else {
            BigDecimal ratio = withdrawalAmount.divide(sum, scale, RoundingMode.FLOOR);
            for (BigDecimal amount : amounts) {
                BigDecimal currentWithdrawAmount = amount.multiply(ratio).setScale(scale, RoundingMode.FLOOR);
                withdrawAmounts.add(currentWithdrawAmount);
            }
        }

        return withdrawAmounts;
    }
}
