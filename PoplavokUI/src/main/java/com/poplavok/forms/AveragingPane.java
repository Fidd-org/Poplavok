package com.poplavok.forms;

import com.poplavok.data.model.Direction;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.utils.BuyPriceInfo;
import com.poplavok.data.utils.PriceCalculator;
import com.poplavok.data.utils.PriceInfo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.utils.BigDecimalUtil.SCALE;
import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;
import static com.poplavok.data.utils.BigDecimalUtil.fromString;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;

public class AveragingPane extends AnchorPane {
    final static Logger LOGGER = LoggerFactory.getLogger(AveragingPane.class);

    public static final BigDecimal ONE_HUNDRED = nullToZero(fromString("100"));

    @FXML @Nullable TextField debtTextField;
    @FXML @Nullable TextField availableTextField;
    @FXML @Nullable TextField toRepayTextField;
    @FXML @Nullable TextField holdingTextField;

    @FXML @Nullable Label debtCurrencyLabel;
    @FXML @Nullable Label availableCurrencyLabel;
    @FXML @Nullable Label toRepayCurrencyLabel;
    @FXML @Nullable Label holdingCurrencyLabel;

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

    @FXML @Nullable TextField debtCurrencyTextField;
    @FXML @Nullable TextField holdingCurrencyTextField;

    @FXML @Nullable RadioButton profitInQuoteRadioButton;
    @FXML @Nullable RadioButton profitInBaseRadioButton;

    @FXML @Nullable Label fxUiEntryCurrencyLabel;
    @FXML @Nullable TextField fxUiEntryTextField;

    @FXML @Nullable Label toTradeCurrencyLabel;
    @FXML @Nullable Label tradePriceLabel;
    @FXML @Nullable Label proceedsCurrencyLabel;
    @FXML @Nullable Label commissionCurrencyLabel;
    @FXML @Nullable Label profitCurrencyLabel;

    @FXML @Nullable Label poplavokDirectionLabel;
    @FXML @Nullable Label poplavokTickerLabel;
    @FXML @Nullable Label averagingActionLabel;
    @FXML @Nullable Label averagingCurrencyLabel;

    @FXML @Nullable CheckBox includeLentAmountsCheckBox;

    protected @Nullable MarketTicker ticker;
    protected @Nullable Collection<Level> levels;
    protected @Nullable Direction direction;
    protected @Nullable BigDecimal fee;

