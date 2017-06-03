/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.controllers;

import bakalarskyprojekt.model.TradeDataProperties;
import bakalarskyprojekt.model.TradeDataPropertiesWrapper;
import bakalarskyprojekt.controlsVerifier.ControlValidator;
import bakalarskyprojekt.controlsVerifier.ValidatorGroup;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * FXML Controller class
 *
 * @author Kofola
 */
public class TradeDataSettingsController implements Initializable {

    @FXML
    private TextField txtFieldNumOfTrades;
    private ControlValidator validatorTxtFieldNumOfTrades;

    @FXML
    private TextField txtFieldSLPercentageS;
    private ControlValidator validatorTxtFieldSLPercentageS;

    @FXML
    private TextField txtFieldSLPercentageL;
    private ControlValidator validatorTxtFieldSLPercentageL;

    @FXML
    private TextField txtFieldSLExact;
    private ControlValidator validatorTxtFieldSLExact;

    @FXML
    private TextField txtFieldStopLossRangeMin;
    private ControlValidator validatorTxtFieldStopLossRangeMin;

    @FXML
    private TextField txtFieldStopLossRangeMax;
    private ControlValidator validatorTxtFieldStopLossRangeMax;

    @FXML
    private TextField txtFieldTargetRangeMin;
    private ControlValidator validatorTxtFieldTargetRangeMin;

    @FXML
    private TextField txtFieldTargetRangeMax;
    private ControlValidator validatorTxtFieldTargetRangeMax;

    @FXML
    private TextField txtFieldAdditionalParams;
    private ControlValidator validatorTxtFieldAdditionalParams;

    @FXML
    private CheckBox checkBoxExact;

    @FXML
    private Button btnSubmit;

    private final ValidatorGroup validatorGroup;
    private final ValidatorGroup validatorGroupSLChoice;
    private final ValidatorGroup validatorGroupExactChoice;

    private TradeDataPropertiesWrapper tradeDataPropertiesWrapper;
    private SimpleBooleanProperty tradeDataPropertiesProp;

    /**
     * Changes to latest selected tradeDataProperties object.Object parameter is passed as reference and will be changed
     *
     * @param tradeDataPropertiesWrapper
     * @param tradeDataPropertiesProp
     */
    public void setTradeProperties(TradeDataPropertiesWrapper tradeDataPropertiesWrapper, SimpleBooleanProperty tradeDataPropertiesProp) {
        this.tradeDataPropertiesWrapper = tradeDataPropertiesWrapper;
        this.tradeDataPropertiesProp = tradeDataPropertiesProp;
    }

