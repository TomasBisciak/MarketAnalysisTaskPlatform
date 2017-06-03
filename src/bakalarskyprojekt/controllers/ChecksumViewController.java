/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.controllers;

import bakalarskyprojekt.controls.EditCell;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author Kofola
 */
public class ChecksumViewController implements Initializable {

    @FXML
    private Button btnGenerate;
    @FXML
    private ProgressBar progressBarGenerate;
    @FXML
    private TableView<Pair> generatedTable;
    @FXML
    private ChoiceBox choiceBoxType;

    public class Pair {

        private SimpleStringProperty id;//name
        private SimpleStringProperty hash;

        public Pair(String id, String hash) {
            this.hash = new SimpleStringProperty(hash);
            this.id = new SimpleStringProperty(id);
        }

        public String getId() {
            return id.get();
        }

        public void setId(String id) {
            this.id.set(id);
        }

        public String getHash() {
            return hash.get();
        }

        public void setHash(String hash) {
            this.hash.set(hash);
        }

    }

    @FXML
    private TableView<Pair> originalTable;
    @FXML
    private TableView<Pair> resultTable;

    @FXML
    private Button btnCopyToClipboard;
    @FXML
    private ImageView imgLoader;

    private List<Pair> multipleSelection = new ArrayList<>();
    private Pair currentSelect;

    private ArrayList<TableColumn> tcmns = new ArrayList<>();

    @FXML
    private Button btnCompare;

    private List<Path> data = new ArrayList<>();

    private final ObservableList<Pair> generatedData = FXCollections.observableArrayList();
    private final ObservableList<Pair> originalData = FXCollections.observableArrayList();
    private final ObservableList<Pair> resultData = FXCollections.observableArrayList();

    private volatile boolean imgLoaderVisible;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        choiceBoxType.getItems().addAll("md2Hex", "md5Hex", "sha1Hex", "sha256Hex", "sha384Hex", "sha512Hex");
        choiceBoxType.getSelectionModel().select("md5Hex");

        Callback<TableColumn, TableCell> cellFactory
                = (TableColumn p) -> new EditingCell();

        generatedTable.getColumns().get(0).setCellValueFactory(
                new PropertyValueFactory<>("id")
        );
        generatedTable.getColumns().get(1).setCellValueFactory(
                new PropertyValueFactory<>("hash")
        );

        tcmns.add(new TableColumn("ID"));
        tcmns.add(new TableColumn("Hash"));
        tcmns.get(0).setCellValueFactory(
                new PropertyValueFactory<>("id")
        );
        tcmns.get(1).setCellValueFactory(
                new PropertyValueFactory<>("hash")
        );

        resultTable.getColumns().get(0).setCellValueFactory(
                new PropertyValueFactory<>("id")
        );

        resultTable.getColumns().get(1).setCellValueFactory(
                new PropertyValueFactory<>("hash")
        );

        tcmns.get(1).setCellFactory(cellFactory);
        tcmns.get(1).setCellFactory(column -> EditCell.createStringEditCell());
        tcmns.get(1).setOnEditCommit(new EventHandler<CellEditEvent<Pair, String>>() {
            @Override
            public void handle(CellEditEvent<Pair, String> t) {
                ((Pair) t.getTableView().getItems().get(
                        t.getTablePosition().getRow())).setHash(t.getNewValue());
            }
        }
        );

        originalTable.getColumns().set(0, tcmns.get(0));
        originalTable.getColumns().set(1, tcmns.get(1));
        originalTable.getColumns().get(1).setPrefWidth(352);

        for (Path path : data) {
            originalData.add(new Pair(path.getFileName().toString(), ""));
        }
        originalTable.setItems(originalData);
        generatedTable.setItems(generatedData);
        resultTable.setItems(resultData);

