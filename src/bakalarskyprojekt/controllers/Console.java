/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.controllers;

import java.io.DataOutputStream;

/**
 *
 * @author Kofola
 */
public interface Console {
    
     public void println(String string);
     public void setExtOutputStream(DataOutputStream extOutputStream);
     public void refresh();
     
}
