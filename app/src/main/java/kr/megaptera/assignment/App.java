package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {
    public static Long newId = 0L;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen

        ServerSocket listener = new ServerSocket(port, 0);
        System.out.println("Listen");

        // 2. Accept

        while (true) {
            Socket socket = listener.accept();

            System.out.println("Accept");

            String request = getRequest(socket);

            System.out.println("sdsdsd\n" + request);

            String message = process(tasks, request);

            writeResponse(socket, message);
            socket.close();
        }
    }

    // 3. Request
    private String getRequest(Socket socket) throws IOException {

        Reader reader = new InputStreamReader(socket.getInputStream());

        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();

        return charBuffer.toString();
    }

    private String getMethod(String request) {
        String httpMethod = request.substring(0, request.indexOf("HTTP"));
        System.out.println("HTTPMETHOD : " + httpMethod);
        return httpMethod;
    }

    private String process(Map<Long, String> tasks, String request) {
        String method = getMethod(request);

        if (method.startsWith("GET /tasks")) {
            return getTask(tasks);
        } else if (method.startsWith("POST /tasks")) {
            return postTask(tasks, request);
        } else if (method.startsWith("PATCH /tasks/")) {
            return patchTask(tasks, request, method);
        } else if (method.startsWith("DELETE /tasks/")) {
            return deleteTask(tasks, method);
        }
        return generateMessage("", "400 Bad Request");
    }

    private String getTask(Map<Long, String> tasks) {
        String content = new Gson().toJson(tasks);
        return generateMessage(content, "200 OK");
    }

    private String postTask(Map<Long, String> tasks, String request) {
        String task = parsePayload(request, "task");

        if (task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }
        tasks.put(generateTaskId(), task);
        String content = new Gson().toJson(tasks);
        return generateMessage(content, "201 Created");
    }

    private String patchTask(Map<Long, String> tasks, String request, String method) {

        Long id = parseTaskId(method);
        if (!tasks.containsKey(id)) {
            return generateMessage("", "404 Bad Request");
        }
        String task = parsePayload(request, "task");
        if (task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }
        tasks.put(id, task);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "200 OK");
    }

    private String deleteTask(Map<Long, String> tasks, String method) {
        Long id = parseTaskId(method);

        if (!tasks.containsKey(id)) {
            return generateMessage("", "404 not found");
        }
        tasks.remove(id);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "200 OK");
    }

    private Long parseTaskId(String method) {
        String[] parts = method.split("/");

        return Long.parseLong(parts[2].trim());
    }

    // 4. Response
    private String parsePayload(String request, String value) {
        String[] lines = request.split("\n");
        String lastLine = lines[lines.length - 1];

        try {
            JsonElement jsonElement = JsonParser.parseString(lastLine);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.get(value).getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private Long generateTaskId() {
        newId += 1;
        return newId;
    }

    private String generateMessage(String content, String status) {
        byte[] bytes = content.getBytes();
        String message = "" +
                "HTTP/1.1 " + status + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Content-Length: " + bytes.length + "\n" +
                "\n" +
                content;
        return message;
    }

    private void writeResponse(Socket socket, String message) throws IOException {

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();

    }
}