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

      CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
      reader.read(charBuffer);
      charBuffer.flip();

      String inputLine = charBuffer.toString();

      String[] inputTokens = inputLine.split("\\r");

      RequestInfo requestInfo = new RequestInfo(inputTokens[0],
          inputTokens[inputTokens.length - 1]);

      System.out.println(requestInfo.getBody());
      System.out.println(requestInfo.getMethod());
      System.out.println(requestInfo.getPath());


    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
