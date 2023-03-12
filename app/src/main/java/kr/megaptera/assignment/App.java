package kr.megaptera.assignment;

import com.google.gson.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class App {
    public static Long taskId = 0L;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {

        int port = 8080;
        // 1. Listen
        ServerSocket listener = new ServerSocket(port);
        System.out.println("listen");
        Map<Long, String> tasks = new HashMap<>();


        while (true) {
            // 2. Accept
            Socket socket = listener.accept();

            // 3. Request
            String request = getRequest(socket);

            // 4. Response
            String message = process(tasks, request);
            writeMessage(socket, message);
            // 5. close
            socket.close();
        }
    }

    private String getRequest(Socket socket) throws IOException {
        InputStreamReader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();
        return charBuffer.toString();

    }

    private String getRequestMethod(String request) {
        return request.substring(0, request.indexOf("HTTP"));
    }

    private String process(Map<Long, String> tasks, String request) {
        String method = getRequestMethod(request);

        if (method.startsWith("GET /tasks")) {
            return processGetTask(tasks);
        }
        if (method.startsWith("POST /tasks")) {
            return processPostTask(tasks, request);
        }
        if (method.startsWith("PATCH /tasks")) {
            return processPatchTask(tasks, request, method);
        }
        if (method.startsWith("DELETE /tasks")) {
            return processDeleteTask(tasks, method);
        }
        return generateMessage("", "400 Bad Request");
    }

    private String processGetTask(Map<Long, String> tasks) {
        String body = new Gson().toJson(tasks);
        return generateMessage(body, "200 OK");
    }

    private String processPostTask(Map<Long, String> tasks, String request) {
        String task = parsePayload(request, "task");

        if (task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }

        tasks.put(generateTaskId(), task);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "201 Created");
    }

    private String processPatchTask(Map<Long, String> tasks, String request, String method) {
        Long id = parseTaskId(method);
        if (!tasks.containsKey(id)) {
            return generateMessage("", "404 Not Found");
        }

        String task = parsePayload(request, "task");

        if (task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }

        tasks.put(id, task);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "200 OK");
    }

    private String processDeleteTask(Map<Long, String> tasks, String method) {
        Long id = parseTaskId(method);

        if (!tasks.containsKey(id)) {
            return generateMessage("", "404 Not Found");
        }
        tasks.remove(id);

        String content = new Gson().toJson(tasks);

        return generateMessage(content, "200 OK");
    }

    private String parsePayload(String request, String key) {
        String[] lines = request.split("\n");
        String lastLine = lines[lines.length - 1];
        try {
            JsonObject jsonObject = JsonParser.parseString(lastLine).getAsJsonObject();
            return jsonObject.get(key).getAsString();
        } catch (Exception e) {
            return "";
        }

    }

    private Long parseTaskId(String method) {
        String[] parts = method.split("/");
        return Long.parseLong(parts[2].trim());
    }

    private String generateMessage(String body, String statusCode) {

        byte[] bytes = body.getBytes();
        return "" +
                "HTTP/1.1 " + statusCode + "\n" +
                "Host: localhost:8080\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "\n" +
                body;
    }

    private Long generateTaskId() {
        taskId += 1;
        return taskId;
    }

    private void writeMessage(Socket socket, String message) throws IOException {
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

}
