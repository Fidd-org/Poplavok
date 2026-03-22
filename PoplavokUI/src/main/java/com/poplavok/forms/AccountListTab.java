package com.poplavok.forms;

import com.flower.fxutils.ModalWindow;
import com.flower.fxutils.ProgressForm;
import com.flower.fxutils.Refreshable;
import com.google.common.base.Preconditions;
import com.poplavok.data.dao.AccountDAO;
import com.poplavok.data.dao.TransactionDAO;
import com.poplavok.data.model.Account;
import com.poplavok.data.model.ExternalTransaction;
import com.poplavok.data.model.Transaction;
import com.poplavok.data.utils.DBUtil;
import com.poplavok.kucoin.KucoinTool;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Date;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public class AccountListTab extends AnchorPane implements Refreshable {
    final static Logger LOGGER = LoggerFactory.getLogger(AccountListTab.class);

    @Nullable FilteredList<Account> accounts;
    @Nullable FilteredList<Transaction> transactions;

    @FXML @Nullable TableView<Account> accountsTable;
    @FXML @Nullable TableView<Transaction> transactionsTable;
    @FXML @Nullable TextField filterTextField;
    @FXML @Nullable CheckBox showArchivedCheckBox;

    protected final MainForm mainApp;

    public AccountListTab(MainForm mainApp) {
        this.mainApp = mainApp;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AccountListTab.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        checkNotNull(filterTextField).textProperty().addListener((observable, oldValue, newValue) -> filterTableView());
        checkNotNull(showArchivedCheckBox).selectedProperty().addListener((observable, oldValue, newValue) -> filterTableView());

        checkNotNull(accountsTable).getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                refreshTransactions(newValue);
            } else {
                if (transactionsTable != null) {
                    transactionsTable.setItems(FXCollections.observableArrayList());
                }
            }
        });

        refreshContent();
    }

    @Override
    public void refreshContent() {
        try {
            Account selectedAccount = null;
            if (accountsTable != null && accountsTable.getSelectionModel().getSelectedItem() != null) {
                selectedAccount = accountsTable.getSelectionModel().getSelectedItem();
            }

            Transaction selectedTransaction = null;
            if (transactionsTable != null && transactionsTable.getSelectionModel().getSelectedItem() != null) {
                selectedTransaction = transactionsTable.getSelectionModel().getSelectedItem();
            }

            ObservableList<Account> masterAccounts = FXCollections.observableList(DBUtil.connectGetResultAndClose(AccountDAO::findAll));
            accounts = new FilteredList<>(masterAccounts);
            SortedList<Account> sortableAccounts = new SortedList<>(accounts);

            accounts.setPredicate(createFilterPredicate());
            Preconditions.checkNotNull(accountsTable).itemsProperty().set(sortableAccounts);
            sortableAccounts.comparatorProperty().bind(accountsTable.comparatorProperty());
            accountsTable.refresh();

            if (selectedAccount != null) {
                for (Account account : accountsTable.getItems()) {
                    if (account.getId().equals(selectedAccount.getId())) {
                        accountsTable.getSelectionModel().select(account);

                        if (selectedTransaction != null && transactionsTable != null) {
                            for (Transaction t : transactionsTable.getItems()) {
                                if (t.getId() != null && t.getId().equals(selectedTransaction.getId())) {
                                    transactionsTable.getSelectionModel().select(t);
                                    break;
                                }
                            }
                        }
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

    private void refreshTransactions(Account account) {
        try {
            ObservableList<Transaction> masterTransactions = FXCollections.observableList(
                    DBUtil.connectGetResultAndClose(sess -> TransactionDAO.findByAccount(sess, account.getId())));

            transactions = new FilteredList<>(masterTransactions);
            SortedList<Transaction> sortableTransactions = new SortedList<>(transactions);

            if (transactionsTable != null) {
                transactionsTable.itemsProperty().set(sortableTransactions);
                sortableTransactions.comparatorProperty().bind(transactionsTable.comparatorProperty());
                transactionsTable.refresh();
            }
        } catch (Exception e) {
            LOGGER.error("Error refreshing transactions:", e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error refreshing transactions: " + e, ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void retrieveFromExchange(ActionEvent event) {
        try {
            ModalWindow.showModal(event,
                    stage -> new ProgressForm(stage, this, KucoinTool::retrieveMarketTickersFromExchange),
                    "Retrieve extended data",
                    StageStyle.UNDECORATED);
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error: " + e, ButtonType.OK);
            LOGGER.error("Error:", e);
            alert.showAndWait();
        }
    }

    private Predicate<Account> createFilterPredicate() {
        String searchText = Preconditions.checkNotNull(filterTextField).textProperty().get();
        boolean showArchived = Preconditions.checkNotNull(showArchivedCheckBox).isSelected();
        return account -> {
            if (!showArchived && account.isArchived()) return false;
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

    public void newAccount() {
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

    public void renameAccount() {
        try {
            Account origAccount = Preconditions.checkNotNull(accountsTable).getSelectionModel().getSelectedItem();
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

    public void archiveAccount() {
        try {
            Account account = Preconditions.checkNotNull(accountsTable).getSelectionModel().getSelectedItem();

            // TODO: make sure that account has 0 available or reserved amount before archiving

            DBUtil.connectCommitAndClose(sess -> AccountDAO.setArchived(sess, account.getId(), !account.isArchived()));
            refreshContent();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error: " + e, ButtonType.OK);
            LOGGER.error("Error:", e);
            alert.showAndWait();
        }
    }

    public void deposit() {
        try {
            Account account = Preconditions.checkNotNull(accountsTable).getSelectionModel().getSelectedItem();

            DepositWithdrawDialog depositWithdrawDialog = new DepositWithdrawDialog(true);
            Stage workspaceStage = ModalWindow.showModal(checkNotNull(mainApp.mainStage),
                    stage -> { depositWithdrawDialog.setStage(stage); return depositWithdrawDialog; },
                    "Deposit to " + account.getAccountName());

            workspaceStage.setOnHidden(
                ev -> {
                    try {
                        ExternalTransaction transaction = depositWithdrawDialog.getReturnTransaction();
                        if (transaction != null) {
                            transaction.setDestinationAccount(account);
                            transaction.setDate(new Date());
                            transaction.setCurrency(account.getCurrency());

                            // update accounts balances according to transaction amount
                            account.setAvailableAmount(account.getAvailableAmount().add(transaction.getAmount()));

                            // add transaction to DB
                            DBUtil.connectCommitAndClose(sess -> {
                                Account managedAccount = AccountDAO.update(sess, checkNotNull(account));

                                transaction.setDestinationAccount(managedAccount);
                                transaction.setCurrency(managedAccount.getCurrency());
                                TransactionDAO.save(sess, checkNotNull(transaction));

                            });
                            refreshContent();
                        }
                    } catch (Exception e) {
                        Alert alert = new Alert(Alert.AlertType.ERROR, "Error depositing: " + e, ButtonType.OK);
                        LOGGER.error("Error depositing: ", e);
                        alert.showAndWait();
                    }
                }
            );
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error depositing: " + e, ButtonType.OK);
            LOGGER.error("Error depositing: ", e);
            alert.showAndWait();
        }
    }

    public void withdraw() {
        // TODO: amount, external transaction details.
    }

    // TODO: update transaction details dialog
}
