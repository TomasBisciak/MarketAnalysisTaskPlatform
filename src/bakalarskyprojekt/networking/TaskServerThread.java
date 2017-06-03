/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.networking;

import bplib.communication.BPMessage;
import bakalarskyprojekt.BakalarskyProjektMain;
import bakalarskyprojekt.controllers.Console;
import bakalarskyprojekt.controllers.MainViewController;
import bakalarskyprojekt.utils.Util;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kofola
 */
public class TaskServerThread extends Thread {

    private final ServerSocket serverSocket;
    private volatile boolean serverAlive;
    public static final int DEFAULT_PORT = 33221;

    private Console console;

    private DataInputStream in;
    private DataOutputStream out;

    public TaskServerThread(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(10000);

    }

    public TaskServerThread(Console console) throws IOException {
        this.console = console;
        serverSocket = new ServerSocket(DEFAULT_PORT);
        serverSocket.setSoTimeout(10000);

    }

    public TaskServerThread(int port, Console console) throws IOException {
        serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(10000);
        this.console = console;
    }

    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {

        setServerAliveStatus(true);
        try {
            console.refresh();
            console.println("[SERVER] !Waiting for test client on port "
                    + getServerSocket().getLocalPort() + "...");

            try (Socket client = getServerSocket().accept()) {//blocks
                console.println("[SERVER]Connected");
                console.println("[SERVER]Connected to " + client.getRemoteSocketAddress());

                in = new DataInputStream(client.getInputStream());
                out = new DataOutputStream(client.getOutputStream());
                console.setExtOutputStream(out);
                console.println(getIn().readUTF());

                getOut().writeUTF("[SERVER]Connected to server " + client.getLocalSocketAddress());

                //reads output until finished
                while (serverAlive) {
                    //read client status and data every second
                    //WRITE COMMANDS
                    String response = getIn().readUTF();
                    //check if end command
                    if (response.equals("/end")) {
                        console.println("[SERVER] End command detected");
                        client.close();
                        break;
                    }
                    if (response.contains("[*msg*]")) {//check if is msg
                        System.out.println("Contains msg");
                        //strip tag
                        response = response.replace("[*msg*]", "");
                        // System.out.println("message json:"+response);
                        processMsg(response);
                    } else {//just string
                        console.println("[CLIENT]" + response);
                    }
                }
                client.close();
            } finally {
                getServerSocket().close();
                setServerAliveStatus(false);
            }

        } catch (SocketTimeoutException s) {
            System.out.println("Socket timed out!");
            MainViewController.showPaneNotification("Connection to client timed out", null);
            try {
                getServerSocket().close();
            } catch (IOException ex) {
                Logger.getLogger(TaskServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            setServerAliveStatus(false);

        } catch (IOException e) {
            e.printStackTrace();
            try {
                getServerSocket().close();
            } catch (IOException ex) {
                Logger.getLogger(TaskServerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
            setServerAliveStatus(false);
        }

    }

    private void processMsg(String jsonMsg) {

        BPMessage msg = Util.jsonStringToObject(jsonMsg, BPMessage.class);

        switch (msg.getMessageType()) {
            case BPMessage.MSG_TYPE_REPORT: {
                System.out.println("Report recieved:" + msg.getReport().toString());
                BakalarskyProjektMain.getModel().setReport(msg.getReport());
                break;
            }
            case BPMessage.MSG_TYPE_RESULT: {
                System.out.println("Result recieved:" + msg.getResult().toString());
                BakalarskyProjektMain.getModel().setResult(msg.getResult());
                break;
            }

        }

    }

    private final Object statusLock = new Object();

    public void setServerAliveStatus(boolean serverAlive) {
        synchronized (statusLock) {
            this.serverAlive = serverAlive;
            statusChanged();
        }

    }

    private void statusChanged() {
        System.out.println("Server status changed - Alive:" + serverAlive);
        console.println("Server alive:" + serverAlive);
        //MAYBE TYPE TO CONSOLE THAT SERVER IS STARTED ETC
    }

    /**
     * @return the in
     */
    public DataInputStream getIn() {
        return in;
    }

    /**
     * @return the out
     */
    public DataOutputStream getOut() {
        return out;
    }

    /**
     * @return the serverSocket
     */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }

}
