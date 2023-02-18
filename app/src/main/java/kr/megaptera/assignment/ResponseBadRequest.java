package kr.megaptera.assignment;

import java.io.OutputStream;

public class ResponseBadRequest extends HttpResponse{

  public ResponseBadRequest(OutputStream outputStream) {
    super(outputStream);
  }

  @Override
  protected String httpStatusCode() {
    return "400";
  }

  @Override
  protected String responseMessage() {
    return "Bad Request";
  }
}
