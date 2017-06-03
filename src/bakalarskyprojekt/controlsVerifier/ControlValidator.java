/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.controlsVerifier;

import java.io.File;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextInputControl;

/**
 *
 * @author Kofola
 */
public class ControlValidator implements Validator {
    
    private final SimpleBooleanProperty isValidStateProperty;
    private String invalidatedStyle;
    private String invalidMessage;
    private Control control;
    
    public ControlValidator(Control control, String message) {
        isValidStateProperty = new SimpleBooleanProperty();
        invalidatedStyle = "-fx-border-color:RED;";
        
        this.control = control;
        this.invalidMessage = message;
        
        validateInit(control);
        validate();//should be safe
    }
    
    private void validateInit(Control control) {
        if (control instanceof TextInputControl) {
            ((TextInputControl) control).textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
                System.out.println("changed value");
                validate();
            });
        }
        if(control instanceof ChoiceBox){
            ((ChoiceBox) control).getSelectionModel().selectedItemProperty().addListener(new ChangeListener<File>() {
                @Override
                public void changed(ObservableValue<? extends File> observable, File oldValue, File newValue) {
                    System.out.println("changed value");
                    validate();
                }
            });
        }
        
        
        control.disableProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                if (control instanceof TextInputControl) {
                    ((TextInputControl) control).setText("");
                }
                control.setStyle("");
            }else{
                validate();
            }
        });
    }
    
    @Override
    public void validate() {
        
    }
    
    @Override
    public boolean isValid() {
        return isValidStateProperty.get();
    }
    
    @Override
    public void setInvalidatedStyle(String invalidatedStyle) {
        this.invalidatedStyle = invalidatedStyle;
    }
    
    @Override
    public void setValidatorGroup(ValidatorGroup vg) {
        if (!vg.getValidators().contains(this)) {
            vg.getValidators().add(this);
        }
    }
    
    @Override
    public void setMessage(String msg) {
        this.invalidMessage = msg;
    }
    
    @Override
    public String getMessage() {
        return invalidMessage;
    }
    
    @Override
    public void setValid(boolean validity) {
        isValidStateProperty.set(validity);
        updateView(validity);
    }
    
    private void updateView(boolean validity) {
        if (!control.isDisable()) {
            if (validity) {
                control.setStyle("");
            } else {
                control.setStyle(invalidatedStyle);
            }
        }
        
    }
    
}
