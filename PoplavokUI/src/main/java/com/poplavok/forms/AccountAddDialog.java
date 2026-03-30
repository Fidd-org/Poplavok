package com.poplavok.forms;

import com.google.common.base.Preconditions;
import com.poplavok.data.dao.CurrencyDAO;
import com.poplavok.data.model.Account;
import com.poplavok.data.model.Currency;
import com.poplavok.data.utils.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public class AccountAddDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(AccountAddDialog.class);

    @FXML @Nullable ComboBox<Currency> currencyComboBox;
    @FXML @Nullable TextField accountNameTextField;
    @FXML @Nullable TextField currencyFilterTextField;
    @FXML @Nullable Button addButton;

    @Nullable Stage stage;

    @Nullable Long accountId = null;
    @Nullable volatile Account returnAccount = null;
    @Nullable FilteredList<Currency> currencies;

    public AccountAddDialog(@Nullable Account account) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AccountAddDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        checkNotNull(currencyComboBox).setConverter(new StringConverter<>() {
            @Override
            public String toString(Currency object) {
                return object != null ? object.getCurrency() : "";
            }

            @Override
            public Currency fromString(String string) {
                // No need for reverse conversion for now, ComboBox is not editable
                throw new UnsupportedOperationException("Conversion from string not supported");
            }
        });

        //Collections.sort(currencies, (c1, c2) -> c1.getCurrency().compareToIgnoreCase(c2.getCurrency()));
        ObservableList<Currency> masterCurrencies =
                FXCollections.observableList(DBUtil.connectGetResultAndClose(CurrencyDAO::findAll));
        currencies = new FilteredList<>(masterCurrencies);
        SortedList<Currency> sortableCurrencies = new SortedList<>(currencies);

        currencies.setPredicate(createFilterPredicate());

        checkNotNull(currencyComboBox).itemsProperty().setValue(sortableCurrencies);

        if (account == null) {
            checkNotNull(currencyComboBox).selectionModelProperty().get().selectFirst();
            checkNotNull(addButton).textProperty().set("Add New Account");
        } else {
            accountId = account.getId();
            checkNotNull(accountNameTextField).textProperty().set(account.getAccountName());
            checkNotNull(currencyComboBox).selectionModelProperty().get().select(account.getCurrency());
            checkNotNull(currencyComboBox).disableProperty().set(true);
            checkNotNull(currencyFilterTextField).disableProperty().set(true);
            checkNotNull(addButton).textProperty().set("Rename Account");
        }
    }

    private Predicate<Currency> createFilterPredicate() {
        String searchText = Preconditions.checkNotNull(currencyFilterTextField).textProperty().get();
        return currency -> {
            if (searchText == null || searchText.isEmpty()) return true;
            return (currency.getCurrency().toLowerCase().contains(searchText.toLowerCase().trim()));
        };
    }

    public void filterTableView() {
        if (currencies != null) {
            currencies.setPredicate(createFilterPredicate());
            checkNotNull(currencyComboBox).selectionModelProperty().get().selectFirst();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void okClose() {
        try {
            Account account = new Account();
            account.setId(accountId);
            account.setCreationDate(new Date());
            account.setCurrency(checkNotNull(currencyComboBox).getSelectionModel().getSelectedItem());
            account.setAccountName(checkNotNull(accountNameTextField).textProperty().get());
            account.setAvailableAmount(BigDecimal.ZERO);
            account.setLentAmount(BigDecimal.ZERO);

            returnAccount = account;
            checkNotNull(stage).close();
       } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "AccountAddDialog close Error: " + e, ButtonType.OK);
            LOGGER.error("AccountAddDialog close Error:", e);
            alert.showAndWait();
        }
    }

    @Nullable Account getReturnAccount() {
        return returnAccount;
    }
}
