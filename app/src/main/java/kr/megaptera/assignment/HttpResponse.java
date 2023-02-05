package kr.megaptera.assignment;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public abstract class HttpResponse {

  private final OutputStream outputStream;

  public HttpResponse(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  public void send(String body) throws IOException {
    String message = "" +
        "HTTP/1.1 " + httpStatusCode() + " " +
        responseMessage() + "\n" +
        "Content-Type: application/json; charset=UTF-8\n" +
        "\n" + body;

    Writer writer = new OutputStreamWriter(outputStream);
    writer.write(message);
    writer.flush();
  }

  public void send() throws IOException {
    String message = "" +
        "HTTP/1.1 " + httpStatusCode() + " " +
        responseMessage() + "\n" +
        "\n";

    Writer writer = new OutputStreamWriter(outputStream);
    writer.write(message);
    writer.flush();
  }

  protected abstract String httpStatusCode();

  protected abstract String responseMessage();
}
