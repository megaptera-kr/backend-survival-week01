package kr.megaptera.assignment;

public class App {
  public static void main(String[] args) {
    WebServer server = new WebServer(8080);
    server.run();
  }

}