    public TradeDataSettingsController() {
        validatorGroup = new ValidatorGroup();
        validatorGroupSLChoice = new ValidatorGroup();
        validatorGroupExactChoice = new ValidatorGroup();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //num of trades
        validatorTxtFieldNumOfTrades = new ControlValidator(txtFieldNumOfTrades, "Number of trades invalid") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                try {
                    int input = Integer.valueOf(txtFieldNumOfTrades.getText());
                    if (input > 0) {
                        setValid(true);
                    }
                } catch (NumberFormatException ex) {
                    setValid(false);
                    //ex.printStackTrace();
                }
            }

        };
        validatorTxtFieldNumOfTrades.setValidatorGroup(validatorGroup);

        //percentage short
        validatorTxtFieldSLPercentageS = new ControlValidator(txtFieldSLPercentageS, "Short percentage invalid") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                try {
                    double input = Double.valueOf(txtFieldSLPercentageS.getText());
                    if (input >= 0 && input <= 100) {
                        double other = 100 - input;
                        if (txtFieldSLPercentageS.isFocused()) {
                            txtFieldSLPercentageL.setText(other + "");
                        }

                    }

                    setValid(true);
                } catch (NumberFormatException ex) {
                    setValid(false);
                    ex.printStackTrace();
                }

            }

        };
        validatorTxtFieldSLPercentageS.setValidatorGroup(validatorGroupSLChoice);

        //percentage long 
        validatorTxtFieldSLPercentageL = new ControlValidator(txtFieldSLPercentageL, "Long percentage invalid") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                try {
                    double input = Double.valueOf(txtFieldSLPercentageL.getText());
                    if (input >= 0 && input <= 100) {
                        double other = 100 - input;
                        if (txtFieldSLPercentageL.isFocused()) {
                            txtFieldSLPercentageS.setText(other + "");
                        }

                    }

                    setValid(true);
                } catch (NumberFormatException ex) {
                    setValid(false);
                    ex.printStackTrace();
                }

            }

        };
        validatorTxtFieldSLPercentageL.setValidatorGroup(validatorGroupSLChoice);

        ///text field exact
        validatorTxtFieldSLExact = new ControlValidator(txtFieldSLExact, "Exact invalid") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                try {
                    int input = Integer.valueOf(txtFieldSLExact.getText());
                    if (input < Integer.valueOf(txtFieldNumOfTrades.getText())) {
                        setValid(true);
                    }
                } catch (NumberFormatException ex) {
                    setValid(false);
                    //ex.printStackTrace();
                }
            }

        };
        validatorTxtFieldSLExact.setValidatorGroup(validatorGroupExactChoice);

        //check box exact
        checkBoxExact.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    txtFieldSLExact.setDisable(false);
                    txtFieldSLPercentageS.setDisable(true);
                    txtFieldSLPercentageL.setDisable(true);
                } else {
                    txtFieldSLExact.setDisable(true);
                    txtFieldSLPercentageS.setDisable(false);
                    txtFieldSLPercentageL.setDisable(false);
                }
            }
        });

        //text field stop loss range min
        validatorTxtFieldStopLossRangeMin = new ControlValidator(txtFieldStopLossRangeMin, "StopLoss range Min invalid") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                try {
                    //BigDecimal slmin = new BigDecimal(txtFieldSLRangeMin.getText().toCharArray());
                    // BigDecimal slmax = new BigDecimal(txtFieldSLRangeMax.getText().toCharArray());
                    double slmin = Double.valueOf(txtFieldStopLossRangeMin.getText());
                    if (slmin >= 1) {

                        try {
                            double slmax = Double.valueOf(txtFieldStopLossRangeMax.getText());
                            if (slmin < slmax) {
                                setValid(true);
                            } else {
                                setValid(false);
                            }
                        } catch (Exception e) {
                            setValid(true);
                        }

                    } else {
                        setValid(false);
                    }

                } catch (NumberFormatException ex) {
                    setValid(false);
                    //ex.printStackTrace();
                }
            }

        };

        validatorTxtFieldStopLossRangeMin.setValidatorGroup(validatorGroup);

        //text field stop loss range max
        validatorTxtFieldStopLossRangeMax = new ControlValidator(txtFieldStopLossRangeMax, "StopLoss range Max invalid") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                try {
                    //BigDecimal slmin = new BigDecimal(txtFieldSLRangeMin.getText().toCharArray());
                    // BigDecimal slmax = new BigDecimal(txtFieldSLRangeMax.getText().toCharArray());
                    double slmax = Double.valueOf(txtFieldStopLossRangeMax.getText());
                    if (slmax >= 1) {

                        try {
                            double slmin = Double.valueOf(txtFieldStopLossRangeMin.getText());
                            if (slmax > slmin) {
                                setValid(true);
                            } else {
                                setValid(false);
                            }
                        } catch (Exception e) {
                            setValid(true);
                        }

                    } else {
                        setValid(false);
                    }

                } catch (NumberFormatException ex) {
                    setValid(false);
                    //ex.printStackTrace();
                }
            }

        };

        validatorTxtFieldStopLossRangeMax.setValidatorGroup(validatorGroup);

        //target min
        validatorTxtFieldTargetRangeMin = new ControlValidator(txtFieldTargetRangeMin, "StopLoss range Max invalid") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                try {
                    //BigDecimal slmin = new BigDecimal(txtFieldSLRangeMin.getText().toCharArray());
                    // BigDecimal slmax = new BigDecimal(txtFieldSLRangeMax.getText().toCharArray());
                    int tRmin = Integer.valueOf(txtFieldTargetRangeMin.getText());
                    if (tRmin >= 1) {

                        try {
                            int trMax = Integer.valueOf(txtFieldTargetRangeMax.getText());
                            if (tRmin <= trMax) {
                                setValid(true);
                            } else {
                                setValid(false);
                            }
                        } catch (Exception e) {
                            setValid(true);
                        }

                    } else {
                        setValid(false);
                    }

                } catch (NumberFormatException ex) {
                    setValid(false);
                    //ex.printStackTrace();
                }
            }

        };

        validatorTxtFieldTargetRangeMin.setValidatorGroup(validatorGroup);

        //target max
        validatorTxtFieldTargetRangeMax = new ControlValidator(txtFieldTargetRangeMax, "Target range Max invalid") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                try {
                    //BigDecimal slmin = new BigDecimal(txtFieldSLRangeMin.getText().toCharArray());
                    // BigDecimal slmax = new BigDecimal(txtFieldSLRangeMax.getText().toCharArray());
                    int tRmax = Integer.valueOf(txtFieldTargetRangeMax.getText());
                    if (tRmax >= 1) {

                        try {
                            int tRmin = Integer.valueOf(txtFieldTargetRangeMin.getText());
                            if (tRmax >= tRmin) {
                                setValid(true);
                            } else {
                                setValid(false);
                            }
                        } catch (Exception e) {
                            setValid(true);
                        }

                    } else {
                        setValid(false);
                    }

                } catch (NumberFormatException ex) {
                    setValid(false);
                    //ex.printStackTrace();
                }
            }

        };

        validatorTxtFieldTargetRangeMax.setValidatorGroup(validatorGroup);

