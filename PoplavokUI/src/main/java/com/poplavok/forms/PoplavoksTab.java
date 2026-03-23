package com.poplavok.forms;

import com.flower.fxutils.ModalWindow;
import com.flower.fxutils.Refreshable;
import com.google.common.base.Preconditions;
import com.poplavok.data.dao.PoplavokDAO;
import com.poplavok.data.model.Poplavok;
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

    @Nullable FilteredList<Poplavok> poplavoks;

    @FXML @Nullable TableView<Poplavok> poplavoksTable;
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
        checkNotNull(showClosedCheckBox).selectedProperty().addListener((observable, oldValue, newValue) -> refreshContent());

        refreshContent();
    }

    @Override
    public void refreshContent() {
        try {
            Poplavok selectedPoplavok = null;
            if (poplavoksTable != null && poplavoksTable.getSelectionModel().getSelectedItem() != null) {
                selectedPoplavok = poplavoksTable.getSelectionModel().getSelectedItem();
            }

            // If checked (Show Closed) -> fetch ALL (Active + Closed)
            // If unchecked (Show Only Active) -> fetch isActive=true
            boolean showClosed = checkNotNull(showClosedCheckBox).isSelected();
            
            ObservableList<Poplavok> masterPoplavoks;
            if (showClosed) {
                 masterPoplavoks = FXCollections.observableList(
                    DBUtil.connectGetResultAndClose(PoplavokDAO::findAll));
            } else {
                 masterPoplavoks = FXCollections.observableList(
                    DBUtil.connectGetResultAndClose(sess -> PoplavokDAO.findByActive(sess, true)));
            }

            poplavoks = new FilteredList<>(masterPoplavoks);
            SortedList<Poplavok> sortablePoplavoks = new SortedList<>(poplavoks);

            poplavoks.setPredicate(createFilterPredicate());
            Preconditions.checkNotNull(poplavoksTable).itemsProperty().set(sortablePoplavoks);
            sortablePoplavoks.comparatorProperty().bind(poplavoksTable.comparatorProperty());
            poplavoksTable.refresh();

            if (selectedPoplavok != null) {
                for (Poplavok poplavok : poplavoksTable.getItems()) {
                    if (poplavok.getId().equals(selectedPoplavok.getId())) {
                        poplavoksTable.getSelectionModel().select(poplavok);
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

    private Predicate<Poplavok> createFilterPredicate() {
        String searchText = Preconditions.checkNotNull(filterTextField).textProperty().get();
        return poplavok -> {
            if (searchText == null || searchText.isEmpty()) return true;
            
            String lowerCaseFilter = searchText.toLowerCase().trim();
            
            if (poplavok.getName() != null && poplavok.getName().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            if (poplavok.getTickerSymbol().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
             if (poplavok.getStrategyStr().toLowerCase().contains(lowerCaseFilter)) {
                return true;
            }
            return false;
        };
    }

    public void filterTableView() {
        if (poplavoks != null) {
            poplavoks.setPredicate(createFilterPredicate());
        }
    }

    public void newPoplavok() {
        /*try {
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
        }*/
    }

    public void renamePoplavok() {
        /*try {
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
        }*/
    }

    // TODO: update transaction details dialog
}
