package com.poplavok.forms;

import com.flower.fxutils.JavaFxUtils;
import com.poplavok.data.dao.PoplavokDAO;
import com.poplavok.data.model.Direction;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.Poplavok;
import com.poplavok.data.utils.AmountAndCommission;
import com.poplavok.data.utils.DBUtil;
import com.poplavok.data.utils.LongShortCalculator;
import com.poplavok.forms.wrapper.PoplavokWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
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
import java.util.List;

import static com.flower.fxutils.JavaFxUtils.createDecimalTextFormatter;
import static com.flower.fxutils.JavaFxUtils.isInvalidAmountChar;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.poplavok.data.utils.BigDecimalUtil.formatAmount;
import static com.poplavok.data.utils.BigDecimalUtil.fromString;
import static com.poplavok.data.utils.BigDecimalUtil.nullToZero;

public class CreateInverseLevelDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(CreateInverseLevelDialog.class);

    @FXML @Nullable Label directionLabel;

    @FXML @Nullable RadioButton createNewRadioButton;
    @FXML @Nullable RadioButton useExistingRadioButton;

    @FXML @Nullable TextField newPoplavokNameTextField;
    @FXML @Nullable ComboBox<PoplavokWrapper> existingPoplavokComboBox;

    @FXML @Nullable TextField feeTextField;
    @FXML @Nullable TextField priceTextField;

    @FXML @Nullable TextField amountTextField;
    @FXML @Nullable TextField proceedsTextField;
    @FXML @Nullable TextField commissionTextField;
    @FXML @Nullable Label commissionCurrencyLabel;

    @FXML @Nullable Label proceedsCurrencyLabel;
    @FXML @Nullable Label amountCurrencyLabel;
    @FXML @Nullable Label priceTickerLabel;
    @FXML @Nullable Button availableCurrencyButton;
    @FXML @Nullable TextField availableTextField;

    @Nullable Stage stage;

    final Poplavok poplavok;
    final List<Level> selected;
    final Direction direction;

    @Nullable FilteredList<PoplavokWrapper> destinationPoplavoks;

    @Nullable volatile Level returnLevel = null;

    public CreateInverseLevelDialog(List<Level> selected, Poplavok poplavok, @Nullable BigDecimal fee, @Nullable BigDecimal price) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CreateInverseLevelDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.selected = selected;
        this.poplavok = poplavok;

        List<Poplavok> poplavoks = DBUtil.connectGetResultAndClose(
                sess -> PoplavokDAO.findInversePoplavoks(sess, checkNotNull(poplavok.getTicker()), checkNotNull(poplavok.getDirection())));
        this.destinationPoplavoks = new FilteredList<>(FXCollections.observableArrayList(
                poplavoks.stream().map(PoplavokWrapper::new).toList()
            ));
        checkNotNull(existingPoplavokComboBox).setItems(destinationPoplavoks);
        checkNotNull(newPoplavokNameTextField).setText("Inverse " + poplavok.getName());
        if (!poplavoks.isEmpty()) {
            existingPoplavokComboBox.getSelectionModel().selectFirst();
        }

        direction = checkNotNull(poplavok.getDirection()) == Direction.LONG ? Direction.SHORT : Direction.LONG;

        checkNotNull(directionLabel).textProperty().setValue(direction.name());

        checkNotNull(amountTextField).setTextFormatter(createDecimalTextFormatter());
        checkNotNull(feeTextField).setTextFormatter(createDecimalTextFormatter());
        checkNotNull(priceTextField).setTextFormatter(createDecimalTextFormatter());
        checkNotNull(proceedsTextField).setTextFormatter(createDecimalTextFormatter());

        checkNotNull(createNewRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> updateControlsState());
        checkNotNull(useExistingRadioButton).selectedProperty().addListener((observable, oldValue, newValue) -> updateControlsState());

        checkNotNull(priceTickerLabel).textProperty().setValue(poplavok.getTicker().getSymbol());
        String base = poplavok.getTicker().getBase().getCurrency();
        String quote = poplavok.getTicker().getQuote().getCurrency();
        if (direction == Direction.SHORT) {
            checkNotNull(proceedsCurrencyLabel).textProperty().setValue(quote);
            checkNotNull(amountCurrencyLabel).textProperty().setValue(base);
            checkNotNull(availableCurrencyButton).textProperty().setValue(base);
        } else { //if (direction == Direction.LONG) {
            checkNotNull(proceedsCurrencyLabel).textProperty().setValue(base);
            checkNotNull(amountCurrencyLabel).textProperty().setValue(quote);
            checkNotNull(availableCurrencyButton).textProperty().setValue(quote);
        }

        checkNotNull(commissionCurrencyLabel).textProperty().setValue(quote);
        checkNotNull(priceTextField).textProperty().addListener(this::onPriceChanged);
        checkNotNull(feeTextField).textProperty().addListener(this::onFeeChanged);

        checkNotNull(feeTextField).textProperty().setValue(formatAmount(nullToZero(fee)));
        checkNotNull(priceTextField).textProperty().setValue(formatAmount(nullToZero(price)));

        BigDecimal available = BigDecimal.ZERO;
        for (Level lvl : selected) {
            if (direction == Direction.SHORT) {
                available = available.add(nullToZero(lvl.getAvailableAmountBase()));
            } else { //if (direction == Direction.LONG) {
                available = available.add(nullToZero(lvl.getAvailableAmountQuote()));
            }
        }

        checkNotNull(availableTextField).textProperty().setValue(formatAmount(available));

        updateControlsState();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void addAllAvailable() {
        try {
            if (!StringUtils.isBlank(checkNotNull(availableTextField).getText())) {
                BigDecimal availableBase = nullToZero(fromString(checkNotNull(availableTextField).getText()));
                checkNotNull(amountTextField).textProperty().setValue(formatAmount(availableBase));

                priceFeeUpdate();
            }
        } catch (Exception e) {
            LOGGER.error("Error on useAllBase:", e);
            JavaFxUtils.showErrorMessage("Error on useAllBase: " + e);
        }
    }

    public void okClose() {
        try {
//            Level newLevel;

            //

//            returnLevel = newLevel;
            checkNotNull(stage).close();
       } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "DepositWithdrawDialog close Error: " + e, ButtonType.OK);
            LOGGER.error("DepositWithdrawDialog close Error:", e);
            alert.showAndWait();
        }
    }

    @Nullable Level getReturnLevel() {
        return returnLevel;
    }

    private void updateControlsState() {
        if (checkNotNull(createNewRadioButton).isSelected()) {
            checkNotNull(newPoplavokNameTextField).setDisable(false);
            checkNotNull(newPoplavokNameTextField).setEditable(true);
            checkNotNull(existingPoplavokComboBox).setDisable(true);
        } else if (checkNotNull(useExistingRadioButton).isSelected()) {
            checkNotNull(newPoplavokNameTextField).setDisable(true);
            checkNotNull(newPoplavokNameTextField).setEditable(false);
            checkNotNull(existingPoplavokComboBox).setDisable(false);
        }
    }

    public void onPriceChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        priceFeeUpdate();
    }

    public void onFeeChanged(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        priceFeeUpdate();
    }

    public void onAmountChanged(@Nullable KeyEvent event) {
        if (isInvalidAmountChar(event)) { return; }
        priceFeeUpdate();
    }

    public void onProceedsChanged(@Nullable KeyEvent event) {
        if (isInvalidAmountChar(event)) { return; }
        reversePriceFeeUpdate();
    }

    /** Update quote sell, base buy, if manual input is not checked */
    protected void priceFeeUpdate() {
        try {
            if (StringUtils.isBlank(checkNotNull(priceTextField).getText())) { return; }
            if (StringUtils.isBlank(checkNotNull(feeTextField).getText())) { return; }
            if (StringUtils.isBlank(checkNotNull(amountTextField).getText())) { return; }
            BigDecimal price = nullToZero(fromString(checkNotNull(priceTextField).getText()));
            BigDecimal fee = nullToZero(fromString(checkNotNull(feeTextField).getText()));

            if (price.compareTo(BigDecimal.ZERO) == 0) { return; }

            if (direction == Direction.LONG) {
                // Update GET BASE BUY amount
                BigDecimal giveQuoteBuy = nullToZero(fromString(checkNotNull(amountTextField).getText()));

                AmountAndCommission getBaseBuy = LongShortCalculator.calculateBaseAmountToGetLong(giveQuoteBuy, price, fee);

                checkNotNull(proceedsTextField).setText(formatAmount(getBaseBuy.amount));
                checkNotNull(commissionTextField).setText(formatAmount(getBaseBuy.commissionQuote));
            } else {
                // Update GET QUOTE SELL amount
                BigDecimal giveBaseSell = nullToZero(fromString(checkNotNull(amountTextField).getText()));

                AmountAndCommission getQuoteSell = LongShortCalculator.calculateQuoteAmountToGetShort(giveBaseSell, price, fee);

                checkNotNull(proceedsTextField).setText(formatAmount(getQuoteSell.amount));
                checkNotNull(commissionTextField).setText(formatAmount(getQuoteSell.commissionQuote));
            }
        } catch (Exception e) {
            LOGGER.error("Error on Recalc:", e);
            JavaFxUtils.showErrorMessage("Error on Recalc: " + e);
        }
    }

    /** Update quote buy, base sell, if manual input is not checked */
    protected void reversePriceFeeUpdate() {
        try {
            if (StringUtils.isBlank(checkNotNull(priceTextField).getText())) { return; }
            if (StringUtils.isBlank(checkNotNull(feeTextField).getText())) { return; }
            if (StringUtils.isBlank(checkNotNull(proceedsTextField).getText())) { return; }
            BigDecimal price = nullToZero(fromString(checkNotNull(priceTextField).getText()));
            BigDecimal fee = nullToZero(fromString(checkNotNull(feeTextField).getText()));

            if (direction == Direction.LONG) {
                // Update GIVE QUOTE BUY amount
                BigDecimal getBaseBuy = nullToZero(fromString(checkNotNull(proceedsTextField).getText()));

                AmountAndCommission giveQuoteBuy = LongShortCalculator.calculateQuoteAmountToGiveLong(getBaseBuy, price, fee);

                checkNotNull(amountTextField).setText(formatAmount(giveQuoteBuy.amount));
                checkNotNull(commissionTextField).setText(formatAmount(giveQuoteBuy.commissionQuote));
            } else {
                // Update GIVE BASE SELL amount
                BigDecimal getQuoteSell = nullToZero(fromString(checkNotNull(proceedsTextField).getText()));

                AmountAndCommission giveBaseSell = LongShortCalculator.calculateBaseAmountToGiveShort(getQuoteSell, price, fee);

                checkNotNull(amountTextField).setText(formatAmount(giveBaseSell.amount));
                checkNotNull(commissionTextField).setText(formatAmount(giveBaseSell.commissionQuote));
            }
        } catch (Exception e) {
            LOGGER.error("Error on reverse Recalc:", e);
            JavaFxUtils.showErrorMessage("Error on reverse Recalc: " + e);
        }
    }
}
