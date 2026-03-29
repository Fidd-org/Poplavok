package com.poplavok.forms;

import com.google.common.base.Preconditions;
import com.poplavok.data.dao.MarketTickerDAO;
import com.poplavok.data.model.Direction;
import com.poplavok.data.model.MarketTicker;
import com.poplavok.data.model.Poplavok;
import com.poplavok.data.utils.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Date;
import java.util.function.Predicate;

import static com.google.common.base.Preconditions.checkNotNull;

public class PoplavokAddDialog extends VBox {
    final static Logger LOGGER = LoggerFactory.getLogger(PoplavokAddDialog.class);

    @FXML @Nullable TextField poplavokNameTextField;
    @FXML @Nullable TextField tickerFilterTextField;
    @FXML @Nullable ComboBox<MarketTicker> tickerComboBox;
    @FXML @Nullable ComboBox<Direction> directionComboBox;
    @FXML @Nullable TextArea notesTextArea;
    @FXML @Nullable Button addButton;

    @Nullable Stage stage;

    @Nullable Long poplavokId = null;
    @Nullable volatile Poplavok returnPoplavok = null;
    @Nullable FilteredList<MarketTicker> tickers;

    public PoplavokAddDialog(@Nullable Poplavok poplavok) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PoplavokAddDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        checkNotNull(tickerComboBox).setConverter(new StringConverter<>() {
            @Override
            public String toString(MarketTicker object) {
                return object != null ? object.getSymbol() : "";
            }

            @Override
            public MarketTicker fromString(String string) {
                // No need for reverse conversion for now, ComboBox is not editable
                throw new UnsupportedOperationException("Conversion from string not supported");
            }
        });

        ObservableList<MarketTicker> masterTickers =
                FXCollections.observableList(DBUtil.connectGetResultAndClose(MarketTickerDAO::findAll));
        tickers = new FilteredList<>(masterTickers);
        SortedList<MarketTicker> sortableTickers = new SortedList<>(tickers);

        tickers.setPredicate(createFilterPredicate());

        checkNotNull(tickerComboBox).itemsProperty().setValue(sortableTickers);

        checkNotNull(directionComboBox).itemsProperty().setValue(FXCollections.observableArrayList(Direction.values()));

        if (poplavok == null) {
            checkNotNull(tickerComboBox).selectionModelProperty().get().selectFirst();
            checkNotNull(directionComboBox).selectionModelProperty().get().selectFirst();
            checkNotNull(addButton).textProperty().set("Add New Poplavok");
        } else {
            poplavokId = poplavok.getId();

            checkNotNull(poplavokNameTextField).textProperty().set(poplavok.getName());
            checkNotNull(tickerComboBox).selectionModelProperty().get().select(poplavok.getTicker());
            checkNotNull(tickerComboBox).disableProperty().set(true);
            checkNotNull(directionComboBox).selectionModelProperty().get().select(poplavok.getDirection());
            checkNotNull(directionComboBox).disableProperty().set(true);
            checkNotNull(tickerFilterTextField).disableProperty().set(true);
            checkNotNull(notesTextArea).textProperty().set(poplavok.getNotes());
            checkNotNull(addButton).textProperty().set("Rename Poplavok");
        }
    }

    private Predicate<MarketTicker> createFilterPredicate() {
        String searchText = Preconditions.checkNotNull(tickerFilterTextField).textProperty().get();
        return ticker -> {
            if (searchText == null || searchText.isEmpty()) return true;
            return (ticker.getSymbol().toLowerCase().contains(searchText.toLowerCase().trim()));
        };
    }

    public void filterTableView() {
        if (tickers != null) {
            tickers.setPredicate(createFilterPredicate());
            checkNotNull(tickerComboBox).selectionModelProperty().get().selectFirst();
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void okClose() {
        try {
            Poplavok poplavok = new Poplavok();
            poplavok.setId(poplavokId);
            poplavok.setCreationDate(new Date());
            poplavok.setActive(true);
            poplavok.setTicker(checkNotNull(tickerComboBox).getSelectionModel().getSelectedItem());
            poplavok.setName(checkNotNull(poplavokNameTextField).textProperty().get());
            poplavok.setNotes(checkNotNull(notesTextArea).textProperty().get());
            poplavok.setDirection(checkNotNull(directionComboBox).getSelectionModel().getSelectedItem());

            returnPoplavok = poplavok;
            checkNotNull(stage).close();
       } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "AccountAddDialog close Error: " + e, ButtonType.OK);
            LOGGER.error("AccountAddDialog close Error:", e);
            alert.showAndWait();
        }
    }

    @Nullable Poplavok getReturnPoplavok() {
        return returnPoplavok;
    }
}
