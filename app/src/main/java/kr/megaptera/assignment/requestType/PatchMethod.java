package kr.megaptera.assignment.requestType;

import com.google.gson.Gson;

import java.util.Map;

public class PatchMethod extends Method{
    @Override
    public String process(Map<Long, String> tasks) {
        Long id = parseTaskId(getRequestMethod(requestString));

        if(!tasks.containsKey(id)) {
            return generateMessage("", "404 Not Found");
        }

        String task = parsePayload(requestString, "task");

        if (task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }

        tasks.put(id, task);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "200 OK");
    }

}