package kr.megaptera.assignment;

import java.io.OutputStream;

public class ResponseSuccess extends HttpResponse {

  public ResponseSuccess(OutputStream outputStream) {
    super(outputStream);
  }

  @Override
  protected String httpStatusCode() {
    return "200";
  }

  @Override
  protected String responseMessage() {
    return "OK";
  }

}
