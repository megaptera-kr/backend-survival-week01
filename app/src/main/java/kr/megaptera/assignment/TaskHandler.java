package kr.megaptera.assignment;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.net.URI;

public class TaskHandler implements HttpHandler {

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    System.out.println(exchange.getRequestMethod());
    URI uri = exchange.getRequestURI();
    System.out.println(uri.getPath());
//    String body = new String(exchange.getRequestBody().readAllBytes());
//    System.out.println(body);

      // todo: ServerSocket 객체로 교체


  }
}
