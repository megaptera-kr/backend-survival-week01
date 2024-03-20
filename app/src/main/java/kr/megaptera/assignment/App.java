package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class App {
  public enum HttpMethods {
    GET, POST, PUT, PATCH, DELETE

  }


  public static class HttpRequestCode {
    private int code;
    private String message;

    public void notFoundRequest() {
      code = 404;
      message = "Not Found";
    }

    public void badRequest() {
      code = 400;
      message = "Bad Request";
    }

    public void successRequest() {
      code = 200;
      message = "OK";
    }

    public void createSuccessRequest() {
      code = 201;
      message = "Created";
    }
  }

  public static void main(String[] args) throws IOException {
    App app = new App();
    app.run();
  }

  private String responseBody = "";

  Map<Long, String> tasks = new HashMap<>();

  private void run() throws IOException {
    int port = 8080;


    // TODO: 요구사항에 맞게 과제를 진행해주세요.

    // 1. Listen
    ServerSocket listener = new ServerSocket(port);
    // 2. Accept
    while (true) {
      Socket socket = listener.accept();
      System.out.println("Accept");
      // 3. Request
      String request = getRequest(socket);
      System.out.println(request);

      String header = request.split("\\n")[0];
      HttpRequestCode requestCode = processRequest(header, request);

      // 4. Response
      sendResponseMessage(socket, requestCode);
      responseBody = "";
      socket.close();
    }
  }

  private void sendResponseMessage(Socket socket, HttpRequestCode requestCode) throws IOException {
    byte[] bytes = responseBody.getBytes();
    String message =
        "HTTP/1.1 " + requestCode.code + " " + requestCode.message + "\n" + "Content-Length: "
            + bytes.length + "\n" + "\n" + responseBody;
    Writer writer = new OutputStreamWriter(socket.getOutputStream());
    writer.write(message);
    writer.flush();
  }

  private String getRequest(Socket socket) throws IOException {
    Reader reader = new InputStreamReader(socket.getInputStream());

    CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
    reader.read(charBuffer);

    charBuffer.flip();
    return charBuffer.toString();
  }


  private HttpRequestCode processRequest(String header, String request) {
    String[] methods = header.split(" ");
    HttpMethods httpMethod = HttpMethods.valueOf(methods[0]);
    String routing = methods[1];


    return switch (httpMethod) {
      case GET -> containGetRequest(routing);
      case POST -> containPostRequest(routing, request);
      case PATCH -> {
        Long id = validCheckMethods(routing);
        yield containPatchRequest(routing, request, id);
      }
      case DELETE -> {
        Long id = validCheckMethods(routing);
        yield containDeleteRequest(routing, request, id);
      }
      default -> {
        HttpRequestCode httpRequestCode = new HttpRequestCode();
        httpRequestCode.notFoundRequest();
        yield httpRequestCode;
      }
    };
  }


  private Long validCheckMethods(String methods) {
    try {
      return Long.parseLong(methods.split("/")[2]);
    } catch (Exception e) {
      return null;
    }
  }

  private HttpRequestCode containDeleteRequest(String routing, String request, Long id) {
    HttpRequestCode requestCode = new HttpRequestCode();
    requestCode.notFoundRequest();
    if (routing.contains("/tasks")) {
      if (!tasks.containsKey(id)) {
        return requestCode;
      }
      tasks.remove(id);
      responseBody = new Gson().toJson(tasks);
      requestCode.successRequest();
    }
    return requestCode;
  }

  private HttpRequestCode containPatchRequest(String routing, String request, Long id) {
    HttpRequestCode requestCode = new HttpRequestCode();
    requestCode.notFoundRequest();

    if (routing.contains("/tasks")) {
      if (!tasks.containsKey(id)) {
        return requestCode;
      }

      String jsonData = getJsonData(request, "task");
      updateTask(jsonData, requestCode, id);
    }
    return requestCode;
  }

  private void updateTask(String jsonData, HttpRequestCode code, Long id) {
    if (jsonData == null) {
      code.badRequest();
      return;
    }
    tasks.put(id, jsonData);
    responseBody = new Gson().toJson(tasks);
    code.successRequest();
  }

  private HttpRequestCode containPostRequest(String routing, String request) {
    HttpRequestCode requestCode = new HttpRequestCode();
    requestCode.notFoundRequest();
    if (routing.equals("/tasks")) {
      String jsonData = getJsonData(request, "task");
      createTask(jsonData, requestCode);
    }
    return requestCode;
  }

  private void createTask(String jsonData, HttpRequestCode code) {
    if (jsonData == null) {
      code.badRequest();
      return;
    }
    tasks.put(createTaskId(), jsonData);
    responseBody = new Gson().toJson(tasks);
    code.createSuccessRequest();
  }

  private Long createTaskId() {
    return (long) (tasks.size() + 1);
  }

  private HttpRequestCode containGetRequest(String routing) {
    HttpRequestCode requestCode = new HttpRequestCode();
    requestCode.notFoundRequest();
    if (routing.equals("/tasks")) {
      responseBody = new Gson().toJson(tasks);
      requestCode.successRequest();
    }

    return requestCode;
  }

  private String getJsonData(String request, String value) {
    String[] lines = request.split("\\n");
    String lastLine = lines[lines.length - 1];
    try {
      JsonElement jsonElement = JsonParser.parseString(lastLine);
      JsonObject jsonObject = jsonElement.getAsJsonObject();
      return jsonObject.get(value).getAsString();
    } catch (Exception e) {
      return null;
    }

  }
}
