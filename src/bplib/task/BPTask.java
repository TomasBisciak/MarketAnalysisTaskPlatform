/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bplib.task;

import bplib.communication.BPMessage;
import bplib.communication.Report;
import bplib.communication.Result;
import bplib.priceData.Bar;
import bplib.tradeData.Trade;
import bplib.util.BPUtil;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author Kofola
 */
public abstract class BPTask<D> {

    private final Path[] priceDataPaths;
    private final Path[] tradeDataPaths;

    //ADD OPTION FOR SSD DISK PARALLEL READS
    private final Class<D> priceDataType;
    private final List<D> priceCache;
    private final List<Trade> tradeCache;

    private final Map<String, String> additionalParameters;
    private final String[] args;

    private final int ARG_INDEX_PRICE_DATA = 0;
    private final int ARG_INDEX_TRADE_DATA = 1;
    private final String ARG_DELIM_DATA_PATHS = "\\*";
    private final String ARG_DELIM_PARAM = ";";
    private final String DATA_DELIM = ";";

    private DataOutputStream out;
    private DataInputStream in;

    private boolean flagMinMem;

    private String state;

    public static final String STATE_INIT = "INITIALIZING";
    public static final String STATE_LOAD_RES = "LOADING RESOURCES";
    public static final String STATE_PAUSED = "PAUSED";
    public static final String STATE_IN_PROG = "CORE TEST IN PROGRESS";

    private LocalDateTime dateTimeOfStart;

    private final StringBuffer localStringBuffer;//synchronized stringbuilder

    //Draconian synchronization
    //thread safe lazy instantiation classic singleton
//    public static synchronized BPTask getInstance(String[] args) {
//        if (instance == null) {
//            instance = new BPTask(args);
//        }
//        return instance;
//    }
    public List<D> getPriceCache() {
        return priceCache;
    }

    public BPTask(String[] args, Class<D> priceDataType) {

        priceCache = Collections.synchronizedList(new ArrayList<D>());
        tradeCache = Collections.synchronizedList(new ArrayList<Trade>());
        this.priceDataType = priceDataType;

        this.args = args;

        additionalParameters = new ConcurrentHashMap<String, String>();
        localStringBuffer = new StringBuffer();
        if (args != null && args.length != 0) {
            priceDataPaths = stringToPathArray(args[ARG_INDEX_PRICE_DATA]);
            tradeDataPaths = stringToPathArray(args[ARG_INDEX_TRADE_DATA]);

            if (args.length > 2) {
                System.out.println("Additional parameters detected");
                for (int i = 2; i < args.length; i += 2) {
                    additionalParameters.put(args[i], args[i + 1]);
                }
            }

            processArguments();
        } else {
            priceDataPaths = null;
            tradeDataPaths = null;
        }

    }

