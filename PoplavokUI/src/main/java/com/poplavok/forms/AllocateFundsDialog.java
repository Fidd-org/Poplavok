package com.poplavok.forms;

import com.poplavok.data.model.Level;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.Poplavok;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;

public class AllocateFundsDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(AllocateFundsDialog.class);

    @FXML @Nullable TextField amountTextField;
    @FXML @Nullable Label currencyLabel;

    @Nullable Stage stage;

    @Nullable volatile Loan returnLoan = null;

    public AllocateFundsDialog(String currency, @Nullable BigDecimal defaultAmount) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("AllocateFundsDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        checkNotNull(currencyLabel).textProperty().set(currency);
        checkNotNull(amountTextField).textProperty().set(Level.formatAmount(defaultAmount));
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void okClose() {
        try {
            Loan loan = new Loan();

            //loan.setId(poplavokId);

            returnLoan = loan;
            checkNotNull(stage).close();
       } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "AccountAddDialog close Error: " + e, ButtonType.OK);
            LOGGER.error("AccountAddDialog close Error:", e);
            alert.showAndWait();
        }
    }

    @Nullable
    public Loan getReturnLoan() {
        return returnLoan;
    }
}
