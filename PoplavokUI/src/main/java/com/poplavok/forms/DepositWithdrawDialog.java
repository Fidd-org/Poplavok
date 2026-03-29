package com.poplavok.forms;

import com.poplavok.data.model.ExternalTransaction;
import com.poplavok.data.model.ExternalTransactionType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;

import static com.flower.fxutils.JavaFxUtils.createDecimalTextFormatter;
import static com.google.common.base.Preconditions.checkNotNull;

public class DepositWithdrawDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(DepositWithdrawDialog.class);

    @FXML @Nullable TextField amountTextField;
    @FXML @Nullable TextArea detailsTextArea;
    @FXML @Nullable Button depositWithdrawButton;

    @Nullable Stage stage;

    boolean isDeposit;
    @Nullable volatile ExternalTransaction externalTransaction = null;

    public DepositWithdrawDialog(boolean isDeposit) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("DepositWithdrawDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        checkNotNull(amountTextField).setTextFormatter(createDecimalTextFormatter());

        this.isDeposit = isDeposit;
        if (isDeposit) {
            checkNotNull(depositWithdrawButton).textProperty().set("Deposit");
        } else {
            checkNotNull(depositWithdrawButton).textProperty().set("Withdraw");
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void okClose() {
        try {
            String amountStr = checkNotNull(amountTextField).getText();
            if (amountStr.isEmpty()) {
                return;
            }
            amountStr = amountStr.replace(',', '.');
            String details = checkNotNull(detailsTextArea).getText();
            BigDecimal amount = new BigDecimal(amountStr);

            ExternalTransaction transaction = new ExternalTransaction();
            transaction.setDetails(details);
            if (!isDeposit) {
                transaction.setType(ExternalTransactionType.WITHDRAWAL);
            } else {
                transaction.setType(ExternalTransactionType.DEPOSIT);
            }
            transaction.setAmount(amount);

            externalTransaction = transaction;
            checkNotNull(stage).close();
       } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "DepositWithdrawDialog close Error: " + e, ButtonType.OK);
            LOGGER.error("DepositWithdrawDialog close Error:", e);
            alert.showAndWait();
        }
    }

    @Nullable ExternalTransaction getReturnTransaction() {
        return externalTransaction;
    }
}
