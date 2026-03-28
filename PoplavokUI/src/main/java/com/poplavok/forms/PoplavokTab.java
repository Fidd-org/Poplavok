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

        refreshContent();
    }

    private void updateLevelsSelection() {
        List<Level> selected = checkNotNull(levelsTable).getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
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

    public void editLevel() {}

    public void closeLevel() {}

    public void refreshPrice() {}

    public void closePoplavok() {}
}
