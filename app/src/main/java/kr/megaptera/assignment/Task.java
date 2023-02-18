package kr.megaptera.assignment;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Task {

  public static final String WRONG_ID_RANGE_MESSAGE = "Id 값은 양의 정수여야 합니다.";
  private Long id;
  private String title;

  public Task(Long id, String title) {
    validatePositiveId(id);
    this.id = id;
    this.title = title;
  }

  private void validatePositiveId(Long id) {
    if (id <= 0) {
      throw new IllegalArgumentException(WRONG_ID_RANGE_MESSAGE);
    }
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Task task = (Task) o;
    return Objects.equals(id, task.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "Task{" +
        "id=" + id +
        ", title='" + title + '\'' +
        '}';
  }

  public void changeTitle(String title) {
    this.title = title;
  }

  public Map<Long, String> toMap() {
    Map<Long, String> map = new HashMap<>();
    map.put(this.getId(), this.getTitle());

    return map;
  }
}
