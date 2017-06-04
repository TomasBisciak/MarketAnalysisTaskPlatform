package bplib.communication;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Kofola
 */
public class BPMessage{

    public static final String MSG_TYPE_REPORT = "TYPE_REPORT";
    public static final String MSG_TYPE_RESULT = "TYPE_RESULT";

    private Report report;
    private Result result;

    private final String messageType;

    public BPMessage(String messageType, Report report) {
        this.messageType = messageType;
        this.report = report;
    }

    public BPMessage(String messageType, Result result) {
        this.messageType = messageType;
        this.result = result;
    }

    /**
     * @return the messageType
     */
    public String getMessageType() {
        return messageType;
    }

    /**
     * @return the report
     */
    public Report getReport() {
        return report;
    }

    /**
     * @return the result
     */
    public Result getResult() {
        return result;
    }

}

