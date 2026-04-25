package com.poplavok.forms;

import com.flower.fxutils.JavaFxUtils;
import com.poplavok.data.dao.AccountDAO;
import com.poplavok.data.dao.LoanDAO;
import com.poplavok.data.dao.LoanInfo;
import com.poplavok.data.model.Account;
import com.poplavok.data.model.Direction;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.model.Repayment;
import com.poplavok.data.utils.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.flower.fxutils.JavaFxUtils.autoResizeTableColumns;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;
import static com.poplavok.data.utils.BigDecimalUtil.fromString;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;

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

    // ---------- TAKE LOSS ----------

    @Nullable FilteredList<LoanInfo> takeLossLoans;
    @FXML @Nullable TableView<LoanInfo> takeLossLoansTableView;

    @FXML @Nullable TextField takeLossOwedTextField;
    @FXML @Nullable TextField takeLossTextField;

    @FXML @Nullable Button takeLossOwedCurrencyButton;
    @FXML @Nullable Label takeLossCurrencyLabel;

    // ---------- TAKE PROFIT ----------

    @Nullable FilteredList<Account> accounts;
    @FXML @Nullable TableView<Account> takeProfitAccountsTableView;

    @FXML @Nullable TextField takeProfitAvailableTextField;
    @FXML @Nullable TextField takeProfitTextField;

    @FXML @Nullable Button takeProfitAvailableCurrencyButton;
    @FXML @Nullable Label takeProfitCurrencyLabel;

    @FXML @Nullable RadioButton takeProfitQuoteRadioButton;
    @FXML @Nullable RadioButton takeProfitBaseRadioButton;

    // ---------------------------------

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
        this.takeLossLoans = new FilteredList<>(FXCollections.observableArrayList(loanInfoList));

        checkNotNull(loansTableView).setItems(this.loans);
        autoResizeTableColumns(loansTableView);
        checkNotNull(takeLossLoansTableView).setItems(this.takeLossLoans);
        autoResizeTableColumns(takeLossLoansTableView);

        checkNotNull(toRepayTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        checkNotNull(takeProfitTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        checkNotNull(takeLossTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());

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

        checkNotNull(loansTableView).getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, loan) -> {
            if (loan != null) {
                String loanCurrency = loan.getLoanCurrency();

                checkNotNull(repaymentDebtTextField).setText(loan.getRemainingOwed());

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

        // ------------------------------------------------------

        List<Account> accountList = DBUtil.connectGetResultAndClose(
                sess -> AccountDAO.findByCurrencies(sess, List.of(ticker.getQuote().getCurrency(), ticker.getBase().getCurrency())));
        this.accounts = new FilteredList<>(FXCollections.observableArrayList(accountList));

        checkNotNull(takeProfitQuoteRadioButton).selectedProperty().addListener((obs, oldVal, newVal) -> updateProfitAccountsFilter(ticker));
        checkNotNull(takeProfitBaseRadioButton).selectedProperty().addListener((obs, oldVal, newVal) -> updateProfitAccountsFilter(ticker));
        updateProfitAccountsFilter(ticker);

        updateLossLoansFilter(ticker);

        checkNotNull(takeProfitAccountsTableView).setItems(this.accounts);
        autoResizeTableColumns(takeProfitAccountsTableView);

        checkNotNull(takeLossLoansTableView).setItems(this.loans);
        autoResizeTableColumns(takeLossLoansTableView);
    }

    private void updateLossLoansFilter(MarketTicker ticker) {
        if (loans != null) {
            //
        }
    }

    private void updateProfitAccountsFilter(MarketTicker ticker) {
        if (accounts != null) {
            accounts.setPredicate(acc -> {
                if (checkNotNull(takeProfitQuoteRadioButton).isSelected()) {
                    BigDecimal amountQuote = nullToZero(level.getAvailableAmountQuote()).subtract(nullToZero(level.getDebtQuote()));

                    checkNotNull(takeProfitCurrencyLabel).textProperty().setValue(ticker.getQuote().getCurrency());
                    checkNotNull(takeProfitAvailableCurrencyButton).textProperty().setValue(ticker.getQuote().getCurrency());

                    checkNotNull(takeProfitAvailableTextField).setText(formatAmount(amountQuote));
                    checkNotNull(takeProfitTextField).setText(formatAmount(BigDecimal.ZERO));

                    return acc.getCurrency().getCurrency().equals(ticker.getQuote().getCurrency());
                } else if (checkNotNull(takeProfitBaseRadioButton).isSelected()) {
                    BigDecimal amountBase = nullToZero(level.getAvailableAmountBase()).subtract(nullToZero(level.getDebtBase()));

                    checkNotNull(takeProfitCurrencyLabel).textProperty().setValue(ticker.getBase().getCurrency());
                    checkNotNull(takeProfitAvailableCurrencyButton).textProperty().setValue(ticker.getBase().getCurrency());

                    checkNotNull(takeProfitAvailableTextField).setText(formatAmount(amountBase));
                    checkNotNull(takeProfitTextField).setText(formatAmount(BigDecimal.ZERO));

                    return acc.getCurrency().getCurrency().equals(ticker.getBase().getCurrency());
                } else {
                    throw new RuntimeException("No Take currency selected");
                }
            });
        }
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
        try {
            Loan selectedLoan = checkNotNull(loansTableView).getSelectionModel().getSelectedItem().loan;
            BigDecimal repayAmount = nullToZero(fromString(checkNotNull(toRepayTextField).textProperty().get()));
            Date date = new Date();

            BigDecimal debtAmount = nullToZero(fromString(checkNotNull(repaymentDebtTextField).textProperty().get()));
            BigDecimal availableAmount = nullToZero(fromString(checkNotNull(availableForRepaymentTextField).textProperty().get()));

            if (repayAmount.compareTo(debtAmount) > 0) {
                JavaFxUtils.showErrorMessage("Repay amount cannot be greater than debt amount");
                return;
            }
            if (repayAmount.compareTo(availableAmount) > 0) {
                JavaFxUtils.showErrorMessage("Repay amount cannot be greater than available amount");
                return;
            }
            if (repayAmount.compareTo(BigDecimal.ZERO) <= 0) {
                JavaFxUtils.showErrorMessage("Repay amount must be greater than zero");
                return;
            }

            returnRepayment = new Repayment(selectedLoan, repayAmount, date);
            returnRepayment.setSourceLevel(level);

            if (selectedLoan.getSourceAccount() != null) {
                returnRepayment.setDestinationAccount(selectedLoan.getSourceAccount());
            } else if (selectedLoan.getSourceLevel() != null) {
                returnRepayment.setDestinationLevel(selectedLoan.getSourceLevel());
            }

            checkNotNull(stage).close();
        } catch (Exception e) {
            JavaFxUtils.showErrorMessage("RepayDialog close Error: " + e);
            LOGGER.error("RepayDialog close Error:", e);
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Nullable
    public Repayment getReturnRepayment() {
        return returnRepayment;
    }

    // --------------------------------------------

    public void moveAvailableAmountToTakeProfit() {
        BigDecimal amount = nullToZero(fromString(checkNotNull(takeProfitAvailableTextField).getText()));
        checkNotNull(takeProfitTextField).setText(formatAmount(amount));
    }

    public void takeProfit() {
        // TODO: implement
    }

    // --------------------------------------------

    public void moveOwedAmountToTakeLoss() {
        BigDecimal amount = nullToZero(fromString(checkNotNull(takeLossOwedTextField).getText()));
        checkNotNull(takeLossTextField).setText(formatAmount(amount));
    }

    public void takeLoss() {
        // TODO: implement
    }
}
