/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.controlsVerifier;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kofola
 */
public class ValidatorGroup {
    
    private final List<ControlValidator> validators;
    
    public ValidatorGroup(){
        validators=new ArrayList<>();
    }
    
    public List<ControlValidator> getValidators(){
        return validators;
    }
    
    public boolean isValid(){
        for(ControlValidator cv:validators){
            if(!cv.isValid()){
                return false;
            }
        }
        return true;
    }
    
}
