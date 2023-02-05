package kr.megaptera.assignment;

import java.io.OutputStream;

public class ResponseDeleted extends HttpResponse{

  public ResponseDeleted(OutputStream outputStream) {
    super(outputStream);
  }

  @Override
  protected String httpStatusCode() {
    return "200";
  }

  @Override
  protected String responseMessage() {
    return "Deleted";
  }
}