        setSelectionModel();
    }

    //public FxmlChecksumViewController(List<Path> filePaths) {
    public ChecksumViewController() {
        //insert test data

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Import data file");
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

        this.data = new ArrayList<>();

        for (int i = 0; i < selectedFiles.size(); i++) {
            this.data.add(selectedFiles.get(i).toPath());
        }

    }

    @FXML
    private void generateHash() {
        btnGenerate.setDisable(true);
        progressBarGenerate.setProgress(0);
        imgLoader.setVisible(true);
        boolean[] flags = new boolean[data.size()];
        double inc = (1.0 / data.size());

        for (Path path : data) {
            new Thread(() -> {
                try (FileInputStream fis = new FileInputStream(new File(path.toString()));) {

                    String hash;
                    switch ((String) choiceBoxType.getSelectionModel().getSelectedItem()) {
                        case "md2Hex": {
                            hash = DigestUtils.md2Hex(fis);
                            break;
                        }
                        case "md5Hex": {
                            hash = DigestUtils.md5Hex(fis);
                            break;
                        }
                        case "sha1Hex": {
                            hash = DigestUtils.sha1Hex(fis);
                            break;
                        }
                        case "sha256Hex": {
                            hash = DigestUtils.sha256Hex(fis);
                            break;
                        }
                        case "sha384Hex": {
                            hash = DigestUtils.sha384Hex(fis);
                            break;
                        }
                        case "sha512Hex": {
                            hash = DigestUtils.sha512Hex(fis);
                            break;
                        }
                        default: {
                            hash = DigestUtils.md5Hex(fis);
                        }
                    }

                    Platform.runLater(() -> {
                        originalTable.getColumns().get(1).setText((String) choiceBoxType.getSelectionModel().getSelectedItem());
                        generatedTable.getColumns().get(1).setText((String) choiceBoxType.getSelectionModel().getSelectedItem());
                        generatedData.add(new Pair(path.getFileName().toString(), hash));
                    });
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(ChecksumViewController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(ChecksumViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
                flags[data.indexOf(path)] = true;
                Platform.runLater(() -> {
                    progressBarGenerate.setProgress(progressBarGenerate.getProgress() + inc);
                });

            }).start();
        }

        imgLoader.visibleProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            imgLoaderVisible = newValue;
        });
        new Thread(() -> {
            imgLoaderVisible = true;
            while (imgLoaderVisible) {
                int c = 0;
                for (boolean ended : flags) {
                    if (ended) {
                        c++;
                    }
                }
                if (c == (flags.length)) {
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ChecksumViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Platform.runLater(() -> {
                imgLoader.setVisible(false);
            });

        }).start();
    }

    @FXML
    private void toClipbrd() {
        Clipboard clpbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < multipleSelection.size(); i++) {
            if (i + 1 == multipleSelection.size()) {
                sb.append(multipleSelection.get(i).getHash());
            } else {
                sb.append(multipleSelection.get(i).getHash()).append(",");
            }

        }
        StringSelection stringSelection = new StringSelection(sb.toString());
        clpbrd.setContents(stringSelection, null);
    }

    @FXML
    private void compare() {
        resultData.removeAll(resultData);
        for (int i = 0; i < generatedData.size(); i++) {
            if (generatedData.get(i).getHash().equals(originalData.get(i).getHash())) {
                resultData.add(new Pair(generatedData.get(i).getId(), "match"));
            } else {
                System.out.println("generatedData.get(i).getHash():" + generatedData.get(i).getHash() + " \t originalData.get(i).getHash()" + originalData.get(i).getHash());
                resultData.add(new Pair(generatedData.get(i).getId(), "does not match"));
            }
        }
    }

    private void setSelectionModel() {

        generatedTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        generatedTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Pair> c) -> {
            multipleSelection.removeAll(multipleSelection);
            currentSelect = ((Pair) c.getList().get(0));
            c.getList().forEach((Object d) -> {
                multipleSelection.add(((Pair) d)
                );
            });

        });

    }

    class EditingCell extends TableCell<Pair, String> {

        private TextField textField;

        public EditingCell() {
        }

        @Override
        public void startEdit() {
            if (!isEmpty()) {
                super.startEdit();
                createTextField();
                setText(null);
                setGraphic(textField);
                textField.selectAll();
            }
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText((String) getItem());
            setGraphic(null);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setText(null);
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.focusedProperty().addListener((ObservableValue<? extends Boolean> arg0, Boolean arg1, Boolean arg2) -> {
                if (!arg2) {
                    commitEdit(textField.getText());
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }

}
