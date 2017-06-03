package bakalarskyprojekt.controllers;

import bakalarskyprojekt.BakalarskyProjektMain;
import bakalarskyprojekt.controlsVerifier.ControlValidator;
import bakalarskyprojekt.db.DbUtil;
import bakalarskyprojekt.model.BPTaskProps;
import bakalarskyprojekt.networking.TaskServerThread;
import bakalarskyprojekt.utils.Info;
import bakalarskyprojekt.utils.FileUtils;
import bakalarskyprojekt.utils.SoundPlayer;
import bakalarskyprojekt.utils.Util;
import bplib.communication.Result;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.controlsfx.control.NotificationPane;

/**
 *
 * @author Kofola import javafx.stage.FileChooser; import javafx.stage.Stage;
 *
 */
public class MainViewController implements Initializable {

    private ToggleGroup tradeDataToggleGroup;
    private ToggleGroup languageToggleGroup;
    private ToggleGroup toggleGroupGraphBtns;

    private BPTaskProps currentTest;

    @FXML
    private Label lblTime;
    @FXML
    private TableView tableView;

    @FXML
    private Button importPriceDataBtn;

    @FXML
    private TextField priceDataFileTextField;
    private ControlValidator priceDataValidator;

    @FXML
    private RadioButton radioJava;
    @FXML
    private RadioButton radioCplus;
    @FXML
    private Button executeTestBtn;

    @FXML
    private TextField tradeDataFileTextField;
    private ControlValidator tradeDataValidator;

    @FXML
    private Button importTradeDataBtn;

    @FXML
    private ChoiceBox choiceBxTestJava;
    private ControlValidator choiceTestJavaValidator;
    @FXML
    private ChoiceBox choiceBxTestCpp;
    private ControlValidator choiceTestCppValidator;

    @FXML
    private TextField txtFieldParams;
    private ControlValidator paramsValidator;

    @FXML
    private GridPane gridPaneTaskHolderStyle;

    @FXML
    private VBox vBoxConsoleHolder;
    @FXML
    private Button btnTestConfirm;
    @FXML
    private Button btnExportTest;

    @FXML
    private Label infoPanelThreadCountLabel;
    @FXML
    private Label lblAllocMem;
    @FXML
    private TextField txtFieldTestName;
    private ControlValidator txtFieldTestNameValidator;

    @FXML
    private CheckBox chckBoxSaveDb;
    @FXML
    private Label lblTestName;
    @FXML
    private ImageView imgViewCheckTestReady;

    @FXML
    private TextField txtFieldJvmParam;
    private ControlValidator jvmParamValidator;
    @FXML
    private ProgressBar taskProgressBar;

    @FXML
    private Button stopExecutionBtn;

    private final SimpleBooleanProperty taskReady;

    private BPTaskProps taskProps;

    private int applicationType;

    public MainViewController() {
        taskReady = new SimpleBooleanProperty();
        System.out.println("Main view controller construct is on jfx:" + Platform.isFxApplicationThread());
    }

    private long taskExecutionTime;

    private void submitTask(BPTaskProps testProps) {

        if (!BakalarskyProjektMain.getModel().getTaskActiveProperty().get()) {
            long startTime = System.nanoTime();

            BakalarskyProjektMain.runLater(() -> {

                try {
                    timedTask = true;
                    Platform.runLater(() -> {
                        BakalarskyProjektMain.getModel().getTaskActiveProperty().set(true);
                    });

                    BakalarskyProjektMain.getModel().setServer(new TaskServerThread(33221, consoleComponentController));

                    System.out.println("STARTING SERVER THREAD");
                    BakalarskyProjektMain.getModel().getServer().setPriority(Thread.MAX_PRIORITY);
                    //server.setDaemon(true);
                    BakalarskyProjektMain.getModel().getServer().start();

                    //start client
                    String command = testProps.generateCmdString();
                    System.out.println("Command to be executed:" + testProps.generateCmdString());
                    CommandLine cmdLine = CommandLine.parse(command);
                    ExecuteResultHandler erh = new ExecuteResultHandler() {

                        @Override
                        public void onProcessComplete(int exitValue) {

                            System.out.println("Process complete");

                            taskExecutionTime = (System.nanoTime() - startTime);
                            System.out.println("Execution time :" + taskExecutionTime + " ns");
                            BakalarskyProjektMain.getModel().getResult().getData().put("Execution time (ns)", taskExecutionTime + "");
                            //CREATE POST REPORT , 
                            //CREATE testreport object with all the required data
                            Platform.runLater(() -> {
                                BakalarskyProjektMain.getModel().getTaskActiveProperty().set(false);
                                if (BakalarskyProjektMain.getModel().getResult() != null) {
                                    System.out.println("Gonna show result data");
                                    showResultData(BakalarskyProjektMain.getModel().getResult());
                                } else {
                                    System.out.println("No result from task execution to show.");
                                }

                            });
                            timedTask = false;

                            System.out.println("Task  finished execution");
                            //set graph data back
                            BakalarskyProjektMain.getModel().setReport(null);

                            if (checkBoxSoundNotif.isSelected()) {
                                try {
                                    SoundPlayer.playSound(SoundPlayer.SOUND_TASK_FINISHED);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                if (SystemTray.isSupported()) {
                                    BakalarskyProjektMain.getTray().showMessage("Task finished!", TrayIcon.MessageType.INFO);
                                }
                            }
                            taskProgressBar.setVisible(false);

                        }

                        @Override
                        public void onProcessFailed(ExecuteException e) {
                            System.out.println("Process  failed");
                            showPaneNotification("Process failed.", null);
                            Platform.runLater(() -> {
                                BakalarskyProjektMain.getModel().getTaskActiveProperty().set(false);
                            });
                            timedTask = false;
                            taskProgressBar.setVisible(false);
                        }
                    };

                    processExecutor.setWatchdog(new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT));
                    processExecutor.execute(cmdLine, erh);

                    System.out.println("Task starting execution");

                } catch (IOException ex) {
                    showPaneNotification("Failed to start task execution.", null);
                    Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
                    Platform.runLater(() -> {
                        BakalarskyProjektMain.getModel().getTaskActiveProperty().set(false);
                    });
                    timedTask = false;
                    taskProgressBar.setVisible(false);
                }

            });

        } else {
            System.out.println("TASK ALREADY EXECUTING");
        }

    }

    private final Executor processExecutor = new DefaultExecutor();

    @FXML
    private Button btnSideMenu;
    @FXML
    private BorderPane sideMenuHolder;
    private boolean isShownSideMenu = true;
    private final Label btnSideMenuLabel = new Label("Hide menu");
    @FXML
    private CheckBox checkBoxSoundNotif;
    @FXML
    private Label progressPercentLbl;
    @FXML
    private Label lblJVMParam;

    @FXML
    private Button btnVersion;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        btnVersion.setTooltip(new Tooltip("Visit download page for latest version of this application."));

