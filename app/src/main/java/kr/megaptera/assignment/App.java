package kr.megaptera.assignment;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;

import java.util.HashMap;
import java.util.Map;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }


    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        Long index = 1L;

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        ServerSocket listener = new ServerSocket(port);

        while (true) {
            // 2. Accept
            Socket socket = listener.accept();

            // 3. Request
            String charBufferString = getCharBufferString(socket);
            String patternString = "(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS)\\s(.+?)\\s.*\\r?\\n((.|\\n|\\r)*)Host:\\s(.+)((.|\\n|\\r)*)\\r?\\n\\r?\\n((.|\\n|\\r)*)$";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(charBufferString);
            matcher.find();
            String requestMethod = matcher.group(1);
            String requestUrl = matcher.group(2);
            String host = matcher.group(5);

            String[] requestUrlArr = requestUrl.split("/");

            if ( requestUrlArr.length == 0 ) {
                sendHelloworld(socket, host);
                continue;
            }

            String urlPath = requestUrlArr[1];
            String body = matcher.group(8);

            if ( urlPath.equals("tasks") ) {
                if ( requestMethod.equals("GET") ) {
                    sendTasks(socket, tasks, host);
                }

                if ( requestMethod.equals("POST") ) {
                    createTask(socket, index++, body, tasks, host);
                }

                if ( requestMethod.equals("PATCH") ) {
                    updateTask(socket, body, requestUrlArr, tasks, host);
                }

                if ( requestMethod.equals("DELETE") ) {
                    deleteTask(socket, requestUrlArr, tasks, host);
                }
            } else {
                Writer writer = new OutputStreamWriter(socket.getOutputStream());
                writer.write(sendResponse("", host, 404, "Not Found"));
                writer.flush();
            }
        }
    }

    private static String getCharBufferString(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);

        charBuffer.flip();
        String charBufferString = charBuffer.toString();
        return charBufferString;
    }

    private void sendHelloworld(Socket socket, String host) throws IOException {
        String body = "Hello, world!";
        byte[] bytes = body.getBytes();

        String message = "" +
                "HTTP/1.1 200 OK\n" +
                "Content-Type: text/html; charset=UTF-8\n" +
                "Connection: close\n" +
                "Host: " + host + "\n" +
                "Content-Length: " + bytes.length + "\n" +
                "\n" + body;

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

    private void sendTasks(Socket socket, Map<Long, String> tasks, String host) throws IOException {
        String serializedJsonTasks = new Gson().toJson(tasks);
        String message = sendResponse(serializedJsonTasks, host, 200, "OK");

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

    private void createTask(Socket socket, Long index, String body, Map<Long, String> tasks, String host) throws IOException {
        String message = "";
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        Gson gson = new Gson();

        String serializedJsonTasks = "";
        Optional<JsonObject> jsonTask = Optional.ofNullable(gson.fromJson(body, JsonObject.class));

        if ( jsonTask.isPresent() ) {
            JsonElement taskValue = jsonTask.get().get("task");

            if ( taskValue != null ) {
                tasks.put(index, taskValue.getAsString());
                serializedJsonTasks = gson.toJson(tasks);
                message = sendResponse(serializedJsonTasks, host, 201, "Created");
            } else {
                message = sendResponse(serializedJsonTasks, host, 400, "Bad Request");
            }
        } else {
            message = sendResponse(serializedJsonTasks, host, 400, "Bad Request");
        }

        writer.write(message);
        writer.flush();
    }

    private void updateTask(Socket socket, String body, String[] requestUrlArr, Map<Long, String> tasks, String host) throws IOException {
        String message = "";
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        Gson gson = new Gson();

        String serializedJsonTasks = "";
        Optional<JsonObject> jsonTask = Optional.ofNullable(gson.fromJson(body, JsonObject.class));
        Optional<String> idString = Optional.ofNullable(requestUrlArr[2]);

        if ( jsonTask.isPresent() && idString.isPresent() ) {
            Long id = Long.parseLong(idString.get());
            JsonElement taskValue = jsonTask.get().get("task");

            if ( tasks.containsKey(id) ) {
                tasks.put(id, taskValue.getAsString());
                serializedJsonTasks = gson.toJson(tasks);
                message = sendResponse(serializedJsonTasks, host, 200, "OK");
            } else {
                message = sendResponse(serializedJsonTasks, host, 404, "Not Found");

            }
        } else {
            message = sendResponse(serializedJsonTasks, host, 400, "Bad Request");
        }

        writer.write(message);
        writer.flush();
    }

    private void deleteTask(Socket socket, String[] requestUrlArr, Map<Long, String> tasks, String host) throws IOException {
        String message = "";
        Writer writer = new OutputStreamWriter(socket.getOutputStream());

        String serializedJsonTasks = "";

        if ( requestUrlArr.length != 3 ) {
            message = sendResponse(serializedJsonTasks, host, 400, "Bad Request");
            writer.write(message);
            writer.flush();
            return;
        }

        Long id = Long.parseLong(requestUrlArr[2]);

        if ( tasks.containsKey(id) ) {
            tasks.remove(id);
            serializedJsonTasks = new Gson().toJson(tasks);
            message = sendResponse(serializedJsonTasks, host, 200, "OK");
        } else {
            message = sendResponse(serializedJsonTasks, host, 404, "Not Found");
        }

        writer.write(message);
        writer.flush();
    }

    private String sendResponse(String serializedJsonTasks, String host, int statusCode, String statusMessage) {
        byte[] serializedJsonTaskBytes = serializedJsonTasks.getBytes();
        return "HTTP/1.1 " + statusCode + " " + statusMessage + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Connection: close\n" +
                "Host: " + host + "\n" +
                "Content-Length: " + serializedJsonTaskBytes.length + "\n" +
                "\n" + serializedJsonTasks;
    }

}
