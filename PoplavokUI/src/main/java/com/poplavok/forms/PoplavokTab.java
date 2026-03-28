package com.poplavok.forms;

import com.flower.fxutils.ModalWindow;
import com.flower.fxutils.Refreshable;
import com.poplavok.data.dao.RateDAO;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.LevelState;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import javafx.collections.ListChangeListener;
import javafx.scene.control.SelectionMode;

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

    @FXML @Nullable TextField holdingBaseTextField;
    @FXML @Nullable TextField holdingQuoteTextField;
    @FXML @Nullable TextField oweBaseTextField;
    @FXML @Nullable TextField oweQuoteTextField;
    @FXML @Nullable TextField lentBaseTextField;
    @FXML @Nullable TextField lentQuoteTextField;
    @FXML @Nullable TextField marketValueBaseTextField;
    @FXML @Nullable TextField marketValueQuoteTextField;
    @FXML @Nullable TextField pnlTextField;
    @FXML @Nullable TextField averagingPriceTextField;

    @FXML @Nullable Button closeLevelButton;
    @FXML @Nullable CheckBox showClosedLevelsCheckBox;

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

        checkNotNull(levelsTable).getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        checkNotNull(levelsTable).getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Level> c) -> {
            updateLevelsSelection();
        });

        checkNotNull(showClosedLevelsCheckBox).selectedProperty().addListener((observable, oldValue, newValue) -> {
            refreshContent();
        });

        refreshContent();
    }

    protected boolean isEmpty(@Nullable BigDecimal amount) {
        return amount == null || amount.compareTo(BigDecimal.ZERO) == 0;
    }

    protected boolean isLevelEmpty(Level lvl) {
        return isEmpty(lvl.getAvailableAmountBase()) &&
                isEmpty(lvl.getAvailableAmountQuote()) &&
                isEmpty(lvl.getDebtBase()) &&
                isEmpty(lvl.getDebtQuote()) &&
                isEmpty(lvl.getLentAmountBase()) &&
                isEmpty(lvl.getLentAmountQuote());
    }

    protected void updateLevelsSelection() {
        List<Level> selected = checkNotNull(levelsTable).getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            if (closeLevelButton != null) {
                closeLevelButton.setDisable(true);
                closeLevelButton.setText("[ Close level ]");
            }
            if (holdingBaseTextField != null) holdingBaseTextField.setText("");
            if (holdingQuoteTextField != null) holdingQuoteTextField.setText("");
            if (oweBaseTextField != null) oweBaseTextField.setText("");
            if (oweQuoteTextField != null) oweQuoteTextField.setText("");
            if (lentBaseTextField != null) lentBaseTextField.setText("");
            if (lentQuoteTextField != null) lentQuoteTextField.setText("");
            if (marketValueBaseTextField != null) marketValueBaseTextField.setText("");
            if (marketValueQuoteTextField != null) marketValueQuoteTextField.setText("");
            if (pnlTextField != null) pnlTextField.setText("");
            if (averagingPriceTextField != null) averagingPriceTextField.setText("");
            return;
        }

        if (closeLevelButton != null) {
            if (selected.size() != 1) {
                closeLevelButton.setDisable(true);
                closeLevelButton.setText("[ Close level ]");
            } else {
                Level lvl = selected.get(0);
                LevelState state = lvl.getState();
                if (state == LevelState.INCEPTION) {
                    closeLevelButton.setDisable(false);
                    closeLevelButton.setText("[ Delete level ]");
                } else if (state == LevelState.FUNDING) {
                    closeLevelButton.setText("[ Delete level ]");
                    closeLevelButton.setDisable(false);
                } else if (state == LevelState.TRADING) {
                    closeLevelButton.setDisable(false);
                    closeLevelButton.setText("[ Close level ]");
                } else if (state == LevelState.CLOSED) {
                    closeLevelButton.setDisable(true);
                    closeLevelButton.setText("[ Close level ]");
                }
            }
        }

        BigDecimal holdingBase = BigDecimal.ZERO;
        BigDecimal holdingQuote = BigDecimal.ZERO;
        BigDecimal oweBase = BigDecimal.ZERO;
        BigDecimal oweQuote = BigDecimal.ZERO;
        BigDecimal lentBase = BigDecimal.ZERO;
        BigDecimal lentQuote = BigDecimal.ZERO;

        for (Level lvl : selected) {
            holdingBase = holdingBase.add(lvl.getAvailableAmountBase() != null ? lvl.getAvailableAmountBase() : BigDecimal.ZERO);
            holdingQuote = holdingQuote.add(lvl.getAvailableAmountQuote() != null ? lvl.getAvailableAmountQuote() : BigDecimal.ZERO);
            oweBase = oweBase.add(lvl.getDebtBase() != null ? lvl.getDebtBase() : BigDecimal.ZERO);
            oweQuote = oweQuote.add(lvl.getDebtQuote() != null ? lvl.getDebtQuote() : BigDecimal.ZERO);
            lentBase = lentBase.add(lvl.getLentAmountBase() != null ? lvl.getLentAmountBase() : BigDecimal.ZERO);
            lentQuote = lentQuote.add(lvl.getLentAmountQuote() != null ? lvl.getLentAmountQuote() : BigDecimal.ZERO);
        }

        if (holdingBaseTextField != null) holdingBaseTextField.setText(holdingBase.toPlainString());
        if (holdingQuoteTextField != null) holdingQuoteTextField.setText(holdingQuote.toPlainString());
        if (oweBaseTextField != null) oweBaseTextField.setText(oweBase.toPlainString());
        if (oweQuoteTextField != null) oweQuoteTextField.setText(oweQuote.toPlainString());
        if (lentBaseTextField != null) lentBaseTextField.setText(lentBase.toPlainString());
        if (lentQuoteTextField != null) lentQuoteTextField.setText(lentQuote.toPlainString());

        BigDecimal totalBase = holdingBase.add(lentBase).subtract(oweBase);
        BigDecimal totalQuote = holdingQuote.add(lentQuote).subtract(oweQuote);

        if (marketValueBaseTextField != null) marketValueBaseTextField.setText(totalBase.toPlainString());
        if (marketValueQuoteTextField != null) marketValueQuoteTextField.setText(totalQuote.toPlainString());

        BigDecimal currentPrice = BigDecimal.ZERO;
        if (priceTextField != null && priceTextField.getText() != null && !priceTextField.getText().trim().isEmpty()) {
            try {
                currentPrice = new BigDecimal(priceTextField.getText().trim());
            } catch (Exception e) {}
        }
        
        if (currentPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pnl = totalQuote.add(totalBase.multiply(currentPrice));
            if (pnlTextField != null) pnlTextField.setText(pnl.toPlainString());
        } else {
            if (pnlTextField != null) pnlTextField.setText("");
        }
        
        if (totalBase.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal avgPrice = totalQuote.negate().divide(totalBase, 8, java.math.RoundingMode.HALF_UP);
            if (averagingPriceTextField != null) averagingPriceTextField.setText(avgPrice.toPlainString());
        } else {
            if (averagingPriceTextField != null) averagingPriceTextField.setText("");
        }
    }

    @Override
    public void refreshContent() {
        List<Long> selectedLevelIds = new ArrayList<>();
        if (levelsTable != null && levelsTable.getSelectionModel() != null) {
            List<Level> selectedItems = levelsTable.getSelectionModel().getSelectedItems();
            if (selectedItems != null) {
                for (Level l : selectedItems) {
                    if (l != null && l.getId() != null) {
                        selectedLevelIds.add(l.getId());
                    }
                }
            }
        }

        try {
            DBUtil.connectCommitAndClose(sess -> {
                this.poplavok = PoplavokDAO.findById(sess, poplavokId)
                        .orElseThrow(() -> new RuntimeException("Poplavok not found"));
                this.poplavok.getTicker().getSymbol();
            });

            if (poplavok != null) {
                boolean showClosed = checkNotNull(showClosedLevelsCheckBox).isSelected();
                List<Level> levelList = DBUtil.connectGetResultAndClose(sess -> LevelDAO.findByPoplavokId(sess, poplavokId, showClosed));
                this.levels = new FilteredList<>(FXCollections.observableArrayList(levelList));
                checkNotNull(levelsTable).setItems(this.levels);

                if (!selectedLevelIds.isEmpty()) {
                    levelsTable.getSelectionModel().clearSelection();
                    for (Level l : this.levels) {
                        if (l != null && l.getId() != null && selectedLevelIds.contains(l.getId())) {
                            levelsTable.getSelectionModel().select(l);
                        }
                    }
                }
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
            updateLevelsSelection(); // Update stats in case something changed
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

    public void editLevel() {
        try {
            List<Level> selected = checkNotNull(levelsTable).getSelectionModel().getSelectedItems();
            if (selected == null || selected.size() != 1) return;

            Level lvl = selected.get(0);
            LevelState state = lvl.getState();

            if (state == LevelState.CLOSED) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "CLOSED level can't be edited.");
                alert.showAndWait();
                return;
            }

            LevelAddDialog levelAddDialog = new LevelAddDialog(lvl, checkNotNull(poplavok).getTicker().getSymbol(), lvl.getProjectedPrice());
            Stage workspaceStage = ModalWindow.showModal(checkNotNull(mainApp.mainStage),
                stage -> { levelAddDialog.setStage(stage); return levelAddDialog; },
                "Edit Level " + StringUtils.defaultIfBlank(lvl.getNotes(), "#" + lvl.getId()));

            workspaceStage.setOnHidden(
                ev -> {
                    try {
                        Level level = levelAddDialog.getReturnLevel();
                        if (level != null) {
                            level.setPoplavok(checkNotNull(poplavok));
                            DBUtil.connectCommitAndClose(sess -> LevelDAO.update(sess, checkNotNull(level)));
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
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error updating level: " + e, ButtonType.OK);
            LOGGER.error("Error updating level: ", e);
            alert.showAndWait();
        }
    }

    public void closeLevel() {
        try {
            List<Level> selected = checkNotNull(levelsTable).getSelectionModel().getSelectedItems();
            if (selected == null || selected.size() != 1) return;

            Level lvl = selected.get(0);
            LevelState state = lvl.getState();

            if (state == LevelState.INCEPTION || (state == LevelState.FUNDING && isLevelEmpty(lvl))) {
                DBUtil.connectCommitAndClose(sess -> LevelDAO.delete(sess, lvl));
            } else if (state == LevelState.TRADING && isLevelEmpty(lvl)) {
                lvl.setState(LevelState.CLOSED);
                DBUtil.connectCommitAndClose(sess -> LevelDAO.update(sess, lvl));
            } else if (!isLevelEmpty(lvl)) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Level is not empty. Can't close / delete.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Can't close / delete level.");
                alert.showAndWait();
            }
            refreshContent();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error closing level: " + e, ButtonType.OK);
            LOGGER.error("Error closing level: ", e);
            alert.showAndWait();
        }
    }

    public void refreshPrice() {}

    public void closePoplavok() {}
}
