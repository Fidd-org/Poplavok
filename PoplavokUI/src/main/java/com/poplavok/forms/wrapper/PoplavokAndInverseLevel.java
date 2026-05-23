package com.poplavok.forms.wrapper;

import com.poplavok.data.model.Level;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.Poplavok;

import java.util.List;

public record PoplavokAndInverseLevel(boolean isNewPoplavok, Poplavok poplavok, Level level, List<Loan> initialLoans, List<Level> initialLoanSourceLevels) {
}
