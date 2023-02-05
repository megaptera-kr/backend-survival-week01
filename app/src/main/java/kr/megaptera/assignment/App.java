package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        List<String> Elements;

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
            if (Elements.get(0).equals("GET")) {
                String body = getTasksToJson(tasks);
                byte[] bytes = body.getBytes();
                String message = "" +
                        "HTTP/1.1 200 OK\n" +
                        "Content-Length: " + bytes.length + "\n" +
                        "Content-Type: application/json; charset=UTF-8\n" +
                        "Host: " + Elements.get(2) + "\n" +
                        "\n" +
                        body;

                OutputStream outputStream = socket.getOutputStream();
                Writer writer = new OutputStreamWriter(outputStream);

                writer.write(message);
                writer.flush();

            } else if (Elements.get(0).equals("POST")) {
                //JSON 파싱
                String taskValue = returnTask(Elements.get(2));
                tasks.put(++count, taskValue);
                getTasksToJson(tasks);

            } else if (Elements.get(0).equals("PATCH")) {

            } else if (Elements.get(0).equals("DELETE")) {

            }
            socket.close();

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

    private List<String> parseElement(CharBuffer buffer) {
        String requestMethod;
        String path;
        String host;
        String requestBody;
        List<String> Elements = new ArrayList<>(3);

        String[] splitBuffer = buffer.toString().split("/");
        String[] splitBuffer2 = buffer.toString().split("\\n");

        // 1. requestMethod
        requestMethod = splitBuffer[0].trim();
        Elements.add(requestMethod);

        // 2. path
        path = splitBuffer[1].substring(0, 5);
        Elements.add(path);

        // 3. host
        host = splitBuffer2[1].substring(5).trim();
        Elements.add(host);

        // 4. requestBody
        if (!requestMethod.equals("GET")) {
            requestBody = splitBuffer2[splitBuffer2.length - 1].trim();
            Elements.add(requestBody);
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

}
