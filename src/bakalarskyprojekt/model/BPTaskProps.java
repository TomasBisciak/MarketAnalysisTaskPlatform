/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * not thread safe
 *
 * @author Kofola
 */
public class BPTaskProps {

    public static final int NO_ID = -1;
    public static final int APP_TYPE_JAVA = 0;
    public static final int APP_TYPE_CPP = 1;

    private long id = NO_ID;
    private final SimpleStringProperty name;
    // convert to NIO , PATHS INSTEAD OF FILES
    private final List<File> priceDataFiles;
    private final List<File> tradeDataFiles;
    private final File testFile;

    private final SimpleIntegerProperty applicationType;
    private final String parameters;
    private final String jvmParam;

    public BPTaskProps(List<File> priceDataFiles, List<File> tradeDataFiles, File testFile, int APP_TYPE, String parameters) {
        this.applicationType = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();

        this.priceDataFiles = new ArrayList<>(priceDataFiles);
        this.tradeDataFiles = new ArrayList<>(tradeDataFiles);
        this.testFile = new File(testFile.getPath());
        this.applicationType.setValue(APP_TYPE);
        this.parameters = parameters;
        jvmParam = "";
    }

    public BPTaskProps(String name, List<File> priceDataFiles, List<File> tradeDataFiles, File testFile, int APP_TYPE, String parameters) {
        this.applicationType = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.name.setValue(name);
        this.priceDataFiles = new ArrayList<>(priceDataFiles);
        this.tradeDataFiles = new ArrayList<>(tradeDataFiles);
        this.testFile = new File(testFile.getPath());
        this.applicationType.setValue(APP_TYPE);
        this.parameters = parameters;
        jvmParam = "";
    }

    public BPTaskProps(long id, String name, List<File> priceDataFiles, List<File> tradeDataFiles, File testFile, int APP_TYPE, String parameters) {
        this.applicationType = new SimpleIntegerProperty();
        this.name = new SimpleStringProperty();
        this.id = id;
        this.name.setValue(name);
        this.priceDataFiles = new ArrayList<>(priceDataFiles);
        this.tradeDataFiles = new ArrayList<>(tradeDataFiles);
        this.testFile = new File(testFile.getPath());
        this.applicationType.setValue(APP_TYPE);
        this.parameters = parameters;
        jvmParam = "";
    }

    public final String generateCmdString() {
        StringBuilder sb = new StringBuilder();

        switch (applicationType.getValue()) {

            case (APP_TYPE_JAVA): {
                sb.append("java ").append(jvmParam).append(" -jar ").append(testFile.getPath()).append(" ");
                break;
            }
            case (APP_TYPE_CPP): {
                sb.append(testFile.getPath());
                break;
            }

        }

        for (int i = 0; i < priceDataFiles.size(); i++) {

            if (i == priceDataFiles.size() - 1) {
                sb.append(priceDataFiles.get(i).getPath()).append(" ");
            } else {
                sb.append(priceDataFiles.get(i).getPath()).append("*");
            }
        }

        for (int i = 0; i < tradeDataFiles.size(); i++) {
            if (i == tradeDataFiles.size() - 1) {
                sb.append(tradeDataFiles.get(i).getPath()).append(" ");
            } else {
                sb.append(tradeDataFiles.get(i).getPath()).append("*");
            }
        }

        sb.append(getParameters());

        return sb.toString();
    }

    /**
     * @return the priceDataFiles
     */
    public List<File> getPriceDataFiles() {
        return priceDataFiles;
    }

    public String getPriceDataFilesString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < priceDataFiles.size(); i++) {

            if (i != (priceDataFiles.size() - 1)) {
                sb.append(priceDataFiles.get(i).getPath()).append("*");
            } else {
                sb.append(priceDataFiles.get(i).getPath());
            }
        }
        return sb.toString();
    }

    /**
     * @return the tradeDataFiles
     */
    public List<File> getTradeDataFiles() {
        return tradeDataFiles;
    }

    public String getTradeDataFilesString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tradeDataFiles.size(); i++) {

            if (i != (tradeDataFiles.size() - 1)) {
                sb.append(tradeDataFiles.get(i).getPath()).append("*");
            } else {
                sb.append(tradeDataFiles.get(i).getPath());
            }
        }
        return sb.toString();
    }

    /**
     * @return the testFile
     */
    public File getTestFile() {
        return testFile;
    }

    /**
     * @return the applicationType
     */
    public int getApplicationType() {
        return applicationType.getValue();
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name.getValue();
    }

    /**
     * @return the parameters
     */
    public String getParameters() {
        return parameters;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

}
