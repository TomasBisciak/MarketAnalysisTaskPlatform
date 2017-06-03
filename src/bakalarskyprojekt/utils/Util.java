/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.utils;

import bakalarskyprojekt.model.BPTaskProps;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.Scanner;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;

/**
 *
 * @author Kofola
 */
public class Util {

    public static final void execCmdTest(String cmd) throws IOException {
        Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        System.out.println("Command:" + cmd);
        System.out.println("Output for '" + cmd + "' :");
        while (s.hasNext()) {
            System.out.println(s.next());
        }
    }

    public static final void execCmdTest(BPTaskProps testProps, TextArea console) throws IOException {

        //blocking
        StringBuilder sb = new StringBuilder();
        String cmd = testProps.generateCmdString();
        Scanner s = new Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        System.out.println("Command:" + cmd);
        System.out.println("Output for '" + cmd + "' :");
        while (s.hasNext()) {
            sb.append("\n").append(s.next());
            Platform.runLater(() -> {
                console.setText(sb.toString());
            });
        }
    }

    public static final Executor execCmdTest(BPTaskProps testProps) throws IOException {

        String command = testProps.generateCmdString();
        CommandLine cmdLine = CommandLine.parse(command);
        DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();

        Executor executor = new DefaultExecutor();
        executor.setExitValue(1);
        executor.execute(cmdLine, resultHandler);
        return executor;

    }

    private static final Gson gson = new Gson();

    public static String objectToJsonString(Object object) {
        synchronized (gson) {
            return gson.toJson(object);
        }
    }

    public static <T> T jsonStringToObject(String jsonString, Class cls) {
        synchronized (gson) {
            return (T) gson.fromJson(jsonString, cls);
        }
    }

    public static String humanReadableByteCount(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = ("KMGTPE").charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

}
