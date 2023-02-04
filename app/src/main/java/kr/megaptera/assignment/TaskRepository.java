package kr.megaptera.assignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TaskRepository {
  private static final Map<Long, Task> map = new HashMap<>();
  private static Long id = 1L;

  public Task save(String title) {
    Task newTask = new Task(id, title);
    map.put(id, newTask);
    id++;
    return newTask;
  }


  public Task findById(Long id) {
    return map.get(id);
  }

  public Tasks findAll() {
    return new Tasks(new ArrayList<>(map.values()));
  }

  public void clear() {
    map.clear();
    id = 1L;
  }
}
