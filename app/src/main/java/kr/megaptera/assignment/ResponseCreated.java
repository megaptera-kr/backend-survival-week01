package kr.megaptera.assignment;

import java.io.OutputStream;

public class ResponseCreated extends HttpResponse{

  public ResponseCreated(OutputStream outputStream) {
    super(outputStream);
  }

  @Override
  protected String httpStatusCode() {
    return "201";
  }

  @Override
  protected String responseMessage() {
    return "Created";
  }
}