    private boolean loadResources() {
        //load resources from files to cache

        //CHECK IF ON SSD //MAYBE? so i can parallel load files if they are big.
        List<String> priceDataLines = new ArrayList<>();
        List<String> tradeDataLines = new ArrayList<>();
        for (Path path : priceDataPaths) {
            printToConsole("Reading price file:" + path.toString());
            try (BufferedReader br = Files.newBufferedReader(path)) {
                //br returns as stream and convert it into a List
                printToConsole("Collecting price data" + path.getFileName());
                priceDataLines.addAll(br.lines().collect(Collectors.toList()));

            } catch (IOException ex) {
                Logger.getLogger(BPTask.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        for (Path path : tradeDataPaths) {
            if (path.toString().equals("null")) {
                printToConsole("no trade data");
                break;
            }
            try (BufferedReader br = Files.newBufferedReader(path)) {//TODO implement
                printToConsole("Collecting trade data:" + path.getFileName());
                tradeDataLines.addAll(br.lines().collect(Collectors.toList()));
            } catch (IOException ex) {
                Logger.getLogger(BPTask.class.getName()).log(Level.SEVERE, null, ex);
            }

        }

        printToConsole("Parsing price data.Number of lines:" + priceDataLines.size());

        if (flagMinMem) {
            printToConsole("Minimal memory parse.");

            //MEMORY SAVING WITH THIS USAGE OF ITERATOR AND NOT 2 LISTS
            for (Iterator<String> iterator = priceDataLines.iterator(); iterator.hasNext();) {
                String[] row = iterator.next().split(DATA_DELIM);
                if (priceDataType == Bar.class) {
                    priceCache.add((D) new Bar("yyyyMMdd HHmmss", row[0], row[1], row[2], row[3], row[4], row[5]));
                } else {
                    //priceCacheTick.add(new Tick());
                }

                iterator.remove();
            }

        } else {
            for (String row : priceDataLines) {
                String[] parsed = row.split(DATA_DELIM);
                if (priceDataType == Bar.class) {
                    priceCache.add((D) new Bar("yyyyMMdd HHmmss", parsed[0], parsed[1], parsed[2], parsed[3], parsed[4], parsed[5]));
                } else {
                    //priceCacheTick.add(new Tick());
                }

            }
        }

        if (!tradeDataLines.isEmpty()) {
            printToConsole("Parsing trade data.Number of lines:" + tradeDataLines.size());

            for (String row : tradeDataLines) {
                try {
                    String[] parsed = row.split(",");
                    getTradeCache().add(new Trade(
                            Integer.valueOf(parsed[0]),
                            parsed[0],
                            new BigDecimal(parsed[1]),
                            Integer.valueOf(parsed[2]),
                            new BigDecimal(parsed[3]),
                            new BigDecimal(parsed[4]),
                            new BigDecimal(parsed[5]),
                            new BigDecimal(parsed[6]),
                            parsed[7],
                            parsed[8]));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        }

        //FASTEST SERIAL
        //parallel
        printToConsole("Completed parsing data.");
        printToConsole("Size of barCache : " + priceCache.size());

        return true;
    }

    //private final Object reportLock=new Object();
    //    private Report<String> report = new Report<>("", 0, 0, 0, 0, "hey", "hou");
    private Report<String> report;

    public void setReport(Report report) {//should be safe//bud im not sure. might have to synchronize this , or ask cern
        this.report = report;
    }

    private void initCommThreads() {

        Thread reciever = new Thread(() -> {
            String msg;
            while (true) {
                try {
                    msg = in.readUTF();
                    System.out.println("message recieved:" + msg);
                    executeRecievedCmd(msg);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.exit(0);
                }
            }

        });
        reciever.setDaemon(true);
        reciever.start();
        @SuppressWarnings("SleepWhileInLoop")
        Thread sender = new Thread(() -> {
            while (true) {
                try {
                    if (report != null) {
                        sendMsg(new BPMessage(BPMessage.MSG_TYPE_REPORT, report));
                        println("message send");
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        });
        sender.setName("SENDER THREAD");
        sender.setDaemon(true);
        sender.start();

    }

    @SuppressWarnings("SleepWhileInLoop")
    public void startTask(String serverName, int port) {// BLOCKING METHOD !!

        dateTimeOfStart = LocalDateTime.now();
        //initialize();
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            try (Socket client = new Socket(serverName, port)) {
                System.out.println("Just connected to " + client.getRemoteSocketAddress());
                OutputStream outToServer = client.getOutputStream();
                out = new DataOutputStream(outToServer);
                //writing to "out" stream shows up on UI console
                //writing to "System.out" shows in output System.out console of Server
                out.writeUTF("Hello from " + client.getLocalSocketAddress());
                InputStream inFromServer = client.getInputStream();
                in = new DataInputStream(inFromServer);

                System.out.println("[SERVER]" + in.readUTF());

                initCommThreads();

                loadResources();

                //CORE TEST EXECUTION HERE
                Thread coreTaskExecThread = new Thread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            sendMsg(new BPMessage(BPMessage.MSG_TYPE_RESULT, execute()));
                        } catch (IOException ex) {
                            printToConsole("Error occured.");
                            Logger.getLogger(BPTask.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });

                coreTaskExecThread.setPriority(Thread.MAX_PRIORITY);
                coreTaskExecThread.setName("Core_Execution_Thread");
                coreTaskExecThread.setDaemon(true);
                coreTaskExecThread.start();
                println("Core task started");
                try {
                    coreTaskExecThread.join();//keep main thread alive  till coretest done, not sure if needed as much
                } catch (InterruptedException ex) {
                    Logger.getLogger(BPTask.class.getName()).log(Level.SEVERE, null, ex);
                }

                //System.out.println("/end");
                //client.close //use this in normal closing procedure i think
                //System.out.println("/end");
                //client.close //use this in normal closing procedure i think
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }

    }

    public abstract Result execute();

    private final Object recieverLock = new Object();

    public void executeRecievedCmd(String cmd) {
        synchronized (recieverLock) {
            switch (cmd) {
                case "/end": {//no need only server kills connection

                    break;
                }
                case "/info": {
                    printToConsole("Test info: " + toString());
                    break;
                }
            }
        }
    }

    private boolean processArguments() {//returns validity of arguments

        System.out.println("Processing arguments");

        for (String key : getAdditionalParameters().keySet()) {
            switch (key) {
                case "-s": {//seed
                    System.out.println("-s (check seed):" + getAdditionalParameters().get(key));
                    break;
                }
                case "-ms": {
                    System.out.println("-mm (minimum memory): :" + getAdditionalParameters().get(key));
                    try {
                        flagMinMem = Boolean.valueOf(getAdditionalParameters().get(key));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                //custom arguments : -c_\\S+    /* basically [a-zA-Z_0-9] */
            }

        }
        return true;
    }

    public final Path[] stringToPathArray(String string) {
        String[] stringArray = string.split(ARG_DELIM_DATA_PATHS);
        System.out.println("Size of split:" + stringArray.length);

        Path[] paths = new Path[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            paths[i] = Paths.get(stringArray[i]);
        }
        return paths;
    }

    public void println(String string) {
        System.out.println("[CLIENT]:" + string + " ");//TO DIFFERENTIATE AT SERVER SIDE OUTPUT
    }

    private final Object commLock = new Object();

    //prints to both outputs
    public void printToConsole(String string) {
        synchronized (commLock) {
            println(string);
            try {
                out.writeUTF(string);
            } catch (IOException ex) {
                System.out.println("IO ex:" + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    public void sendMsg(BPMessage msg) throws IOException {
        synchronized (commLock) {
            println("Sending msg:" + BPUtil.objectToJsonString(msg));
            out.writeUTF("[*msg*]" + BPUtil.objectToJsonString(msg));
        }
    }

    @Override
    public String toString() {
        return "STATE:";
    }

    /**
     * @return the status
     */
    public String getState() {
        return state;
    }

    public void setState(String STATE) {
        this.state = STATE;
        stateChanged();
    }

    private void stateChanged() {
        println("State changed:" + state);
    }

    /**
     * @return the localStringBuffer
     */
    public StringBuffer getLocalStringBuffer() {
        return localStringBuffer;
    }

    /**
     * @return the additionalParameters
     */
    public Map<String, String> getAdditionalParameters() {
        return additionalParameters;
    }

    /**
     * @return the tradeCache
     */
    public List<Trade> getTradeCache() {
        return tradeCache;
    }

}
