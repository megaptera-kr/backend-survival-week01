package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    public static final String REGEX_HTTP = "(?<httpMethod>[A-Z]+) (?<path>.+) (?<httpVersion>.+)\\R" +
            "(?<headers>(.+?:.+?\\R)+)" +
            "\\R" +
            "(?<body>.*)";
    Logger logger = Logger.getLogger(getClass().getName());
    Map<Long, String> tasks = new HashMap<>();
    long newTaskId = 1;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        // 1. Listen

        try (ServerSocket listener = new ServerSocket(port, 0)) {
            logger.info("Listen!");
            while (true) {
                // 2. Accept
                Socket socket = listener.accept();

                logger.info("Accept!");
                // 3. Request -> 처리 -> Response

                Map<String, String> httpRequestMap = httpRequestParse(socket);

                if (httpRequestMap.isEmpty()) {
                    respond(socket, "400", "Bad Request", "");
                    continue;
                }

                controller(httpRequestMap, socket);

            }


        }
    }

    private void controller(Map<String, String> httpRequestMap, Socket socket) throws IOException {
        String path = httpRequestMap.get("path");
        String method = httpRequestMap.get("httpMethod");
        String requestBody = httpRequestMap.get("requestBody");
        long requestId = 0;

        if (method.equals("PATCH") || method.equals("DELETE")) {
            String[] pathArray = path.split("/");
            try {
                requestId = Long.parseLong(pathArray[pathArray.length - 1]);
            } catch (NumberFormatException e) {
                respond(socket, "400", "Bad Request", "");
            }
        }


        if (path.startsWith("/tasks") && method.equals("GET")) {
            getTasks(socket);
        }

        if (path.startsWith("/tasks") && method.equals("POST")) {
            postTask(socket, requestBody);
        }

        if (path.startsWith("/tasks") && method.equals("PATCH")) {
            patchTask(socket, requestId, requestBody);
        }

        if (path.startsWith("/tasks") && method.equals("DELETE")) {
            deleteTask(socket, requestId);
        }


    }

    private void deleteTask(Socket socket, long requestId) throws IOException {

        String taskDeleted = tasks.remove(requestId);

        if (taskDeleted == null) {
            respond(socket, "404", "Not Found", "");
        } else {
            String body = new Gson().toJson(tasks);
            respond(socket, "200", "OK", body);
        }
    }

    private void patchTask(Socket socket, long requestId, String requestBody) throws IOException {
        if ("".equals(requestBody)) {
            respond(socket, "400", "Bad Request", "");
            return;
        }
        JsonElement jsonElement = JsonParser.parseString(requestBody);
        String taskName = jsonElement.getAsJsonObject().get("task").getAsString();
        if (tasks.get(requestId) == null) {
            respond(socket, "404", "Not Found", "");
        } else {
            tasks.put(requestId, taskName);
            String body = new Gson().toJson(tasks);
            respond(socket, "200", "OK", body);
        }
    }

    private void postTask(Socket socket, String requestBody) throws IOException {
        if ("".equals(requestBody)) {
            respond(socket, "400", "Bad Request", "");
        } else {
            JsonElement jsonElement = JsonParser.parseString(requestBody);
            String taskName = jsonElement.getAsJsonObject().get("task").getAsString();
            tasks.put(newTaskId++, taskName);

            String body = new Gson().toJson(tasks);
            respond(socket, "201", "Created", body);
        }
    }

    private void getTasks(Socket socket) throws IOException {
        String body = new Gson().toJson(tasks);
        respond(socket, "200", "OK", body);
    }


    private static Map<String, String> httpRequestParse(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);

        charBuffer.flip();

        Pattern pattern = Pattern.compile(REGEX_HTTP);
        Matcher matcher = pattern.matcher(charBuffer);


        if (!matcher.find()) {
            return Collections.emptyMap();
        }
        Map<String, String> httpRequestMap = new HashMap<>();
        httpRequestMap.put("httpMethod", matcher.group("httpMethod"));
        httpRequestMap.put("path", matcher.group("path"));
        httpRequestMap.put("requestBody", matcher.group("body"));

        return httpRequestMap;
    }

    private static void respond(Socket socket, String statusCode, String statusMessage, String body) throws IOException {
        // 4. Response

        byte[] bytes = body.getBytes();

        String message = "" +
                "HTTP/1.1 " + statusCode + " " + statusMessage + "\n" +
                "Content-Type: application-json; charset=UTF-8\n" +
                "Content-Length: " + bytes.length + "\n" +
                "\n" +
                body;

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

}
