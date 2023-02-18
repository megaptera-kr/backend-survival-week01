package kr.megaptera.assignment.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.Map;

public class ServerJsonParser {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public String returnTask(String requestBody) {
        com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
        JsonElement element = parser.parse(requestBody);

        String task = element.getAsJsonObject().get("task").getAsString();
        return task;
    }

    public String getTasksToJson(Map<Long, String> tasks) {
        String tasksToJson = gson.toJson(tasks);
        return tasksToJson;
    }
}
