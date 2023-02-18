package kr.megaptera.assignment;

import com.google.gson.JsonElement;
import java.util.Map;

public class TaskService {
  private final TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }


  public Map<Long, String> getTaskList() {
    return taskRepository.findAll().toMap();
  }

  public Map<Long, String> getTask(Long id) {
    return taskRepository.findById(id).toMap();
  }

  public Map<Long, String> createTask(JsonElement requestBody) {
    return taskRepository.save(requestBody.getAsJsonObject().get("task").getAsString()).toMap();
  }

  public Map<Long, String> deleteTask(Long id) {
    Task task = taskRepository.deleteById(id);
    if (task == null) {
      return null;
    } else {
      return task.toMap();
    }
  }

  public Map<Long, String> updateTask(Long id, JsonElement requestBody) {
    return taskRepository.update(id, requestBody.getAsJsonObject().get("task").getAsString()).toMap();
  }

}
