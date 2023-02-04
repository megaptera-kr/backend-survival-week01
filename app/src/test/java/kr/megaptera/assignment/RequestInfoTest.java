package kr.megaptera.assignment;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RequestInfoTest {

  @DisplayName("GET method, path 파싱하기")
  @Test
  void parseGetRequest() {
    String readLine = "GET /tasks/1 HTTP/1.1";
    RequestInfo info = new RequestInfo(readLine, null);

    assertThat(info.getMethod()).isEqualTo("GET");
    assertThat(info.getPath()).isEqualTo("/tasks/1");
    assertThat(info.getPathDetailId()).isEqualTo(1L);

  }

  @DisplayName("POST method, body 파싱하기")
  @Test
  void parsePostRequest() {
    String readLine = "POST /tasks HTTP/1.1";
    String bodyLine = "{task:놀러가기}";

    RequestInfo info = new RequestInfo(readLine, bodyLine);

    assertThat(info.getMethod()).isEqualTo("POST");
    assertThat(info.getPath()).isEqualTo("/tasks");
    assertThat(info.getBody()).isEqualTo(JsonParser.parseString(bodyLine));

  }
}
