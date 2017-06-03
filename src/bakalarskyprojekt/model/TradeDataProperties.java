/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.model;

import javafx.beans.property.SimpleBooleanProperty;

/**
 *
 * @author hp
 */
public class TradeDataProperties {

    private final int numOfTradesShort;
    private final int numOfTradesLong;
    private final int stopLossRangeMin;
    private final int stopLossRangeMax;
    private final int targetRangeMin;
    private final int targetRangeMax;
    private final String[] param;

    @SuppressWarnings("FieldMayBeFinal")
    private boolean paramFlag;
    private boolean paramValid;
    

    public TradeDataProperties(int numOfTradesShort, int numOfTradesLong, int stopLossRangeMin, int stopLossRangeMax, int targetRangeMin, int targetRangeMax, String... param) {
        this.numOfTradesShort = numOfTradesShort;
        this.numOfTradesLong = numOfTradesLong;
        this.stopLossRangeMin = stopLossRangeMin;
        this.stopLossRangeMax = stopLossRangeMax;
        this.targetRangeMin = targetRangeMin;
        this.targetRangeMax = targetRangeMax;
        paramFlag = !(param == null||param[0].equals(""));
        if (paramFlag) {
            paramValid = validateArguments(param);
        }
        this.param = param;
    
    }

      /**
     *
     * @param args passed into application
     * @return returns if valid arguments
     */
    public static boolean validateArguments(String[] args) {
        //holds which values were sucessfully parsed/used
        boolean[] paramFlags = new boolean[2];//which are set.
        if (args.length != 0) {

            //switch case to check parameters
            for (int i = 0; i < args.length; i += 2) {//check -paramId

                switch (args[i]) {
                    case "-s": {

                        try {
                            int seed=Integer.valueOf(args[i + 1]);
                        } catch (NumberFormatException ex) {
                            return false;
                        }

//                        try {
//                            if (!occupyPort(Integer.valueOf(args[i + 1]))) {
//                                System.out.println("Port" + Integer.valueOf(args[i + 1]) + " cannot be occupied by application");
//                                continue;
//                            }
//                            paramFlags[0] = true;
//                        } catch (NumberFormatException | IndexOutOfBoundsException ex) {
//                            System.err.println("Argument is not a valid number/argument not found");
//                        }
                        break;
                    }
                    case "": {

                        break;
                    }
                    default: {
                        System.out.println("Invalid parameter \" " + args[i] + " \"");
                        return false;
                    }

                }

            }

        }
        return true;

    }

    /**
     * @return the numOfTradesShort
     */
    public int getNumOfTradesShort() {
        return numOfTradesShort;
    }

    /**
     * @return the numOfTradesLong
     */
    public int getNumOfTradesLong() {
        return numOfTradesLong;
    }

    /**
     * @return the stopLossRangeMin
     */
    public int getStopLossRangeMin() {
        return stopLossRangeMin;
    }

    /**
     * @return the stopLossRangeMax
     */
    public int getStopLossRangeMax() {
        return stopLossRangeMax;
    }

    /**
     * @return the targetRangeMin
     */
    public int getTargetRangeMin() {
        return targetRangeMin;
    }

    /**
     * @return the targetRangeMax
     */
    public int getTargetRangeMax() {
        return targetRangeMax;
    }

    /**
     * @return the param
     */
    public String[] getParam() {
        return param;
    }

    /**
     * @return the paramValidity
     */
    public boolean isParamValid() {
        return paramValid;
    }

    @Override
    public String toString() {
        return "TradeDataProperties{" + "numOfTradesShort=" + numOfTradesShort + ", numOfTradesLong=" + numOfTradesLong + ", stopLossRangeMin=" + stopLossRangeMin + ", stopLossRangeMax=" + stopLossRangeMax + ", targetRangeMin=" + targetRangeMin + ", targetRangeMax=" + targetRangeMax + ", param=" + param + ", paramFlag=" + paramFlag + ", paramValid=" + paramValid + '}';
    }

    
    
}
