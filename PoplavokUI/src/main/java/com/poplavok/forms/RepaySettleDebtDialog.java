package com.poplavok.forms;

import com.flower.fxutils.JavaFxUtils;
import com.poplavok.data.dao.LoanDAO;
import com.poplavok.data.dao.LoanInfo;
import com.poplavok.data.model.Direction;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.model.Repayment;
import com.poplavok.data.utils.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.flower.fxutils.JavaFxUtils.autoResizeTableColumns;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;

public class RepaySettleDebtDialog extends TabPane {
    final static Logger LOGGER = LoggerFactory.getLogger(RepaySettleDebtDialog.class);

    @FXML @Nullable TextField repaymentDebtTextField;
    @FXML @Nullable TextField availableForRepaymentTextField;
    @FXML @Nullable TextField toRepayTextField;

    @FXML @Nullable Button availableForRepaymentButton;

    @FXML @Nullable Label debtCurrencyLabel;
    @FXML @Nullable Label toRepayCurrencyLabel;

    @Nullable FilteredList<LoanInfo> loans;
    @FXML @Nullable TableView<LoanInfo> loansTableView;

    @Nullable Stage stage;

    @Nullable Long levelId = null;
    Level level;
    final Direction tradeDirection;
    @Nullable volatile Repayment returnRepayment = null;

    public RepaySettleDebtDialog(Level level, MarketTicker ticker, @Nullable BigDecimal price, @Nullable BigDecimal fee, Direction tradeDirection) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RepaySettleDebtDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.tradeDirection = tradeDirection;

        this.level = level;
        //checkNotNull(addButton).textProperty().set("Update Level");
        levelId = level.getId();

        List<LoanInfo> loanInfoList = DBUtil.connectGetResultAndClose(sess -> LoanDAO.getLoanInfosByDestinationLevel(sess, level));
        this.loans = new FilteredList<>(FXCollections.observableArrayList(loanInfoList));

        checkNotNull(loansTableView).setItems(this.loans);
        autoResizeTableColumns(loansTableView);

        checkNotNull(toRepayTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());

        String currency;
        if (tradeDirection == Direction.LONG) {
            currency = ticker.getQuote().getCurrency();
        } else if (tradeDirection == Direction.SHORT) {
            currency = ticker.getBase().getCurrency();
        } else {
            throw new RuntimeException("Unknown direction: " + tradeDirection);
        }

        checkNotNull(debtCurrencyLabel).textProperty().setValue(currency);
        checkNotNull(toRepayCurrencyLabel).textProperty().setValue(currency);
        checkNotNull(availableForRepaymentButton).textProperty().setValue(currency);

        loansTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, loan) -> {
            if (loan != null) {
                String loanCurrency = loan.getLoanCurrency();

                checkNotNull(repaymentDebtTextField).setText(formatAmount(loan.getRemainingOwed()));

                if (loanCurrency.equals(ticker.getBase().getCurrency())) {
                    checkNotNull(availableForRepaymentTextField).setText(formatAmount(level.getAvailableAmountBase()));
                } else if (loanCurrency.equals(ticker.getQuote().getCurrency())) {
                    checkNotNull(availableForRepaymentTextField).setText(formatAmount(level.getAvailableAmountQuote()));
                } else {
                    throw new RuntimeException("Loan currency doesn't exist on level: " + loanCurrency + " ticker: " + ticker);
                }

                checkNotNull(toRepayTextField).setText(formatAmount(BigDecimal.ZERO));

                checkNotNull(debtCurrencyLabel).textProperty().setValue(loanCurrency);
                checkNotNull(toRepayCurrencyLabel).textProperty().setValue(loanCurrency);
                checkNotNull(availableForRepaymentButton).textProperty().setValue(loanCurrency);
            } else {
                checkNotNull(repaymentDebtTextField).setText("");
                checkNotNull(availableForRepaymentTextField).setText("");
                checkNotNull(toRepayTextField).setText(formatAmount(BigDecimal.ZERO));

                checkNotNull(debtCurrencyLabel).textProperty().setValue("");
                checkNotNull(toRepayCurrencyLabel).textProperty().setValue("");
                checkNotNull(availableForRepaymentButton).textProperty().setValue("");
            }
        });

        if (this.loans != null && !this.loans.isEmpty()) {
            // Select the first loan by default
            checkNotNull(loansTableView).getSelectionModel().selectFirst();
        }
    }

    public void repay() {
        //
    }

    public void moveAllAvailableToRepay() {
        String debtStr = checkNotNull(repaymentDebtTextField).getText();
        String availableStr = checkNotNull(availableForRepaymentTextField).getText();

        BigDecimal debt = BigDecimal.ZERO;
        BigDecimal available = BigDecimal.ZERO;
        if (debtStr != null && !debtStr.isEmpty()) {
            debt = new BigDecimal(debtStr.replace(",", ""));
        }
        if (availableStr != null && !availableStr.isEmpty()) {
            available = new BigDecimal(availableStr.replace(",", ""));
        }
        try {
            checkNotNull(toRepayTextField).setText(formatAmount(debt.min(available)));
        } catch (NumberFormatException e) {
            LOGGER.error("Error parsing amount", e);
        }
    }

    public void okClose() {
        // TODO: remove this from FXML
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Nullable
    public Repayment getReturnRepayment() {
        return returnRepayment;
    }
}
