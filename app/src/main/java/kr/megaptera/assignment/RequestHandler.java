package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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

      try {
        HttpRequest httpRequest = toHttpRequest(readHttpMessage(reader));
        handleRequest(httpRequest, outputStream);
      } catch (IllegalStateException e) {
        new ResponseBadRequest(outputStream).send();
      }

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

  private void handleRequest(HttpRequest request, OutputStream outputStream) throws IOException {
    TaskService taskService = new TaskService(new TaskRepository());
    Gson gson = new Gson();

    if (!request.getPath().startsWith("/tasks")) {
      new ResponseNotFound(outputStream).send();
      return;
    }

    if (request.isGetMethod() && request.getPathDetailId() == null) {
      Map<Long, String> taskList = taskService.getTaskList();
      new ResponseSuccess(outputStream).send(gson.toJson(taskList));
      return;
    }

    if (request.isGetMethod() && request.getPathDetailId() != null) {
      Map<Long, String> task = taskService.getTask(request.getPathDetailId());
      if (task == null) {
        new ResponseNotFound(outputStream).send();
      } else {
        new ResponseSuccess(outputStream).send(gson.toJson(task));
      }
      return;
    }

    if (request.isPostMethod()) {
      Map<Long, String> task = taskService.createTask(request.getBody());
      new ResponseCreated(outputStream).send(gson.toJson(task));
      return;
    }

    if (request.isDeleteMethod() && request.getPathDetailId() != null) {
      if (!isPositiveNumber(request.getPathDetailId())) {
        new ResponseNotFound(outputStream).send();
      }
      Map<Long, String> task = taskService.deleteTask(request.getPathDetailId());
      if (task == null) {
        new ResponseNotFound(outputStream).send();
      } else {
        new ResponseDeleted(outputStream).send();
      }
      return;
    }

    if (request.isPatchMethod() && request.getPathDetailId() != null) {
      try {
        Map<Long, String> task = taskService.updateTask(request.getPathDetailId(), request.getBody());
        new ResponseSuccess(outputStream).send(gson.toJson(task));
      } catch (IllegalArgumentException e) {
        new ResponseNotFound(outputStream).send();
      }

    }
  }

  private boolean isPositiveNumber(Long id) {
    return id > 0;
  }
}
