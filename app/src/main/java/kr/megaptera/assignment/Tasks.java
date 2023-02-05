package kr.megaptera.assignment;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Tasks {

  private final List<Task> tasks;
  private static final String DUPLICATED_ID_MESSAGE = "각 Task 끼리 Id 가 겹칠 수 없습니다.";

  public Tasks(List<Task> tasks) {
    validateDuplicatedId(tasks);
    this.tasks = tasks;
  }

  private void validateDuplicatedId(List<Task> tasks) {
    Set<Task> taskSet = new HashSet<>(tasks);
    if (taskSet.size() != tasks.size()) {
      throw new IllegalArgumentException(DUPLICATED_ID_MESSAGE);
    }
  }

  public Task get(int index) {
    return tasks.get(index);
  }

  public Map<Long, String> toMap() {
    Map<Long, String> map = new HashMap<>();
    for (Task task : this.tasks) {
      map.put(task.getId(), task.getTitle());
    }

    return map;
  }
}
