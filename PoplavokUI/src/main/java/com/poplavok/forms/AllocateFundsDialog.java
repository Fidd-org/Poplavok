package com.poplavok.forms;

import com.flower.fxutils.JavaFxUtils;
import com.poplavok.data.model.Account;
import com.poplavok.data.model.Currency;
import com.poplavok.data.model.InterestRateType;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.Loan;
import com.poplavok.data.dao.AccountDAO;
import com.poplavok.data.dao.LevelDAO;
import com.poplavok.data.model.LoanType;
import com.poplavok.data.utils.DBUtil;
import com.poplavok.forms.wrapper.LevelWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.flower.fxutils.JavaFxUtils.autoResizeTableColumns;
import static com.flower.fxutils.JavaFxUtils.createDecimalTextFormatter;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;
import static com.poplavok.data.utils.BigDecimalUtil.fromString;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;

public class AllocateFundsDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(AllocateFundsDialog.class);

    @FXML @Nullable TextField amountTextField;
    @FXML @Nullable TextField notesTextField;
    @FXML @Nullable Label currencyLabel;

    @FXML @Nullable TextField interestRateTextField;
    @FXML @Nullable ComboBox<InterestRateType> interestTypeComboBox;
    @FXML @Nullable ComboBox<LoanType> externalLoanTypeComboBox;

    @FXML @Nullable TableView<Account> accountsTableView;
    @FXML @Nullable TableView<LevelWrapper> levelsTableView;

    @FXML @Nullable TabPane sourceTabPane;
    @FXML @Nullable Tab sourceAccountTab;
    @FXML @Nullable Tab sourceLevelTab;
    @FXML @Nullable Tab sourceExternalTab;

    @Nullable FilteredList<Account> accounts;
    @Nullable FilteredList<LevelWrapper> levels;

    @Nullable Stage stage;

    final Level lvl;
    final Currency currency;
    @Nullable final BigDecimal defaultAmount;
    @Nullable volatile Loan returnLoan = null;

    public AllocateFundsDialog(Level lvl, Currency currency, @Nullable BigDecimal defaultAmount) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AllocateFundsDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.lvl = lvl;
        this.currency = currency;
        this.defaultAmount = defaultAmount;

        checkNotNull(currencyLabel).textProperty().set(currency.getCurrency());
        resetAmount();

        checkNotNull(interestRateTextField).setTextFormatter(createDecimalTextFormatter());

        checkNotNull(interestTypeComboBox).getItems().setAll(InterestRateType.values());
        checkNotNull(interestTypeComboBox).getSelectionModel().selectFirst();

        checkNotNull(externalLoanTypeComboBox).getItems().setAll(LoanType.EXTERNAL_ISOLATED_MARGIN, LoanType.EXTERNAL_CROSS_MARGIN);
        checkNotNull(externalLoanTypeComboBox).getSelectionModel().selectFirst();

        ObservableList<Account> observableAccounts = FXCollections.observableArrayList();
        ObservableList<LevelWrapper> observableLevels = FXCollections.observableArrayList();

        DBUtil.connectCommitAndClose(session -> {
            List<Account> allAccounts = AccountDAO.findAvailableByCurrency(session, currency.getCurrency());
            observableAccounts.addAll(allAccounts);

            List<Level> allLevels = LevelDAO.findAvailableByCurrency(session, currency.getCurrency());
            observableLevels.addAll(allLevels.stream().map(level -> new LevelWrapper(level, currency.getCurrency())).toList());
        });

        this.accounts = new FilteredList<>(observableAccounts);
        this.levels = new FilteredList<>(observableLevels);

        checkNotNull(accountsTableView).setItems(this.accounts);
        autoResizeTableColumns(accountsTableView);
        checkNotNull(levelsTableView).setItems(this.levels);
        autoResizeTableColumns(levelsTableView);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void okClose() {
        try {
            Loan loan = new Loan();

            loan.setAmount(nullToZero(fromString(checkNotNull(amountTextField).textProperty().get())));
            loan.setDate(new Date());
            loan.setCurrency(currency);

            loan.setActive(true);
            loan.setNotes(checkNotNull(notesTextField).textProperty().get());

            loan.setDestinationLevel(lvl);

            // (determine source)
            Tab selectedTab = checkNotNull(sourceTabPane).getSelectionModel().getSelectedItem();
            if (selectedTab == sourceAccountTab) {
                Account selectedAccount = checkNotNull(accountsTableView).getSelectionModel().getSelectedItem();
                if (selectedAccount == null) {
                    JavaFxUtils.showErrorMessage("Source Account not selected");
                    return;
                }
                if (selectedAccount.getAvailableAmount().compareTo(loan.getAmount()) < 0) {
                    JavaFxUtils.showErrorMessage("Source Account does not have enough available amount");
                    return;
                }

                loan.setLoanType(LoanType.ACCOUNT_FUNDED);
                loan.setSourceAccount(selectedAccount);
            } else if (selectedTab == sourceLevelTab) {
                LevelWrapper selectedLevel = checkNotNull(levelsTableView).getSelectionModel().getSelectedItem();
                if (selectedLevel == null) {
                    JavaFxUtils.showErrorMessage("Source Level not selected");
                    return;
                }
                Currency levelBase = selectedLevel.level.getPoplavok().getTicker().getBase();
                Currency levelQuote = selectedLevel.level.getPoplavok().getTicker().getQuote();

                if (levelQuote.getCurrency().equals(currency.getCurrency())) {
                    if (selectedLevel.level.getAvailableAmountQuote() != null && selectedLevel.level.getAvailableAmountQuote().compareTo(loan.getAmount()) < 0) {
                        JavaFxUtils.showErrorMessage("Source Level does not have enough available amount in " + currency.getCurrency());
                        return;
                    }
                } else if (levelBase.getCurrency().equals(currency.getCurrency())) {
                    if (selectedLevel.level.getAvailableAmountBase() != null && selectedLevel.level.getAvailableAmountBase().compareTo(loan.getAmount()) < 0) {
                        JavaFxUtils.showErrorMessage("Source Level does not have enough available amount in " + currency.getCurrency());
                        return;
                    }
                } else {
                    JavaFxUtils.showErrorMessage("Source Level does not hold " + currency.getCurrency());
                    return;
                }

                loan.setLoanType(LoanType.POPLAVOK_FUNDED);
                loan.setSourceLevel(selectedLevel.level);
            } else if (selectedTab == sourceExternalTab) {
                // External loan, has no source
                BigDecimal interestRate = nullToZero(fromString(checkNotNull(interestRateTextField).textProperty().get()));
                InterestRateType interestRateType = checkNotNull(interestTypeComboBox).getSelectionModel().getSelectedItem();

                loan.setLoanType(checkNotNull(externalLoanTypeComboBox).getSelectionModel().getSelectedItem());
                loan.setInterestRate(interestRate);
                loan.setInterestRateType(interestRateType);
            } else {
                JavaFxUtils.showErrorMessage("Source not selected");
                return;
            }

            returnLoan = loan;
            checkNotNull(stage).close();
       } catch (Exception e) {
            LOGGER.error("AccountAddDialog close Error:", e);
            JavaFxUtils.showErrorMessage("AccountAddDialog close Error: " + e);
        }
    }

    @Nullable
    public Loan getReturnLoan() {
        return returnLoan;
    }

    public void useMaxAvailableAmount() {
        Tab selectedTab = checkNotNull(sourceTabPane).getSelectionModel().getSelectedItem();
        if (selectedTab == sourceAccountTab) {
            Account selectedAccount = checkNotNull(accountsTableView).getSelectionModel().getSelectedItem();
            if (selectedAccount != null) {
                checkNotNull(amountTextField).textProperty().set(formatAmount(selectedAccount.getAvailableAmount()));
            }
        } else if (selectedTab == sourceLevelTab) {
            Level selectedLevel = checkNotNull(levelsTableView).getSelectionModel().getSelectedItem().level;
            if (selectedLevel != null) {
                Currency levelBase = selectedLevel.getPoplavok().getTicker().getBase();
                Currency levelQuote = selectedLevel.getPoplavok().getTicker().getQuote();

                if (levelBase.getCurrency().equals(currency.getCurrency())) {
                    checkNotNull(amountTextField).textProperty().set(formatAmount(selectedLevel.getAvailableAmountBase()));
                } else if (levelQuote.getCurrency().equals(currency.getCurrency())) {
                    checkNotNull(amountTextField).textProperty().set(formatAmount(selectedLevel.getAvailableAmountQuote()));
                }
            }
        }
    }

    public void resetAmount() {
        if (defaultAmount != null) {
            checkNotNull(amountTextField).textProperty().set(formatAmount(defaultAmount));
        } else {
            checkNotNull(amountTextField).textProperty().set("");
        }
    }
}
