package kr.megaptera.assignment;

import com.google.gson.JsonElement;

public class TaskService {
  private final TaskRepository taskRepository;

  public TaskService(TaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }


  public Tasks getTaskList() {
    return taskRepository.findAll();
  }

  public Task getTask(Long id) {
    return taskRepository.findById(id);
  }

  public Task createTask(JsonElement requestBody) {
    return taskRepository.save(requestBody.getAsJsonObject().get("task").getAsString());
  }

  public Task deleteTask(Long id) {
    return taskRepository.deleteById(id);
  }

  public Task updateTask(Long id, JsonElement requestBody) {
    return taskRepository.update(id, requestBody.getAsJsonObject().get("task").getAsString());
  }
}
