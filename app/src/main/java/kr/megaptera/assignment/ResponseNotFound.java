package kr.megaptera.assignment;

import java.io.OutputStream;

public class ResponseNotFound extends HttpResponse{

  public ResponseNotFound(OutputStream outputStream) {
    super(outputStream);
  }

  @Override
  protected String httpStatusCode() {
    return "404";
  }

  @Override
  protected String responseMessage() {
    return "Not Found";
  }
}
