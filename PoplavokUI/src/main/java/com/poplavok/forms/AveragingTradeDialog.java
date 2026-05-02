package com.poplavok.forms;

import com.flower.fxutils.JavaFxUtils;
import com.poplavok.data.model.Direction;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.model.Trade;
import com.poplavok.data.model.TradeOperation;
import com.poplavok.data.utils.AmountAndCommission;
import com.poplavok.data.utils.LongShortCalculator;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
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
import java.util.List;

import static com.flower.fxutils.JavaFxUtils.autoResizeTableColumns;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.utils.BigDecimalUtil.SCALE;
import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;
import static com.poplavok.data.utils.BigDecimalUtil.fromString;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;

public class AveragingTradeDialog extends AnchorPane {
    final static Logger LOGGER = LoggerFactory.getLogger(AveragingTradeDialog.class);

    @FXML @Nullable AnchorPane averagingPaneContainer;
    @Nullable AveragingPane averagingPane;
    @FXML @Nullable TableView<Level> levelsTable;

    @Nullable Stage stage;
    @Nullable Trade returnTrade;

    final List<Level> levels;
    final MarketTicker ticker;

    public AveragingTradeDialog(List<Level> levels, MarketTicker ticker, Direction direction, BigDecimal fee, @Nullable BigDecimal price) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AveragingTradeDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        averagingPane = new AveragingPane();
        checkNotNull(averagingPaneContainer).getChildren().add(averagingPane);
        AnchorPane.setTopAnchor(averagingPane, 0.0);
        AnchorPane.setBottomAnchor(averagingPane, 0.0);
        AnchorPane.setLeftAnchor(averagingPane, 0.0);
        AnchorPane.setRightAnchor(averagingPane, 0.0);

        this.levels = levels;
        checkNotNull(levelsTable).setItems(new FilteredList<>(FXCollections.observableArrayList(levels)));
        autoResizeTableColumns(levelsTable);

        this.ticker = ticker;

        checkNotNull(averagingPane).init(
                ticker, direction,
                levels,
                fee
        );
        averagingPane.updateLabels();

        /*checkNotNull(tickerLabel).textProperty().setValue(ticker.getSymbol());
        checkNotNull(priceTextField).textProperty().setValue(price != null ? price.toPlainString() : "");

        checkNotNull(availableBaseTextField).textProperty().setValue(formatAmount(lvl.getAvailableAmountBase()));
        checkNotNull(availableQuoteTextField).textProperty().setValue(formatAmount(lvl.getAvailableAmountQuote()));
        checkNotNull(useAllBaseButton).textProperty().setValue(ticker.getBase().getCurrency());
        checkNotNull(useAllQuoteButton).textProperty().setValue(ticker.getQuote().getCurrency());
        BigDecimal fee = ticker.getMakerFee();
        checkNotNull(feeTextField).textProperty().setValue(formatAmount(fee));
        checkNotNull(quoteBuyLabel).textProperty().setValue(ticker.getQuote().getCurrency());
        checkNotNull(quoteBuyCommissionLabel).textProperty().setValue(ticker.getQuote().getCurrency());
        checkNotNull(baseBuyLabel).textProperty().setValue(ticker.getBase().getCurrency());
        checkNotNull(baseSellLabel).textProperty().setValue(ticker.getBase().getCurrency());
        checkNotNull(quoteSellCommissionLabel).textProperty().setValue(ticker.getQuote().getCurrency());
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

        switch (direction) {
            case LONG:
                checkNotNull(tradeTabPane).getSelectionModel().select(checkNotNull(buyTab));
                break;
            case SHORT:
                checkNotNull(tradeTabPane).getSelectionModel().select(checkNotNull(sellTab));
                break;
            default: throw new IllegalStateException("Unknown direction: " + direction);
        }*/
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Nullable
    public Trade getReturnTrade() {
        return returnTrade;
    }

}
