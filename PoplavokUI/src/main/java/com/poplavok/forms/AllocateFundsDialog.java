package com.poplavok.forms;

import com.poplavok.data.model.Account;
import com.poplavok.data.model.Currency;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.Loan;
import com.poplavok.data.dao.AccountDAO;
import com.poplavok.data.dao.LevelDAO;
import com.poplavok.data.utils.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;

public class AllocateFundsDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(AllocateFundsDialog.class);

    @FXML @Nullable TextField amountTextField;
    @FXML @Nullable Label currencyLabel;

    @FXML @Nullable TableView<Account> accountsTableView;
    @FXML @Nullable TableView<Level> levelsTableView;

    @Nullable FilteredList<Account> accounts;
    @Nullable FilteredList<Level> levels;

    @Nullable Stage stage;

    final Level lvl;
    final Currency currency;
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

        checkNotNull(currencyLabel).textProperty().set(currency.getCurrency());
        checkNotNull(amountTextField).textProperty().set(formatAmount(defaultAmount));
        
        ObservableList<Account> observableAccounts = FXCollections.observableArrayList();
        ObservableList<Level> observableLevels = FXCollections.observableArrayList();

        DBUtil.connectCommitAndClose(session -> {
            List<Account> allAccounts = AccountDAO.findAvailableByCurrency(session, currency.getCurrency());
            observableAccounts.addAll(allAccounts);

            List<Level> allLevels = LevelDAO.findAvailableByCurrency(session, currency.getCurrency());
            observableLevels.addAll(allLevels);
        });

        this.accounts = new FilteredList<>(observableAccounts);
        this.levels = new FilteredList<>(observableLevels);

        checkNotNull(accountsTableView).setItems(this.accounts);
        checkNotNull(levelsTableView).setItems(this.levels);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void okClose() {
        try {
            Loan loan = new Loan();

            loan.setDestinationLevel(lvl);
            loan.setAmount(new BigDecimal(checkNotNull(amountTextField).textProperty().get()));
            loan.setCurrency(currency);

            // TODO: set source
            //  if no source selected, show alert
            //  show account id/name levelid/name and available funds in the table view
            //  create source based on selected tab and selection
            //  set interest rate types
            //  maybe add interest type field to loan as well

            returnLoan = loan;
            checkNotNull(stage).close();
       } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "AccountAddDialog close Error: " + e, ButtonType.OK);
            LOGGER.error("AccountAddDialog close Error:", e);
            alert.showAndWait();
        }
    }

    @Nullable
    public Loan getReturnLoan() {
        return returnLoan;
    }
}
