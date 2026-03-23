package com.poplavok.forms;

import com.flower.fxutils.ModalWindow;
import com.flower.fxutils.Refreshable;
import com.google.common.base.Preconditions;
import com.poplavok.data.dao.AccountDAO;
import com.poplavok.data.model.Account;
import com.poplavok.data.utils.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public class PoplavoksTab extends AnchorPane implements Refreshable {
    final static Logger LOGGER = LoggerFactory.getLogger(PoplavoksTab.class);

    @Nullable FilteredList<Account> accounts;
    @Nullable FilteredList<AccountTransaction> transactions;

    @FXML @Nullable TableView<Account> poplavoksTable;
    @FXML @Nullable TextField filterTextField;
    @FXML @Nullable CheckBox showClosedCheckBox;

    protected final MainForm mainApp;

    public PoplavoksTab(MainForm mainApp) {
        this.mainApp = mainApp;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PoplavoksTab.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        checkNotNull(filterTextField).textProperty().addListener((observable, oldValue, newValue) -> filterTableView());
        checkNotNull(showClosedCheckBox).selectedProperty().addListener((observable, oldValue, newValue) -> filterTableView());

        refreshContent();
    }

    @Override
    public void refreshContent() {
        try {
            Account selectedAccount = null;
            if (poplavoksTable != null && poplavoksTable.getSelectionModel().getSelectedItem() != null) {
                selectedAccount = poplavoksTable.getSelectionModel().getSelectedItem();
            }

            ObservableList<Account> masterAccounts = FXCollections.observableList(DBUtil.connectGetResultAndClose(AccountDAO::findAll));
            accounts = new FilteredList<>(masterAccounts);
            SortedList<Account> sortableAccounts = new SortedList<>(accounts);

            accounts.setPredicate(createFilterPredicate());
            Preconditions.checkNotNull(poplavoksTable).itemsProperty().set(sortableAccounts);
            sortableAccounts.comparatorProperty().bind(poplavoksTable.comparatorProperty());
            poplavoksTable.refresh();

            if (selectedAccount != null) {
                for (Account account : poplavoksTable.getItems()) {
                    if (account.getId().equals(selectedAccount.getId())) {
                        poplavoksTable.getSelectionModel().select(account);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error: " + e, ButtonType.OK);
            LOGGER.error("Error:", e);
            alert.showAndWait();
        }
    }

    private Predicate<Account> createFilterPredicate() {
        String searchText = Preconditions.checkNotNull(filterTextField).textProperty().get();
        boolean showClosed = Preconditions.checkNotNull(showClosedCheckBox).isSelected();
        return account -> {
            if (!showClosed && account.isArchived()) return false;
            if (searchText == null || searchText.isEmpty()) return true;
            
            String lowerCaseFilter = searchText.toLowerCase().trim();
            
            if (account.getCurrency().getCurrency().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            return account.getAccountName() != null && account.getAccountName().toLowerCase().contains(lowerCaseFilter);
        };
    }

    public void filterTableView() {
        if (accounts != null) {
            accounts.setPredicate(createFilterPredicate());
        }
    }

    public void newPoplavok() {
        try {
            AccountAddDialog accountAddDialog = new AccountAddDialog(null);
            Stage workspaceStage = ModalWindow.showModal(checkNotNull(mainApp.mainStage),
                    stage -> { accountAddDialog.setStage(stage); return accountAddDialog; },
                    "New Account");

            workspaceStage.setOnHidden(
                    ev -> {
                        try {
                            Account account = accountAddDialog.getReturnAccount();
                            if (account != null) {
                                DBUtil.connectCommitAndClose(sess -> AccountDAO.save(sess, checkNotNull(account)));
                                refreshContent();
                            }
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating account: " + e, ButtonType.OK);
                            LOGGER.error("Error creating account: ", e);
                            alert.showAndWait();
                        }
                    }
            );
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating account: " + e, ButtonType.OK);
            LOGGER.error("Error creating account: ", e);
            alert.showAndWait();
        }
    }

    public void renamePoplavok() {
        try {
            Account origAccount = Preconditions.checkNotNull(poplavoksTable).getSelectionModel().getSelectedItem();
            if (origAccount == null) { return; }

            AccountAddDialog accountAddDialog = new AccountAddDialog(origAccount);
            Stage workspaceStage = ModalWindow.showModal(checkNotNull(mainApp.mainStage),
                    stage -> { accountAddDialog.setStage(stage); return accountAddDialog; },
                    "Rename Account");

            workspaceStage.setOnHidden(
                    ev -> {
                        try {
                            Account account = accountAddDialog.getReturnAccount();
                            if (account != null) {
                                DBUtil.connectCommitAndClose(sess -> AccountDAO.update(sess, checkNotNull(account)));
                                refreshContent();
                            }
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating account: " + e, ButtonType.OK);
                            LOGGER.error("Error creating account: ", e);
                            alert.showAndWait();
                        }
                    }
            );
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating account: " + e, ButtonType.OK);
            LOGGER.error("Error creating account: ", e);
            alert.showAndWait();
        }
    }

    // TODO: update transaction details dialog
}
