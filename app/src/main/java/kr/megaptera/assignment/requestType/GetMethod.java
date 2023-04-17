package kr.megaptera.assignment.requestType;

import com.google.gson.Gson;

import java.util.Map;

public class GetMethod extends Method{
    @Override
    public String process(Map<Long, String> tasks) {
        String taskString = new Gson().toJson(tasks);
        return generateMessage(taskString, "200 OK");
    }
}