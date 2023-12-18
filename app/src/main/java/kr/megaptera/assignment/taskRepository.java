package kr.megaptera.assignment;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class taskRepository {

    // Tasks
    private Long taskKey = 0L;
    private Map<Long, String> tasks = new HashMap<>();

    public String getTasksByJson() {
        return new Gson().toJson(tasks);
    }

    public void insert(String requestBody) {
        Gson gson = new Gson();
        // TODO(Fix warning)
        Map<String, String> entry = gson.fromJson(requestBody, Map.class);

        // TODO(반복문 제거. 단일 Key만 뽑도록)
        for (var key : entry.keySet()) {
            String value = entry.get(key);
            taskKey++;
            tasks.put(taskKey, value);
        }
    }

    public void update(Long taskKey, String requestBody) {
        Gson gson = new Gson();
        // TODO(Fix warning)
        Map<String, String> entry = gson.fromJson(requestBody, Map.class);

        // TODO(반복문 제거. 단일 Key만 뽑도록)
        for (var key : entry.keySet()) {
            String value = entry.get(key);
            // 덮어쓰기
            tasks.put(taskKey, value);
        }
    }

    public void delete(Long taskKey) {
        tasks.remove(taskKey);
    }

    public boolean checkKey(Long key) {
        return this.tasks.containsKey(key);
    }
}
