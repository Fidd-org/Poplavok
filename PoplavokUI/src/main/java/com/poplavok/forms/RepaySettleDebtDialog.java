package com.poplavok.forms;

import com.poplavok.data.model.Direction;
import com.poplavok.data.model.Level;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;

public class RepaySettleDebtDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(RepaySettleDebtDialog.class);

    @FXML @Nullable TextField repaymentDebtTextField;
    @FXML @Nullable Button repaymentDebtCurrencyButton;
    @FXML @Nullable TextField availableForRepaymentTextField;
    @FXML @Nullable Label availableForRepaymentLabel;
    @FXML @Nullable TextField toRepayRepaymentTextField;
    @FXML @Nullable Label toRepayRepaymentCurrencyLabel;
    @FXML @Nullable Button repaymentButton;

    @Nullable Stage stage;

    @Nullable Long levelId = null;
    @Nullable Level level;
    @Nullable volatile Level returnLevel = null;
    final Direction tradeDirection;

    public RepaySettleDebtDialog(@Nullable Level level, String ticker, @Nullable BigDecimal price, @Nullable BigDecimal fee, Direction tradeDirection) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LevelAddDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.tradeDirection = tradeDirection;

        if (level == null) {
            //checkNotNull(addButton).textProperty().set("Add New Level");
            if (price != null) {
                //checkNotNull(priceTextField).textProperty().set(formatAmount(price));
            }
        } else {
            this.level = level;
            //checkNotNull(addButton).textProperty().set("Update Level");
            levelId = level.getId();
        }
    }

    public void repaymentDebtCurrency() {
        //
    }

    public void repayment() {
        //
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}
