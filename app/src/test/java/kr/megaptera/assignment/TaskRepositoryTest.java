package kr.megaptera.assignment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TaskRepositoryTest {

  private TaskRepository taskRepository;

  @BeforeEach
  void setUp() {
    taskRepository = new TaskRepository();
    taskRepository.clear();
  }

  @DisplayName("Task 저장하기")
  @Test
  void saveTask() {
    String title = "놀러가기";

    Task newTask = taskRepository.save(title);

    assertThat(newTask.getId()).isEqualTo(1L);
    assertThat(newTask.getTitle()).isEqualTo(title);
  }

  @DisplayName("Task 하나 가져오기")
  @Test
  void getTaskDetail() {
    String title = "놀러가기";
    Task newTask = taskRepository.save(title);

    Task resultTask = taskRepository.findById(1L);

    assertThat(resultTask.getId()).isEqualTo(newTask.getId());
    assertThat(resultTask.getTitle()).isEqualTo(newTask.getTitle());
  }

  @DisplayName("Task 여러개 가져오기")
  @Test
  void getTaskList() {
    String title1 = "놀러가기";
    String title2 = "책보기";
    List<Task> newTasks = Arrays.asList(taskRepository.save(title1), taskRepository.save(title2));

    Tasks resultTask = taskRepository.findAll();

    for (int i = 0; i < 2; i++) {
      assertThat(newTasks.get(i).getId()).isEqualTo(resultTask.get(i).getId());
      assertThat(newTasks.get(i).getTitle()).isEqualTo(resultTask.get(i).getTitle());
    }
  }
}