    public void init(MarketTicker ticker, Direction direction, Collection<Level> lvls, BigDecimal fee) {
        this.levels = lvls;
        this.ticker = ticker;
        this.direction = direction;
        this.fee = fee;

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

        checkNotNull(profitInQuoteRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && ticker != null) {
                checkNotNull(profitCurrencyLabel).textProperty().setValue(ticker.getQuote().getCurrency());
                updateAverageTab(checkNotNull(levels));
            }
        });
        checkNotNull(profitInBaseRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && ticker != null) {
                checkNotNull(profitCurrencyLabel).textProperty().setValue(ticker.getBase().getCurrency());
                updateAverageTab(checkNotNull(levels));
            }
        });
        checkNotNull(commsRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { updateAverageTab(checkNotNull(levels)); }
        });
        checkNotNull(percentRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { updateAverageTab(checkNotNull(levels)); }
        });
        checkNotNull(includeLentAmountsCheckBox).selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateAverageTab(checkNotNull(levels));
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

        checkNotNull(commsCountTextField).textProperty().addListener((observable, oldValue, newValue) -> {
            updateAverageTab(checkNotNull(levels));
        });

        checkNotNull(percentTextField).textProperty().addListener((observable, oldValue, newValue) -> {
            updateAverageTab(checkNotNull(levels));
        });

        updateAverageTab(lvls);
    }

    public AveragingPane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AveragingPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public void updateAverageTab(Collection<Level> levels) {
        if (levels == null || levels.isEmpty()) {
            return;
        }

        boolean includeLentAmounts = checkNotNull(includeLentAmountsCheckBox).selectedProperty().get();

        BigDecimal debt = BigDecimal.ZERO;
        BigDecimal available = BigDecimal.ZERO;
        BigDecimal holding = BigDecimal.ZERO;

        for (Level lvl : levels) {
            if (checkNotNull(direction) == Direction.LONG) {
                debt = debt.add(nullToZero(lvl.getDebtQuote()));
                available = available.add(nullToZero(lvl.getAvailableAmountQuote()));
                holding = holding.add(nullToZero(lvl.getAvailableAmountBase()));
                if (includeLentAmounts) {
                    holding = holding.add(nullToZero(lvl.getLentAmountBase()));
                }
            } else {
                debt = debt.add(nullToZero(lvl.getDebtBase()));
                available = available.add(nullToZero(lvl.getAvailableAmountBase()));
                holding = holding.add(nullToZero(lvl.getAvailableAmountQuote()));
                if (includeLentAmounts) {
                    holding = holding.add(nullToZero(lvl.getLentAmountQuote()));
                }
            }
        }
        BigDecimal toRepay = debt.subtract(available);

        checkNotNull(debtTextField).setText(formatAmount(debt));
        checkNotNull(availableTextField).setText(formatAmount(available));
        checkNotNull(toRepayTextField).setText(formatAmount(toRepay));
        checkNotNull(holdingTextField).setText(formatAmount(holding));

        // ------------------------

        BigDecimal profitRate;

        if (checkNotNull(commsRadioButton).selectedProperty().get()) {
            BigDecimal commsNumber = nullToZero(fromString(checkNotNull(commsCountTextField).textProperty().get()));
            profitRate = checkNotNull(fee).multiply(commsNumber);
        } else if (checkNotNull(percentRadioButton).selectedProperty().get()) {
            profitRate = nullToZero(fromString(checkNotNull(percentTextField).textProperty().get())).divide(ONE_HUNDRED, SCALE, RoundingMode.CEILING);
        } else {
            throw new RuntimeException("Unknown Profit Mode Selection");
        }

        boolean profitInQuote = checkNotNull(profitInQuoteRadioButton).selectedProperty().get();
        boolean profitInBase = checkNotNull(profitInBaseRadioButton).selectedProperty().get();

        if (holding.compareTo(BigDecimal.ZERO) != 0) {
            if (checkNotNull(direction) == Direction.LONG) {
                // LONG

                if (profitInBase) {
                    // LONG - Take Profit in BASE
                    BigDecimal profitBase = holding.multiply(profitRate);
                    BigDecimal holdingWithoutProfit = holding.subtract(profitBase);

                    PriceInfo tradeResult = PriceCalculator.calculateSellPrice(holdingWithoutProfit, debt, checkNotNull(fee));

                    checkNotNull(toSellTextField).textProperty().setValue(formatAmount(holdingWithoutProfit));
                    checkNotNull(sellPriceTextField).textProperty().setValue(formatAmount(tradeResult.price));
                    checkNotNull(proceedsTextField).textProperty().setValue(formatAmount(debt));
                    checkNotNull(commissionTextField).textProperty().setValue(formatAmount(tradeResult.commissionQuote));
                    checkNotNull(profitTextField).textProperty().setValue(formatAmount(profitBase));
                    checkNotNull(fxUiEntryTextField).textProperty().setValue(formatAmount(holdingWithoutProfit));
                } else if (profitInQuote) {
                    // LONG - Take Profit in QUOTE

                    BigDecimal profitQuote = debt.multiply(profitRate);
                    BigDecimal proceedsQuote = debt.add(profitQuote);

                    PriceInfo tradeResult = PriceCalculator.calculateSellPrice(holding, proceedsQuote, checkNotNull(fee));

                    checkNotNull(toSellTextField).textProperty().setValue(formatAmount(holding));
                    checkNotNull(sellPriceTextField).textProperty().setValue(formatAmount(tradeResult.price));
                    checkNotNull(proceedsTextField).textProperty().setValue(formatAmount(proceedsQuote));
                    checkNotNull(commissionTextField).textProperty().setValue(formatAmount(tradeResult.commissionQuote));
                    checkNotNull(profitTextField).textProperty().setValue(formatAmount(profitQuote));
                    checkNotNull(fxUiEntryTextField).textProperty().setValue(formatAmount(holding));
                } else {
                    throw new RuntimeException("Please select to take profit in base or quote");
                }

                // tradeResult = PriceCalculator.calculateSellPrice(holding, proceeds, fee);
            } else {
                // SHORT

                if (profitInQuote) {
                    // SHORT - Take Profit in QUOTE

                    BigDecimal profitQuote = holding.multiply(profitRate);
                    BigDecimal holdingWithoutProfit = holding.subtract(profitQuote);

                    BuyPriceInfo tradeResult = PriceCalculator.calculateBuyPriceExact(holdingWithoutProfit, debt, checkNotNull(fee));

                    checkNotNull(toSellTextField).textProperty().setValue(formatAmount(holdingWithoutProfit));
                    checkNotNull(sellPriceTextField).textProperty().setValue(formatAmount(tradeResult.price));
                    checkNotNull(proceedsTextField).textProperty().setValue(formatAmount(debt));
                    checkNotNull(commissionTextField).textProperty().setValue(formatAmount(tradeResult.commissionQuote));
                    checkNotNull(profitTextField).textProperty().setValue(formatAmount(profitQuote));
                    checkNotNull(fxUiEntryTextField).textProperty().setValue(formatAmount((tradeResult).entryQuote));
                } else if (profitInBase) {
                    // SHORT - Take Profit in BASE

                    BigDecimal profitBase = debt.multiply(profitRate);
                    BigDecimal proceedsBase = debt.add(profitBase);

                    BuyPriceInfo tradeResult = PriceCalculator.calculateBuyPriceExact(holding, proceedsBase, checkNotNull(fee));

                    checkNotNull(toSellTextField).textProperty().setValue(formatAmount(holding));
                    checkNotNull(sellPriceTextField).textProperty().setValue(formatAmount(tradeResult.price));
                    checkNotNull(proceedsTextField).textProperty().setValue(formatAmount(proceedsBase));
                    checkNotNull(commissionTextField).textProperty().setValue(formatAmount(tradeResult.commissionQuote));
                    checkNotNull(profitTextField).textProperty().setValue(formatAmount(profitBase));
                    checkNotNull(fxUiEntryTextField).textProperty().setValue(formatAmount((tradeResult).entryQuote));
                } else {
                    throw new RuntimeException("Please select to take profit in base or quote");
                }
            }
        } else {
            checkNotNull(toSellTextField).textProperty().setValue(formatAmount(BigDecimal.ZERO));
            checkNotNull(sellPriceTextField).textProperty().setValue(formatAmount(BigDecimal.ZERO));
            checkNotNull(proceedsTextField).textProperty().setValue(formatAmount(BigDecimal.ZERO));
            checkNotNull(commissionTextField).textProperty().setValue(formatAmount(BigDecimal.ZERO));
            checkNotNull(profitTextField).textProperty().setValue(formatAmount(BigDecimal.ZERO));
            checkNotNull(fxUiEntryTextField).textProperty().setValue(formatAmount(BigDecimal.ZERO));
        }

        /*
        @FXML @Nullable CheckBox reserveCurrencyCheckBox;
        @FXML @Nullable CheckBox removeCommissionCheckBox;
        @FXML @Nullable CheckBox debtCurrencyCheckBox;
        @FXML @Nullable CheckBox holdingCurrencyCheckBox;
        @FXML @Nullable TextField debtCurrencyTextField;
        @FXML @Nullable TextField holdingCurrencyTextField;
        */
    }

    public void updateLabels() {
        String quoteCurrency = checkNotNull(ticker).getQuote().getCurrency();
        String debtCurrency = checkNotNull(direction) == Direction.LONG
                ? ticker.getQuote().getCurrency() : ticker.getBase().getCurrency();
        String holdCurrency = direction == Direction.LONG
                ? ticker.getBase().getCurrency() : ticker.getQuote().getCurrency();

        checkNotNull(poplavokDirectionLabel).textProperty().setValue(checkNotNull(direction).name());
        checkNotNull(poplavokTickerLabel).textProperty().setValue(checkNotNull(ticker).getSymbol());
        checkNotNull(averagingActionLabel).textProperty().setValue(checkNotNull(direction) == Direction.LONG ? "SELL" : "BUY");
        checkNotNull(averagingCurrencyLabel).textProperty().setValue(ticker.getBase().getCurrency());

        // Commission is always in QUOTE currency
        checkNotNull(commissionCurrencyLabel).textProperty().setValue(quoteCurrency);
        checkNotNull(debtCurrencyLabel).textProperty().setValue(debtCurrency);
        checkNotNull(availableCurrencyLabel).textProperty().setValue(debtCurrency);
        checkNotNull(toRepayCurrencyLabel).textProperty().setValue(debtCurrency);
        checkNotNull(fxUiEntryCurrencyLabel).setText(holdCurrency);
        checkNotNull(holdingCurrencyLabel).textProperty().setValue(holdCurrency);

        checkNotNull(toTradeCurrencyLabel).textProperty().setValue(holdCurrency);
        checkNotNull(tradePriceLabel).textProperty().setValue(ticker.getSymbol());
        checkNotNull(proceedsCurrencyLabel).textProperty().setValue(debtCurrency);
        // Default: take profit in quote
        checkNotNull(profitCurrencyLabel).textProperty().setValue(quoteCurrency);
    }
}
