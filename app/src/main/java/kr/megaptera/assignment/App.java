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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class App {

    public static final String regexForHTTP = "(?<httpMethod>[A-Z]+) (?<path>.+) (?<httpVersion>.+)\\R" +
                                                "(?<headers>(.+?:.+?\\R)+)" +
                                                "\\R" +
                                                "(?<body>.*)";

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();
        long newTaskId = 1;

        // 1. Listen

        ServerSocket listener;
        listener = new ServerSocket(port, 0);


        System.out.println("Listen!");
        while (true) {
            // 2. Accept
            Socket socket = listener.accept();

            System.out.println("Accept!");
            // 3. Request -> 처리 -> Response

            Reader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);

            charBuffer.flip();
            // System.out.println(charBuffer);

            Pattern pattern = Pattern.compile(regexForHTTP);
            Matcher matcher = pattern.matcher(charBuffer);

            String statusCode = "";
            String statusMessage = "";

            matcher.find();
            String httpMethod = matcher.group("httpMethod");
            String path = matcher.group("path");
            String requestBody = matcher.group("body");



            String body = "";

            if ("GET".equals(httpMethod)) {
                if ("/tasks".equals(path)) {
                    statusCode = "200";
                    statusMessage = "OK";
                    body = new Gson().toJson(tasks);
                }
            }

            String taskName;

            if ("POST".equals(httpMethod)) {
                if ("".equals(requestBody)) {
                    statusCode = "400";
                    statusMessage = "Bad Request";
                } else {
                    JsonElement jsonElement = JsonParser.parseString(requestBody);
                    taskName = jsonElement.getAsJsonObject().get("task").getAsString();
                    tasks.put(newTaskId++, taskName);

                    statusCode = "201";
                    statusMessage = "Created";
                    body = new Gson().toJson(tasks);
                }
            }

            if ("PATCH".equals(httpMethod)) {
                if ("".equals(requestBody)) {
                    statusCode = "400";
                    statusMessage = "Bad Request";
                } else {
                    String[] pathArray = path.split("/");
                    long requestedId = Long.parseLong(pathArray[pathArray.length - 1]);
                    JsonElement jsonElement = JsonParser.parseString(requestBody);
                    taskName = jsonElement.getAsJsonObject().get("task").getAsString();
                    if(tasks.get(requestedId) == null){
                        statusCode = "404";
                        statusMessage = "Not Found";
                    }else{
                        tasks.put(requestedId, taskName);

                        body = new Gson().toJson(tasks);
                        statusCode = "200";
                        statusMessage = "OK";
                    }

                }
            }

            if ("DELETE".equals(httpMethod)) {

                String[] pathArray = path.split("/");
                long requestedId = Long.parseLong(pathArray[pathArray.length - 1]);
                String taskDeleted = tasks.remove(requestedId);

                if(taskDeleted == null){
                    statusCode = "404";
                    statusMessage = "Not Found";
                }else{
                    statusCode = "200";
                    statusMessage = "OK";
                    body = new Gson().toJson(tasks);
                }
            }

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
            // 5. Close
            socket.close();
        }
    }

}
