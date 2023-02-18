package kr.megaptera.assignment;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class HttpRequestTest {

  @DisplayName("GET method, path 파싱하기")
  @Test
  void parseGetRequest() {
    String readLine = "GET /tasks/1 HTTP/1.1";
    HttpRequest request = new HttpRequest(readLine, null);

    assertThat(request.getMethod()).isEqualTo("GET");
    assertThat(request.getPath()).isEqualTo("/tasks/1");
    assertThat(request.getPathDetailId()).isEqualTo(1L);

  }

  @DisplayName("POST method, body 파싱하기")
  @Test
  void parsePostRequest() {
    String readLine = "POST /tasks HTTP/1.1";
    String bodyLine = "{task:놀러가기}";

    HttpRequest request = new HttpRequest(readLine, bodyLine);

    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/tasks");
    assertThat(request.getBody()).isEqualTo(JsonParser.parseString(bodyLine));

  }
}
