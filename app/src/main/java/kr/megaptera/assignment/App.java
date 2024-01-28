package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static void main(String[] args) throws Exception {

        App app = new App();
        app.run();
    }

    private void run() throws Exception {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.
        /*
         * GET /tasks
         * - 200 OK
         * POST /tasks
         * - 201 Created
         * - 400 Bad Request (Empty Body data)
         * PATCH /tasks/{id} (Title)
         * - 200 OK
         * - 400 Bad Request (Empty Body data)
         * - 404 Not Found (Not exist id)
         * DELETE /tasks/{id}
         * - 200 OK
         * - 404 Not Found (Not exist id)
         * */


        String host = "localhost";
        int listenerBacklog = 0;

        try (ServerSocket listener = new ServerSocket(port, listenerBacklog)) { // 1. Listen
            System.out.println("Listen! host: " + host + ", port: " + port + "");

            while (true) {
                try (Socket socket = listener.accept()) { // 2. Accept
                    System.out.println("Accept!");

                    // 3. Request
                    String request = parseRequest(socket);
                    String requestMethod = request.substring(0, request.indexOf("HTTP"));

                    System.out.println("requestMethod: " + requestMethod);
                    // 4. Response
                    String message = "";
                    if (requestMethod.startsWith("GET /tasks")) {
                        message = getTaskHandler(tasks);
                    } else if (requestMethod.startsWith("POST /tasks")) {
                        message = postTaskHandler(tasks, request);
                    } else if (requestMethod.startsWith("PATCH /tasks")) {
                        message = patchTaskHandler(tasks, request);
                    } else if (requestMethod.startsWith("DELETE /tasks")) {
                        message = deleteTaskHandler(tasks, request);
                    } else {
                        message = generateResponseMessage(StatusCode.BAD_REQUEST, "");
                    }

                    response(socket, message);
                }
            }
        } // Close listener
    }

    private String parseRequest(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());

        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();

        String request = charBuffer.toString();
        System.out.println("Request: " + request);

        return request;
    }

    private String findPayload(String request, String payloadKey) {
        String[] requestLines = request.split("\n");
        String lastLine = requestLines[requestLines.length - 1];

        try {
            JsonElement jsonElement = JsonParser.parseString(lastLine);
            return jsonElement.getAsJsonObject().get(payloadKey).getAsString();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return "";
        }
    }

    private String findRequestPath(String request) {
        String[] requestLines = request.split("\n");
        String firstLine = requestLines[0];
        String[] firstLineSplit = firstLine.split(" ");
        String requestPath = firstLineSplit[1];

        return requestPath;
    }

    private String generateResponseMessage(StatusCode statusCode, String respBody) {
        byte[] respBodyBytes = respBody.getBytes();
        String response = """
                HTTP/1.1 %d %s
                Host: localhost:8080
                Content-Type: application/json; charset=utf-8
                Content-Length: %d
                                
                %s
                """.formatted(statusCode.getCode(), statusCode.getMessage(), respBodyBytes.length, respBody);

        return response;
    }

    private void response(Socket socket, String message) throws IOException {

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

    private String getTaskHandler(Map<Long, String> tasks) {

        return generateResponseMessage(StatusCode.OK, getTasks(tasks));
    }

    private String postTaskHandler(Map<Long, String> tasks, String request) {

        String requestTask = findPayload(request, "task");
        if (requestTask.isEmpty()) {
            return generateResponseMessage(StatusCode.BAD_REQUEST, "");
        }

        tasks.put((long) tasks.size() + 1, requestTask);

        return generateResponseMessage(StatusCode.CREATED, getTasks(tasks));
    }

    private String patchTaskHandler(Map<Long, String> tasks, String request) {

        String requestPath = findRequestPath(request);
        String[] requestPathSplit = requestPath.split("/");

        if (requestPathSplit.length < 3) {
            return generateResponseMessage(StatusCode.BAD_REQUEST, "");
        }
        long taskId = Long.parseLong(requestPathSplit[2]);

        if (!tasks.containsKey(taskId)) {
            return generateResponseMessage(StatusCode.NOT_FOUND, "");
        }

        String requestTask = findPayload(request, "task");
        if (requestTask.isEmpty()) {
            return generateResponseMessage(StatusCode.BAD_REQUEST, "");
        }

        tasks.put(taskId, requestTask);

        return generateResponseMessage(StatusCode.OK, getTasks(tasks));
    }

    private String deleteTaskHandler(Map<Long, String> tasks, String request) {

        String requestPath = findRequestPath(request);
        String[] requestPathSplit = requestPath.split("/");

        if (requestPathSplit.length < 3) {
            return generateResponseMessage(StatusCode.BAD_REQUEST, "");
        }
        long taskId = Long.parseLong(requestPathSplit[2]);

        if (!tasks.containsKey(taskId)) {
            return generateResponseMessage(StatusCode.NOT_FOUND, "");
        }

        tasks.remove(taskId);

        return generateResponseMessage(StatusCode.OK, getTasks(tasks));
    }

    private String getTasks(Map<Long, String> tasks) {

        String json = new Gson().toJson(tasks);

        return json;
    }

    private String deleteTask(Map<Long, String> tasks, String request) throws IOException {
        return null;
    }
}
