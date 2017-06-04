/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javatestone;

import bplib.communication.Report;
import bplib.communication.Result;
import bplib.priceData.Bar;
import bplib.task.BPTask;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kofola
 */
public class JavaTestOne {

    public static final String serverName = "127.0.0.1";
    public static final int port = 33221;

    /**
     * java -jar .\tests\Java\JavaClient.jar PRICE_path*path TRADE_path*path -s 1000 ...
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        //Vytvorenie ulohy
        BPTask<Bar> test = new BPTask<Bar>(args, Bar.class) {

            @Override
            public Result execute() {

                //Demonstracia nevyuziva vsetky parametre
                setReport(new Report<String>("Status", 0l, Thread.activeCount(), Runtime.getRuntime().totalMemory(), 0));
                //Pisanie do konzoly UI
                printToConsole("BPTask execute processing.");
                //BigDecimal zarucuje preciznost vypoctov (IEEE 128-bit floating point numbers)
                BigDecimal average = new BigDecimal(0);
                //Ziskanie dat z nasho parametra
                int numberOfThreads = Integer.valueOf(getAdditionalParameters().get("-c_numOfThreads"));
                ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
                /*
                 Vyuzitie datoveho paralelizmu.
                 Do exekutora vlakname Callable<T>, vracia Future
                 drziace vysledne data vypoctu.
                 */
                long startTime = System.nanoTime();
                int partSize;
                partSize = Math.round(getPriceCache().size() / numberOfThreads);
                ArrayList<Future<BigDecimal>> sums = new ArrayList<>();

                for (int i = 0, indexStart = 0; i < numberOfThreads; i++, indexStart += partSize) {
                    if (i == numberOfThreads - 1) {//Posledna cast.

                        int tmpIndexStart = indexStart;
                        sums.add(executor.submit(new Callable<BigDecimal>() {

                            @Override
                            public BigDecimal call() throws Exception {

                                BigDecimal sum = new BigDecimal(0);
                                for (Bar bar : getPriceCache().subList(tmpIndexStart, getPriceCache().size() - 1)) {
                                    sum = sum.add(bar.getCloseBid());
                                }
                                setReport(new Report<String>("Status", 0l, Thread.activeCount(), Runtime.getRuntime().totalMemory(), 0));
                                return sum;
                            }
                        }
                        ));
                        println("Part " + (i + 1) + ": startIndex:" + indexStart + "  endIndex:" + (getPriceCache().size() - 1));
                        break;
                    }

                    int tmpIndexStart = indexStart;
                    sums.add(executor.submit(new Callable<BigDecimal>() {

                        @Override
                        public BigDecimal call() throws Exception {

                            BigDecimal sum = new BigDecimal(0);
                            for (Bar bar : getPriceCache().subList(tmpIndexStart, (tmpIndexStart + partSize) - 1)) {
                                sum = sum.add(bar.getCloseBid());
                            }
                            println("Sum" + sum.toString());
                            return sum;
                        }
                    }
                    ));
                    println("Part " + (i + 1) + ": startIndex:" + indexStart + "  endIndex:" + ((indexStart + partSize) - 1));
                }
                //Ziskanie priemeru.
                for (Future<BigDecimal> sum : sums) {
                    try {
                        average = average.add(sum.get());   //blokuje
                        System.out.println("average sum :" + average.toString() + " is done?" + sum.isDone());
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(JavaTestOne.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                average = average.divide(new BigDecimal(getPriceCache().size()), RoundingMode.HALF_UP);
                //Pridanie dat do objektu Result, automaticky zaslane serveru.
                Result result = new Result();
                result.getData().put("Task command", Arrays.toString(args));
                result.getData().put("Average price", average.toString());

                long stopTime = System.nanoTime();
                long elapsedTime = stopTime - startTime;
                result.getData().put("Num of Threads", "" + numberOfThreads);
                result.getData().put("Exec Time nano", String.valueOf(elapsedTime));
                result.getData().put("Exec Time milisec", String.valueOf(TimeUnit.NANOSECONDS.toMillis(elapsedTime)));
                result.getData().put("Exec Time sec", String.valueOf(TimeUnit.NANOSECONDS.toSeconds(elapsedTime)));
                return result;
            }
        };
        test.startTask(serverName, port);
    }

//    private static void test() {
//        System.out.println("Method Test");
//    }
}
