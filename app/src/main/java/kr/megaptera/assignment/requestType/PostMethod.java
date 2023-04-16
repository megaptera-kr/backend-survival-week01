package kr.megaptera.assignment.requestType;

import com.google.gson.Gson;

import java.util.Map;

import static kr.megaptera.assignment.App.newId;

public class PostMethod extends Method{

    @Override
    public String process(Map<Long, String> tasks) {
        String task = parsePayload(requestString, "task");

        if (task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }

        newId += 1;
        tasks.put(newId, task);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "201 Created");
    }
}