

package com.siliconstudios.dynamicshopguiplus.util;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public final class JsonUtil {
    private static final JSONParser parser = new JSONParser();

    public static JSONObject parseJson(InputStreamReader stream) throws ParseException, IOException {
        return (JSONObject)parser.parse((Reader)stream);
    }

    public static JSONObject parseJson(FileReader file) throws ParseException, IOException {
        return (JSONObject)parser.parse((Reader)file);
    }

    public static JSONObject parseJson(String format) throws ParseException {
        return (JSONObject)parser.parse(format);
    }

    public static JSONArray parseArray(List<String> array) throws ParseException {
        return (JSONArray)parser.parse(array.toString());
    }

    public static JSONArray parseArray(String s) throws ParseException {
        return (JSONArray)parser.parse(s);
    }

    public static JSONArray parseArray(InputStreamReader stream) throws ParseException, IOException {
        return (JSONArray)parser.parse((Reader)stream);
    }

    public static Long getLong(JSONObject json, String key) {
        return (Long)json.get((Object)key);
    }

    public static String getString(JSONObject json, String key) {
        return (String)json.get((Object)key);
    }

    public static ArrayList<String> getArray(JSONObject json, String key) {
        return (ArrayList)json.get((Object)key);
    }
}

