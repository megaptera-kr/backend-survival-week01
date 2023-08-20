package com.studio.http.server;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static void main(String[] args) {
        try {
            ServerSocket listener = new ServerSocket(8080, 0);
            System.out.println("Server listening on port 8080...");

            Map<Integer, String> tasks = new HashMap<>();
            int taskId = 1;

            while (true) {
                Socket socket = listener.accept();
                System.out.println("Accepted connection from client...");

                com.studio.http.server.HttpRequestHandler requestHandler = new com.studio.http.server.HttpRequestHandler(socket, tasks, taskId);
                requestHandler.start(); // Use start() instead of run() for multi-threading
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class HttpRequestHandler extends Thread {
    private final Socket socket;
    private final Map<Integer, String> tasks;
    private static int taskId;

    public HttpRequestHandler(Socket socket, Map<Integer, String> tasks, int taskId) {
        this.socket = socket;
        this.tasks = tasks;
        this.taskId = taskId;
    }

    @Override
    public void run() {
        try {
            Reader reader = new InputStreamReader(socket.getInputStream());

            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);

            charBuffer.flip();
            String strBuffer = charBuffer.toString();
            String[] lines = strBuffer.split("\r\n");
            String requestLine = lines[0];

            if (requestLine != null && requestLine.startsWith("GET /tasks")) {
                String responseBody = new Gson().toJson(tasks);
                byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);

                String response = "" +
                        "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/json; charset=UTF-8\r\n" +
                        "Content-Length: " + bytes.length + "\r\n" +
                        "Host: localhost:8080\r\n" +
                        "\r\n" +
                        responseBody;

                Writer writer = new OutputStreamWriter(socket.getOutputStream());
                writer.write(response);
                writer.flush();
            } else if (requestLine.startsWith("POST /tasks")) {
                StringBuilder requestBody = new StringBuilder();

                // Read the request body
                while (reader.ready()) {
                    char c = (char) reader.read();
                    requestBody.append(c);
                }

                String task = null;

                System.out.println("-----body-----");
                for (String line : lines) {
                    if (line.contains("\"task\":")) {
                        int startIndex = line.indexOf('"', line.indexOf("\"task\":") + 8); // Find the second occurrence of "
                        int endIndex = line.lastIndexOf('"');
                        task = line.substring(startIndex + 1, endIndex);
                        System.out.println(task);
                        break;
                    } else {
                        System.out.println(task);
                    }
                }

                if (task == null) {
                    System.out.println("errorrrrr");
                } else {
                    byte[] bytess = task.getBytes();

                    String uniString = new String(bytess, "UTF-8");
                    System.out.println(uniString);

                    addTask(tasks, taskId, uniString);
                    taskId = taskId + 1;

                    String responseBody = new Gson().toJson(tasks);
                    byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
                    System.out.println(responseBody);

                    String response = "" +
                            "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json; charset=UTF-8\r\n" +
                            "Content-Length: " + bytes.length + "\r\n" +
                            "Host: localhost:8080\r\n" +
                            "\r\n" +
                            responseBody;

                    Writer writer = new OutputStreamWriter(socket.getOutputStream());
                    writer.write(response);
                    writer.flush();
                }
            } else {
                String response = "HTTP/1.1 404 Not Found\r\n\r\n";
                Writer writer = new OutputStreamWriter(socket.getOutputStream());
                writer.write(response);
                writer.flush();
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addTask(Map<Integer, String> tasks, int id, String task) {
        tasks.put(id, task);
    }
}