package kr.megaptera.assignment;


import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Collections;

import java.util.HashMap;
import java.util.Map;

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

            if ( requestUrl.equals("/") ) {
                sendHelloworld(socket, host);
            }

            String[] requestUrlArr = requestUrl.split("/");
            String urlPath = requestUrlArr[1];

            String body = matcher.group(8);

            if ( urlPath.equals("tasks") ) {
                if ( requestMethod.equals("GET") ) {
                    sendTasks(socket, tasks, host);
                }

                if ( requestMethod.equals("POST") ) {
                    createTask(socket, body, tasks, host);
                }

                if ( requestMethod.equals("PATCH") ) {
                    updateTask(socket, body, requestUrlArr, tasks, host);
                }

                if ( requestMethod.equals("DELETE") ) {
                    deleteTask(socket, requestUrlArr, tasks, host);
                }
            } else {
                Writer writer = new OutputStreamWriter(socket.getOutputStream());
                writer.write(sendResponse(0, host, 404, "Not Found"));
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
                "Content-Length: " + bytes.length + "\n" +
                "\n" + body;

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

    private void sendTasks(Socket socket, Map<Long, String> tasks, String host) throws IOException {
        String serializedJsonTasks = new Gson().toJson(tasks);
        byte[] serializedJsonTaskBytes = serializedJsonTasks.getBytes();

        String message = sendResponse(serializedJsonTaskBytes.length, host, 200, "OK")
                + "\n" + serializedJsonTasks;

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

    private void createTask(Socket socket, String body, Map<Long, String> tasks, String host) throws IOException {
        String message = "";
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        Gson gson = new Gson();

        String serializedJsonTasks = "";
        byte[] serializedJsonTaskBytes = serializedJsonTasks.getBytes();

        if ( body.isBlank() ) {
            message = sendResponse(serializedJsonTaskBytes.length, host, 400, "Bad Request");
            writer.write(message);
            writer.flush();
            return;
        }

        JsonObject jsonTask = gson.fromJson(body, JsonObject.class);
        JsonElement taskValue = jsonTask.get("task");

        if ( taskValue != null ) {
            Long key = ( !(tasks.isEmpty()) ) ? Collections.max(tasks.keySet()) + 1 : 1;
            tasks.put(key, taskValue.getAsString());
            serializedJsonTasks = gson.toJson(tasks);
            serializedJsonTaskBytes = serializedJsonTasks.getBytes();
            message = sendResponse(serializedJsonTaskBytes.length, host, 201, "Created")
                    + "\n" + serializedJsonTasks;
        } else {
            message = sendResponse(serializedJsonTaskBytes.length, host, 400, "Bad Request");
        }

        writer.write(message);
        writer.flush();
    }

    private void updateTask(Socket socket, String body, String[] requestUrlArr, Map<Long, String> tasks, String host) throws IOException {
        String message = "";
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        Gson gson = new Gson();

        String serializedJsonTasks = "";
        byte[] serializedJsonTaskBytes = serializedJsonTasks.getBytes();
        JsonObject jsonTask = gson.fromJson(body, JsonObject.class);

        if ( jsonTask == null || requestUrlArr.length != 3 ) {
            message = sendResponse(serializedJsonTaskBytes.length, host, 400, "Bad Request");
            writer.write(message);
            writer.flush();
            return;
        }

        Long id = Long.parseLong(requestUrlArr[2]);
        JsonElement taskValue = jsonTask.get("task");

        if ( taskValue == null ) {
            message = sendResponse(serializedJsonTaskBytes.length, host, 400, "Bad Request");
            writer.write(message);
            writer.flush();
            return;
        }

        if ( tasks.containsKey(id) ) {
            tasks.put(id, taskValue.getAsString());
            serializedJsonTasks = gson.toJson(tasks);
            serializedJsonTaskBytes = serializedJsonTasks.getBytes();
            message = sendResponse(serializedJsonTaskBytes.length, host, 200, "OK")
                    + "\n" + serializedJsonTasks;
        } else {
            message = sendResponse(serializedJsonTaskBytes.length, host, 404, "Not Found");
        }

        writer.write(message);
        writer.flush();
    }

    private void deleteTask(Socket socket, String[] requestUrlArr, Map<Long, String> tasks, String host) throws IOException {
        String message = "";
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        Gson gson = new Gson();

        String serializedJsonTasks = "";
        byte[] serializedJsonTaskBytes = serializedJsonTasks.getBytes();

        if ( requestUrlArr.length != 3 ) {
            message = sendResponse(serializedJsonTaskBytes.length, host, 400, "Bad Request");
            writer.write(message);
            writer.flush();
            return;
        }

        Long id = Long.parseLong(requestUrlArr[2]);

        if ( tasks.containsKey(id) ) {
            tasks.remove(id);
            serializedJsonTasks = gson.toJson(tasks);
            serializedJsonTaskBytes = serializedJsonTasks.getBytes();
            message = sendResponse(serializedJsonTaskBytes.length, host, 200, "OK")
                    + "\n" + serializedJsonTasks;
        } else {
            message = sendResponse(serializedJsonTaskBytes.length, host, 404, "Not Found");
        }

        writer.write(message);
        writer.flush();
    }

    private String sendResponse(int contentLength, String host, int statusCode, String statusMessage) {
        return "HTTP/1.1 " + statusCode + " " + statusMessage + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Connection: close\n" +
                "Host: " + host + "\n" +
                "Content-Length: " + contentLength + "\n";
    }

}
