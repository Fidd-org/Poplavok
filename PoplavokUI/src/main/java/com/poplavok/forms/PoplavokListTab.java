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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.function.Predicate;

import static com.flower.fxutils.JavaFxUtils.autoResizeTableColumns;
import static com.google.common.base.Preconditions.checkNotNull;

public class PoplavokListTab extends AnchorPane implements Refreshable {
    final static Logger LOGGER = LoggerFactory.getLogger(PoplavokListTab.class);

    @Nullable FilteredList<Poplavok> poplavoks;

    @FXML @Nullable TableView<Poplavok> poplavoksTable;
    @FXML @Nullable TextField filterTextField;
    @FXML @Nullable CheckBox showClosedCheckBox;

    protected final MainForm mainApp;

    public PoplavokListTab(MainForm mainApp) {
        this.mainApp = mainApp;
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PoplavokListTab.fxml"));
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
            autoResizeTableColumns(poplavoksTable);

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
        try {
            PoplavokAddDialog poplavokAddDialog = new PoplavokAddDialog(null);
            Stage workspaceStage = ModalWindow.showModal(checkNotNull(mainApp.mainStage),
                    stage -> { poplavokAddDialog.setStage(stage); return poplavokAddDialog; },
                    "New Poplavok");

            workspaceStage.setOnHidden(
                    ev -> {
                        try {
                            Poplavok poplavok = poplavokAddDialog.getReturnPoplavok();
                            if (poplavok != null) {
                                DBUtil.connectCommitAndClose(sess -> PoplavokDAO.save(sess, checkNotNull(poplavok)));
                                refreshContent();
                            }
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating poplavok: " + e, ButtonType.OK);
                            LOGGER.error("Error creating poplavok: ", e);
                            alert.showAndWait();
                        }
                    }
            );
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating poplavok: " + e, ButtonType.OK);
            LOGGER.error("Error creating poplavok: ", e);
            alert.showAndWait();
        }
    }

    public void renamePoplavok() {
        try {
            Poplavok origPoplavok = Preconditions.checkNotNull(poplavoksTable).getSelectionModel().getSelectedItem();
            if (origPoplavok == null) { return; }

            PoplavokAddDialog poplavokAddDialog = new PoplavokAddDialog(origPoplavok);
            Stage workspaceStage = ModalWindow.showModal(checkNotNull(mainApp.mainStage),
                    stage -> { poplavokAddDialog.setStage(stage); return poplavokAddDialog; },
                    "Rename Poplavok");

            workspaceStage.setOnHidden(
                    ev -> {
                        try {
                            Poplavok poplavok = poplavokAddDialog.getReturnPoplavok();
                            if (poplavok != null) {
                                DBUtil.connectCommitAndClose(sess -> PoplavokDAO.update(sess, checkNotNull(poplavok)));
                                refreshContent();
                            }
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Error renaming poplavok: " + e, ButtonType.OK);
                            LOGGER.error("Error renaming poplavok: ", e);
                            alert.showAndWait();
                        }
                    }
            );
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error renaming poplavok: " + e, ButtonType.OK);
            LOGGER.error("Error renaming poplavok: ", e);
            alert.showAndWait();
        }
    }

    public void openPoplavok() {
        try {
            Poplavok origPoplavok = Preconditions.checkNotNull(poplavoksTable).getSelectionModel().getSelectedItem();
            if (origPoplavok == null) { return; }

            mainApp.openPoplavokTab(origPoplavok.getId(), origPoplavok.getName());
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error opening poplavok: " + e, ButtonType.OK);
            LOGGER.error("Error opening poplavok: ", e);
            alert.showAndWait();
        }
    }

    public void poplavokDoubleClick(MouseEvent event) {
        if (event.getClickCount() == 2) {
            openPoplavok();
        }
    }
}
