/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.controllers;

import bplib.util.BPSystemInfo;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import oshi.util.FormatUtil;

/**
 * FXML Controller class
 *
 * @author Kofola
 */
public class SysInfoViewController implements Initializable {

    @FXML
    private GridPane gridPaneStatHolderS;
    @FXML
    private TabPane tabPane;
    @FXML
    private GridPane gridPaneStatHolderC;
    @FXML
    private BarChart chartCPU;

    private ArrayList<String> sysInfoData;
    private int cpuCount;

    private final XYChart.Series seriesPCPU;
    private final XYChart.Series seriesCpuOther;

    @FXML
    private Label lblCpuTemp;
    @FXML
    private Label lblProcAndThreads;
    @FXML
    private Label lblRAM;

    @FXML
    private Label lblOs;
    @FXML
    private Label lblProc;
    @FXML
    private Label lblCPUP;
    @FXML
    private Label lblCPUL;
    @FXML
    private Label lblSwap;
    @FXML
    private Label lblUptime;
    @FXML
    private Label lblHost;
    @FXML
    private Label lblDomain;
    @FXML
    private StackPane stackPane;
    @FXML
    private Label lblLoading;
    @FXML
    private ToggleButton tBtnOSHI;
    private volatile boolean canUpdateOSHI = true;
    private volatile boolean initStats;
    private final Object oshiLock = new Object();

