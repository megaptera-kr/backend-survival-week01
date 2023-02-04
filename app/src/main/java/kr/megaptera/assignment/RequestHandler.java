package kr.megaptera.assignment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class RequestHandler implements Runnable {

  public static final int MAX_BUFFER_SIZE = 1_000_000;
  private final Socket clientSocket;

  public RequestHandler(Socket clientSocket) {
    this.clientSocket = clientSocket;
  }

  @Override
  public void run() {
    System.out.println("RequestHandler is Ready");
    try (InputStream inputStream = clientSocket.getInputStream();
        OutputStream outputStream = clientSocket.getOutputStream()) {

      try (BufferedReader bufferedReader = new BufferedReader(
          new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
        System.out.println(bufferedReader.readLine());
        //Todo : 라인 단위로 읽어서 Method, Path, Body 분리
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
