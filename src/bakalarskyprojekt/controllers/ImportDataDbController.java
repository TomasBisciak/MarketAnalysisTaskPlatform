/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.controllers;

import bakalarskyprojekt.BakalarskyProjektMain;
import bakalarskyprojekt.db.DbUtil;
import bakalarskyprojekt.model.BPTaskProps;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 *
 * @author Kofola
 */
public class ImportDataDbController implements Initializable {

    @FXML
    private Button btnLoad;
    @FXML
    private TableView tableView;
    @FXML
    private TableColumn<BPTaskProps, String> tableColumnName;
    @FXML
    private TableColumn<BPTaskProps, Integer> tableColumnType;

    private ObservableList<BPTaskProps> data;

    private BPTaskProps testProps;

    public ImportDataDbController() {
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //load all data from database
        BakalarskyProjektMain.runLater(() -> {
            //load necessery data
            try {
                data = FXCollections.observableList(DbUtil.getTests());
                tableColumnName.setCellValueFactory(
                        new PropertyValueFactory<>("name"));

                tableColumnType.setCellValueFactory(
                        new PropertyValueFactory<>("applicationType"));

                tableColumnType.setCellFactory(new Callback<TableColumn<BPTaskProps, Integer>, TableCell<BPTaskProps, Integer>>() {

                    @Override
                    public TableCell<BPTaskProps, Integer> call(TableColumn<BPTaskProps, Integer> param
                    ) {
                        return new TableCell<BPTaskProps, Integer>() {
                            //private Label lbl = new Label("State");

                            @Override
                            protected void updateItem(Integer item, boolean empty) {
                                super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                                if (item == null || empty) {
                                    setText(null);
                                    setStyle("");
                                } else {
                                    setStyle("-fx-alignment:CENTER;");
                                    switch (item) {
                                        case BPTaskProps.APP_TYPE_JAVA: {
                                            setText("Java");
                                            setStyle("-fx-alignment:CENTER;-fx-background-color:#e67e22;-fx-text-fill:WHITE");
                                            break;
                                        }
                                        case BPTaskProps.APP_TYPE_CPP: {
                                            setText("C++");
                                            setStyle("-fx-alignment:CENTER;-fx-background-color:#9b59b6;-fx-text-fill:WHITE");
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
                }
                );

                tableView.setItems(data);
                System.out.println("loaded data");
            } catch (SQLException ex) {
                System.out.println("faild to load data");
                Logger.getLogger(ImportDataDbController.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void loadOnAction() {
        testProps = (BPTaskProps) tableView.getSelectionModel().getSelectedItem();
        stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    /**
     * @return the testProps
     */
    public BPTaskProps getTestProps() {
        return testProps;
    }

}
