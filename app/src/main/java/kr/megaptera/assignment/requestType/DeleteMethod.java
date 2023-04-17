package kr.megaptera.assignment.requestType;

import com.google.gson.Gson;

import java.util.Map;

public class DeleteMethod extends Method{
    @Override
    public String process(Map<Long, String> tasks) {

        Long id = parseTaskId(getRequestMethod(requestString));

        if (!tasks.containsKey(id)) {
            return generateMessage("", "404 Not Found");
        }

        tasks.remove(id);
        String content = new Gson().toJson(tasks);
        return generateMessage(content, "200 OK");
    }

}