    /**
     * Initializes the controller class.
     */
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void initialize(URL url, ResourceBundle rb) {

        sysInfoData = new ArrayList<>();
        new Thread(() -> {
            int iter = 0;
            while (!initStats) {// not sure if this has to be volatile
                int tmpIter = iter;
                Platform.runLater(() -> {
                    if (tmpIter == 0) {
                        lblLoading.setText("Loading.");
                    } else if (tmpIter == 1) {
                        lblLoading.setText("Loading..");
                    } else {
                        lblLoading.setText("Loading...");
                    }
                });
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SysInfoViewController.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (iter == 2) {
                    iter = 0;
                    continue;
                }
                iter++;
            }

        }).start();

        Thread refreshOSHI = new Thread(() -> {
            initStats();
            boolean skipFlags[] = new boolean[1];
            while (true) {
                synchronized (oshiLock) {
                    while (!canUpdateOSHI) {
                        try {
                            oshiLock.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(SysInfoViewController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }// so we dont block FXAT
               // lblLoading.setVisible(false);
                updateLoad();
                String cpuTemp = "";
                try {
                    if (skipFlags[0] != true) {
                        cpuTemp = String.format("%.1f°C", BPSystemInfo.getHal().getSensors().getCpuTemperature());
                        if (cpuTemp.equals("0.0°C")) {
                            throw new Exception("Exception: Failed to get correct cpu temp.");
                        }
                    } else {
                        cpuTemp = "N/A";
                    }
                } catch (Exception e) {
                    skipFlags[0] = true;
                    cpuTemp = "N/A";
                    System.out.println("DETECTED WMI FAIL.");
                    e.printStackTrace();
                }
                cpuTemp = "CPU Temperature:" + cpuTemp;
                String processesAndThreads = "Processes: " + BPSystemInfo.getOs().getProcessCount() + " Threads: " + BPSystemInfo.getOs().getThreadCount();
                String ramInfo = "Used:" + FormatUtil.formatBytes(BPSystemInfo.getHal().getMemory().getTotal() - BPSystemInfo.getHal().getMemory().getAvailable()) + "  Total:"
                        + FormatUtil.formatBytes(BPSystemInfo.getHal().getMemory().getTotal());
                String uptime = String.valueOf(FormatUtil.formatElapsedSecs(BPSystemInfo.getHal().getProcessor().getSystemUptime()));
                String tmpCpuTemp = cpuTemp;
                Platform.runLater(new Runnable() {

                    @Override
                    public void run() {
                        if (canUpdateOSHI) {
                            lblLoading.setVisible(false);
                            lblCpuTemp.setText(tmpCpuTemp);
                            lblProcAndThreads.setText(processesAndThreads);
                            lblRAM.setText(ramInfo);
                            lblUptime.setText(uptime);
                        }
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(SysInfoViewController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        refreshOSHI.setDaemon(true);
        refreshOSHI.start();

        chartCPU.getData().addAll(seriesPCPU, seriesCpuOther);

        //tBtnOSHI.
        tBtnOSHI.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                tBtnOSHI.setText("Turn off OSHI");
            } else {
                tBtnOSHI.setText("Turn on OSHI");
            }

            synchronized (oshiLock) {
                canUpdateOSHI = !newValue;
                if (canUpdateOSHI) {
                    oshiLock.notify();
                    lblLoading.setVisible(false);
                } else {
                    lblLoading.setText("OSHI Disabled");
                    lblLoading.setVisible(true);
                    lblCpuTemp.setText("N/A");
                    lblProcAndThreads.setText("N/A");
                    lblRAM.setText("N/A");
                    lblUptime.setText("N/A");
                }
            }

        });

    }

    public SysInfoViewController() {
        this.seriesCpuOther = new XYChart.Series();
        this.seriesPCPU = new XYChart.Series();

    }

    private double[] getLoad() {
        double[] load = BPSystemInfo.getHal().getProcessor().getProcessorCpuLoadBetweenTicks();
        double[] loadPCPU = new double[load.length];
        for (int i = 0; i < load.length; i++) {
            loadPCPU[i] = load[i] * 100;
        }
        return loadPCPU;
    }

    private void updateLoad() {
        double[] vals = getLoad();

        double cpuLBT = BPSystemInfo.getHal().getProcessor().getSystemCpuLoadBetweenTicks() * 100;
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ((XYChart.Data) seriesCpuOther.getData().get(0)).setYValue(cpuLBT);
                for (int i = 0; i < vals.length; i++) {
                    ((XYChart.Data) seriesPCPU.getData().get(i)).setYValue(vals[i]);
                }
            }
        });

    }

    private void initStats() {

        cpuCount = BPSystemInfo.getHal().getProcessor().getLogicalProcessorCount();

        //show some load
        String os = BPSystemInfo.getOs().toString();
        String processor = BPSystemInfo.getHal().getProcessor().toString();
        String physicalCPUs = String.valueOf(BPSystemInfo.getHal().getProcessor().getPhysicalProcessorCount());
        String logicalCPUs = String.valueOf(BPSystemInfo.getHal().getProcessor().getLogicalProcessorCount());
        String swapUsed = FormatUtil.formatBytes(BPSystemInfo.getHal().getMemory().getSwapUsed()) + "/"
                + FormatUtil.formatBytes(BPSystemInfo.getHal().getMemory().getSwapTotal());
        String uptime = String.valueOf(FormatUtil.formatElapsedSecs(BPSystemInfo.getHal().getProcessor().getSystemUptime()));
        String hostName = BPSystemInfo.getOs().getNetworkParams().getHostName();
        String domainName = BPSystemInfo.getOs().getNetworkParams().getDomainName();
        String ram = "Used:" + FormatUtil.formatBytes(BPSystemInfo.getHal().getMemory().getTotal() - BPSystemInfo.getHal().getMemory().getAvailable()) + "  Total:"
                + FormatUtil.formatBytes(BPSystemInfo.getHal().getMemory().getTotal());

        Platform.runLater(() -> {

            for (int i = 0; i < cpuCount; i++) {
                seriesPCPU.getData().add(new XYChart.Data("CPU" + (i + 1), 0));
            }
            seriesCpuOther.getData().add(new XYChart.Data("load(CT)", 0));
            lblLoading.setVisible(false);

            lblOs.setText(os);
            lblProc.setText(processor);
            lblCPUP.setText(physicalCPUs);
            lblCPUL.setText(logicalCPUs);
            lblSwap.setText(swapUsed);
            lblUptime.setText(uptime);
            lblHost.setText(hostName);
            lblDomain.setText(domainName);
            lblRAM.setText(ram);
        });
        initStats = true;
    }

}
