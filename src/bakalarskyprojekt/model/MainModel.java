/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.model;

import bakalarskyprojekt.networking.TaskServerThread;
import bplib.communication.Report;
import bplib.communication.Result;
import javafx.beans.property.SimpleBooleanProperty;
import org.apache.commons.exec.Executor;

/**
 *
 * @author Kofola
 */
public class MainModel {

    //private TestProps loadedTestPropsDb;
    private Executor processExecutor;

    private final SimpleBooleanProperty taskActiveProperty;

    private TaskServerThread server;

    private Report report;
    private Result result;

    private static MainModel instance;

    /*
     Not needed bud its nice to demonstrate this. Since this class is singleton anyway doesnt hurt.
     This solution takes advantage of the Java memory model's guarantees about class 
     initialization to ensure thread safety. Each class can only be loaded once,
     and it will only be loaded when it is needed. That means that the first time getInstance is called,
     InstanceHolder will be loaded and instance will be created, 
     and since this is controlled by ClassLoaders, no additional synchronization is necessary.
     */
    private static class InstanceHolder {

        private static final MainModel instance = new MainModel();
    }

    public static MainModel getInstance() {
        return InstanceHolder.instance;
    }

    //data thats beign hold in table.

    private MainModel() {
        taskActiveProperty = new SimpleBooleanProperty(false);
        report = new Report("", 0, 0, 0, 0);
//        this.result=new Result();
//        this.result.getData().put("Test", "Test");
    }

    /**
     * @return the processExecutor
     */
    public Executor getProcessExecutor() {
        return processExecutor;
    }

    /**
     * @param processExecutor the processExecutor to set
     */
    public void setProcessExecutor(Executor processExecutor) {
        this.processExecutor = processExecutor;
    }

    /**
     * @return the testActiveProperty
     */
    public SimpleBooleanProperty getTaskActiveProperty() {
        return taskActiveProperty;
    }

    /**
     * @return the report
     */
    public Report getReport() {
        return report;
    }

    private final Object reportLock = new Object();

    /**
     * @param report the report to set
     */
    public void setReport(Report report) {
        synchronized (reportLock) {
            if (report == null) {
                System.out.println("Report null");
                this.report = new Report("", 0, 0, 0, 0);
                return;
            }
            this.report = report;
        }
    }

    /**
     * @return the server
     */
    public TaskServerThread getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(TaskServerThread server) {
        this.server = server;
    }

    /**
     * @return the result
     */
    public Result getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(Result result) {
        this.result = result;
    }

}