//            @FXML
//    private TextField txtFieldAdditionalParams;
//    private ControlValidator validatorTxtFieldAdditionalParams;
        //parameters
        validatorTxtFieldAdditionalParams = new ControlValidator(txtFieldAdditionalParams, "Parmas invalid") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                //validateParameters
                // if (TradeGenerator.validateArguments(txtFieldAdditionalParams.getText().split(" "))) {
                if (TradeDataProperties.validateArguments(txtFieldAdditionalParams.getText().split(" "))) {
                    setValid(true);
                } else {
                    setValid(false);
                }

            }

        };

        validatorTxtFieldAdditionalParams.setValidatorGroup(validatorGroup);

        btnSubmit.setOnAction((ActionEvent event) -> {
            if (submitOnAction()) {
                ((Stage) btnSubmit.getScene().getWindow()).fireEvent(new WindowEvent(btnSubmit.getScene().getWindow(), WindowEvent.WINDOW_CLOSE_REQUEST));
            }

        });
    }

    @FXML
    private boolean submitOnAction() {

        int shortTrades;
        int longTrades;

        if (checkDataInputValidity()) {
            if (checkBoxExact.isSelected()) {
                shortTrades = Integer.valueOf(txtFieldSLExact.getText());
            } else {
                shortTrades = (int) Math.floor((Double.valueOf(txtFieldNumOfTrades.getText()) / 100) * Double.valueOf(txtFieldSLPercentageS.getText()));
            }
            longTrades = Integer.valueOf(txtFieldNumOfTrades.getText()) - shortTrades;

            tradeDataPropertiesWrapper.setTradeDataProperties(new TradeDataProperties(shortTrades, longTrades,
                    Integer.valueOf(txtFieldStopLossRangeMin.getText()),
                    Integer.valueOf(txtFieldStopLossRangeMax.getText()),
                    Integer.valueOf(txtFieldTargetRangeMin.getText()),
                    Integer.valueOf(txtFieldTargetRangeMax.getText()),
                    txtFieldAdditionalParams.getText().split(" ")
            ));
            tradeDataPropertiesProp.set(true);
            System.out.println("Valid input");
            // get a handle to the stage
            Stage stage = (Stage) btnSubmit.getScene().getWindow();
            // do what you have to do

            stage.close();
            return true;
        } else {

            //todo notify user or do some error visible verification
            System.out.println("Invalid input");
            return false;
        }

        //check for validator group validity
        //put data where it needs to be put.
    }

    private boolean checkDataInputValidity() {

        if (validatorGroup.isValid()) {
            if (checkBoxExact.isSelected()) {
                if (validatorGroupExactChoice.isValid()) {
                    return true;
                }
            } else {
                if (validatorGroupSLChoice.isValid()) {
                    return true;
                }
            }
        }

        return false;
    }

}
