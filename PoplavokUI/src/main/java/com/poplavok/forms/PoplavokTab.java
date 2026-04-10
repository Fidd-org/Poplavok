package com.poplavok.forms;

import com.flower.fxutils.ModalWindow;
import com.flower.fxutils.Refreshable;
import com.poplavok.data.dao.AccountDAO;
import com.poplavok.data.dao.RateDAO;
import com.poplavok.data.dao.TransactionDAO;
import com.poplavok.data.model.Account;
import com.poplavok.data.model.Currency;
import com.poplavok.data.model.Direction;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.LevelState;
import com.poplavok.data.model.LevelTrade;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.model.Poplavok;
import com.poplavok.data.model.Rate;
import com.poplavok.data.model.Trade;
import com.poplavok.data.model.Transaction;
import com.poplavok.data.dao.LevelDAO;
import com.poplavok.data.dao.PoplavokDAO;
import com.poplavok.data.dao.LoanDAO;
import com.poplavok.data.utils.BigDecimalUtil;
import com.poplavok.data.utils.DBUtil;
import com.poplavok.forms.wrapper.LevelTransaction;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
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

import static com.flower.fxutils.JavaFxUtils.autoResizeTableColumns;
import static com.flower.fxutils.JavaFxUtils.showErrorMessage;
import static com.flower.fxutils.JavaFxUtils.showMessage;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.model.LoanType.ACCOUNT_FUNDED;
import static com.poplavok.data.model.LoanType.EXTERNAL_CROSS_MARGIN;
import static com.poplavok.data.model.LoanType.EXTERNAL_ISOLATED_MARGIN;
import static com.poplavok.data.model.LoanType.POPLAVOK_FUNDED;
import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;

public class PoplavokTab extends AnchorPane implements Refreshable {
    final static Logger LOGGER = LoggerFactory.getLogger(PoplavokTab.class);

    @Nullable Poplavok poplavok;
    @Nullable FilteredList<Level> levels;
    @Nullable FilteredList<LevelTransaction> transactions;

    @FXML @Nullable TableView<Level> levelsTable;
    @FXML @Nullable TableView<LevelTransaction> transactionsTable;
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

    @FXML @Nullable RadioButton commsRadioButton;
    @FXML @Nullable RadioButton percentRadioButton;
    @FXML @Nullable TextField commsCountTextField;
    @FXML @Nullable Slider commsCountSlider;
    @FXML @Nullable TextField percentTextField;

    @FXML @Nullable TextField toSellTextField;
    @FXML @Nullable TextField sellPriceTextField;
    @FXML @Nullable TextField proceedsTextField;
    @FXML @Nullable TextField commissionTextField;
    @FXML @Nullable TextField profitTextField;
    @FXML @Nullable TextField profitPercentTextField;

    @FXML @Nullable CheckBox reserveCurrencyCheckBox;
    @FXML @Nullable CheckBox removeCommissionCheckBox;
    @FXML @Nullable CheckBox debtCurrencyCheckBox;
    @FXML @Nullable CheckBox holdingCurrencyCheckBox;
    @FXML @Nullable TextField debtCurrencyTextField;
    @FXML @Nullable TextField holdingCurrencyTextField;

    @FXML @Nullable Label debtCurrencyLabel;
    @FXML @Nullable Label availableCurrencyLabel;
    @FXML @Nullable Label toRepayCurrencyLabel;
    @FXML @Nullable Label holdingCurrencyLabel;

    @FXML @Nullable TextField debtTextField;
    @FXML @Nullable TextField availableTextField;
    @FXML @Nullable TextField toRepayTextField;
    @FXML @Nullable TextField holdingTextField;

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

