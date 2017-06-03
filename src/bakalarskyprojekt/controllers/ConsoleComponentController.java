/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.controllers;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

/**
 *
 * @author Kofola
 */
public class ConsoleComponentController implements Initializable, Console {

    @FXML
    private AnchorPane anchp;
    @FXML
    private Button executeBtn;
    @FXML
    private ToggleButton btnSaveConsoleOutput;
    @FXML
    private TextField commandField;

    private final TextArea consoleTextArea;

    private DataOutputStream extOutputStream;

    public ConsoleComponentController() {
        consoleTextArea = new TextArea();
        consoleTextArea.setEditable(false);
        consoleTextArea.setWrapText(true);

       // consoleOutputStream = new ConsoleOutputStream(consoleTextArea);

    }

    @Override
    public void setExtOutputStream(DataOutputStream extOutputStream) {
        this.extOutputStream = extOutputStream;
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void initialize(URL location, ResourceBundle resources) {

        anchp.getChildren().add(consoleTextArea);
        AnchorPane.setTopAnchor(consoleTextArea, 0d);
        AnchorPane.setLeftAnchor(consoleTextArea, 0d);
        AnchorPane.setRightAnchor(consoleTextArea, 0d);
        AnchorPane.setBottomAnchor(consoleTextArea, 0d);
        System.out.println("Initialize called");

    }

    @Override
    public void println(String text) {//called from this and server thread //doesnt have to be synchronized since whole thing is executed on JFXAT  anyway
        Platform.runLater(() -> {
            consoleTextArea.appendText(LocalTime.now()+" "+text + "\n");
        });
    }

    @Override
    public void refresh() {//when starts new 
        Platform.runLater(() -> {
            consoleTextArea.setText("");
            extOutputStream=null;
        });

    }

    @FXML
    private void exteClick() {

        String command = commandField.getText();
        System.out.println("Command typed:" + command);
        switch(command){
            case "/end":{
                System.out.println("End from server ");
//            try {
//                bakalarskyprojekt.BakalarskyProjektMain.getModel().getSocket().close();
//                System.out.println("Closed socket");
//            } catch (IOException ex) {
//                System.out.println("Cant close");
//                Logger.getLogger(ConsoleComponentController.class.getName()).log(Level.SEVERE, null, ex);
//            }
            }
        }
        println("[USER] "+command);
        commandField.setText("");

        try {
            extOutputStream.writeUTF(command);
            
        } catch (IOException ex) {
            if(ex instanceof SocketException){
                println("Failed to execute-Socket closed.");
            }
            Logger.getLogger(ConsoleComponentController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    @FXML
    private void saveOutputOnAction(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save console Output");
        //Set extension filter
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".txt file (*.txt)", "*.txt"));
        fileChooser.setInitialFileName("consoleOutput");
        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);
        
        if (file != null) {
            try (FileWriter fileWriter = new FileWriter(file)) {//maybe wrap inside buffered one
                fileWriter.write(consoleTextArea.getText());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        btnSaveConsoleOutput.setSelected(false);
    }
        

}
