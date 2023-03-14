package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
        ServerSocket listener = new ServerSocket(port);

        while (true) {
            // 2. Accept
            Socket socket = listener.accept();

            // 3. Request
            String requestMessage = get_request(socket);

            // 4. Response
            String responseMessage = process_response(tasks, requestMessage);
            writeMessage(socket, responseMessage);
        }
    }

    private String process_response(Map<Long, String> tasks, String requestMessage) {
        String[] messageLines = requestMessage.split("\n");

        String startLine = messageLines[0];

        int lineSize = messageLines.length;
        String requestBody = messageLines[lineSize - 1];

        String methodAndPath = startLine.substring(0, startLine.indexOf("HTTP"));

        if (methodAndPath.startsWith("GET /tasks")) {
            return processGetMethod(tasks);
        } else if (methodAndPath.startsWith("POST /tasks")) {
            return processPostMethod(tasks, requestMessage);
        } else if (methodAndPath.startsWith("PATCH /tasks/")) {
            return processPatchMethod(tasks, requestMessage, methodAndPath);
        } else if (methodAndPath.startsWith("DELETE /tasks/")) {
            return processDeleteMethod(tasks, methodAndPath);
        } else {
            return generateMessage("", "400 Bad Request");
        }
    }


    /**
     * Request 받아서 Request Message 반환
     *
     * @param socket
     * @return : Request Message
     * @throws IOException
     */
    private String get_request(Socket socket) throws IOException {
        InputStreamReader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);        //charBuffer에 읽어온다
        charBuffer.flip();

        return charBuffer.toString();
    }

    private String processGetMethod(Map<Long, String> tasks) {
        //Map에 있는 내용들 json 문자열로 반환하기
        String content = new Gson().toJson(tasks);
        return generateMessage(content, "200 OK");
    }

    private String processPostMethod(Map<Long, String> tasks, String requestMessage) {
        String task = parsePayload(requestMessage, "task");

        if (task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }

        tasks.put(generateTaskId(), task);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "201 Created");
    }

    private String processPatchMethod(Map<Long, String> tasks, String requestMessage, String methodAndPath) {
        Long id = parseTaskId(methodAndPath);

        if (!tasks.containsKey(id)) {
            return generateMessage("", "400 Not Found");
        }

        String task = parsePayload(requestMessage, "task");

        if (task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }

        tasks.put(id, task);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "200 OK");
    }

    private String processDeleteMethod(Map<Long, String> tasks, String methodAndPath) {
        Long id = parseTaskId(methodAndPath);

        if (!tasks.containsKey(id)) {
            return generateMessage("", "404 Not Found");
        }

        tasks.remove(id);
        String content = new Gson().toJson(tasks);

        return generateMessage(content, "200 OK");
    }

    private Long generateTaskId() {
        newId += 1;
        return newId;
    }

    private Long parseTaskId(String method) {
        String[] parts = method.split("/");

        return Long.parseLong(parts[2].trim());
    }

    private String parsePayload(String requestMessage, String value) {
        String[] lines = requestMessage.split("\n");
        String lastLine = lines[lines.length - 1];

        try {
            JsonElement jsonElement = JsonParser.parseString(lastLine);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.get(value).getAsString();
        } catch (Exception e) {
            return "";
        }
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

    private void writeMessage(Socket socket, String message) throws IOException {
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }
}