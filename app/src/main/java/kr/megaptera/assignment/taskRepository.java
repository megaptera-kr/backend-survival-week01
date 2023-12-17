package kr.megaptera.assignment;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class taskRepository {

    // Tasks
    private Long taskKey = 1L;
    private Map<Long, String> tasks = new HashMap<>();

    public String getTasksByJson() {
        return new Gson().toJson(tasks);
    }

    public boolean insert(String requestBody) {
        Gson gson = new Gson();
        // TODO(Fix warning)
        Map<String, String> entry = gson.fromJson(requestBody, Map.class);

        // TODO(반복문 제거. 단일 Key만 뽑도록)
        for (var key : entry.keySet()) {
            String value = entry.get(key);
            tasks.put(taskKey, value);
            taskKey++;
        }
        return true;
    }
}
