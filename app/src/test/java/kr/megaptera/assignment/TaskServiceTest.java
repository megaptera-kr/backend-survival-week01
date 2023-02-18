package kr.megaptera.assignment;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TaskServiceTest {

  private TaskRepository taskRepository;
  private TaskService taskService;

  @BeforeEach
  void setUp() {
    taskRepository = new TaskRepository();
    taskRepository.clear();

    taskService = new TaskService(taskRepository);

  }

  @DisplayName("Task 리스트 가져오기")
  @Test
  void getTaskList() {
    Tasks tasks = new Tasks(Arrays.asList(taskRepository.save("놀러가기"),
        taskRepository.save("책보기")));

    Tasks resultList = taskService.getTaskList();

    for (int i = 0; i < 2; i++) {
      assertThat(resultList.get(i).getId()).isEqualTo(tasks.get(i).getId());
      assertThat(resultList.get(i).getTitle()).isEqualTo(tasks.get(i).getTitle());
    }

  }

  @DisplayName("Task 하나 가져오기")
  @Test
  void getTaskDetail() {
    Task expectedTask = taskRepository.save("놀러가기");

    Task resultTask = taskService.getTask(expectedTask.getId());

    assertThat(resultTask.getId()).isEqualTo(expectedTask.getId());
    assertThat(resultTask.getTitle()).isEqualTo(expectedTask.getTitle());
  }
}
