package kr.megaptera.assignment;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.Arrays;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TasksTest {

  @DisplayName("각 Task 는 중복 Id를 가질 수 없다.")
  @Test
  void mustNotHaveDuplicatedId() {
    assertThatCode(() -> new Tasks(
        Arrays.asList(
            new Task(1L, "놀러가기"),
            new Task(1L, "책보기"))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("각 Task 끼리 Id 가 겹칠 수 없습니다.");
  }
}