        checkNotNull(commsRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                checkNotNull(commsCountTextField).setDisable(false);
                checkNotNull(commsCountSlider).setDisable(false);
                checkNotNull(percentTextField).setDisable(true);
            }
        });
        checkNotNull(percentRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                checkNotNull(percentTextField).setDisable(false);
                checkNotNull(commsCountTextField).setDisable(true);
                checkNotNull(commsCountSlider).setDisable(true);
            }
        });

        if (checkNotNull(commsRadioButton).isSelected()) {
            checkNotNull(commsCountTextField).setDisable(false);
            checkNotNull(commsCountSlider).setDisable(false);
            checkNotNull(percentTextField).setDisable(true);
        } else if (checkNotNull(percentRadioButton).isSelected()) {
            checkNotNull(percentTextField).setDisable(false);
            checkNotNull(commsCountTextField).setDisable(true);
            checkNotNull(commsCountSlider).setDisable(true);
        }

        checkNotNull(commsCountSlider).valueProperty().addListener((observable, oldValue, newValue) -> {
            checkNotNull(commsCountTextField).setText(String.valueOf(newValue.intValue()));
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

    protected void updateAverageTab(List<Level> selected) {
        if (selected == null || selected.isEmpty()) {
            return;
        }

        BigDecimal debt = BigDecimal.ZERO;
        BigDecimal available = BigDecimal.ZERO;
        BigDecimal holding = BigDecimal.ZERO;

        for (Level lvl : selected) {
            if (checkNotNull(poplavok).getDirection() == Direction.LONG) {
                debt = debt.add(nullToZero(lvl.getDebtQuote()));
                available = available.add(nullToZero(lvl.getAvailableAmountQuote()));
                holding = holding.add(nullToZero(lvl.getAvailableAmountBase()));
            } else {
                debt = debt.add(nullToZero(lvl.getDebtBase()));
                available = available.add(nullToZero(lvl.getAvailableAmountBase()));
                holding = holding.add(nullToZero(lvl.getAvailableAmountQuote()));
            }
        }
        BigDecimal toRepay = debt.subtract(available);

        checkNotNull(debtTextField).setText(formatAmount(debt));
        checkNotNull(availableTextField).setText(formatAmount(available));
        checkNotNull(toRepayTextField).setText(formatAmount(toRepay));
        checkNotNull(holdingTextField).setText(formatAmount(holding));

        // ------------------------

        BigDecimal fee = new BigDecimal(checkNotNull(feeTextField).textProperty().get());
        BigDecimal profitPercent;

        if (checkNotNull(commsRadioButton).selectedProperty().get()) {
            BigDecimal commsNumber = new BigDecimal(checkNotNull(commsCountTextField).textProperty().get());
            profitPercent = fee.multiply(commsNumber);
        } else if (checkNotNull(percentRadioButton).selectedProperty().get()) {
            profitPercent = new BigDecimal(checkNotNull(percentTextField).textProperty().get());
        } else {
            throw new RuntimeException("Unknown Profit Mode Selection");
        }

        checkNotNull(toSellTextField).textProperty().setValue(formatAmount(holding));

        if (checkNotNull(poplavok).getDirection() == Direction.LONG) {
            //
        } else {
            //
        }

        /*
        @FXML @Nullable TextField proceedsTextField;
        @FXML @Nullable TextField commissionTextField;
        @FXML @Nullable TextField profitTextField;
        @FXML @Nullable TextField profitPercentTextField;
        */

        /*
        @FXML @Nullable CheckBox reserveCurrencyCheckBox;
        @FXML @Nullable CheckBox removeCommissionCheckBox;
        @FXML @Nullable CheckBox debtCurrencyCheckBox;
        @FXML @Nullable CheckBox holdingCurrencyCheckBox;
        @FXML @Nullable TextField debtCurrencyTextField;
        @FXML @Nullable TextField holdingCurrencyTextField;
        */

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
            if (transactionsTable != null) transactionsTable.setItems(FXCollections.emptyObservableList());
            return;
        }

        updateAverageTab(selected);
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

        if (holdingBaseTextField != null) holdingBaseTextField.setText(formatAmount(holdingBase));
        if (holdingQuoteTextField != null) holdingQuoteTextField.setText(formatAmount(holdingQuote));
        if (oweBaseTextField != null) oweBaseTextField.setText(formatAmount(oweBase));
        if (oweQuoteTextField != null) oweQuoteTextField.setText(formatAmount(oweQuote));
        if (lentBaseTextField != null) lentBaseTextField.setText(formatAmount(lentBase));
        if (lentQuoteTextField != null) lentQuoteTextField.setText(formatAmount(lentQuote));

        BigDecimal totalBase = holdingBase.add(lentBase).subtract(oweBase);
        BigDecimal totalQuote = holdingQuote.add(lentQuote).subtract(oweQuote);

        if (marketValueBaseTextField != null) marketValueBaseTextField.setText(formatAmount(totalBase));
        if (marketValueQuoteTextField != null) marketValueQuoteTextField.setText(formatAmount(totalQuote));

        BigDecimal currentPrice = BigDecimal.ZERO;
        if (priceTextField != null && priceTextField.getText() != null && !priceTextField.getText().trim().isEmpty()) {
            try {
                currentPrice = new BigDecimal(priceTextField.getText().trim());
            } catch (Exception e) {}
        }
        
        if (currentPrice.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal pnl = totalQuote.add(totalBase.multiply(currentPrice));
            if (pnlTextField != null) pnlTextField.setText(formatAmount(pnl));
        } else {
            if (pnlTextField != null) pnlTextField.setText("");
        }
        
        if (totalBase.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal avgPrice = totalQuote.negate().divide(totalBase, 8, java.math.RoundingMode.HALF_UP);
            if (averagingPriceTextField != null) averagingPriceTextField.setText(formatAmount(avgPrice));
        } else {
            if (averagingPriceTextField != null) averagingPriceTextField.setText("");
        }

        if (transactionsTable != null && selected.size() == 1) {
            Level selectedLevel = selected.get(0);
            try {
                List<Transaction> levelTransactions = DBUtil.connectGetResultAndClose(sess ->
                    TransactionDAO.findByLevel(sess, selectedLevel.getId())
                );
                this.transactions = new FilteredList<>(FXCollections.observableArrayList(
                        levelTransactions.stream().map(t -> new LevelTransaction(t, selectedLevel)).toList()));
                transactionsTable.setItems(this.transactions);
                autoResizeTableColumns(transactionsTable);
            } catch (Exception e) {
                LOGGER.error("Error loading transactions for level", e);
            }
        } else if (transactionsTable != null) {
            transactionsTable.setItems(FXCollections.emptyObservableList());
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

            String debtCurrency = checkNotNull(poplavok).getDirection() == Direction.LONG
                    ? poplavok.getTicker().getQuote().getCurrency() : poplavok.getTicker().getBase().getCurrency();
            String holdCurrency = checkNotNull(poplavok).getDirection() == Direction.LONG
                    ? poplavok.getTicker().getBase().getCurrency() : poplavok.getTicker().getQuote().getCurrency();

            checkNotNull(debtCurrencyLabel).textProperty().setValue(debtCurrency);
            checkNotNull(availableCurrencyLabel).textProperty().setValue(debtCurrency);
            checkNotNull(toRepayCurrencyLabel).textProperty().setValue(debtCurrency);
            checkNotNull(holdingCurrencyLabel).textProperty().setValue(holdCurrency);

            boolean showClosed = checkNotNull(showClosedLevelsCheckBox).isSelected();
            List<Level> levelList = DBUtil.connectGetResultAndClose(sess -> LevelDAO.findByPoplavokId(sess, poplavokId, showClosed));
            this.levels = new FilteredList<>(FXCollections.observableArrayList(levelList));
            checkNotNull(levelsTable).setItems(this.levels);
            autoResizeTableColumns(levelsTable);

            if (!selectedLevelIds.isEmpty()) {
                levelsTable.getSelectionModel().clearSelection();
                for (Level l : this.levels) {
                    if (l != null && l.getId() != null && selectedLevelIds.contains(l.getId())) {
                        levelsTable.getSelectionModel().select(l);
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
                    checkNotNull(priceTextField).setText(formatAmount(rate.getPrice()));
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
            BigDecimal price = null;
            try {
                price = new BigDecimal(checkNotNull(priceTextField).textProperty().get());
            } catch (Exception e) {}
            BigDecimal fee = null;
            try {
                fee = new BigDecimal(checkNotNull(feeTextField).textProperty().get());
            } catch (Exception e) {}
            LevelAddDialog levelAddDialog = new LevelAddDialog(null, checkNotNull(poplavok).getTicker().getSymbol(), price, fee, checkNotNull(poplavok.getDirection()));
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

            BigDecimal fee = null;
            try {
                fee = new BigDecimal(checkNotNull(feeTextField).textProperty().get());
            } catch (Exception e) {}
            LevelAddDialog levelAddDialog = new LevelAddDialog(lvl, checkNotNull(poplavok).getTicker().getSymbol(), lvl.getProjectedPrice(), fee, checkNotNull(poplavok.getDirection()));
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

            if (nullToZero(lvl.getAvailableAmountBase()).compareTo(BigDecimal.ZERO) != 0 ||
                nullToZero(lvl.getAvailableAmountQuote()).compareTo(BigDecimal.ZERO) != 0 ||
                nullToZero(lvl.getLentAmountBase()).compareTo(BigDecimal.ZERO) != 0 ||
                nullToZero(lvl.getLentAmountQuote()).compareTo(BigDecimal.ZERO) != 0 ||
                nullToZero(lvl.getDebtBase()).compareTo(BigDecimal.ZERO) != 0 ||
                nullToZero(lvl.getDebtQuote()).compareTo(BigDecimal.ZERO) != 0) {
                showErrorMessage("Can't close / delete level with non-zero holding, lent amount or debt.");
                return;
            }

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

    public void performTrade() {
        try {
            List<Level> selected = checkNotNull(levelsTable).getSelectionModel().getSelectedItems();
            if (selected == null || selected.size() != 1) return;

            Level lvl = selected.get(0);
            LevelState state = lvl.getState();

            if (state != LevelState.TRADING && state != LevelState.FUNDING) {
                showErrorMessage("Levels in " + state + " state can't perform trades.");
                return;
            }

            BigDecimal price = BigDecimalUtil.fromString(checkNotNull(priceTextField).textProperty().get());
            price = price == null ? lvl.getProjectedPrice() : price;
            PerformTradeDialog performTradeDialog = new PerformTradeDialog(lvl, checkNotNull(poplavok).getTicker(), price);
            Stage workspaceStage = ModalWindow.showModal(checkNotNull(mainApp.mainStage),
                    stage -> { performTradeDialog.setStage(stage); return performTradeDialog; },
                    "Perform Trade");

            workspaceStage.setOnHidden(
                    ev -> {
                        try {
                            Trade trade = performTradeDialog.getReturnTrade();
                            if (trade != null) {
                                BigDecimal baseIn = nullToZero(trade.getAmountBaseIn());
                                BigDecimal quoteIn = nullToZero(trade.getAmountQuoteIn());
                                BigDecimal baseOut = nullToZero(trade.getAmountBaseOut());
                                BigDecimal quoteOut = nullToZero(trade.getAmountQuoteOut());
                                BigDecimal baseCommission = nullToZero(trade.getCommissionBase());
                                BigDecimal quoteCommission = nullToZero(trade.getCommissionQuote());

                                BigDecimal lvlBase = nullToZero(lvl.getAvailableAmountBase());
                                BigDecimal lvlQuote = nullToZero(lvl.getAvailableAmountQuote());

                                if (lvlBase.compareTo(baseIn) < 0) {
                                    throw new RuntimeException("Not enough Base available in level for this trade");
                                }

                                if (lvlQuote.compareTo(quoteIn) < 0) {
                                    throw new RuntimeException("Not enough Quote available in level for this trade");
                                }

                                lvl.setState(LevelState.TRADING);

                                lvlBase = lvlBase.subtract(baseIn).add(baseOut);
                                lvlQuote = lvlQuote.subtract(quoteIn).add(quoteOut);

                                lvl.setAvailableAmountBase(lvlBase);
                                lvl.setAvailableAmountQuote(lvlQuote);

                                LevelTrade levelTrade = new LevelTrade();
                                levelTrade.setLevel(lvl);
                                levelTrade.setTrade(trade);

                                DBUtil.connectCommitAndClose(sess -> {
                                    sess.persist(trade);
                                    sess.persist(levelTrade);
                                    LevelDAO.update(sess, lvl);
                                });

                                refreshContent();
                            }
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Error performing trade: " + e, ButtonType.OK);
                            LOGGER.error("Error performing trade: ", e);
                            alert.showAndWait();
                        }
                    }
            );
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error performing trade: " + e, ButtonType.OK);
            LOGGER.error("Error performing trade: ", e);
            alert.showAndWait();
        }
    }

    public void allocateFunds() {
        try {
            List<Level> selected = checkNotNull(levelsTable).getSelectionModel().getSelectedItems();
            if (selected == null || selected.isEmpty()) {
                showMessage("Choose a level to allocate funds.");
                return;
            }

            if (selected.size() != 1) {
                showMessage("Choose one level to allocate funds (not many levels)");
                return;
            }

            Level lvl = selected.get(0);
            LevelState state = lvl.getState();

            if (state == LevelState.CLOSED) {
                showMessage("Funds can't be allocated for CLOSED levels.");
                return;
            }

            Currency loanCurrency;
            BigDecimal defaultAmount;
            Direction poplavokDirection = checkNotNull(poplavok).getDirection();
            if (poplavokDirection == Direction.LONG) {
                loanCurrency = checkNotNull(poplavok).getTicker().getQuote();
                defaultAmount = lvl.getProjectedAmountQuote();
                defaultAmount = defaultAmount == null ? BigDecimal.ZERO : defaultAmount;

                // Debt assumes funds already allocated
                if (lvl.getDebtQuote() != null) {
                    if (defaultAmount.compareTo(lvl.getDebtQuote()) > 0) {
                        defaultAmount = defaultAmount.subtract(lvl.getDebtQuote());
                    } else {
                        defaultAmount = BigDecimal.ZERO;
                    }
                }
            } else {
                loanCurrency = checkNotNull(poplavok).getTicker().getBase();
                defaultAmount = lvl.getProjectedAmountBase();
                defaultAmount = defaultAmount == null ? BigDecimal.ZERO : defaultAmount;

                // Debt assumes funds already allocated
                if (lvl.getDebtBase() != null) {
                    if (defaultAmount.compareTo(lvl.getDebtBase()) > 0) {
                        defaultAmount = defaultAmount.subtract(lvl.getDebtBase());
                    } else {
                        defaultAmount = BigDecimal.ZERO;
                    }
                }
            }

            AllocateFundsDialog allocateFundsDialog = new AllocateFundsDialog(lvl, loanCurrency, defaultAmount);
            Stage workspaceStage = ModalWindow.showModal(checkNotNull(mainApp.mainStage),
                    stage -> { allocateFundsDialog.setStage(stage); return allocateFundsDialog; },
                    "Allocate Funds");

            workspaceStage.setOnHidden(
                    ev -> {
                        try {
                            Loan loan = allocateFundsDialog.getReturnLoan();
                            if (loan != null) {
                                if (loan.getLoanType() != ACCOUNT_FUNDED && loan.getLoanType() != POPLAVOK_FUNDED
                                        && loan.getLoanType() != EXTERNAL_CROSS_MARGIN && loan.getLoanType() != EXTERNAL_ISOLATED_MARGIN) {
                                    showMessage("Unsupported loan type: " + loan.getLoanType());
                                    return;
                                }

                                // Add loan funds to our level
                                if (poplavokDirection == Direction.LONG) {
                                    lvl.setAvailableAmountQuote(lvl.getAvailableAmountQuote() != null ? lvl.getAvailableAmountQuote().add(loan.getAmount()) : loan.getAmount());
                                    lvl.setDebtQuote(lvl.getDebtQuote() != null ? lvl.getDebtQuote().add(loan.getAmount()) : loan.getAmount());
                                } else {
                                    lvl.setAvailableAmountBase(lvl.getAvailableAmountBase() != null ? lvl.getAvailableAmountBase().add(loan.getAmount()) : loan.getAmount());
                                    lvl.setDebtBase(lvl.getDebtBase() != null ? lvl.getDebtBase().add(loan.getAmount()) : loan.getAmount());
                                }

                                if (lvl.getState() == LevelState.INCEPTION) {
                                    lvl.setState(LevelState.FUNDING);
                                }

                                if (loan.getLoanType() == ACCOUNT_FUNDED) {
                                    // Remove loan funds from source account
                                    Account sourceAccount = checkNotNull(loan.getSourceAccount());
                                    sourceAccount.setAvailableAmount(loan.getSourceAccount().getAvailableAmount().subtract(loan.getAmount()));
                                    sourceAccount.setLentAmount(sourceAccount.getLentAmount() != null ? sourceAccount.getLentAmount().add(loan.getAmount()) : loan.getAmount());

                                    // save new loan, update level and source account
                                    DBUtil.connectCommitAndClose(sess -> {
                                        AccountDAO.update(sess, sourceAccount);
                                        LoanDAO.save(sess, loan);
                                        LevelDAO.update(sess, lvl);
                                    });
                                }
                                if (loan.getLoanType() == POPLAVOK_FUNDED) {
                                    Level sourceLevel = checkNotNull(loan.getSourceLevel());
                                    Poplavok sourcePoplavok = sourceLevel.getPoplavok();
                                    Currency sourceBase = sourcePoplavok.getTicker().getBase();
                                    Currency sourceQuote = sourcePoplavok.getTicker().getQuote();

                                    if (loanCurrency.getCurrency().equals(sourceBase.getCurrency())) {
                                        sourceLevel.setAvailableAmountBase(checkNotNull(sourceLevel.getAvailableAmountBase()).subtract(loan.getAmount()));
                                        sourceLevel.setLentAmountBase(nullToZero(sourceLevel.getLentAmountBase()).add(loan.getAmount()));
                                    } else if (loanCurrency.getCurrency().equals(sourceQuote.getCurrency())) {
                                        sourceLevel.setAvailableAmountQuote(checkNotNull(sourceLevel.getAvailableAmountQuote()).subtract(loan.getAmount()));
                                        sourceLevel.setLentAmountQuote(nullToZero(sourceLevel.getLentAmountQuote()).add(loan.getAmount()));
                                    } else {
                                        showMessage("Source level currency doesn't match loan currency");
                                        return;
                                    }
                                    DBUtil.connectCommitAndClose(sess -> {
                                        LevelDAO.update(sess, sourceLevel);
                                        LoanDAO.save(sess, loan);
                                        LevelDAO.update(sess, lvl);
                                    });
                                }
                                if (loan.getLoanType() == EXTERNAL_CROSS_MARGIN || loan.getLoanType() == EXTERNAL_ISOLATED_MARGIN) {
                                    DBUtil.connectCommitAndClose(sess -> {
                                        LoanDAO.save(sess, loan);
                                        LevelDAO.update(sess, lvl);
                                    });
                                }

                                refreshContent();
                            }
                        } catch (Exception e) {
                            Alert alert = new Alert(Alert.AlertType.ERROR, "Error allocating funds: " + e, ButtonType.OK);
                            LOGGER.error("Error allocating funds: ", e);
                            alert.showAndWait();
                        }
                    }
            );
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error allocating funds: " + e, ButtonType.OK);
            LOGGER.error("Error allocating funds: ", e);
            alert.showAndWait();
        }
    }
}
