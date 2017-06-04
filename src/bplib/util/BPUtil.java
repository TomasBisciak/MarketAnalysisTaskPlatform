/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bplib.util;

import com.google.gson.Gson;

/**
 *
 * @author Kofola
 */
public class BPUtil {

    private static final Gson gson = new Gson();
    //SYNCHRONIZACIA ASI NIE JE POTREBNA.
    public static String objectToJsonString(Object object) {
            return gson.toJson(object);
    }

    public static <T> T jsonStringToObject(String jsonString, Class cls) {
            return (T) gson.fromJson(jsonString, cls);
    }
}
