package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {
    long count = 0;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;
        Map<String, String> Elements;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen
        ServerSocket listener = new ServerSocket(port, 0);
        System.out.println("listener!");

        // 2. Accept
        while (true) {
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request

            // 3-1. readRequest
            CharBuffer buffer = readRequest(socket);

            // 3-2 parseElement
            Elements = parseElement(buffer);

            // 3.3 method 분기
            if (Elements.get("method").equals("GET")) {
                System.out.println("GET Method!");

                sendMessage(Elements, tasks, socket, 200);

            } else if (Elements.get("method").equals("POST")) {
                System.out.println("POST Method!");

                String taskValue = returnTask(Elements.get("body"));
                tasks.put(++count, taskValue);

                sendMessage(Elements, tasks, socket, 201);

            } else if (Elements.get("method").equals("PATCH")) {
                System.out.println("PATCH Method!");

                String path2 = Elements.get("path2");
                String taskValue = returnTask(Elements.get("body"));

                tasks.put(Long.parseLong(path2), taskValue);

                sendMessage(Elements, tasks, socket, 200);

            } else if (Elements.get("method").equals("DELETE")) {
                System.out.println("DELETE Method!");

                String path2 = Elements.get("path2");

                tasks.remove(Long.parseLong(path2));

                sendMessage(Elements, tasks, socket, 200);

            }

            socket.close();
            System.out.println("Close!");

        }
    }
    //////////////////////////////////////////////////////////////////////////
    // METHODS

    private CharBuffer readRequest(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        Reader reader = new InputStreamReader(inputStream);

        CharBuffer buffer = CharBuffer.allocate(1_000_000);

        reader.read(buffer);
        buffer.flip();
        System.out.println("buffer.toString() = " + buffer.toString());
        return buffer;
    }

    private Map<String, String> parseElement(CharBuffer buffer) {
        String requestMethod;
        String path1;
        String path2;
        String host;
        String requestBody;
        Map<String, String> Elements = new HashMap<>();

        String[] splitBuffer = buffer.toString().split("/");
        String[] splitBuffer2 = buffer.toString().split("\\n");

        // 1. requestMethod
        requestMethod = splitBuffer[0].trim();
        Elements.put("method", requestMethod);

        // 2. path1
        path1 = splitBuffer[1].substring(0, 5);
        Elements.put("path1", path1);

        // 3. path2
        path2 = splitBuffer[2].toUpperCase().replaceAll("[^0-9]", "");
        Elements.put("path2", path2);

        // 4. host
        host = splitBuffer2[1].substring(5).trim();
        Elements.put("host", host);

        // 5. requestBody
        if (!requestMethod.equals("GET")) {
            requestBody = splitBuffer2[splitBuffer2.length - 1].trim();
            Elements.put("body", requestBody);
        }

        return Elements;
    }

    private String returnTask(String requestBody) {
        JsonParser parser = new JsonParser();
        JsonElement element = parser.parse(requestBody);

        String task = element.getAsJsonObject().get("task").getAsString();
        return task;
    }

    private String getTasksToJson(Map<Long, String> tasks) {
        String tasksToJson = gson.toJson(tasks);
        return tasksToJson;
    }

    private void sendMessage(Map<String, String> Elements, Map<Long, String> tasks, Socket socket, int statusCode) throws IOException {
        String body = getTasksToJson(tasks);
        byte[] bytes = body.getBytes();
        String message = "" +
                "HTTP/1.1 " + String.valueOf(statusCode) + " OK\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: " + Elements.get("host") + "\n" +
                "\n" +
                body;

        OutputStream outputStream = socket.getOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);

        writer.write(message);
        writer.flush();
    }
}
