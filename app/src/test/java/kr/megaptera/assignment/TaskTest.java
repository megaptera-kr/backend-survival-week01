package kr.megaptera.assignment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TaskTest {

  @DisplayName("Task 생성 테스트")
  @Test
  void createTask() {
    assertThatCode(() -> new Task(1L, "놀러가기"))
        .doesNotThrowAnyException();
  }

  @DisplayName("Id 값이 양의 정수가 아니면 Exception 이 발생한다.")
  @Test
  void shouldHavePositiveId() {
    assertThatCode(() -> new Task(-1L, "놀러가기"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Id 값은 양의 정수여야 합니다.");
  }

  @DisplayName("Id 값이 같으면 같은 객체이다.")
  @Test
  void equalIdThenSame() {
    Task task1 = new Task(1L, "놀러가기");
    Task task2 = new Task(1L, "잠자기");

    assertThat(task1.equals(task2)).isTrue();
  }
}
