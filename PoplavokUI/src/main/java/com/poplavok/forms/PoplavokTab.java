package com.poplavok.forms;

import com.flower.fxutils.ModalWindow;
import com.flower.fxutils.Refreshable;
import com.poplavok.data.dao.RateDAO;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.model.Poplavok;
import com.poplavok.data.model.Rate;
import com.poplavok.data.model.Trade;
import com.poplavok.data.model.Transaction;
import com.poplavok.data.dao.LevelDAO;
import com.poplavok.data.dao.PoplavokDAO;
import com.poplavok.data.utils.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class PoplavokTab extends AnchorPane implements Refreshable {
    final static Logger LOGGER = LoggerFactory.getLogger(PoplavokTab.class);

    @Nullable Poplavok poplavok;
    @Nullable FilteredList<Level> levels;
    @Nullable FilteredList<Transaction> transactions;

    @FXML @Nullable TableView<Level> levelsTable;
    @FXML @Nullable TableView<Transaction> transactionsTable;
    @FXML @Nullable TableView<Trade> tradesTable;

    @FXML @Nullable TextField tickerTextField;
    @FXML @Nullable TextField feeTextField;
    @FXML @Nullable TextField priceTextField;

    @FXML @Nullable TextField directionTextBox;

    protected final MainForm mainApp;
    protected final Long poplavokId;

    public PoplavokTab(MainForm mainApp, Long poplavokId) {
        this.mainApp = mainApp;
        this.poplavokId = poplavokId;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PoplavokTab.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        refreshContent();
    }

    @Override
    public void refreshContent() {
        try {
            DBUtil.connectCommitAndClose(sess -> {
                this.poplavok = PoplavokDAO.findById(sess, poplavokId)
                        .orElseThrow(() -> new RuntimeException("Poplavok not found"));

                // Initialize lazy collections
                List<Level> levels = this.poplavok.getLevels();
                if (levels != null) { int size = levels.size(); }
                this.poplavok.getTicker().getSymbol();
            });

            if (levelsTable != null && poplavok != null) {
                this.levels = new FilteredList<>(FXCollections.observableArrayList(poplavok.getLevels()));
                levelsTable.setItems(this.levels);
            }
            MarketTicker ticker = checkNotNull(poplavok).getTicker();
            if (ticker != null) {
                checkNotNull(tickerTextField).setText(poplavok.getTicker().getSymbol());
                checkNotNull(feeTextField).setText(poplavok.getTicker().getMakerFeeRate());
                checkNotNull(directionTextBox).setText(checkNotNull(poplavok.getDirection()).name());
                Rate rate = RateDAO.getLatestRateForTicker(ticker.getId());
                if (rate != null && rate.getPrice() != null) {
                    checkNotNull(priceTextField).setText(rate.getPrice().toPlainString());
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error refreshing content: ", e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error loading Poplavok: " + e, ButtonType.OK);
            alert.showAndWait();
        }
    }

    public void newLevel() {
        try {
            LevelAddDialog levelAddDialog = new LevelAddDialog(null, checkNotNull(poplavok).getTicker().getSymbol(), null);
            Stage workspaceStage = ModalWindow.showModal(checkNotNull(mainApp.mainStage),
                    stage -> { levelAddDialog.setStage(stage); return levelAddDialog; },
                    "New Level");

            workspaceStage.setOnHidden(
                    ev -> {
                        try {
                            Level level = levelAddDialog.getReturnLevel();
                            if (level != null) {
                                level.setPoplavok(checkNotNull(poplavok));
                                DBUtil.connectCommitAndClose(sess -> LevelDAO.save(sess, checkNotNull(level)));
                                refreshContent();
                            }
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating level: " + e, ButtonType.OK);
                            LOGGER.error("Error creating level: ", e);
                            alert.showAndWait();
                        }
                    }
            );
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating level: " + e, ButtonType.OK);
            LOGGER.error("Error creating level: ", e);
            alert.showAndWait();
        }
    }

    public void editLevel() {}

    public void closeLevel() {}

    public void refreshPrice() {}

    public void closePoplavok() {}
}
