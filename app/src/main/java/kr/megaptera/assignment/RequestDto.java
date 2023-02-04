package kr.megaptera.assignment;

import com.google.gson.JsonElement;

public class RequestDto {
  private String host;
  private String httpMethod;
  private String path = "";
  private String pathFirstPart;
  private String pathSecondPart;
  private String rawBody;
  private JsonElement body;

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public void setHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getPathFirstPart() {
    return pathFirstPart;
  }

  public void setPathFirstPart(String pathFirstPart) {
    this.pathFirstPart = pathFirstPart;
  }

  public String getPathSecondPart() {
    return pathSecondPart;
  }

  public void setPathSecondPart(String pathSecondPart) {
    this.pathSecondPart = pathSecondPart;
  }

  public String getRawBody() {
    return rawBody;
  }

  public void setRawBody(String rawBody) {
    this.rawBody = rawBody;
  }

  public JsonElement getBody() {
    return body;
  }

  public void setBody(JsonElement body) {
    this.body = body;
  }
}
