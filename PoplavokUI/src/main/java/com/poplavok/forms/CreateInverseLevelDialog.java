package com.poplavok.forms;

import com.poplavok.data.model.Level;
import com.poplavok.data.model.Poplavok;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

import static com.flower.fxutils.JavaFxUtils.createDecimalTextFormatter;
import static com.google.common.base.Preconditions.checkNotNull;

public class CreateInverseLevelDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(CreateInverseLevelDialog.class);

    @FXML @Nullable Label directionLabel;

    @FXML @Nullable RadioButton createNewRadioButton;
    @FXML @Nullable RadioButton useExistingRadioButton;

    @FXML @Nullable TextField newPoplavokNameTextField;
    @FXML @Nullable ComboBox<Poplavok> existingPoplavokComboBox;

    @FXML @Nullable TextField feeTextField;
    @FXML @Nullable TextField priceTextField;

    @FXML @Nullable TextField amountTextField;
    @FXML @Nullable TextField proceedsTextField;

    @FXML @Nullable Label proceedsCurrencyLabel;
    @FXML @Nullable Label amountCurrencyLabel;
    @FXML @Nullable Label priceTickerLabel;
    @FXML @Nullable Label feeCurrencyLabel;
    @FXML @Nullable Button availableCurrencyButton;
    @FXML @Nullable TextField availableTextField;

    @Nullable Stage stage;

    final List<Level> selected;

    @Nullable volatile Level returnLevel = null;

    public CreateInverseLevelDialog(List<Level> selected) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("CreateInverseLevelDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.selected = selected;

        checkNotNull(amountTextField).setTextFormatter(createDecimalTextFormatter());
        checkNotNull(feeTextField).setTextFormatter(createDecimalTextFormatter());
        checkNotNull(priceTextField).setTextFormatter(createDecimalTextFormatter());
        checkNotNull(proceedsTextField).setTextFormatter(createDecimalTextFormatter());
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void addAllAvailable() {
        //
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
}
