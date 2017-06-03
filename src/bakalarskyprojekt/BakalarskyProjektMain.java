/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt;

import bakalarskyprojekt.db.DbUtil;
import bakalarskyprojekt.model.MainModel;
import bakalarskyprojekt.model.BPTaskProps;
import bakalarskyprojekt.tray.Tray;
import bakalarskyprojekt.utils.Info;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Kofola
 */
public class BakalarskyProjektMain extends Application {

    private static MainModel model;
    private static Tray tray;

    public static MainModel getModel() {
        return model;
    }

    //MAIN THREAD Logic executor. FIFO
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void runLater(Runnable runnable) {
        executor.execute(runnable);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource(Info.FXML_MAIN_VIEW));

        Scene scene = new Scene(root);

        stage.setScene(scene);
        stage.setTitle("Bakalarsky projekt " + Info.APP_VERSION);
        stage.getScene().getWindow().setOnCloseRequest((WindowEvent event) -> {
            //check if test active
            if (getModel().getTaskActiveProperty().get()) {
                String[] options = {"Exit",
                    "Stay"};
                ChoiceDialog<String> cd = new ChoiceDialog<>(options[1], options);
                cd.setTitle("Test in progress, are you sure you want to close application?");

                Optional<String> response = cd.showAndWait();
                if (!response.get().equals(options[0])) {
                    event.consume();
                }
            }
        });
        if (SystemTray.isSupported()) {
            tray = new Tray(stage, Info.IMG_TRAY_ICON);
        }
        stage.setMaximized(true);
        stage.show();
    }

    public static Tray getTray() {
        return tray;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        //clear out some less important resources
        System.out.println("STOP CALLED");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        Files.createDirectories(Paths.get(Info.TEST_DIR));
        Files.createDirectories(Paths.get(Info.TEST_C_PLUS_PLUS_DIR));
        Files.createDirectories(Paths.get(Info.TEST_JAVA_DIR));
        Files.createDirectories(Paths.get(Info.TEST_JAVA_LIB_DIR));

        try {
            DbUtil.createTables();
           // insertDebugData();
        } catch (SQLException ex) {
            ex.printStackTrace();
            System.out.println("Failed to create database");
        }

        ///stop tests if not stopped byitself
        hookShutdownHook();
        model = MainModel.getInstance();
        System.out.println("JVM:"+System.getProperty("sun.arch.data.model") );

    }

    private static void insertDebugData() {//testing
        //insert test data
        List<File> l1 = new ArrayList<>();
        l1.add(new File("C:/test/test.csv"));
        List<File> l2 = new ArrayList<>();
        l2.add(new File("C:/test/test.json"));

        try {
            DbUtil.storeTest(new BPTaskProps("jsut test", l1, l2, new File("C:/test/testFile.json"), BPTaskProps.APP_TYPE_CPP, null));
            System.out.println("working");

        } catch (SQLException ex) {
            System.out.println("not working");
            Logger.getLogger(BakalarskyProjektMain.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            DbUtil.updateTest(new BPTaskProps(1, "jsut test-edited2", l1, l2, new File("C:/test/testFile.json"), BPTaskProps.APP_TYPE_CPP, null));
            for (BPTaskProps test : DbUtil.getTests()) {
                System.out.println("TEST ID :" + test.getId());
            }
        } catch (SQLException ex) {
            Logger.getLogger(BakalarskyProjektMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //execute before shutdown
    private static void hookShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("ShutdownHook execution:");
            //CLEANUP 
            if (model.getProcessExecutor() != null && model.getProcessExecutor().getWatchdog() != null) {
                System.out.println("ShutdownHook executor not null destroying");
                try {
                    model.getProcessExecutor().getWatchdog().destroyProcess();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if (model.getServer().getServerSocket() != null) {
                    try {
                        //kill server if active at the moment of jvm kill
                        model.getServer().getServerSocket().close();
                    } catch (IOException ex) {
                        System.out.println("Problem at closing server maybe closed already");
                        Logger.getLogger(BakalarskyProjektMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }

            System.out.println("ShutdownHook executed.");

        }));
    }

}
