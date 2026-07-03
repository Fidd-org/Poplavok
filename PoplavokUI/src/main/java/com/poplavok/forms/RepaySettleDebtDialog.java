package com.poplavok.forms;

import com.flower.fxutils.JavaFxUtils;
import com.poplavok.data.dao.AccountDAO;
import com.poplavok.data.dao.LevelDAO;
import com.poplavok.data.dao.LoanDAO;
import com.poplavok.data.dao.LoanInfo;
import com.poplavok.data.model.Account;
import com.poplavok.data.model.Direction;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.utils.DBUtil;
import com.poplavok.forms.wrapper.repayment.LossRepaymentInfo;
import com.poplavok.forms.wrapper.repayment.ProfitRepaymentInfo;
import com.poplavok.forms.wrapper.repayment.RepayRepaymentInfo;
import com.poplavok.forms.wrapper.repayment.RepaymentInfo;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
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

    // ---------- TAKE PROFIT LEVEL ----------

    @Nullable FilteredList<Level> levels;
    @FXML @Nullable TableView<Level> takeProfitLevelsTableView;

    @FXML @Nullable RadioButton takeProfitLevelQuoteRadioButton;
    @FXML @Nullable RadioButton takeProfitLevelBaseRadioButton;

    @FXML @Nullable Button takeProfitLevelAvailableCurrencyButton;
    @FXML @Nullable TextField takeProfitLevelAvailableTextField;

    @FXML @Nullable TextField takeProfitLevelTextField;
    @FXML @Nullable Label takeProfitLevelCurrencyLabel;

    // ---------------------------------

    @FXML @Nullable Tab takeProfitToAccTab;
    @FXML @Nullable Tab takeProfitToLvlTab;
    @FXML @Nullable Tab takeLossTab;

    @Nullable Stage stage;

    @Nullable Long levelId = null;
    final Level level;
    final MarketTicker ticker;
    final Direction tradeDirection;
    @Nullable volatile RepaymentInfo returnRepayment = null;

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

        this.ticker = ticker;
        this.level = level;
        //checkNotNull(addButton).textProperty().set("Update Level");
        levelId = level.getId();
        boolean hasDebt = nullToZero(level.getDebtBase()).compareTo(BigDecimal.ZERO) > 0 ||
                nullToZero(level.getDebtQuote()).compareTo(BigDecimal.ZERO) > 0;

        boolean disableTakeProfit = hasDebt;

        if (hasDebt) {
            // We allow taking profit in non-Loan currency under certain conditions when we consider the existing position profitable.
            BigDecimal debtBase = nullToZero(level.getDebtBase());
            BigDecimal debtQuote = nullToZero(level.getDebtQuote());
            BigDecimal projBase = nullToZero(level.getProjectedAmountBase());
            BigDecimal projQuote = nullToZero(level.getProjectedAmountQuote());
            BigDecimal availBase = nullToZero(level.getAvailableAmountBase());
            BigDecimal availQuote = nullToZero(level.getAvailableAmountQuote());

            if (tradeDirection == Direction.LONG) {
                if (debtQuote.compareTo(projQuote) <= 0 && projBase.compareTo(availBase) < 0) {
                    disableTakeProfit = false;
                    checkNotNull(takeProfitQuoteRadioButton).setDisable(true);
                    checkNotNull(takeProfitLevelQuoteRadioButton).setDisable(true);

                    checkNotNull(takeProfitBaseRadioButton).setSelected(true);
                    checkNotNull(takeProfitLevelBaseRadioButton).setSelected(true);
                }
            } else if (tradeDirection == Direction.SHORT) {
                if (debtBase.compareTo(projBase) <= 0 && projQuote.compareTo(availQuote) < 0) {
                    disableTakeProfit = false;
                    checkNotNull(takeProfitBaseRadioButton).setDisable(true);
                    checkNotNull(takeProfitLevelBaseRadioButton).setDisable(true);

                    checkNotNull(takeProfitQuoteRadioButton).setSelected(true);
                    checkNotNull(takeProfitLevelQuoteRadioButton).setSelected(true);
                }
            }
        }

        if (disableTakeProfit) {
            checkNotNull(takeProfitToAccTab).disableProperty().setValue(true);
            checkNotNull(takeProfitToLvlTab).disableProperty().setValue(true);
        }
        
        if (!hasDebt) {
            checkNotNull(takeLossTab).disableProperty().setValue(true);
        }

        List<LoanInfo> loanInfoList = DBUtil.connectGetResultAndClose(sess -> LoanDAO.getLoanInfosByDestinationLevel(sess, level));
        this.loans = new FilteredList<>(FXCollections.observableArrayList(loanInfoList));
        this.takeLossLoans = new FilteredList<>(FXCollections.observableArrayList(loanInfoList));

        checkNotNull(loansTableView).setItems(this.loans);
        autoResizeTableColumns(loansTableView);
        checkNotNull(takeLossLoansTableView).setItems(this.takeLossLoans);
        autoResizeTableColumns(takeLossLoansTableView);

        checkNotNull(toRepayTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        checkNotNull(takeProfitTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        checkNotNull(takeProfitLevelTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
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

        checkNotNull(takeLossLoansTableView).getSelectionModel().selectedItemProperty()
                .addListener((obs, oldSelection, loan) -> {
            if (loan != null) {
                String loanCurrency = loan.getLoanCurrency();

                checkNotNull(takeLossOwedTextField).setText(loan.getRemainingOwed());
                checkNotNull(takeLossTextField).setText(formatAmount(BigDecimal.ZERO));

                checkNotNull(takeLossOwedCurrencyButton).textProperty().setValue(loanCurrency);
                checkNotNull(takeLossCurrencyLabel).textProperty().setValue(loanCurrency);
            } else {
                checkNotNull(takeLossOwedTextField).setText("");
                checkNotNull(takeLossTextField).setText(formatAmount(BigDecimal.ZERO));

                checkNotNull(takeLossOwedCurrencyButton).textProperty().setValue("");
                checkNotNull(takeLossCurrencyLabel).textProperty().setValue("");
            }
        });

        if (this.takeLossLoans != null && !this.takeLossLoans.isEmpty()) {
            checkNotNull(takeLossLoansTableView).getSelectionModel().selectFirst();
        }

        // ------------------------------------------------------

        List<Account> accountList = DBUtil.connectGetResultAndClose(
                sess -> AccountDAO.findByCurrencies(sess, List.of(ticker.getQuote().getCurrency(), ticker.getBase().getCurrency())));
        this.accounts = new FilteredList<>(FXCollections.observableArrayList(accountList));

        checkNotNull(takeProfitQuoteRadioButton).selectedProperty().addListener((obs, oldVal, newVal) -> updateProfitAccountsFilter(ticker));
        checkNotNull(takeProfitBaseRadioButton).selectedProperty().addListener((obs, oldVal, newVal) -> updateProfitAccountsFilter(ticker));
        updateProfitAccountsFilter(ticker);

        checkNotNull(takeProfitAccountsTableView).setItems(this.accounts);
        autoResizeTableColumns(takeProfitAccountsTableView);

        List<Level> levelList = DBUtil.connectGetResultAndClose(
                sess -> LevelDAO.findByCurrencies(sess, List.of(ticker.getQuote().getCurrency(), ticker.getBase().getCurrency())));
        levelList.removeIf(l -> l.getId().equals(levelId));
        this.levels = new FilteredList<>(FXCollections.observableArrayList(levelList));

        checkNotNull(takeProfitLevelQuoteRadioButton).selectedProperty().addListener((obs, oldVal, newVal) -> updateProfitLevelsFilter(ticker));
        checkNotNull(takeProfitLevelBaseRadioButton).selectedProperty().addListener((obs, oldVal, newVal) -> updateProfitLevelsFilter(ticker));
        updateProfitLevelsFilter(ticker);

        checkNotNull(takeProfitLevelsTableView).setItems(this.levels);
        autoResizeTableColumns(takeProfitLevelsTableView);

        checkNotNull(takeLossLoansTableView).setItems(this.loans);
        autoResizeTableColumns(takeLossLoansTableView);
    }

    private void updateProfitAccountsFilter(MarketTicker ticker) {
        if (accounts != null) {
            accounts.setPredicate(acc -> {
                if (checkNotNull(takeProfitQuoteRadioButton).isSelected()) {
                    boolean hasDebt = nullToZero(level.getDebtBase()).compareTo(BigDecimal.ZERO) > 0
                            || nullToZero(level.getDebtQuote()).compareTo(BigDecimal.ZERO) > 0;
                    BigDecimal profitAmountQuote;
                    if (hasDebt && tradeDirection == Direction.SHORT) {
                        profitAmountQuote = nullToZero(level.getAvailableAmountQuote()).subtract(nullToZero(level.getProjectedAmountQuote()));
                    } else {
                        profitAmountQuote = nullToZero(level.getAvailableAmountQuote()).subtract(nullToZero(level.getDebtQuote()));
                    }
                    if (profitAmountQuote.compareTo(BigDecimal.ZERO) <= 0) {
                        profitAmountQuote = BigDecimal.ZERO;
                    }

                    checkNotNull(takeProfitCurrencyLabel).textProperty().setValue(ticker.getQuote().getCurrency());
                    checkNotNull(takeProfitAvailableCurrencyButton).textProperty().setValue(ticker.getQuote().getCurrency());

                    checkNotNull(takeProfitAvailableTextField).setText(formatAmount(profitAmountQuote));
                    checkNotNull(takeProfitTextField).setText(formatAmount(BigDecimal.ZERO));

                    return acc.getCurrency().getCurrency().equals(ticker.getQuote().getCurrency());
                } else if (checkNotNull(takeProfitBaseRadioButton).isSelected()) {
                    boolean hasDebt = nullToZero(level.getDebtBase()).compareTo(BigDecimal.ZERO) > 0
                            || nullToZero(level.getDebtQuote()).compareTo(BigDecimal.ZERO) > 0;
                    BigDecimal profitAmountBase;
                    if (hasDebt && tradeDirection == Direction.LONG) {
                        profitAmountBase = nullToZero(level.getAvailableAmountBase()).subtract(nullToZero(level.getProjectedAmountBase()));
                    } else {
                        profitAmountBase = nullToZero(level.getAvailableAmountBase()).subtract(nullToZero(level.getDebtBase()));
                    }
                    if (profitAmountBase.compareTo(BigDecimal.ZERO) <= 0) {
                        profitAmountBase = BigDecimal.ZERO;
                    }

                    checkNotNull(takeProfitCurrencyLabel).textProperty().setValue(ticker.getBase().getCurrency());
                    checkNotNull(takeProfitAvailableCurrencyButton).textProperty().setValue(ticker.getBase().getCurrency());

                    checkNotNull(takeProfitAvailableTextField).setText(formatAmount(profitAmountBase));
                    checkNotNull(takeProfitTextField).setText(formatAmount(BigDecimal.ZERO));

                    return acc.getCurrency().getCurrency().equals(ticker.getBase().getCurrency());
                } else {
                    throw new RuntimeException("No Take currency selected");
                }
            });
        }
    }

    public void updateProfitLevelsFilter(MarketTicker ticker) {
        if (levels != null) {
            levels.setPredicate(lvl -> {
                if (checkNotNull(takeProfitLevelQuoteRadioButton).isSelected()) {
                    String quote = ticker.getQuote().getCurrency();

                    boolean hasDebt = nullToZero(level.getDebtBase()).compareTo(BigDecimal.ZERO) > 0
                            || nullToZero(level.getDebtQuote()).compareTo(BigDecimal.ZERO) > 0;
                    BigDecimal profitAmountQuote;
                    if (hasDebt && tradeDirection == Direction.SHORT) {
                        profitAmountQuote = nullToZero(level.getAvailableAmountQuote()).subtract(nullToZero(level.getProjectedAmountQuote()));
                    } else {
                        profitAmountQuote = nullToZero(level.getAvailableAmountQuote()).subtract(nullToZero(level.getDebtQuote()));
                    }
                    if (profitAmountQuote.compareTo(BigDecimal.ZERO) <= 0) {
                        profitAmountQuote = BigDecimal.ZERO;
                    }

                    checkNotNull(takeProfitLevelCurrencyLabel).textProperty().setValue(quote);
                    checkNotNull(takeProfitLevelAvailableCurrencyButton).textProperty().setValue(quote);

                    checkNotNull(takeProfitLevelAvailableTextField).setText(formatAmount(profitAmountQuote));
                    checkNotNull(takeProfitLevelTextField).setText(formatAmount(BigDecimal.ZERO));

                    return quote.equals(lvl.getPoplavok().getTicker().getQuote().getCurrency())
                            || quote.equals(lvl.getPoplavok().getTicker().getBase().getCurrency());
                } else if (checkNotNull(takeProfitLevelBaseRadioButton).isSelected()) {
                    String base = ticker.getBase().getCurrency();

                    boolean hasDebt = nullToZero(level.getDebtBase()).compareTo(BigDecimal.ZERO) > 0
                            || nullToZero(level.getDebtQuote()).compareTo(BigDecimal.ZERO) > 0;
                    BigDecimal profitAmountBase;
                    if (hasDebt && tradeDirection == Direction.LONG) {
                        profitAmountBase = nullToZero(level.getAvailableAmountBase()).subtract(nullToZero(level.getProjectedAmountBase()));
                    } else {
                        profitAmountBase = nullToZero(level.getAvailableAmountBase()).subtract(nullToZero(level.getDebtBase()));
                    }
                    if (profitAmountBase.compareTo(BigDecimal.ZERO) <= 0) {
                        profitAmountBase = BigDecimal.ZERO;
                    }

                    checkNotNull(takeProfitLevelCurrencyLabel).textProperty().setValue(base);
                    checkNotNull(takeProfitLevelAvailableCurrencyButton).textProperty().setValue(base);

                    checkNotNull(takeProfitLevelAvailableTextField).setText(formatAmount(profitAmountBase));
                    checkNotNull(takeProfitLevelTextField).setText(formatAmount(BigDecimal.ZERO));

                    return base.equals(lvl.getPoplavok().getTicker().getQuote().getCurrency())
                            || base.equals(lvl.getPoplavok().getTicker().getBase().getCurrency());
                } else {
                    throw new RuntimeException("No Take Level currency selected");
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

    public void repay() {
        try {
            Loan selectedLoan = checkNotNull(loansTableView).getSelectionModel().getSelectedItem().loan;
            if (selectedLoan == null) {
                JavaFxUtils.showErrorMessage("Please select loan");
                return;
            }

            BigDecimal repayAmount = nullToZero(fromString(checkNotNull(toRepayTextField).textProperty().get()));

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

            returnRepayment = new RepayRepaymentInfo(repayAmount, selectedLoan.getCurrency().getCurrency(), selectedLoan);
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
    public RepaymentInfo getReturnRepayment() {
        return returnRepayment;
    }

    // --------------------------------------------

    public void moveAvailableAmountToTakeProfit() {
        BigDecimal amount = nullToZero(fromString(checkNotNull(takeProfitAvailableTextField).getText()));
        checkNotNull(takeProfitTextField).setText(formatAmount(amount));
    }

    public void moveAvailableAmountToTakeProfitLevel() {
        BigDecimal amount = nullToZero(fromString(checkNotNull(takeProfitLevelAvailableTextField).getText()));
        checkNotNull(takeProfitLevelTextField).setText(formatAmount(amount));
    }

    public void takeProfit() {
        // Taking profit from this level to the account chosen by user
        try {
            Account selectedAccount = checkNotNull(takeProfitAccountsTableView).getSelectionModel().getSelectedItem();
            if (selectedAccount == null) {
                JavaFxUtils.showErrorMessage("Please select account");
                return;
            }

            BigDecimal repayAmount = nullToZero(fromString(checkNotNull(takeProfitTextField).textProperty().get()));
            BigDecimal availableAmount = nullToZero(fromString(checkNotNull(takeProfitAvailableTextField).textProperty().get()));

            if (repayAmount.compareTo(availableAmount) > 0) {
                JavaFxUtils.showErrorMessage("TakeProfit amount cannot be greater than available amount");
                return;
            }
            if (repayAmount.compareTo(BigDecimal.ZERO) <= 0) {
                JavaFxUtils.showErrorMessage("TakeProfit amount must be greater than zero");
                return;
            }

            returnRepayment = new ProfitRepaymentInfo(repayAmount, selectedAccount.getCurrency().getCurrency(), selectedAccount);
            checkNotNull(stage).close();
        } catch (Exception e) {
            JavaFxUtils.showErrorMessage("RepayDialog close Error: " + e);
            LOGGER.error("RepayDialog close Error:", e);
        }
    }

    public void takeProfitToLevel() {
        // Taking profit from this level to the account chosen by user
        try {
            Level selectedLevel = checkNotNull(takeProfitLevelsTableView).getSelectionModel().getSelectedItem();
            if (selectedLevel == null) {
                JavaFxUtils.showErrorMessage("Please select level");
                return;
            }

            BigDecimal repayAmount = nullToZero(fromString(checkNotNull(takeProfitLevelTextField).textProperty().get()));
            BigDecimal availableAmount = nullToZero(fromString(checkNotNull(takeProfitLevelAvailableTextField).textProperty().get()));

            if (repayAmount.compareTo(availableAmount) > 0) {
                JavaFxUtils.showErrorMessage("TakeProfit amount cannot be greater than available amount");
                return;
            }
            if (repayAmount.compareTo(BigDecimal.ZERO) <= 0) {
                JavaFxUtils.showErrorMessage("TakeProfit amount must be greater than zero");
                return;
            }

            String currency;
            if (checkNotNull(takeProfitLevelQuoteRadioButton).isSelected()) {
                currency = ticker.getQuote().getCurrency();
            } else if (checkNotNull(takeProfitLevelBaseRadioButton).isSelected()) {
                currency = ticker.getBase().getCurrency();
            } else {
                throw new RuntimeException("No Take Level currency selected");
            }

            returnRepayment = new ProfitRepaymentInfo(repayAmount, currency, selectedLevel);

            checkNotNull(stage).close();
        } catch (Exception e) {
            JavaFxUtils.showErrorMessage("RepayDialog close Error: " + e);
            LOGGER.error("RepayDialog close Error:", e);
        }
    }

    // --------------------------------------------

    public void moveOwedAmountToTakeLoss() {
        BigDecimal amount = nullToZero(fromString(checkNotNull(takeLossOwedTextField).getText()));
        checkNotNull(takeLossTextField).setText(formatAmount(amount));
    }

    public void takeLoss() {
        // Taking loss - reduce owed amount
        try {
            LoanInfo selectedLoan = checkNotNull(takeLossLoansTableView).getSelectionModel().getSelectedItem();
            if (selectedLoan == null) {
                JavaFxUtils.showErrorMessage("Please select loan");
                return;
            }

            BigDecimal takeLossAmount = nullToZero(fromString(checkNotNull(takeLossTextField).textProperty().get()));
            if (takeLossAmount.compareTo(selectedLoan.getRemainingOwedAmount()) > 0) {
                JavaFxUtils.showErrorMessage("TakeLoss amount cannot be greater than owed amount");
                return;
            }
            if (takeLossAmount.compareTo(BigDecimal.ZERO) <= 0) {
                JavaFxUtils.showErrorMessage("TakeLoss amount must be greater than zero");
                return;
            }

            returnRepayment = new LossRepaymentInfo(takeLossAmount, selectedLoan.loan.getCurrency().getCurrency(), selectedLoan.loan);
            checkNotNull(stage).close();
        } catch (Exception e) {
            JavaFxUtils.showErrorMessage("RepayDialog close Error: " + e);
            LOGGER.error("RepayDialog close Error:", e);
        }
    }
}
