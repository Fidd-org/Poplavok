package com.poplavok.forms;

import com.poplavok.data.dao.LevelDAO;
import com.poplavok.data.dao.LoanDAO;
import com.poplavok.data.dao.LoanInfo;
import com.poplavok.data.model.Currency;
import com.poplavok.data.model.Direction;
import com.poplavok.data.model.Level;
import com.poplavok.data.model.Loan;
import com.poplavok.data.model.Repayment;
import com.poplavok.data.utils.DBUtil;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static com.flower.fxutils.JavaFxUtils.autoResizeTableColumns;
import static com.google.common.base.Preconditions.checkNotNull;

public class RepaySettleDebtDialog extends TabPane {
    final static Logger LOGGER = LoggerFactory.getLogger(RepaySettleDebtDialog.class);

    @FXML @Nullable TextField repaymentDebtTextField;
    @FXML @Nullable Button repaymentDebtCurrencyButton;
    @FXML @Nullable TextField availableForRepaymentTextField;
    @FXML @Nullable Label availableForRepaymentLabel;
    @FXML @Nullable TextField toRepayRepaymentTextField;
    @FXML @Nullable Label toRepayRepaymentCurrencyLabel;
    @FXML @Nullable Button repaymentButton;

    @Nullable FilteredList<LoanInfo> loans;
    @FXML @Nullable TableView<LoanInfo> loansTableView;

    @Nullable Stage stage;

    @Nullable Long levelId = null;
    Level level;
    final Direction tradeDirection;
    @Nullable volatile Repayment returnRepayment = null;

    public RepaySettleDebtDialog(Level level, String ticker, @Nullable BigDecimal price, @Nullable BigDecimal fee, Direction tradeDirection) {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("RepaySettleDebtDialog.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.tradeDirection = tradeDirection;

        this.level = level;
        //checkNotNull(addButton).textProperty().set("Update Level");
        levelId = level.getId();

        List<LoanInfo> loanInfoList = DBUtil.connectGetResultAndClose(sess -> LoanDAO.getLoanInfosByDestinationLevel(sess, level));
        this.loans = new FilteredList<>(FXCollections.observableArrayList(loanInfoList));

        checkNotNull(loansTableView).setItems(this.loans);
        autoResizeTableColumns(loansTableView);
    }

    public void repaymentDebtCurrency() {
        //
    }

    public void repayment() {
        //
    }

    public void okClose() {
        //
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @Nullable
    public Repayment getReturnRepayment() {
        return returnRepayment;
    }
}
