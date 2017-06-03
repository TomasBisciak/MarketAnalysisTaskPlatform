/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.tray;

import bakalarskyprojekt.utils.Info;
import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author tomas
 */
public class Tray {

    private Stage stage;
    private TrayIcon icon;
    private MenuItem schedulerEnableDisable;

    public MenuItem getSchedulerEnableDisable() {
        return schedulerEnableDisable;
    }

    public Tray(Stage stage, String imagePath) {

        this.stage = stage;
        hookTray(imagePath);
    }

    public void showMessage(String message, TrayIcon.MessageType TYPE) {
        icon.displayMessage("BakalarskyProjekt", message, TYPE);
    }

    private void hookTray(String image) {

        try {
            icon = new TrayIcon(ImageIO.read(getClass().getResourceAsStream(image)), "BakalarskyProjekt " + Info.APP_VERSION,
                    null);
            SystemTray.getSystemTray().add(icon);

        } catch (AWTException | IOException ex) {
            ex.printStackTrace();
        }

    }

    public void setTrayImage(String imagePath) {
        try {
            icon.setImage(ImageIO.read(getClass().getResourceAsStream(imagePath)));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
