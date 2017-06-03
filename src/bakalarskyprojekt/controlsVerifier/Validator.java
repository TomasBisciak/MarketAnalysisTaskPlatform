/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.controlsVerifier;

/**
 *
 * @author Kofola
 */
public interface Validator {
    
    public void validate();
    public boolean isValid();
    public void setInvalidatedStyle(String style);
    public void setValidatorGroup(ValidatorGroup vg);
    public void setMessage(String msg);
    public String getMessage();
    public void setValid(boolean validity);
    
}