        new Thread(() -> {

        }).start();
        try {
            if (isNewVersionAvailable()) {
                System.out.println("DEBUG:Application is out of date.");
                btnVersion.setVisible(true);
                btnVersion.setText("Out of date.");
                btnVersion.setStyle("-fx-background-radius:0;-fx-background-color:#f39c12");
            } else {
                System.out.println("Up to date.");
                btnVersion.setVisible(false);
                // btnVersion.setText("Application is up to date.");
                // btnVersion.setStyle("-fx-background-radius:0;-fx-background-color:#16a085");
            }

        } catch (IOException ex) {
            System.out.println("DEBUG:Failed to check version");
            btnVersion.setVisible(true);
            btnVersion.setText("Failed to check version");
            btnVersion.setStyle("-fx-background-radius:0;-fx-background-color:red");
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

        btnTableViewResultExport.disableProperty().bind(Bindings.isEmpty(tableView.getItems()));

        progressPercentLbl.visibleProperty().bind(taskProgressBar.visibleProperty());
        taskProgressBar.setVisible(false);
        taskProgressBar.progressProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            if ((double) newValue == 0d) {
                taskProgressBar.setVisible(false);
                progressPercentLbl.setText("N/A");
            } else {
                taskProgressBar.setVisible(true);
                progressPercentLbl.setText(new Double(((double) newValue * 100)).intValue() + "%");

            }
        });

        rbtnDiskSpace.setSelected(true);
        rbtnDiskSpace.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            rbtnDiskSpace.setSelected(true);
        });
        rbtnDiskSpace.setFocusTraversable(false);

        //maybe animate later
        btnSideMenuLabel.setRotate(-90);
        btnSideMenu.setGraphic(new Group(btnSideMenuLabel));
        btnSideMenu.addEventHandler(ActionEvent.ACTION, (ActionEvent event) -> {
            if (isShownSideMenu) {
                sideMenuHolder.setPrefWidth(0);
                btnSideMenuLabel.setText("Show menu");
                //hide controls or something
                isShownSideMenu = false;
            } else {
                sideMenuHolder.setPrefWidth(130);
                btnSideMenuLabel.setText("Hide menu");
                isShownSideMenu = true;
            }
        });

        BakalarskyProjektMain.getModel().setProcessExecutor(processExecutor);
        System.out.println("Main view controller initialize is on jfx:" + Platform.isFxApplicationThread());
        languageToggleGroup = new ToggleGroup();
        radioJava.setToggleGroup(languageToggleGroup);
        radioCplus.setToggleGroup(languageToggleGroup);

        radioJava.setSelected(true);
        applicationType = BPTaskProps.APP_TYPE_JAVA;

        initNotifPane();

        chckBoxSaveDb.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                txtFieldTestName.setDisable(false);
                lblTestName.setDisable(false);
            } else {
                txtFieldTestName.setDisable(true);
                lblTestName.setDisable(true);
            }

        });

        refreshJavaTestSelection();
        choiceBxTestJava.setConverter(new StringConverter() {

            @Override
            public String toString(Object object) {
                return ((File) object).getName();
            }

            @Override
            public Object fromString(String string) {
                return null;
            }
        });

        refreshCppTestSelection();
        choiceBxTestCpp.setConverter(new StringConverter() {

            @Override
            public String toString(Object object) {
                return ((File) object).getName();
            }

            @Override
            public Object fromString(String string) {
                return null;
            }
        });

        priceDataValidator = new ControlValidator(priceDataFileTextField, "Price data") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                if (priceDataFileTextField.getText() != null && !priceDataFileTextField.getText().equals("")) {
                    //validate if files are valid.

                    for (String str : priceDataFileTextField.getText().split("\\*")) {
                        if (!Files.exists(Paths.get(str)) || Files.isDirectory(Paths.get(str), LinkOption.NOFOLLOW_LINKS)) {
                            taskReady.set(false);
                            setValid(false);
                            showPaneNotification("File does not exists. File:" + str, null);
                            return;
                        }
                        if (str.contains(" ")) {
                            taskReady.set(false);
                            setValid(false);
                            showPaneNotification("FileName cannot contain spaces. File:" + str, null);
                            return;
                        }
                    }
                    setValid(true);
                } else {
                    taskReady.set(false);
                    setValid(false);
                }
            }

        };

        tradeDataValidator = new ControlValidator(tradeDataFileTextField, "Price data") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                if (tradeDataFileTextField.getText() != null && !tradeDataFileTextField.getText().equals("")) {

                    for (String str : tradeDataFileTextField.getText().split("\\*")) {
                        if (!tradeDataFileTextField.getText().equals("null")) {//if equals null its ok.
                            if (!Files.exists(Paths.get(str)) || Files.isDirectory(Paths.get(str), LinkOption.NOFOLLOW_LINKS)) {
                                taskReady.set(false);
                                setValid(false);
                                showPaneNotification("File does not exists. File:" + str, null);
                                return;
                            }
                            if (str.contains(" ")) {
                                taskReady.set(false);
                                setValid(false);
                                showPaneNotification("FileName/Path cannot contain spaces. File:" + str, null);
                                return;
                            }
                        }

                    }

                    setValid(true);
                } else {
                    taskReady.set(false);
                    setValid(false);
                }
            }

        };
        paramsValidator = new ControlValidator(txtFieldParams, "Parameters") {

            @Override
            public void validate() {
                if (validateArguments(txtFieldParams.getText().split(" "), ARG_GEN) || txtFieldParams.getText().equals("")) {
                    setValid(true);
                } else {
                    taskReady.set(false);
                    setValid(false);
                }
            }

        };

        jvmParamValidator = new ControlValidator(txtFieldJvmParam, "JVM Parameters") {

            @Override
            public void validate() {
                if (validateArguments(txtFieldJvmParam.getText().split(" "), ARG_JVM)) {
                    setValid(true);

                } else {
                    taskReady.set(false);
                    setValid(false);
                }
            }

        };

        choiceTestJavaValidator = new ControlValidator(choiceBxTestJava, "Java Test") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                if (radioJava.isSelected()) {
                    if (!choiceBxTestJava.getSelectionModel().isEmpty()) {
                        setValid(true);
                    } else {
                        taskReady.set(false);
                        setValid(false);
                    }
                } else {
                    setValid(true);
                }

            }

        };

        choiceTestCppValidator = new ControlValidator(choiceBxTestCpp, "C++ Test") {

            @Override
            public void validate() {
                super.validate(); //To change body of generated methods, choose Tools | Templates.
                if (radioCplus.isSelected()) {
                    if (!choiceBxTestCpp.getSelectionModel().isEmpty()) {
                        setValid(true);
                    } else {
                        taskReady.set(false);
                        setValid(false);
                    }
                } else {

                    setValid(true);
                }

            }

        };
        taskReady.addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                executeTestBtn.disableProperty().set(false);
                btnTestConfirm.setStyle("-fx-background-radius:0;-fx-background-color:#27ae60");
                btnExportTest.disableProperty().set(false);
                imgViewCheckTestReady.setVisible(true);
            } else {
                executeTestBtn.disableProperty().set(true);
                btnExportTest.disableProperty().set(true);
                btnTestConfirm.setStyle("-fx-background-radius:0;");
                imgViewCheckTestReady.setVisible(false);
            }
        });

        languageToggleGroup.selectedToggleProperty().addListener((ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) -> {

            if (newValue == radioJava) {                //java
                applicationType = BPTaskProps.APP_TYPE_JAVA;
                choiceTestJavaValidator.validate();
                choiceTestCppValidator.setValid(true);
                txtFieldJvmParam.setDisable(false);
                lblJVMParam.setDisable(false);
            } else {                                    //cpp
                applicationType = BPTaskProps.APP_TYPE_CPP;
                choiceTestCppValidator.validate();
                choiceTestJavaValidator.setValid(true);;
                txtFieldJvmParam.setDisable(false);
                lblJVMParam.setDisable(false);
            }

        });

        BakalarskyProjektMain.getModel().getTaskActiveProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (newValue) {
                gridPaneTaskHolderStyle.setStyle("-fx-background-color:#16a085");
                showPaneNotification("Test execution started:" + taskProps.toString(), null);
                stopExecutionBtn.setVisible(true);
            } else {
                gridPaneTaskHolderStyle.setStyle("-fx-background-color:GRAY");
                stopExecutionBtn.setVisible(false);
            }
        });

        txtFieldTestNameValidator = new ControlValidator(txtFieldTestName, "Parameters") {

            @Override
            public void validate() {
                //check if some name like this is already in database!<-- database,
                try {
                    //Path filePath = Paths.get(txtFieldTestName.getText());//valid name?
                    //name in database?
//                    if(DbUtil.isValidTestName()){
//                        
//                    }
                    setValid(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Invalid file name.");
                    setValid(false);
                }

            }

        };
        txtFieldTestName.setText("Test_" + LocalDateTime.now().toString().replace(":", "-") + "");

        BakalarskyProjektMain.getModel().getTaskActiveProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

            }
        });

        //ADDITIONAL INITIALIZATION
        initConsoleComponent();
        initStatistics();
        initUIRefreshThread(1000);
    }

    @FXML
    private Button btnClearTableView;

    @FXML
    private void clearTableView() {
        tableView.getItems().removeAll(tableView.getItems());
        tableView.getColumns().removeAll(tableView.getColumns());

    }

    @FXML
    private void clearTableCompareView() {
        vBoxCompareHolder.setVisible(true);
        tableViewResultCompare.getItems().removeAll(tableViewResultCompare.getItems());
        tableViewResultCompare.getColumns().removeAll(tableViewResultCompare.getColumns());
    }

    @FXML
    private Button btnTableViewResultExport;

    @FXML
    private void exportResultDataOnAction() {
        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".JSON file (*.json)", "*.json"));
        // fileChooser.setInitialFileName(taskProps.getName());
        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(Util.objectToJsonString(BakalarskyProjektMain.getModel().getResult()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public void showResultData(Result result) {
        //get data from model
        Map<String, String> map = result.getData();

        // use fully detailed type for Map.Entry<String, String> 
        TableColumn<Map.Entry<String, String>, String> columnTitle = new TableColumn<>("Title");
        columnTitle.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                // this callback returns property for just one cell, you can't use a loop here
                // for first column we use key
                return new SimpleStringProperty(p.getValue().getKey());
            }
        });

        TableColumn<Map.Entry<String, String>, String> columnValue = new TableColumn<>("Value");
        columnValue.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                // for second column we use value
                return new SimpleStringProperty(p.getValue().getValue());
            }
        });
        columnValue.setPrefWidth(150);
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                ObservableList<Map.Entry<String, String>> items = FXCollections.observableArrayList(map.entrySet());
                tableView.getColumns().setAll(columnTitle, columnValue);
                tableView.getItems().setAll(items);
                System.out.println("Result data set");
            }
        });

    }

    @FXML
    private VBox vBoxCompareHolder;

    private class ResultCompareWrapper {

        private final Result result;
        private final Result resultCompare;

        public ResultCompareWrapper(Result result, Result resultCompare) {
            this.result = result;
            this.resultCompare = resultCompare;
        }

        /**
         * @return the result
         */
        public Result getResult() {
            return result;
        }

        /**
         * @return the resultCompare
         */
        public Result getResultCompare() {
            return resultCompare;
        }

    }
    TableColumn<Map.Entry<String, String[]>, String> columnTitle;
    TableColumn<Map.Entry<String, String[]>, String[]> columnCompare;
    TableColumn<Map.Entry<String, String[]>, String> columnValueCompare;
    TableColumn<Map.Entry<String, String[]>, String> columnValue;

    private void showResultCompareData(ResultCompareWrapper resultWrapper) {//functionality to compare , colors of table cell etc

        if (resultWrapper.getResult() != null) {
            vBoxCompareHolder.setVisible(false);
            //get data from model
            tableViewResultCompare.getColumns().removeAll(columnTitle, columnCompare, columnValueCompare, columnValue);// JUST ADDED POSSIBLE BREAK POINT DIDNT TESTED

            Map<String, String[]> map = new HashMap<>();

            for (String key : resultWrapper.getResult().getData().keySet()) {
                map.put(key, new String[]{resultWrapper.getResult().getData().get(key), resultWrapper.getResultCompare().getData().get(key)});
            }

            // use fully detailed type for Map.Entry<String, String> 
            columnTitle = new TableColumn<>("Title");

            columnTitle.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String[]>, String>, ObservableValue<String>>() {
                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String[]>, String> p) {
                    return new SimpleStringProperty(p.getValue().getKey());
                }
            });
            columnCompare = new TableColumn<>("Comparsion");
            //WOW
            columnCompare.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String[]>, String[]>, ObservableValue<String[]>>() {

                @Override
                public ObservableValue<String[]> call(TableColumn.CellDataFeatures<Map.Entry<String, String[]>, String[]> param) {
                    return new SimpleObjectProperty<>(param.getValue().getValue());
                }
            });
            columnCompare.setCellFactory(new Callback<TableColumn<Map.Entry<String, String[]>, String[]>, TableCell<Map.Entry<String, String[]>, String[]>>() {

                @Override
                public TableCell<Map.Entry<String, String[]>, String[]> call(TableColumn<Map.Entry<String, String[]>, String[]> param) {

                    return new TableCell<Map.Entry<String, String[]>, String[]>() {

                        @Override
                        public void updateItem(String[] item, boolean empty) {
                            super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                            if (item == null || empty) {
                                setText(null);
                                setStyle("");
                                System.out.println("Just empty");
                            } else {
                                System.out.println(" NENI EMPTYYY");
                                setStyle("-fx-alignment:CENTER;");
                                String cellText;
                                if (item[0] == null || item[1] == null) {
                                    System.out.println("No data");
                                    cellText = "No data";
                                } else if (item[0].equals(item[1])) {
                                    System.out.println("Match");
                                    cellText = "Match";
                                } else {
                                    System.out.println("no match");
                                    cellText = "No match";
                                }

                                switch (cellText) {
                                    case "Match": {
                                        setText("Match");
                                        setStyle("-fx-alignment:CENTER;-fx-background-color:#27ae60;-fx-text-fill:WHITE");
                                        break;
                                    }
                                    case "No match": {
                                        setText("No match");
                                        setStyle("-fx-alignment:CENTER;-fx-background-color:#c0392b;-fx-text-fill:WHITE");
                                        break;
                                    }
                                    case "No data": {
                                        setText("No data");
                                        setStyle("-fx-alignment:CENTER;-fx-background-color:#7f8c8d;-fx-text-fill:WHITE");
                                        break;
                                    }
                                    default: {
                                        setText(null);
                                        setStyle("");
                                    }
                                }
                            }
                        }

                    };
                }
            });

            columnValueCompare = new TableColumn<>("Compared value");
            columnValueCompare.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String[]>, String>, ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String[]>, String> p) {
                    // for second column we use value
                    return new SimpleStringProperty(p.getValue().getValue()[1]);
                }
            });
            columnValueCompare.setPrefWidth(150);

            columnValue = new TableColumn<>("Current value");
            columnValue.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String[]>, String>, ObservableValue<String>>() {

                @Override
                public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String[]>, String> p) {
                    // for second column we use value
                    return new SimpleStringProperty(p.getValue().getValue()[0]);
                }
            });
            columnValue.setPrefWidth(150);

            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    ObservableList<Map.Entry<String, String[]>> items = FXCollections.observableArrayList(map.entrySet());
                    tableViewResultCompare.getColumns().setAll(columnTitle, columnCompare, columnValueCompare, columnValue);
                    tableViewResultCompare.getItems().setAll(items);
                    System.out.println("Compare  Result data set");
                }
            });
        }

    }

    @FXML
    private void compareResultOnAction() {
        System.out.println("Hereeeee");
        showResultCompareData(new ResultCompareWrapper(BakalarskyProjektMain.getModel().getResult(), resultCompare));
    }

    @FXML
    private void importResultCompareDB() {

    }

    @FXML
    private void stopExecutionOnAction() {
        BakalarskyProjektMain.getModel().getServer().setServerAliveStatus(false); // testExecutor.sh
    }

    private volatile boolean timedTask;
    SimpleLongProperty timedTaskTotalSec = new SimpleLongProperty(0);

    @FXML
    private Label lblTaskStatus;

    //TODO maybe pause execution here
    private void initUIRefreshThread(long uiRefreshRate) {

        @SuppressWarnings("SleepWhileInLoop")
        Thread uiRefreshThread = new Thread(() -> {

            int iterNum = 0;
            timedTaskTotalSec.addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                lblTime.setText(String.format("%02d:%02d:%02d", timedTaskTotalSec.get() / 3600, (timedTaskTotalSec.get() % 3600) / 60, timedTaskTotalSec.get() % 60));
            });

            while (true) {
                int tmpIter = iterNum;
                Platform.runLater(() -> {

                    taskProgressBar.setProgress((double) BakalarskyProjektMain.getModel().getReport().getProgress() / 100);
                    lblTaskStatus.setText(BakalarskyProjektMain.getModel().getReport().getState());

                    long avgDataRateMinute = 0;
                    int avgThreadMinute = 0;
                    long avgMemoryMinute = 0;
                    long avgDataRateHour = 0;
                    int avgThreadHour = 0;
                    long avgMemoryHour = 0;

                    infoPanelThreadCountLabel.setText("Threads:" + (Thread.activeCount()));
                    lblAllocMem.setText("JVM memory:" + (Runtime.getRuntime().totalMemory() / 1024) / 1024 + "MB");
                    //if timed add second
                    if (timedTask) {
                        timedTaskTotalSec.set(timedTaskTotalSec.get() + (uiRefreshRate / 1000));
                    } else {
                        timedTaskTotalSec.set(0);
                    }

                    addGraphSeriesSpeedToSeconds(BakalarskyProjektMain.getModel().getReport().getDataRate());
                    addGraphSeriesThreadToSeconds(BakalarskyProjektMain.getModel().getReport().getThreadNum());
                    addGraphSeriesMemoryToSeconds(BakalarskyProjektMain.getModel().getReport().getJvmMem());

                    //MINUTES
                    if ((tmpIter % 60) == 0) {
                        //ADD AVERAGE SPEED PER 1 MINUTE, average speed  60 seconds previous
                        //datarate
                        for (XYChart.Data xydata : (ObservableList<XYChart.Data>) getSpeedSeriesSeconds().getData()) {
                            avgDataRateMinute += (long) xydata.getYValue();
                        }
                        //threads
                        for (XYChart.Data xydata : (ObservableList<XYChart.Data>) getThreadSeriesSeconds().getData()) {
                            avgThreadMinute += (int) xydata.getYValue();
                        }
                        //memory
                        for (XYChart.Data xydata : (ObservableList<XYChart.Data>) getMemorySeriesSeconds().getData()) {
                            avgMemoryMinute += (long) xydata.getYValue();
                        }

                        //dataRate
                        if (getSpeedSeriesSeconds().getData().size() != 0) {
                            avgDataRateMinute /= getSpeedSeriesSeconds().getData().size();
                            addGraphSeriesSpeedToMinutes(avgDataRateMinute);
                        }
                        //thread
                        if (getThreadSeriesSeconds().getData().size() != 0) {
                            avgThreadMinute /= getThreadSeriesSeconds().getData().size();
                            addGraphSeriesThreadToMinutes(avgThreadMinute);
                        }
                        //memory
                        if (getMemorySeriesSeconds().getData().size() != 0) {
                            avgMemoryMinute /= getMemorySeriesSeconds().getData().size();
                            addGraphSeriesMemoryToMinutes(avgMemoryMinute);
                        }

                        avgDataRateMinute = 0;
                        avgThreadMinute = 0;
                        avgMemoryMinute = 0;
                    }

                    //THREAD GRAPH
                    //HOURS
                    if ((tmpIter % 3600) == 0) {//hours
                        //add average speed of 60 minutes
                        //avg dr
                        for (XYChart.Data xydata : (ObservableList<XYChart.Data>) getSpeedSeriesMinutes().getData()) {
                            avgDataRateHour += (long) xydata.getYValue();
                        }
                        //avg thread
                        for (XYChart.Data xydata : (ObservableList<XYChart.Data>) getThreadSeriesMinutes().getData()) {
                            avgThreadHour += (int) xydata.getYValue();
                        }
                        //avg mem
                        for (XYChart.Data xydata : (ObservableList<XYChart.Data>) getMemorySeriesMinutes().getData()) {
                            avgMemoryHour += (long) xydata.getYValue();
                        }

                        //add to speed hours
                        if (getSpeedSeriesMinutes().getData().size() != 0) {
                            avgDataRateHour /= getSpeedSeriesMinutes().getData().size();
                            addGraphSeriesSpeedToHours(avgDataRateHour);
                        }
                        //add to threeead hours
                        if (getThreadSeriesMinutes().getData().size() != 0) {
                            avgThreadHour /= getThreadSeriesMinutes().getData().size();
                            addGraphSeriesThreadToHours(avgThreadHour);
                        }

                        //add to memory hours
                        if (getMemorySeriesMinutes().getData().size() != 0) {
                            avgMemoryHour /= getMemorySeriesMinutes().getData().size();
                            addGraphSeriesMemoryToHours(avgMemoryHour);
                        }

                        avgDataRateHour = 0;
                        avgThreadHour = 0;
                        avgMemoryHour = 0;
                    }

                    if ((tmpIter % 5) == 0) {
                        updateDiskSpaceInfoView();
                    }

                });

                try {
                    Thread.sleep(uiRefreshRate);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
                iterNum++;
            }
        });
        uiRefreshThread.setName("UIRefreshThread");
        uiRefreshThread.setDaemon(true);
        uiRefreshThread.setPriority(Thread.MIN_PRIORITY);
        uiRefreshThread.start();

    }

    private boolean isTestSettingsValid() {

        if (priceDataValidator.isValid() && tradeDataValidator.isValid() && paramsValidator.isValid()) {

            Toggle selectedLanguage = languageToggleGroup.getSelectedToggle();

            if (selectedLanguage.equals(radioJava)) {
                if (!choiceTestJavaValidator.isValid()) {
                    return false;
                }
            } else if (selectedLanguage.equals(radioCplus)) {
                if (!choiceTestCppValidator.isValid()) {
                    return false;
                }
            }

            if (chckBoxSaveDb.isSelected()) {
                if (!txtFieldTestNameValidator.isValid()) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     *
     * @param filePaths string of paths with delimiter
     * @param regex regex to split string
     * @param verifyExistence checks wether file exists and is not a directory
     * @param failOnFailure returns null if failOnFailure is true and one of the files failed to be added to files list, does nothing if verifyExistence is false.
     * @return
     */
    private List<File> stringToFileList(String filePaths, String regex, boolean verifyExistence, boolean failOnFailure) {
        List<File> files = new ArrayList<>();

        for (String path : filePaths.split(regex)) {
            File f = new File(path);
            if (verifyExistence) {

                if (f.exists() && !f.isDirectory()) {
                    files.add(f);
                } else {
                    if (failOnFailure) {
                        return null;
                    }
                }
            } else {
                files.add(f);
            }

        }

        return files;
    }

    /**
     * Get file that is selected from UI
     *
     * @return Returns Test file that is selected from UI.If selected test is not valid returns null.Calls validator of choicebox control to chec kfor validity of Test file.
     */
    private File getSelectedTestFile() {

        if (languageToggleGroup.getSelectedToggle().equals(radioJava)) {
            if (choiceTestJavaValidator.isValid()) {
                return (File) choiceBxTestJava.getSelectionModel().getSelectedItem();
            }
        }
        if (languageToggleGroup.getSelectedToggle().equals(radioCplus)) {
            if (choiceTestCppValidator.isValid()) {
                return (File) choiceBxTestCpp.getSelectionModel().getSelectedItem();
            }
        }
        return null;

    }

    @FXML
    private void importJsonOnAction() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Import test file (*.json)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".JSON file (*.json)", "*.json"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                setTestProps((BPTaskProps) Util.jsonStringToObject(new String(Files.readAllBytes(Paths.get(file.getPath()))), BPTaskProps.class));

                showPaneNotification("Test loaded.", Info.IMG_ALERT_INFO);
            } catch (IOException ex) {
                showPaneNotification("Invalid Test file.", Info.IMG_ALERT_ERROR);
                Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private boolean isNewVersionAvailable() throws IOException {
        //TODO change this to downlaod this file on another thread , probably
        URL url = new URL("https://raw.githubusercontent.com/TomasBisciak/VersionCheck/master/README.md");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(url.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            if (inputLine.contains("tbbp")) {
                System.out.println("Application version:" + Info.APP_VERSION);
                double lv = Double.valueOf(inputLine.split(",")[1]);
                System.out.println("Latest version version:" + lv);
                return lv > Info.APP_VERSION;
            }
            System.out.println(inputLine);
        }
        in.close();
        return false;
    }

    @FXML
    private void exportJsonOnAction() {

        FileChooser fileChooser = new FileChooser();

        //Set extension filter
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".JSON file (*.json)", "*.json"));
        fileChooser.setInitialFileName(taskProps.getName());
        //Show save file dialog
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(Util.objectToJsonString(taskProps));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    @FXML
    private void btnTestConfirmOnAction() {
        if (isTestSettingsValid()) {
            System.out.println("Valid data");

            List<File> tradeDataFiles;
            List<File> priceDataFiles = stringToFileList(priceDataFileTextField.getText(), "\\*", true, true);
            if (tradeDataFileTextField.getText().equals("null")) {
                tradeDataFiles = new ArrayList<>();
                tradeDataFiles.add(new File("null"));
            } else {
                tradeDataFiles = stringToFileList(tradeDataFileTextField.getText(), "\\*", true, true);
            }

            File testFile = getSelectedTestFile();

            if (priceDataFiles != null && tradeDataFiles != null) {
                //  if (testProps == null) {//not loaded //TODO not sure if we will load like this , id doesnt have to be even in json not sure

                if (taskProps != null) {// you can do it check

                    if (chckBoxSaveDb.isSelected()) {//name required

                        if (taskProps.getId() != BPTaskProps.NO_ID) {//has id
                            taskProps = new BPTaskProps(taskProps.getId(), txtFieldTestName.getText(), priceDataFiles, tradeDataFiles, testFile, applicationType, txtFieldParams.getText());
                            try {
                                DbUtil.updateTest(taskProps);
                                System.out.println("Updated data");
                            } catch (SQLException ex) {
                                System.out.println("failed to update data");
                                Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } else {//doesnt have id
                            taskProps = new BPTaskProps(txtFieldTestName.getText(), priceDataFiles, tradeDataFiles, testFile, applicationType, txtFieldParams.getText());
                            try {
                                taskProps.setId(DbUtil.storeTest(taskProps));
                                System.out.println("saved data");
                            } catch (SQLException ex) {
                                System.out.println("failed to save data");
                                Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    } else {//no need to save , then no id or name required
                        taskProps = new BPTaskProps(priceDataFiles, tradeDataFiles, testFile, applicationType, txtFieldParams.getText());
                    }

                } else {

                    if (chckBoxSaveDb.isSelected()) {//name required /wanna save //doesnth ave id since testProps is null
                        taskProps = new BPTaskProps(txtFieldTestName.getText(), priceDataFiles, tradeDataFiles, testFile, applicationType, txtFieldParams.getText());
                        try {
                            taskProps.setId(DbUtil.storeTest(taskProps));
                            System.out.println("saved data");
                        } catch (SQLException ex) {
                            System.out.println("failed to save data");
                            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    } else {//not required name //donst wanna save
                        taskProps = new BPTaskProps(priceDataFiles, tradeDataFiles, testFile, applicationType, txtFieldParams.getText());
                    }

                }

                taskReady.set(true);

            } else {
                showPaneNotification("Invalid file data.", null);
            }

        } else {
            showPaneNotification("Invalid test data.", null);

        }
    }

    private static final int ARG_GEN = 0;
    private static final int ARG_JVM = 1;

    /**
     *
     * @param args passed into application
     * @return returns if valid arguments
     */
    public static boolean validateArguments(String[] args, int ARG_TYPE) {
        //holds which values were sucessfully parsed/used
        if (args.length != 0) {

            switch (ARG_TYPE) {
                case ARG_GEN: {
                    //switch case to check parameters
                    for (int i = 0; i < args.length; i += 2) {//check -paramId
                        //check if contains value after parameter if not insta invalidate.
                        try {
                            System.out.println("Param val:" + args[i + 1]); //doesnt matter what we call any access to non existing will throw npe/ioob // what i want

                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                        switch (args[i]) {
                            case "-s": {

                                try {
                                    int seed = Integer.valueOf(args[i + 1]);
                                } catch (NumberFormatException ex) {
                                    return false;
                                }
                                break;
                            }
                            case "-mm": {
                                try {
                                    boolean mmVal = Boolean.valueOf(args[i + 1]);
                                } catch (Exception e) {
                                    return false;
                                }
                                break;
                            }
                            //support for additional parameters

                            default: {
                                //regex check
                                System.out.println("In default checking :" + args[i]);
                                if (args[i].matches("-c_\\S+")) {//not sure about performance here bud should be ok , be weary
                                    System.out.println("Custom parameter:" + args[i]);
                                } else {
                                    System.out.println("Invalid parameter \" " + args[i] + " \"");
                                    return false;
                                }

                            }

                        }

                    }

                    break;
                }
                case ARG_JVM: { //usefull site http://jvmmemory.com/
                    //REGEX VALIDATOR  NEEDED FOR THIS //IMPLEMENT LATER OR NOT FOR FIRST VERSION //not sure if needed to use regex  matcher etc , or just matches method.
                    for (String arg : args) {
                        switch (arg) {

                        }
                    }
                    break;
                }
                default: {
                    System.out.println("Wrong function param flag");
                }
            }

        }
        return true;

    }

    @FXML
    private void refreshJavaTestSelection() {
        choiceBxTestJava.getItems().removeAll(choiceBxTestJava.getItems());
        choiceBxTestJava.getItems().addAll(FileUtils.getFilesInDir(Info.TEST_JAVA_DIR, "jar"));
    }

    @FXML
    private void refreshCppTestSelection() {
        choiceBxTestCpp.getItems().removeAll(choiceBxTestCpp.getItems());
        choiceBxTestCpp.getItems().addAll(FileUtils.getFilesInDir(Info.TEST_C_PLUS_PLUS_DIR, "exe"));
    }

    private List<File> selectedPriceDataFiles;
    private File latestDirPriceData;

    @FXML
    private void importPriceDataOnAction() {
        FileChooser fileChooser = new FileChooser();
        if (latestDirPriceData != null) {
            if (latestDirPriceData.exists() && latestDirPriceData.isDirectory()) {
                fileChooser.setInitialDirectory(latestDirPriceData);
            }
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv", "*.csv"));
        fileChooser.setTitle("Select price data");
        selectedPriceDataFiles = fileChooser.showOpenMultipleDialog(new Stage());
        String paths = "";
        boolean isFirst = true;
        for (File file : selectedPriceDataFiles) {
            if (isFirst) {
                paths += "" + file.getPath();
                latestDirPriceData = new File(file.getParent());
                System.out.println("Parent :" + latestDirPriceData.getPath());
            } else {
                paths += "*" + file.getPath();
            }
            isFirst = false;
        }
        priceDataFileTextField.setText(paths);

    }

    private List<File> selectedTradeDataFiles;
    private File latestDirTradeData;

    @FXML
    private void importTradeDataOnAction() {
        FileChooser fileChooser = new FileChooser();
        if (latestDirTradeData != null) {
            if (latestDirTradeData.exists() && latestDirTradeData.isDirectory()) {
                fileChooser.setInitialDirectory(latestDirTradeData);
            }
        }
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv", "*.csv"));
        fileChooser.setTitle("Select trade data");
        selectedTradeDataFiles = fileChooser.showOpenMultipleDialog(new Stage());
        String paths = "";
        boolean isFirst = true;
        for (File file : selectedTradeDataFiles) {
            if (isFirst) {
                paths += "" + file.getPath();
            } else {
                paths += "*" + file.getPath();
            }
            isFirst = false;
        }
        tradeDataFileTextField.setText(paths);

    }

//    @FXML
//    private void tradeSettingsOnAction() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource(Info.FXML_TRADE_SETTINGS));
//            BorderPane root = (BorderPane) loader.load(
//                    MainViewController.class
//                    .getResourceAsStream(Info.FXML_TRADE_SETTINGS));
//            ((TradeDataSettingsController) loader.getController()).setTradeProperties(tradeDataPropertiesWrapper,tradeDataPropertiesProp);//DATA stored here
//            Scene scene = new Scene(root);
//            Stage stage = new Stage();
//            stage.setOnCloseRequest((WindowEvent event) -> {
//                tradeSettingsToggleButton.setSelected(false);
//            });
//
//            stage.setScene(scene);
//
//            stage.setTitle(
//                    "Trade data settings");
//            stage.show();
//        } catch (IOException ex) {
//            Logger.getLogger(MainViewController.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    private void setTestProps(BPTaskProps testProps) {
        this.taskProps = testProps;
        StringBuilder paths = new StringBuilder();

        boolean isFirst = true;
        for (File file : testProps.getPriceDataFiles()) {
            if (isFirst) {
                paths.append(file.getPath());
                latestDirPriceData = new File(file.getParent());
                System.out.println("Parent :" + latestDirPriceData.getPath());
            } else {
                paths.append("*").append(file.getPath());
            }
            isFirst = false;
        }

        priceDataFileTextField.setText(paths.toString());

        paths.delete(0, paths.length());
        isFirst = true;

        for (File file : testProps.getTradeDataFiles()) {
            if (isFirst) {
                paths.append(file.getPath());
                try {
                    latestDirTradeData = new File(file.getParent());
                    System.out.println("Parent :" + latestDirTradeData.getPath());
                } catch (Exception e) {// "null" case for tradeData / invalid path
                    e.printStackTrace();
                }

            } else {
                paths.append("*").append(file.getPath());
            }
            isFirst = false;
        }
        tradeDataFileTextField.setText(paths.toString());

        switch (testProps.getApplicationType()) {
            case BPTaskProps.APP_TYPE_JAVA: {
                radioJava.setSelected(true);
                refreshJavaTestSelection();
                for (Object file : choiceBxTestJava.getItems()) {
                    if (((File) file).equals(testProps.getTestFile())) {
                        choiceBxTestJava.getSelectionModel().select(file);//should work
                    }

                }

                break;
            }
            case BPTaskProps.APP_TYPE_CPP: {
                radioCplus.setSelected(true);
                refreshCppTestSelection();

                break;
            }

        }

        //set PARAM txtfield
        txtFieldParams.setText(testProps.getParameters());

    }

    @FXML
    private void importTestDbOnAction() {

        //just to get off of jfxat for constructor
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Info.FXML_DB_IMPORT_VIEW));
            BorderPane root = (BorderPane) loader.load(
                    MainViewController.class
                    .getResourceAsStream(Info.FXML_DB_IMPORT_VIEW));

            Scene scene = new Scene(root);
            Stage stage = new Stage();
            stage.setOnCloseRequest((WindowEvent event) -> {
                BPTaskProps tProps;
                if ((tProps = ((ImportDataDbController) loader.getController()).getTestProps()) != null) {
                    setTestProps(tProps);
                }
            });

            ((ImportDataDbController) loader.getController()).setStage(stage);
            stage.setScene(scene);

            stage.setTitle(
                    "Import(db)");
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @FXML
    private ToggleButton databaseToggleButton;

    private boolean isDatabaseViewShown;
    private Stage databaseViewStage;

    @FXML
    private void showDatabaseView() {

        if (!isDatabaseViewShown) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Info.FXML_DATABASE_VIEW));
                BorderPane root = (BorderPane) loader.load(
                        MainViewController.class
                        .getResourceAsStream(Info.FXML_DATABASE_VIEW));

                Scene scene = new Scene(root);
                databaseViewStage = new Stage();

                databaseViewStage.setScene(scene);
                databaseViewStage.setOnCloseRequest((WindowEvent event) -> {
                    databaseToggleButton.setSelected(false);
                    isDatabaseViewShown = false;
                });
                databaseViewStage.setTitle("BP Database View");
                databaseViewStage.show();
                isDatabaseViewShown = true;

            } catch (IOException ex) {
                Logger.getLogger(MainViewController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            databaseViewStage.requestFocus();
        }

    }

    @FXML
    private ToggleButton btnPriceData;

    @FXML
    private void openPriceLinkOnAction() {
        try {
            FileUtils.openWebpage(new URI("http://www.histdata.com/download-free-forex-data/"));
        } catch (URISyntaxException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            btnPriceData.setDisable(true);
        }
    }

    @FXML
    private ToggleButton btnJVMParamCreator;

    @FXML
    private void openJVMParamCreateOnAction() {
        try {
            FileUtils.openWebpage(new URI("http://www.jvmmemory.com/"));
        } catch (URISyntaxException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            btnJVMParamCreator.setDisable(true);
        }
    }

    @FXML
    private Button importResultFile;
    @FXML
    private TableView tableViewResultCompare;

    private Result resultCompare;

    @FXML
    private void importResultCompareFileOnAction() {
        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Import result file (*.json)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(".JSON file (*.json)", "*.json"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            try {
                resultCompare = Util.jsonStringToObject(new String(Files.readAllBytes(Paths.get(file.getPath()))), Result.class);
                showResultCompareData(new ResultCompareWrapper(BakalarskyProjektMain.getModel().getResult(), resultCompare));
            } catch (Exception ex) {
                showPaneNotification("Invalid result file.", Info.IMG_ALERT_ERROR);
                Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @FXML
    private void saveResultDbOnAction() {

    }

    ConsoleComponentController consoleComponentController;

    private void initConsoleComponent() {
        try {
            FXMLLoader loader = new FXMLLoader();
            vBoxConsoleHolder.getChildren().add(loader.load(getClass().getResourceAsStream(Info.FXML_CONSOLE_COMPONENT_VIEW)));
            consoleComponentController = ((ConsoleComponentController) loader.getController());//GET CONTROLLER FROM FXML loader
            //  Scene scene = new Scene(root);

            //  Scene scene = new Scene(root);
            //  databaseViewStage = new Stage();
            //  databaseViewStage.setScene(scene);
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private SysInfoViewController SysInfoViewController;

    private void initCSInfoComponent() {
        try {
            FXMLLoader loader = new FXMLLoader();
            statHolder.setCenter(loader.load(getClass().getResourceAsStream(Info.FXML_SYSINFO_VIEW)));
            SysInfoViewController = ((SysInfoViewController) loader.getController());//GET CONTROLLER FROM FXML loader
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Node getView(String fxmlPath) {
        try {
            return new FXMLLoader(getClass().getResource(fxmlPath)).load();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @FXML
    private void showAboutView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Info.FXML_ABOUT_VIEW));
            BorderPane root = (BorderPane) loader.load(
                    MainViewController.class
                    .getResourceAsStream(Info.FXML_ABOUT_VIEW));
            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setScene(scene);

            stage.setTitle(
                    "BP About");
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void showChecksumView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Info.FXML_CHECKSUM_VIEW));
            BorderPane root = (BorderPane) loader.load(
                    MainViewController.class
                    .getResourceAsStream(Info.FXML_CHECKSUM_VIEW));
            Scene scene = new Scene(root);
            Stage stage = new Stage();

            stage.setScene(scene);

            stage.setTitle(
                    "File verification");
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(MainViewController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private ToggleButton tBnInfoPriceData;
    @FXML
    private ToggleButton tBnInfoTradeData;
    @FXML
    private ToggleButton tBnInfoParam;
    @FXML
    private ToggleButton tBtnInfoJvmParam;

    private List<Boolean> hintShown = new ArrayList<Boolean>() {
        {
            add(false);
            add(false);
            add(false);
            add(false);
        }
    };

    @FXML
    private void showHintPriceData() {
        showHintView("Price data input field.\n"
                + "Field format: <Path>*<Path>"
                + "Restriction: No spaces allowed.\n"
                + "Data format: \n"
                + "    1. Generic ASCII in M1 Bars (1 Minute Bars):\n"
                + "    Row Fields:\n"
                + "    DateTime Stamp;Bar OPEN Bid Quote;Bar HIGH Bid Quote;Bar LOW Bid Quote;Bar CLOSE Bid Quote;Volume\n"
                + "    TimeZone: Eastern Standard Time (EST) time-zone WITHOUT Day Light Savings \n"
                + "    DateTime Stamp Format:\n"
                + "    YYYYMMDD HHMMSS\n"
                + "    \n"
                + "    Legend:\n"
                + "    YYYY – Year\n"
                + "    MM – Month (01 to 12)\n"
                + "    DD – Day of the Month\n"
                + "    HH – Hour of the day (in 24h format)\n"
                + "    MM – Minute\n"
                + "    SS – Second, in this case it will be allways 00"
                + " 2. Generic ASCII in Ticks:\n"
                + "   Row Fields:\n"
                + "    DateTime Stamp,Bid Quote,Ask Quote,Volume\n"
                + "    TimeZone: Eastern Standard Time (EST) time-zone WITHOUT Day Light Savings \n"
                + "    DateTime Stamp Format:\n"
                + "    YYYYMMDD HHMMSSNNN\n"
                + "    \n"
                + "    Legend:\n"
                + "    YYYY – Year\n"
                + "    MM – Month (01 to 12)\n"
                + "    DD – Day of the Month\n"
                + "    HH – Hour of the day (in 24h format)\n"
                + "    MM – Minute\n"
                + "    SS – Second\n"
                + "    NNN – Millisecond", tBnInfoPriceData, 0);
    }

    @FXML
    private void showHintTradeData() {
        showHintView("Trade data input field.\n"
                + "Field format:<Path>*<Path>\n"
                + "Restriction: No spaces allowed.\n"
                + "Data format:\n"
                + "CSV File - Type(Short/Long(Integer)),MarketName(String),Amount(BigDecimal),Leverage(Integer),"
                + "StopLoss(BigDecimal),(Target price(BigDecimal),EntryPrice((BigDecimal)),ExitPrice/ClosePrice((BigDecimal)),"
                + "Open time(String(yyyyMMdd HHmmss),Close/Exit time(String(yyyyMMdd HHmmss)))"
                + "Date time format:yyyyMMdd HHmmss\n"
                + "yyyy - Year\n"
                + "MM - month of year\n"
                + "dd - day of month\n"
                + "HH - hour of day (0-23)\n"
                + "mm - minute of hour\n"
                + "ss - second of minute\n", tBnInfoTradeData, 1);
    }

    @FXML
    private void showHintParam() {
        showHintView("Parameters text field.\n"
                + "Field Format:<key> <value> <key> <value>"
                + "Custom parameter: c_"
                + "[Several non-whitespace characters]\n"
                + "Task supported:\n"
                + "-mm <true/false>:minimal memory cache load\n"
                + "-s <integer>:seed", tBnInfoParam, 2);
    }

    @FXML
    private void showHintJVMParam() {
        showHintView("JVM Parameters text field.\n"
                + "No validation of text property.\n"
                + "For JDK 8 please see the Windows, Solaris, Linux and Mac OS X reference pages.\n"
                + "Usefull third party website: http://jvmmemory.com/ ,click at\"JVM param Creator\" located at the useful link section of the menu.", tBtnInfoJvmParam, 3);
    }

    private void showHintView(String hint, ToggleButton tb, int hintIndex) {
        if (!hintShown.get(hintIndex)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(Info.FXML_HINT_VIEW));
                BorderPane root = (BorderPane) loader.load(
                        MainViewController.class
                        .getResourceAsStream(Info.FXML_HINT_VIEW));
                ((HintViewController) loader.getController()).setTextAreaText(hint);

                Scene scene = new Scene(root);
                Stage stage = new Stage();

                stage.setScene(scene);
                stage.setOnCloseRequest((WindowEvent event) -> {
                    tb.setSelected(false);
                    hintShown.set(hintIndex, false);
                });

                stage.setTitle(
                        "Hint");
                stage.show();
                hintShown.set(hintIndex, true);
            } catch (IOException ex) {
                Logger.getLogger(MainViewController.class
                        .getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @FXML
    private void executeOnAction() {
        if (!BakalarskyProjektMain.getModel().getTaskActiveProperty().get()) {
            submitTask(taskProps);
        }

    }

    private static NotificationPane notifPane;
    @FXML
    private BorderPane mViewHolder;
    @FXML
    private HBox mView;

    private void initNotifPane() {
        notifPane = new NotificationPane(mView);
        notifPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
        notifPane.setShowFromTop(true);

        notifPane.setOnMouseClicked((MouseEvent event) -> {
            notifPane.hide();
        });
        mViewHolder.setCenter(notifPane);

    }
//prerobit na executor , A PRIDAT MOZNOST CANCELACIE ANIMACIE AK JE VIAC TASKOV V EXEKUTOROVY ASI TAK NEAK
    private static final long DEFAULT_MAIN_NOTIF_TIMEOUT = 4000l;
    private static boolean isShownNotif;
    private static volatile boolean isNotifPending;

    @SuppressWarnings("SleepWhileInLoop")
    public static final void showPaneNotification(String text, String graphics) {

        new Thread(() -> {
            while (true) {
                if (isShownNotif) {
                    isNotifPending = true;
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(BakalarskyProjektMain.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    isShownNotif = true;

                    Platform.runLater(() -> {
                        if (graphics != null) {
                            notifPane.setGraphic(new ImageView(graphics));
                        }
                        if (notifPane.isShowing()) {
                            notifPane.hide();
                            notifPane.show(text);
                        } else {
                            notifPane.show(text);
                        }
                    });

                    for (int i = 0; i < DEFAULT_MAIN_NOTIF_TIMEOUT / 200 && !isNotifPending; i++) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(BakalarskyProjektMain.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    notifPane.hide();
                    isShownNotif = false;
                    isNotifPending = false;
                    return;
                }
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(MainViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    // --------------GRAPHING-------------
    //graph holders
    @FXML
    private BorderPane statHolder;
    @FXML
    private HBox hBoxStatHolderMenuRight;

    //info selection
    @FXML
    private ToggleButton tBtnDataRate;
    @FXML
    private ToggleButton tBtnCSInfo;
    @FXML
    private ToggleButton tBtnThreads;
    @FXML
    private ToggleButton tBtnMemory;

    private ToggleGroup tgStatButtonsGroup;

    private final static NumberAxis xAxisDR = new NumberAxis(1, 60, 1);
    private final static NumberAxis yAxisDR = new NumberAxis();
    public static final AreaChart<Number, Number> dataRateChart = new AreaChart<>(xAxisDR, yAxisDR);

    private final static NumberAxis xAxisTD = new NumberAxis(1, 60, 1);
    private final static NumberAxis yAxisTD = new NumberAxis();
    public static final AreaChart<Number, Number> threadChart = new AreaChart<>(xAxisTD, yAxisTD);

    private final static NumberAxis xAxisM = new NumberAxis(1, 60, 1);
    private final static NumberAxis yAxisM = new NumberAxis();
    public static final AreaChart<Number, Number> memoryChart = new AreaChart<>(xAxisM, yAxisM);

    private static final XYChart.Series speedSeriesSeconds = new XYChart.Series();//hold 60 units
    private static final XYChart.Series speedSeriesMinutes = new XYChart.Series();//hold 60 units
    private static final XYChart.Series speedSeriesHours = new XYChart.Series();//hold 24 units

    private static final XYChart.Series threadSeriesSeconds = new XYChart.Series();//hold 60 units
    private static final XYChart.Series threadSeriesMinutes = new XYChart.Series();//hold 60 units
    private static final XYChart.Series threadSeriesHours = new XYChart.Series();//hold 24 units

    private static final XYChart.Series memorySeriesSeconds = new XYChart.Series();//hold 60 units
    private static final XYChart.Series memorySeriesMinutes = new XYChart.Series();//hold 60 units
    private static final XYChart.Series memorySeriesHours = new XYChart.Series();//hold 24 units

    /**
     * @return the speedSeriesSeconds
     */
    public static XYChart.Series getSpeedSeriesSeconds() {
        return speedSeriesSeconds;
    }

    /**
     * @return the speedSeriesMinutes
     */
    public static XYChart.Series getSpeedSeriesMinutes() {
        return speedSeriesMinutes;
    }

    /**
     * @return the speedSeriesHours
     */
    public static XYChart.Series getSpeedSeriesHours() {
        return speedSeriesHours;
    }

    public static XYChart.Series getThreadSeriesSeconds() {
        return threadSeriesSeconds;
    }

    /**
     * @return the threadSeriesMinutes
     */
    public static XYChart.Series getThreadSeriesMinutes() {
        return threadSeriesMinutes;
    }

    /**
     * @return the threadSeriesHours
     */
    public static XYChart.Series getThreadSeriesHours() {
        return threadSeriesHours;
    }
    //adapt for others
    //NOT USED ATM
//    private void updateDataRateAreaUpperYBound(double value, AreaChart<Number, Number> chart) {
//        if (value > ((NumberAxis) chart.getYAxis()).getUpperBound()) {
//            ((NumberAxis) chart.getYAxis()).setUpperBound((int) ((((NumberAxis) chart.getYAxis()).
//                    getUpperBound() / 100) * 50) + ((NumberAxis) chart.getYAxis()).getUpperBound());
//
//            ((NumberAxis) chart.getYAxis()).setTickUnit((int) (((NumberAxis) chart.getYAxis()).
//                    getUpperBound() / 100) * 10);
//
//        }
//    }
//
//    private void updateThreadAreaUpperYBound(int value, AreaChart<Number, Number> chart) {
//        if (value > ((NumberAxis) chart.getYAxis()).getUpperBound()) {
//            ((NumberAxis) chart.getYAxis()).setUpperBound((int) ((((NumberAxis) chart.getYAxis()).
//                    getUpperBound() / 10) * 10) + ((NumberAxis) chart.getYAxis()).getUpperBound());
//
//            ((NumberAxis) chart.getYAxis()).setTickUnit((int) (((NumberAxis) chart.getYAxis()).
//                    getUpperBound() / 10) * 10);
//
//        }
//    }

    /**
     * @return the memorySeriesSeconds
     */
    public static XYChart.Series getMemorySeriesSeconds() {
        return memorySeriesSeconds;
    }

    /**
     * @return the memorySeriesMinutes
     */
    public static XYChart.Series getMemorySeriesMinutes() {
        return memorySeriesMinutes;
    }

    /**
     * @return the memorySeriesHours
     */
    public static XYChart.Series getMemorySeriesHours() {
        return memorySeriesHours;
    }

    //create datasets for all resolutions
    private void addGraphSeriesSpeedToSeconds(long speed) {

        if (getSpeedSeriesSeconds().getData().size() != 60) {

            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getSpeedSeriesSeconds().getData().size() + 1, speed);
            data.setNode(new HoveredThresholdNode(0, speed));
            getSpeedSeriesSeconds().getData().add(data);
        } else {
            getSpeedSeriesSeconds().getData().remove(0);
            getSpeedSeriesSeconds().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });

            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getSpeedSeriesSeconds().getData().size() + 1, speed);
            data.setNode(new HoveredThresholdNode(0, speed));
            getSpeedSeriesSeconds().getData().add(data);

        }

    }

    private void addGraphSeriesThreadToSeconds(int thread) {

        if (getThreadSeriesSeconds().getData().size() != 60) {

            XYChart.Data<Integer, Integer> data = new XYChart.Data<>(getThreadSeriesSeconds().getData().size() + 1, thread);
            data.setNode(new HoveredThresholdNode(0, thread));
            getThreadSeriesSeconds().getData().add(data);
        } else {
            getThreadSeriesSeconds().getData().remove(0);
            getThreadSeriesSeconds().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });

            XYChart.Data<Integer, Integer> data = new XYChart.Data<>(getThreadSeriesSeconds().getData().size() + 1, thread);
            data.setNode(new HoveredThresholdNode(0, thread));
            getThreadSeriesSeconds().getData().add(data);

        }

    }

    private void addGraphSeriesMemoryToSeconds(long memory) {

        if (getMemorySeriesSeconds().getData().size() != 60) {

            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getMemorySeriesSeconds().getData().size() + 1, memory);
            data.setNode(new HoveredThresholdNode(0, memory));
            getMemorySeriesSeconds().getData().add(data);
        } else {
            getMemorySeriesSeconds().getData().remove(0);
            getMemorySeriesSeconds().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });

            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getMemorySeriesSeconds().getData().size() + 1, memory);
            data.setNode(new HoveredThresholdNode(0, memory));
            getMemorySeriesSeconds().getData().add(data);

        }

    }

    private void addGraphSeriesSpeedToMinutes(long speed) {

        if (getSpeedSeriesMinutes().getData().size() != 60) {
            //getSpeedSeriesMinutes().getData().add(new XYChart.Data(getSpeedSeriesMinutes().getData().size() + 1, speed));

            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getSpeedSeriesMinutes().getData().size() + 1, speed);
            data.setNode(new HoveredThresholdNode(0, speed));
            getSpeedSeriesMinutes().getData().add(data);

        } else {
            getSpeedSeriesMinutes().getData().remove(0);
            getSpeedSeriesMinutes().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });

            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getSpeedSeriesMinutes().getData().size() + 1, speed);
            data.setNode(new HoveredThresholdNode(0, speed));
            getSpeedSeriesMinutes().getData().add(data);

            //getSpeedSeriesMinutes().getData().add(new XYChart.Data(getSpeedSeriesMinutes().getData().size() + 1, speed));
        }
    }

    private void addGraphSeriesThreadToMinutes(int thread) {

        if (getThreadSeriesMinutes().getData().size() != 60) {
            //getThreadSeriesMinutes().getData().add(new XYChart.Data(getThreadSeriesMinutes().getData().size() + 1, thread));

            XYChart.Data<Integer, Integer> data = new XYChart.Data<>(getThreadSeriesMinutes().getData().size() + 1, thread);
            data.setNode(new HoveredThresholdNode(0, thread));
            getThreadSeriesMinutes().getData().add(data);
        } else {
            getThreadSeriesMinutes().getData().remove(0);
            getThreadSeriesMinutes().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });

            XYChart.Data<Integer, Integer> data = new XYChart.Data<>(getThreadSeriesMinutes().getData().size() + 1, thread);
            data.setNode(new HoveredThresholdNode(0, thread));
            getThreadSeriesMinutes().getData().add(data);

            //getThreadSeriesMinutes().getData().add(new XYChart.Data(getThreadSeriesMinutes().getData().size() + 1, thread));
        }
    }

    private void addGraphSeriesMemoryToMinutes(long memory) {

        if (getMemorySeriesMinutes().getData().size() != 60) {
            //getMemorySeriesMinutes().getData().add(new XYChart.Data(getMemorySeriesMinutes().getData().size() + 1, memory));

            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getMemorySeriesMinutes().getData().size() + 1, memory);
            data.setNode(new HoveredThresholdNode(0, memory));
            getMemorySeriesMinutes().getData().add(data);
        } else {
            getMemorySeriesMinutes().getData().remove(0);
            getMemorySeriesMinutes().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });

            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getMemorySeriesMinutes().getData().size() + 1, memory);
            data.setNode(new HoveredThresholdNode(0, memory));
            getMemorySeriesMinutes().getData().add(data);

            // getMemorySeriesMinutes().getData().add(new XYChart.Data(getMemorySeriesMinutes().getData().size() + 1, memory));
        }
    }

    private void addGraphSeriesSpeedToHours(long speed) {

        //make sure i have smooth transition from seocnds/minutes
        if (getSpeedSeriesHours().getData().size() != 24) {

            // getSpeedSeriesHours().getData().add(new XYChart.Data(getSpeedSeriesHours().getData().size() + 1, speed));
            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getSpeedSeriesHours().getData().size() + 1, speed);
            data.setNode(new HoveredThresholdNode(0, speed));
            getSpeedSeriesHours().getData().add(data);

        } else {
            getSpeedSeriesHours().getData().remove(0);
            getSpeedSeriesHours().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });

            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getSpeedSeriesHours().getData().size() + 1, speed);
            data.setNode(new HoveredThresholdNode(0, speed));
            getSpeedSeriesHours().getData().add(data);

            //getSpeedSeriesHours().getData().add(new XYChart.Data(getSpeedSeriesHours().getData().size() + 1, speed));
        }
    }

    private void addGraphSeriesThreadToHours(int thread) {
        //make sure i have smooth transition from seocnds/minutes
        if (getThreadSeriesHours().getData().size() != 24) {
            // getThreadSeriesHours().getData().add(new XYChart.Data(getThreadSeriesHours().getData().size() + 1, thread));

            XYChart.Data<Integer, Integer> data = new XYChart.Data<>(getThreadSeriesHours().getData().size() + 1, thread);
            data.setNode(new HoveredThresholdNode(0, thread));
            getThreadSeriesHours().getData().add(data);

        } else {
            getThreadSeriesHours().getData().remove(0);
            getThreadSeriesHours().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });

            XYChart.Data<Integer, Integer> data = new XYChart.Data<>(getThreadSeriesHours().getData().size() + 1, thread);
            data.setNode(new HoveredThresholdNode(0, thread));
            getThreadSeriesHours().getData().add(data);
            // getThreadSeriesHours().getData().add(new XYChart.Data(getThreadSeriesHours().getData().size() + 1, thread));
        }
    }

    private void addGraphSeriesMemoryToHours(long memory) {
        //make sure i have smooth transition from seocnds/minutes
        if (getMemorySeriesHours().getData().size() != 24) {
            // getMemorySeriesHours().getData().add(new XYChart.Data(getMemorySeriesHours().getData().size() + 1, memory));
            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getMemorySeriesHours().getData().size() + 1, memory);
            data.setNode(new HoveredThresholdNode(0, memory));
            getMemorySeriesHours().getData().add(data);

        } else {
            getMemorySeriesHours().getData().remove(0);
            getMemorySeriesHours().getData().forEach((d) -> {//posun index o 1 pre vsetky
                ((XYChart.Data) d).setXValue(((int) ((XYChart.Data) d).getXValue()) - 1);
            });

            XYChart.Data<Integer, Long> data = new XYChart.Data<>(getMemorySeriesHours().getData().size() + 1, memory);
            data.setNode(new HoveredThresholdNode(0, memory));
            getMemorySeriesHours().getData().add(data);
            //getMemorySeriesHours().getData().add(new XYChart.Data(getMemorySeriesHours().getData().size() + 1, memory));
        }
    }

    public static final int RESOLUTION_SECONDS = 0;
    public static final int RESOLUTION_MINUTES = 1;
    public static final int RESOLUTION_HOURS = 2;
    private static int activeResolution = 0;

    public static void setActiveResolution(int resolution) {
        if (resolution <= 2 && resolution >= 0) {
            activeResolution = resolution;
        } else {
            activeResolution = 0;
        }
    }
    private final ChoiceBox choiceBoxResolutionDR = new ChoiceBox();
    private final ChoiceBox choiceBoxResolutionTD = new ChoiceBox();
    private final ChoiceBox choiceBoxResolutionM = new ChoiceBox();
    private final Label resolutionLabel = new Label("Resolution");

    private void initStatistics() {

        hBoxStatHolderMenuRight.setSpacing(5);
        tgStatButtonsGroup = new ToggleGroup();
        tBtnDataRate.setToggleGroup(tgStatButtonsGroup);
        tBtnCSInfo.setToggleGroup(tgStatButtonsGroup);
        tBtnThreads.setToggleGroup(tgStatButtonsGroup);
        tBtnMemory.setToggleGroup(tgStatButtonsGroup);

        choiceBoxResolutionDR.getItems().addAll("Seconds", "Minutes", "Hours");
        choiceBoxResolutionDR.getSelectionModel().selectFirst();
        choiceBoxResolutionTD.getItems().addAll("Seconds", "Minutes", "Hours");
        choiceBoxResolutionTD.getSelectionModel().selectFirst();
        choiceBoxResolutionM.getItems().addAll("Seconds", "Minutes", "Hours");
        choiceBoxResolutionM.getSelectionModel().selectFirst();

        ((NumberAxis) dataRateChart.getXAxis()).setUpperBound(60);//startin with seconds //not sure if needed here

        choiceBoxResolutionDR.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            switch ((int) newValue) {
                case RESOLUTION_SECONDS: {
                    ((NumberAxis) dataRateChart.getXAxis()).setUpperBound(60);
                    dataRateChart.getData().removeAll(getSpeedSeriesHours(), getSpeedSeriesMinutes(), getSpeedSeriesSeconds());
                    dataRateChart.getData().add(getSpeedSeriesSeconds());
                    activeResolution = RESOLUTION_SECONDS;
                    break;
                }
                case RESOLUTION_MINUTES: {
                    ((NumberAxis) dataRateChart.getXAxis()).setUpperBound(60);
                    dataRateChart.getData().removeAll(getSpeedSeriesHours(), getSpeedSeriesMinutes(), getSpeedSeriesSeconds());
                    dataRateChart.getData().add(getSpeedSeriesMinutes());
                    activeResolution = RESOLUTION_MINUTES;
                    break;
                }
                case RESOLUTION_HOURS: {
                    ((NumberAxis) dataRateChart.getXAxis()).setUpperBound(24);
                    dataRateChart.getData().removeAll(getSpeedSeriesHours(), getSpeedSeriesMinutes(), getSpeedSeriesSeconds());
                    dataRateChart.getData().add(getSpeedSeriesHours());
                    activeResolution = RESOLUTION_HOURS;
                    break;
                }
            }
        });

        choiceBoxResolutionTD.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            switch ((int) newValue) {
                case RESOLUTION_SECONDS: {
                    ((NumberAxis) threadChart.getXAxis()).setUpperBound(60);
                    threadChart.getData().removeAll(getThreadSeriesHours(), getThreadSeriesMinutes(), getThreadSeriesSeconds());
                    threadChart.getData().add(getThreadSeriesSeconds());
                    activeResolution = RESOLUTION_SECONDS;
                    break;
                }
                case RESOLUTION_MINUTES: {
                    ((NumberAxis) threadChart.getXAxis()).setUpperBound(60);
                    threadChart.getData().removeAll(getThreadSeriesHours(), getThreadSeriesMinutes(), getThreadSeriesSeconds());
                    threadChart.getData().add(getThreadSeriesMinutes());
                    activeResolution = RESOLUTION_MINUTES;
                    break;
                }
                case RESOLUTION_HOURS: {
                    ((NumberAxis) threadChart.getXAxis()).setUpperBound(24);
                    threadChart.getData().removeAll(getSpeedSeriesHours(), getThreadSeriesMinutes(), getThreadSeriesSeconds());
                    threadChart.getData().add(getThreadSeriesHours());
                    activeResolution = RESOLUTION_HOURS;
                    break;
                }
            }
        });

        choiceBoxResolutionM.getSelectionModel().selectedIndexProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            switch ((int) newValue) {
                case RESOLUTION_SECONDS: {
                    ((NumberAxis) memoryChart.getXAxis()).setUpperBound(60);
                    memoryChart.getData().removeAll(getMemorySeriesHours(), getMemorySeriesMinutes(), getMemorySeriesSeconds());
                    memoryChart.getData().add(getMemorySeriesSeconds());
                    activeResolution = RESOLUTION_SECONDS;
                    break;
                }
                case RESOLUTION_MINUTES: {
                    ((NumberAxis) memoryChart.getXAxis()).setUpperBound(60);
                    memoryChart.getData().removeAll(getMemorySeriesHours(), getMemorySeriesMinutes(), getMemorySeriesSeconds());
                    memoryChart.getData().add(getMemorySeriesMinutes());
                    activeResolution = RESOLUTION_MINUTES;
                    break;
                }
                case RESOLUTION_HOURS: {
                    ((NumberAxis) memoryChart.getXAxis()).setUpperBound(24);
                    memoryChart.getData().removeAll(getMemorySeriesHours(), getMemorySeriesMinutes(), getMemorySeriesSeconds());
                    memoryChart.getData().add(getMemorySeriesHours());
                    activeResolution = RESOLUTION_HOURS;
                    break;
                }
            }
        });

        tBtnCSInfo.selectedProperty().addListener((val, oldVal, newVal) -> {
            if (newVal) {
                hBoxStatHolderMenuRight.getChildren().removeAll(resolutionLabel, choiceBoxResolutionDR, choiceBoxResolutionTD, choiceBoxResolutionM);
                initCSInfoComponent();
                //#2980b9
                tBtnCSInfo.setStyle(tBtnCSInfo.getStyle().replace("#3498db", "#2980b9"));
            } else {
                hBoxStatHolderMenuRight.getChildren().removeAll(resolutionLabel, choiceBoxResolutionDR, choiceBoxResolutionTD, choiceBoxResolutionM);
                statHolder.setCenter(null);
                tBtnCSInfo.setStyle(tBtnCSInfo.getStyle().replace("#2980b9", "#3498db"));
            }

        });

        tBtnDataRate.selectedProperty().addListener((val, oldVal, newVal) -> {
            if (newVal) {
                hBoxStatHolderMenuRight.getChildren().removeAll(hBoxStatHolderMenuRight.getChildren());
                hBoxStatHolderMenuRight.getChildren().addAll(resolutionLabel, choiceBoxResolutionDR);
                statHolder.setCenter(dataRateChart);
                tBtnDataRate.setStyle(tBtnDataRate.getStyle().replace("#3498db", "#2980b9"));
            } else {
                hBoxStatHolderMenuRight.getChildren().removeAll(resolutionLabel, choiceBoxResolutionDR);
                statHolder.setCenter(null);
                tBtnDataRate.setStyle(tBtnDataRate.getStyle().replace("#2980b9", "#3498db"));
            }
        });

        tBtnThreads.selectedProperty().addListener((val, oldVal, newVal) -> {
            if (newVal) {
                hBoxStatHolderMenuRight.getChildren().removeAll(hBoxStatHolderMenuRight.getChildren());
                hBoxStatHolderMenuRight.getChildren().addAll(resolutionLabel, choiceBoxResolutionTD);
                statHolder.setCenter(threadChart);
                tBtnThreads.setStyle(tBtnThreads.getStyle().replace("#3498db", "#2980b9"));
            } else {
                hBoxStatHolderMenuRight.getChildren().removeAll(resolutionLabel, choiceBoxResolutionTD);
                statHolder.setCenter(null);
                tBtnThreads.setStyle(tBtnThreads.getStyle().replace("#2980b9", "#3498db"));
            }
        });

        tBtnMemory.selectedProperty().addListener((val, oldVal, newVal) -> {
            if (newVal) {
                hBoxStatHolderMenuRight.getChildren().removeAll(hBoxStatHolderMenuRight.getChildren());
                hBoxStatHolderMenuRight.getChildren().addAll(resolutionLabel, choiceBoxResolutionM);
                statHolder.setCenter(memoryChart);
                tBtnMemory.setStyle(tBtnMemory.getStyle().replace("#3498db", "#2980b9"));
            } else {
                hBoxStatHolderMenuRight.getChildren().removeAll(resolutionLabel, choiceBoxResolutionM);
                statHolder.setCenter(null);
                tBtnMemory.setStyle(tBtnMemory.getStyle().replace("#2980b9", "#3498db"));
            }
        });

        dataRateChart.getData().add(getSpeedSeriesSeconds());
        dataRateChart.setAnimated(false);
        dataRateChart.setLegendVisible(false);

        threadChart.getData().add(getThreadSeriesSeconds());
        threadChart.setAnimated(false);
        threadChart.setLegendVisible(false);

        memoryChart.getData().add(getMemorySeriesSeconds());
        memoryChart.setAnimated(false);
        memoryChart.setLegendVisible(false);

        //initialize with this button selected by default
        tBtnCSInfo.setSelected(true);

        //TITLED PANE UPPER HOLDER INITIALIZATION
        //add dailyStats loaded , from mainDataModel instance
    }

    ///-----------hover over plotting
    /**
     * @return plotted y values for monotonically increasing integer x values, starting from x=1
     */
    public ObservableList<XYChart.Data<Integer, Integer>> plot(int... y) {
        final ObservableList<XYChart.Data<Integer, Integer>> dataset = FXCollections.observableArrayList();

        int i = 0;
        while (i < y.length) {
            final XYChart.Data<Integer, Integer> data = new XYChart.Data<>(i + 1, y[i]);
            data.setNode(
                    new HoveredThresholdNode(
                            (i == 0) ? 0 : y[i - 1],
                            y[i]
                    )
            );

            dataset.add(data);
            i++;
        }

        return dataset;
    }

    // new XYChart.Data(getSpeedSeriesSeconds().getData().size() + 1, speed)
    //single plot value
    public XYChart.Data<Integer, Double> plot(int index, double priorVal, double val) {

        final XYChart.Data<Integer, Double> data = new XYChart.Data<>(index, val);
        data.setNode(new HoveredThresholdNode(
                priorVal,
                val
        )
        );

        return data;
    }

    public XYChart.Data<Integer, Long> plot(int index, long priorVal, long val) {

        final XYChart.Data<Integer, Long> data = new XYChart.Data<>(index, val);
        data.setNode(new HoveredThresholdNode(
                priorVal,
                val
        )
        );

        return data;
    }

    public XYChart.Data<Integer, Integer> plot(int index, int priorVal, int val) {

        final XYChart.Data<Integer, Integer> data = new XYChart.Data<>(index, val);
        data.setNode(new HoveredThresholdNode(
                priorVal,
                val
        )
        );

        return data;
    }

    /**
     * a node which displays a value on hover, but is otherwise empty
     */
    class HoveredThresholdNode extends StackPane {

        HoveredThresholdNode(double priorValue, double value) {
            setPrefSize(8, 8);

            final Label label = createDataThresholdLabel(priorValue, value);

            setOnMouseEntered((MouseEvent mouseEvent) -> {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
            });
            setOnMouseExited((MouseEvent mouseEvent) -> {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
            });
        }

        HoveredThresholdNode(int priorValue, int value) {
            setPrefSize(8, 8);

            final Label label = createDataThresholdLabel(priorValue, value);

            setOnMouseEntered((MouseEvent mouseEvent) -> {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
            });
            setOnMouseExited((MouseEvent mouseEvent) -> {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
            });
        }

        HoveredThresholdNode(long priorValue, long value) {
            setPrefSize(8, 8);

            final Label label = createDataThresholdLabel(priorValue, value);

            setOnMouseEntered((MouseEvent mouseEvent) -> {
                getChildren().setAll(label);
                setCursor(Cursor.NONE);
                toFront();
            });
            setOnMouseExited((MouseEvent mouseEvent) -> {
                getChildren().clear();
                setCursor(Cursor.CROSSHAIR);
            });
        }

        private Label createDataThresholdLabel(double priorValue, double value) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

            if (priorValue == 0) {
                label.setTextFill(Color.DARKGRAY);
            } else if (value > priorValue) {
                label.setTextFill(Color.FORESTGREEN);
            } else {
                label.setTextFill(Color.FIREBRICK);
            }

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }

        private Label createDataThresholdLabel(int priorValue, int value) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

            if (priorValue == 0) {
                label.setTextFill(Color.DARKGRAY);
            } else if (value > priorValue) {
                label.setTextFill(Color.FORESTGREEN);
            } else {
                label.setTextFill(Color.FIREBRICK);
            }

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }

        private Label createDataThresholdLabel(long priorValue, long value) {
            final Label label = new Label(value + "");
            label.getStyleClass().addAll("default-color0", "chart-line-symbol", "chart-series-line");
            label.setStyle("-fx-font-size: 15; -fx-font-weight: bold;");

            if (priorValue == 0) {
                label.setTextFill(Color.DARKGRAY);
            } else if (value > priorValue) {
                label.setTextFill(Color.FORESTGREEN);
            } else {
                label.setTextFill(Color.FIREBRICK);
            }

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }

    @FXML
    private RadioButton rbtnDiskSpace;

    private void updateDiskSpaceInfoView() {
        //TODO change logic to show mb if small
        //optimize
        float diskSpace = FileUtils.getAllDiskSpace();
        float usable = FileUtils.getAllUsableDiskSpace();

        //TODO MUST OPTIMIZE!.
        if ((usable / 1024 / 1024 / 1024) >= 0.2 * (diskSpace / 1024 / 1024 / 1024)) {
            rbtnDiskSpace.setStyle("-fx-mark-color:green");
            rbtnDiskSpace.setText(String.format("Disk size:%.1fGB , Free space:%.1fGB ", diskSpace / 1024 / 1024 / 1024, usable / 1024 / 1024 / 1024));

        } else if ((usable / 1024 / 1024 / 1024) >= 0.1 * (diskSpace / 1024 / 1024 / 1024)) {
            rbtnDiskSpace.setStyle("-fx-mark-color:orange");
            rbtnDiskSpace.setText(String.format("Disk size:%.1fGB , Free space:%.1fGB Above 10%%", diskSpace / 1024 / 1024 / 1024, usable / 1024 / 1024 / 1024));
        } else {
            rbtnDiskSpace.setStyle("-fx-mark-color:orange");
            rbtnDiskSpace.setText(String.format("Disk size:%.1fGB , Free space:%.1fGB Bellow 10%%", diskSpace / 1024 / 1024 / 1024, usable / 1024 / 1024 / 1024));
        }

        //other actions
    }

}
