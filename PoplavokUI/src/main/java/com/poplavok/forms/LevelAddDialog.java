package com.poplavok.forms;

import com.poplavok.data.model.Level;
import com.poplavok.data.model.LevelState;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.function.UnaryOperator;

import static com.google.common.base.Preconditions.checkNotNull;

public class LevelAddDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(LevelAddDialog.class);

    @FXML @Nullable TextField tickerTextField;
    @FXML @Nullable TextField priceTextField;
    @FXML @Nullable TextField notesTextField;
    @FXML @Nullable TextField quoteAmountTextField;
    @FXML @Nullable TextField baseAmountTextField;
    @FXML @Nullable Button addButton;

    @Nullable Stage stage;

    @Nullable Long levelId = null;
    @Nullable volatile Level returnLevel = null;

    public LevelAddDialog(@Nullable Level level, String ticker, @Nullable BigDecimal price) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LevelAddDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        UnaryOperator<TextFormatter.Change> decimalTextFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*([.,]\\d*)?")) {
                return change;
            }
            return null;
        };

        checkNotNull(priceTextField).setTextFormatter(new javafx.scene.control.TextFormatter<>(decimalTextFilter));
        if (baseAmountTextField != null) {
            baseAmountTextField.setTextFormatter(new javafx.scene.control.TextFormatter<>(decimalTextFilter));
            baseAmountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (baseAmountTextField != null && baseAmountTextField.isFocused()) recalculateQuoteFromBase();
            });
        }
        if (quoteAmountTextField != null) {
            quoteAmountTextField.setTextFormatter(new javafx.scene.control.TextFormatter<>(decimalTextFilter));
            quoteAmountTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (quoteAmountTextField != null && quoteAmountTextField.isFocused()) recalculateBaseFromQuote();
            });
        }
        checkNotNull(priceTextField).textProperty().addListener((observable, oldValue, newValue) -> {
            if (priceTextField != null && priceTextField.isFocused()) recalculateBaseFromQuote();
        });

        checkNotNull(tickerTextField).textProperty().set(ticker);
        if (level == null) {
            checkNotNull(addButton).textProperty().set("Add New Level");
            if (price != null) {
                checkNotNull(priceTextField).textProperty().set(price.toPlainString());
            }
        } else {
            checkNotNull(addButton).textProperty().set("Rename Level");
            levelId = level.getId();

            checkNotNull(priceTextField).textProperty().set(level.getProjectedPrice().toPlainString());
            checkNotNull(notesTextField).textProperty().set(level.getNotes());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void okClose() {
        try {
            Level level = new Level();
            level.setState(LevelState.INCEPTION);
            level.setId(levelId);
            level.setCreationDate(new Date());
            level.setProjectedPrice(new BigDecimal(checkNotNull(priceTextField).textProperty().get()));
            level.setProjectedAmountBase(new BigDecimal(checkNotNull(baseAmountTextField).textProperty().get()));
            level.setProjectedAmountQuote(new BigDecimal(checkNotNull(quoteAmountTextField).textProperty().get()));
            level.setNotes(checkNotNull(notesTextField).textProperty().get());

            returnLevel = level;
            checkNotNull(stage).close();
       } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "LevelAddDialog close Error: " + e, ButtonType.OK);
            LOGGER.error("LevelAddDialog close Error:", e);
            alert.showAndWait();
        }
    }

    private void recalculateQuoteFromBase() {
        try {
            if (baseAmountTextField == null || quoteAmountTextField == null || priceTextField == null) return;
            BigDecimal base = new BigDecimal(baseAmountTextField.getText().replace(',', '.'));
            BigDecimal price = new BigDecimal(priceTextField.getText().replace(',', '.'));
            quoteAmountTextField.setText(base.multiply(price).stripTrailingZeros().toPlainString());
        } catch (Exception e) {
            // ignore parsing errors
        }
    }

    private void recalculateBaseFromQuote() {
        try {
            if (baseAmountTextField == null || quoteAmountTextField == null || priceTextField == null) return;
            BigDecimal quote = new BigDecimal(quoteAmountTextField.getText().replace(',', '.'));
            BigDecimal price = new BigDecimal(priceTextField.getText().replace(',', '.'));
            if (price.compareTo(BigDecimal.ZERO) != 0) {
                baseAmountTextField.setText(quote.divide(price, 8, RoundingMode.CEILING).stripTrailingZeros().toPlainString());
            }
        } catch (Exception e) {
            // ignore parsing errors
        }
    }

    @Nullable Level getReturnLevel() {
        return returnLevel;
    }
}
