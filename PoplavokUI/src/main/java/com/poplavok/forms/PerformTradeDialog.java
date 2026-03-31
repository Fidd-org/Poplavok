package com.poplavok.forms;

import com.flower.fxutils.JavaFxUtils;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.Trade;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.model.TradeOperation;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;

public class PerformTradeDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(PerformTradeDialog.class);
    final static int SCALE = 6;

    @FXML @Nullable TextField priceTextField;
    @FXML @Nullable Label tickerLabel;
    @FXML @Nullable TextField availableBaseTextField;
    @FXML @Nullable TextField availableQuoteTextField;
    @FXML @Nullable Button useAllBaseButton;
    @FXML @Nullable Button useAllQuoteButton;
    @FXML @Nullable TextField feeTextField;
    @FXML @Nullable Label quoteBuyLabel;
    @FXML @Nullable Label baseBuyLabel;
    @FXML @Nullable Label baseSellLabel;
    @FXML @Nullable Label quoteSellLabel;
    @FXML @Nullable Button buyButton;
    @FXML @Nullable Button sellButton;

    @FXML @Nullable CheckBox manualEntryCheckBox;
    @FXML @Nullable Tab buyTab;
    @FXML @Nullable Tab sellTab;
    @FXML @Nullable TextField giveQuoteBuyTextField;
    @FXML @Nullable TextField getBaseBuyTextField;
    @FXML @Nullable TextField giveBaseSellTextField;
    @FXML @Nullable TextField getQuoteSellTextField;

    @FXML @Nullable TabPane tradeTabPane;

    @Nullable Stage stage;
    @Nullable Trade returnTrade;

    final Level lvl;
    final MarketTicker ticker;

    public PerformTradeDialog(Level lvl, MarketTicker ticker, @Nullable BigDecimal price) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PerformTradeDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.lvl = lvl;
        this.ticker = ticker;

        checkNotNull(tickerLabel).textProperty().setValue(ticker.getSymbol());
        checkNotNull(priceTextField).textProperty().setValue(price != null ? price.toPlainString() : "");

        checkNotNull(availableBaseTextField).textProperty().setValue(formatAmount(lvl.getAvailableAmountBase()));
        checkNotNull(availableQuoteTextField).textProperty().setValue(formatAmount(lvl.getAvailableAmountQuote()));
        checkNotNull(useAllBaseButton).textProperty().setValue(ticker.getBase().getCurrency());
        checkNotNull(useAllQuoteButton).textProperty().setValue(ticker.getQuote().getCurrency());
        BigDecimal fee = new BigDecimal(checkNotNull(ticker.getMakerFeeRate())).multiply(new BigDecimal(checkNotNull(ticker.getMakerCoefficient())));
        checkNotNull(feeTextField).textProperty().setValue(formatAmount(fee));
        checkNotNull(quoteBuyLabel).textProperty().setValue(ticker.getQuote().getCurrency());
        checkNotNull(baseBuyLabel).textProperty().setValue(ticker.getBase().getCurrency());
        checkNotNull(baseSellLabel).textProperty().setValue(ticker.getBase().getCurrency());
        checkNotNull(quoteSellLabel).textProperty().setValue(ticker.getQuote().getCurrency());
        checkNotNull(buyButton).textProperty().setValue("BUY " + ticker.getBase().getCurrency());
        checkNotNull(sellButton).textProperty().setValue("SELL " + ticker.getBase().getCurrency());
        
        checkNotNull(priceTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        checkNotNull(availableBaseTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        checkNotNull(availableQuoteTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        checkNotNull(giveQuoteBuyTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        checkNotNull(getBaseBuyTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        checkNotNull(giveBaseSellTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        checkNotNull(getQuoteSellTextField).setTextFormatter(JavaFxUtils.createDecimalTextFormatter());
        
        checkNotNull(priceTextField).textProperty().addListener(this::onPriceChanged);
        checkNotNull(feeTextField).textProperty().addListener(this::onFeeChanged);

        checkNotNull(manualEntryCheckBox).selectedProperty().addListener(this::onManualEntryCheckedChanged);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void buy() {
        try {
            returnTrade = new Trade();

            BigDecimal price = new BigDecimal(checkNotNull(priceTextField).getText());
            BigDecimal fee = new BigDecimal(checkNotNull(feeTextField).getText());
            BigDecimal tradeQuoteAmount = new BigDecimal(checkNotNull(giveQuoteBuyTextField).getText());
            BigDecimal tradeBaseAmount = new BigDecimal(checkNotNull(getBaseBuyTextField).getText());
            returnTrade.setAmountQuoteIn(tradeQuoteAmount);
            returnTrade.setAmountBaseOut(tradeBaseAmount);
            returnTrade.setOperation(TradeOperation.BUY);
            returnTrade.setDate(new Date());

            returnTrade.setCommissionQuote(tradeQuoteAmount.multiply(fee));
            returnTrade.setCommissionBase(tradeQuoteAmount.multiply(fee).divide(price, RoundingMode.UP));

            checkNotNull(stage).close();
        } catch (Exception e) {
            LOGGER.error("Buy Error:", e);
            JavaFxUtils.showErrorMessage("Buy Error: " + e);
        }
    }

    public void sell() {
        try {
            returnTrade = new Trade();

            BigDecimal price = new BigDecimal(checkNotNull(priceTextField).getText());
            BigDecimal fee = new BigDecimal(checkNotNull(feeTextField).getText());

            BigDecimal tradeBaseAmount = new BigDecimal(checkNotNull(giveBaseSellTextField).getText());
            BigDecimal tradeQuoteAmount = new BigDecimal(checkNotNull(getQuoteSellTextField).getText());
            returnTrade.setAmountBaseIn(tradeBaseAmount);
            returnTrade.setAmountQuoteOut(tradeQuoteAmount);
            returnTrade.setOperation(TradeOperation.SELL);
            returnTrade.setDate(new Date());

            returnTrade.setCommissionBase(tradeBaseAmount.multiply(fee));
            returnTrade.setCommissionQuote(tradeBaseAmount.multiply(fee).multiply(price));

            checkNotNull(stage).close();
       } catch (Exception e) {
            LOGGER.error("Sell Error:", e);
            JavaFxUtils.showErrorMessage("Sell Error: " + e);
        }
    }

    public void onPriceChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        priceFeeUpdate(true, true);
    }

    public void onFeeChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        priceFeeUpdate(true, true);
    }

    public void onManualEntryCheckedChanged(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        priceFeeUpdate(true, true);
    }

    /** Update quote sell, base buy, if manual input is not checked */
    protected void priceFeeUpdate(boolean updateBaseBuy, boolean updateQuoteSell) {
        try {
            boolean manualInput = checkNotNull(manualEntryCheckBox).isSelected();
            if (!manualInput) {
                if (StringUtils.isBlank(checkNotNull(priceTextField).getText())) { return; }
                if (StringUtils.isBlank(checkNotNull(feeTextField).getText())) { return; }
                BigDecimal price = new BigDecimal(checkNotNull(priceTextField).getText()).setScale(SCALE);
                BigDecimal fee = new BigDecimal(checkNotNull(feeTextField).getText()).setScale(SCALE);

                if (price.compareTo(BigDecimal.ZERO) == 0) { return; }

                // Update GET BASE BUY amount
                if (updateBaseBuy && !StringUtils.isBlank(checkNotNull(giveQuoteBuyTextField).getText())) {
                    BigDecimal giveQuoteBuy = new BigDecimal(checkNotNull(giveQuoteBuyTextField).getText()).setScale(SCALE);
                    BigDecimal getBaseBuy =
                            giveQuoteBuy
                                    .multiply(BigDecimal.ONE.subtract(fee))
                                    .divide(price, RoundingMode.DOWN);
                    checkNotNull(getBaseBuyTextField).setText(formatAmount(getBaseBuy));
                }

                // Update GET QUOTE SELL amount
                if (updateQuoteSell && !StringUtils.isBlank(checkNotNull(giveBaseSellTextField).getText())) {
                    BigDecimal giveBaseSell = new BigDecimal(checkNotNull(giveBaseSellTextField).getText()).setScale(SCALE);
                    BigDecimal getQuoteSell =
                            giveBaseSell
                                    .multiply(price)
                                    .multiply(BigDecimal.ONE.subtract(fee));
                    checkNotNull(getQuoteSellTextField).setText(formatAmount(getQuoteSell));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on Recalc:", e);
            JavaFxUtils.showErrorMessage("Error on Recalc: " + e);
        }
    }

    public void useAllBase() {
        try {
            if (!checkNotNull(tradeTabPane).getSelectionModel().getSelectedItem().equals(sellTab)) {
                JavaFxUtils.showErrorMessage("Select Sell tab to use all BASE");
                return;
            }

            if (!StringUtils.isBlank(checkNotNull(availableBaseTextField).getText())) {
                BigDecimal availableBase = new BigDecimal(checkNotNull(availableBaseTextField).getText());
                checkNotNull(giveBaseSellTextField).textProperty().setValue(formatAmount(availableBase));
                onGiveBaseSellChanged(null);
            }
        } catch (Exception e) {
            LOGGER.error("Error on useAllBase:", e);
            JavaFxUtils.showErrorMessage("Error on useAllBase: " + e);
        }
    }

    public void useAllQuote() {
        try {
            if (!checkNotNull(tradeTabPane).getSelectionModel().getSelectedItem().equals(buyTab)) {
                JavaFxUtils.showErrorMessage("Select Buy tab to use all QUOTE");
                return;
            }

            if (!StringUtils.isBlank(checkNotNull(availableQuoteTextField).getText())) {
                BigDecimal availableQuote = new BigDecimal(checkNotNull(availableQuoteTextField).getText());
                checkNotNull(giveQuoteBuyTextField).textProperty().setValue(formatAmount(availableQuote));
                onGiveQuoteBuyChanged(null);
            }
        } catch (Exception e) {
            LOGGER.error("Error on useAllQuote:", e);
            JavaFxUtils.showErrorMessage("Error on useAllQuote: " + e);
        }
    }

    private boolean isInvalidAmountChar(@Nullable KeyEvent event) {
        if (event == null || event.getCharacter() == null || event.getCharacter().isEmpty()) {
            return false;
        }
        char c = event.getCharacter().charAt(0);
        return !(c >= '0' && c <= '9' || c == '.' || c == '\b' || c == '\u007F');
    }

    public void onGiveQuoteBuyChanged(@Nullable KeyEvent event) {
        if (event != null && isInvalidAmountChar(event)) return;
        priceFeeUpdate(true, false);
    }

    public void onGiveBaseSellChanged(@Nullable KeyEvent event) {
        if (event != null && isInvalidAmountChar(event)) return;
        priceFeeUpdate(false, true);
    }

    public void onGetBaseBuyChanged(@Nullable KeyEvent event) {
        if (isInvalidAmountChar(event)) return;
        reversePriceFeeUpdate(true, false);
    }

    public void onGetQuoteSellChanged(@Nullable KeyEvent event) {
        if (isInvalidAmountChar(event)) return;
        reversePriceFeeUpdate(false, true);
    }

    protected void reversePriceFeeUpdate(boolean updateQuoteBuy, boolean updateBaseSell) {
        try {
            boolean manualInput = checkNotNull(manualEntryCheckBox).isSelected();
            if (!manualInput) {
                if (StringUtils.isBlank(checkNotNull(priceTextField).getText())) { return; }
                if (StringUtils.isBlank(checkNotNull(feeTextField).getText())) { return; }
                BigDecimal price = new BigDecimal(checkNotNull(priceTextField).getText()).setScale(SCALE);
                BigDecimal fee = new BigDecimal(checkNotNull(feeTextField).getText()).setScale(SCALE);

                // Update GET BASE BUY amount
                if (updateQuoteBuy && !StringUtils.isBlank(checkNotNull(getBaseBuyTextField).getText())) {
                    BigDecimal getBaseBuy = new BigDecimal(checkNotNull(getBaseBuyTextField).getText()).setScale(SCALE);
                    BigDecimal giveQuoteBuy =
                            getBaseBuy
                                    .multiply(price)
                                    .divide(BigDecimal.ONE.subtract(fee), RoundingMode.UP);
                    checkNotNull(giveQuoteBuyTextField).setText(formatAmount(giveQuoteBuy ));
                }

                // Update GET QUOTE SELL amount
                if (updateBaseSell && !StringUtils.isBlank(checkNotNull(getQuoteSellTextField).getText())) {
                    BigDecimal getQuoteSell = new BigDecimal(checkNotNull(getQuoteSellTextField).getText()).setScale(SCALE);
                    BigDecimal giveBaseSell =
                            getQuoteSell
                                    .divide(
                                    BigDecimal.ONE.subtract(fee)
                                        .multiply(price), RoundingMode.UP);
                    checkNotNull(giveBaseSellTextField).setText(formatAmount(giveBaseSell));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on reverse Recalc:", e);
            JavaFxUtils.showErrorMessage("Error on reverse Recalc: " + e);
        }
    }

    @Nullable
    public Trade getReturnTrade() {
        return returnTrade;
    }

}
