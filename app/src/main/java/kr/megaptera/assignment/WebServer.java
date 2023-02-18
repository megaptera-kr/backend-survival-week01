package kr.megaptera.assignment;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {

  private final int port;
  private final ExecutorService executorService = Executors.newFixedThreadPool(10);

  public WebServer(int port) {
    this.port = port;
  }

  public void run() {
    try (ServerSocket listener = new ServerSocket(port)) {
      Socket clientSocket;
      System.out.println("Server is Ready");


      while((clientSocket = listener.accept()) != null) {
        System.out.println("Client is Connected");

        executorService.execute(new RequestHandler(clientSocket));
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
