package kr.megaptera.assignment;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

public class RequestHandler implements Runnable {

  private static final int MAX_BUFFER_SIZE = 1_000_000;
  private final Socket clientSocket;

  public RequestHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    System.out.println("RequestHandler is Ready");
    try (InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream()) {

      Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);

      HttpRequest httpRequest = toHttpRequest(readHttpMessage(reader));


    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String readHttpMessage(Reader reader) throws IOException {
    CharBuffer charBuffer = CharBuffer.allocate(MAX_BUFFER_SIZE);
    reader.read(charBuffer);
    charBuffer.flip();

    return charBuffer.toString();
  }

  private HttpRequest toHttpRequest(String inputLine) {
    String[] inputTokens = inputLine.split("\\r");

    return new HttpRequest(inputTokens[0], inputTokens[inputTokens.length - 1]);
  }
}
