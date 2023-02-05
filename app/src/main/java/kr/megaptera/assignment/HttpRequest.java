package kr.megaptera.assignment;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class HttpRequest {
  private final String method;
  private final String path;
  private Long pathDetailId = null;
  private final JsonElement body;

  public HttpRequest(String firstLine, String body) {
    String[] tokens = firstLine.split(" ");
    this.method = tokens[0];

    String[] pathTokens = tokens[1].split("\\?");
    this.path = pathTokens[0];

    String[] pathDetails = pathTokens[0].split("/");

    if (pathDetails.length == 3) {
      this.pathDetailId = Long.parseLong(pathDetails[2]);
    }

    if (body != null) {
      this.body = JsonParser.parseString(body);
    }else {
      this.body = null;
    }

  }

  public String getMethod() {
    return method;
  }

  public String getPath() {
    return path;
  }

  public Long getPathDetailId() {
    return pathDetailId;
  }

  public JsonElement getBody() {
    return body;
  }
}
