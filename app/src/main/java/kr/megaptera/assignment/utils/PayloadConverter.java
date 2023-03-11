package kr.megaptera.assignment.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class PayloadConverter {
    public static String Convert(String jsonMessage, String key) {
        try {
            JsonElement jsonElement = JsonParser.parseString(jsonMessage);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.get(key).getAsString();
        } catch (Exception e){
            return "";
        }
    }
}
