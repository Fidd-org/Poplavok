package com.poplavok.forms;

import com.poplavok.data.model.Direction;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.utils.AmountAndCommission;
import com.poplavok.data.utils.BuyPriceInfo;
import com.poplavok.data.utils.LongShortCalculator;
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
import java.util.List;

import static com.flower.fxutils.JavaFxUtils.createDecimalTextFormatter;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.model.Direction.LONG;
import static com.poplavok.data.model.Direction.SHORT;
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

    // Retain holdings

    @FXML @Nullable CheckBox retainHoldingsCheckBox;
    @FXML @Nullable CheckBox excludeCommissionCheckBox;

    @FXML @Nullable Label retainCaptionLabel;
    @FXML @Nullable Label retainWorthCaptionLabel;
    @FXML @Nullable Label retainCommissionCaptionLabel;

    @FXML @Nullable TextField retainTextField;
    @FXML @Nullable TextField retainWorthTextField;
    @FXML @Nullable TextField retainCommissionTextField;

    @FXML @Nullable Label retainCurrencyLabel;
    @FXML @Nullable Label retainWorhCurrencyLabel;
    @FXML @Nullable Label retainCommisionCurrencyLabel;

    protected final MarketTicker ticker;
    protected Collection<Level> averageLevels;
    protected final Direction direction;
    protected final BigDecimal fee;

    public AveragingPane(MarketTicker ticker, Direction direction, Collection<Level> lvls, BigDecimal fee) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AveragingPane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.averageLevels = lvls;
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
                updateAverageTabEvent();
            }
        });
        checkNotNull(profitInBaseRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && ticker != null) {
                checkNotNull(profitCurrencyLabel).textProperty().setValue(ticker.getBase().getCurrency());
                updateAverageTabEvent();
            }
        });
        checkNotNull(commsRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { updateAverageTabEvent(); }
        });
        checkNotNull(percentRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { updateAverageTabEvent(); }
        });
        checkNotNull(includeLentAmountsCheckBox).selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateAverageTabEvent();
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
            updateAverageTabEvent();
        });

        checkNotNull(percentTextField).setTextFormatter(createDecimalTextFormatter());
        checkNotNull(retainTextField).setTextFormatter(createDecimalTextFormatter());
        checkNotNull(retainWorthTextField).setTextFormatter(createDecimalTextFormatter());

        checkNotNull(retainHoldingsCheckBox).selectedProperty().addListener((observable, oldValue, enable) -> {
            boolean disable = !enable;
            switchDisableRetainControls(disable);
            if (!disable) {
                updateRetainedAmountByQuote();
            }
        });

        checkNotNull(excludeCommissionCheckBox).selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateRetainedAmountByQuote();
        });

        updateAverageTabEvent();
    }

    public void updateAverageTabEvent() {
        updateAverageTab(checkNotNull(averageLevels));
    }

    public void switchDisableRetainControls(boolean disable) {
        checkNotNull(excludeCommissionCheckBox).disableProperty().setValue(disable);

        checkNotNull(retainCaptionLabel).disableProperty().setValue(disable);
        checkNotNull(retainWorthCaptionLabel).disableProperty().setValue(disable);
        checkNotNull(retainCommissionCaptionLabel).disableProperty().setValue(disable);

        checkNotNull(retainTextField).disableProperty().setValue(disable);
        checkNotNull(retainWorthTextField).disableProperty().setValue(disable);
        checkNotNull(retainCommissionTextField).disableProperty().setValue(disable);

        checkNotNull(retainCurrencyLabel).disableProperty().setValue(disable);
        checkNotNull(retainWorhCurrencyLabel).disableProperty().setValue(disable);
        checkNotNull(retainCommisionCurrencyLabel).disableProperty().setValue(disable);
    }

    public void updateRetainedAmountByQuote() {
        if (direction == LONG) {
            updateRetainedAmountByWorth();
        } else if (direction == SHORT) {
            updateRetainedAmountByRetained();
        } else {
            throw new RuntimeException("Unknown direction " + direction);
        }
    }

    public void updateRetainedAmountByRetained() {
        String retainedAmountStr = checkNotNull(retainTextField).textProperty().get();
        BigDecimal retainedAmount = nullToZero(fromString(retainedAmountStr));

        BigDecimal fee = checkNotNull(excludeCommissionCheckBox).selectedProperty().get() ? BigDecimal.ZERO : this.fee;
        BigDecimal price = getAveragingPrice();

        BigDecimal debtAmount;
        BigDecimal commissionAmount;
        if (!retainedAmount.equals(BigDecimal.ZERO)) {
            AmountAndCommission aac;
            if (direction == LONG) {
                aac = LongShortCalculator.calculateQuoteAmountToGetShort(retainedAmount, price, checkNotNull(fee));
            } else if (direction == SHORT) {
                aac = LongShortCalculator.calculateBaseAmountToGetLong(retainedAmount, price, checkNotNull(fee));
            } else {
                throw new RuntimeException("Unknown Direction " + direction);
            }

            debtAmount = aac.amount;
            commissionAmount = aac.commissionQuote;
        } else {
            debtAmount = BigDecimal.ZERO;
            commissionAmount = BigDecimal.ZERO;
            checkNotNull(retainTextField).textProperty().set(formatAmount(retainedAmount));
        }

        checkNotNull(retainWorthTextField).textProperty().setValue(formatAmount(debtAmount));
        checkNotNull(retainCommissionTextField).textProperty().setValue(formatAmount(commissionAmount));
    }

    public void updateRetainedAmountByWorth() {
        String debtAmountStr = checkNotNull(retainWorthTextField).textProperty().get();
        BigDecimal debtAmount = nullToZero(fromString(debtAmountStr));

        BigDecimal fee = checkNotNull(excludeCommissionCheckBox).selectedProperty().get() ? BigDecimal.ZERO : this.fee;
        BigDecimal price = getAveragingPrice();

        BigDecimal retainedAmount;
        BigDecimal commissionAmount;
        if (!debtAmount.equals(BigDecimal.ZERO)) {
            AmountAndCommission aac;
            if (direction == LONG) {
                aac = LongShortCalculator.calculateBaseAmountToGiveShort(debtAmount, price, checkNotNull(fee));
            } else if (direction == SHORT) {
                aac = LongShortCalculator.calculateQuoteAmountToGiveLong(debtAmount, price, checkNotNull(fee));
            } else {
                throw new RuntimeException("Unknown Direction " + direction);
            }

            retainedAmount = aac.amount;
            commissionAmount = aac.commissionQuote;
        } else {
            retainedAmount = BigDecimal.ZERO;
            commissionAmount = BigDecimal.ZERO;
            checkNotNull(retainWorthTextField).textProperty().set(formatAmount(debtAmount));
        }

        checkNotNull(retainTextField).textProperty().setValue(formatAmount(retainedAmount));
        checkNotNull(retainCommissionTextField).textProperty().setValue(formatAmount(commissionAmount));
    }

    public void updateAverageTab(Collection<Level> lvls) {
        if (lvls == null) { lvls = List.of(); }
        averageLevels = lvls;

        boolean includeLentAmounts = checkNotNull(includeLentAmountsCheckBox).selectedProperty().get();

        BigDecimal debt = BigDecimal.ZERO;
        BigDecimal available = BigDecimal.ZERO;
        BigDecimal holding = BigDecimal.ZERO;

        for (Level lvl : averageLevels) {
            if (checkNotNull(direction) == LONG) {
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
            if (checkNotNull(direction) == LONG) {
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

        updateRetainedAmountByQuote();
    }

    public void updateLabels() {
        String debtCurrency = checkNotNull(direction) == LONG
                ? ticker.getQuote().getCurrency() : ticker.getBase().getCurrency();
        String holdCurrency = direction == LONG
                ? ticker.getBase().getCurrency() : ticker.getQuote().getCurrency();

        checkNotNull(poplavokDirectionLabel).textProperty().setValue(checkNotNull(direction).name());
        checkNotNull(poplavokTickerLabel).textProperty().setValue(checkNotNull(ticker).getSymbol());
        checkNotNull(averagingActionLabel).textProperty().setValue(checkNotNull(direction) == LONG ? "SELL" : "BUY");
        checkNotNull(averagingCurrencyLabel).textProperty().setValue(ticker.getBase().getCurrency());

        checkNotNull(debtCurrencyLabel).textProperty().setValue(debtCurrency);
        checkNotNull(availableCurrencyLabel).textProperty().setValue(debtCurrency);
        checkNotNull(toRepayCurrencyLabel).textProperty().setValue(debtCurrency);
        checkNotNull(fxUiEntryCurrencyLabel).setText(holdCurrency);
        checkNotNull(holdingCurrencyLabel).textProperty().setValue(holdCurrency);

        checkNotNull(toTradeCurrencyLabel).textProperty().setValue(holdCurrency);
        checkNotNull(tradePriceLabel).textProperty().setValue(ticker.getSymbol());
        checkNotNull(proceedsCurrencyLabel).textProperty().setValue(debtCurrency);

        checkNotNull(retainCurrencyLabel).textProperty().setValue(holdCurrency);
        checkNotNull(retainWorhCurrencyLabel).textProperty().setValue(debtCurrency);

        String quoteCurrency = checkNotNull(ticker).getQuote().getCurrency();
        // Commission is always in QUOTE currency
        checkNotNull(retainCommisionCurrencyLabel).textProperty().setValue(quoteCurrency);
        checkNotNull(commissionCurrencyLabel).textProperty().setValue(quoteCurrency);
        // Default: take profit in quote
        checkNotNull(profitCurrencyLabel).textProperty().setValue(quoteCurrency);
    }

    public BigDecimal getTotalDebt() {
        return nullToZero(fromString(checkNotNull(debtTextField).textProperty().get()));
    }

    public BigDecimal getDebtToRepay() {
        return nullToZero(fromString(checkNotNull(toRepayTextField).textProperty().get()));
    }

    public BigDecimal getAveragingPrice() {
        return nullToZero(fromString(checkNotNull(sellPriceTextField).textProperty().get()));
    }

    public BigDecimal getRetainedDebt() {
        if (checkNotNull(retainHoldingsCheckBox).isSelected()) {
            return nullToZero(fromString(checkNotNull(retainWorthTextField).textProperty().get()));
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getRetainedAmount() {
        if (checkNotNull(retainHoldingsCheckBox).isSelected()) {
            return nullToZero(fromString(checkNotNull(retainTextField).textProperty().get()));
        } else {
            return BigDecimal.ZERO;
        }
    }
}
