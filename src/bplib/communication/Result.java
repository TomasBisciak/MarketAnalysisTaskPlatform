package bplib.communication;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Kofola
 */
public class Result {

    //nieco sem asi pojde.
    private final Map<String, String> data;

    public Result() {

        data = new ConcurrentHashMap<>();
    }

    /**
     * @return the data
     */
    public Map<String, String> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Result{" + "data KEY SET=" + Arrays.toString(data.keySet().toArray()) + "data VALUES=" + Arrays.toString(data.values().toArray()) + '}';
    }

}
