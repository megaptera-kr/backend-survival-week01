package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static Long num = 1L;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;
        int backlog = 0;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen
        ServerSocket listener = new ServerSocket(port, backlog);
        System.out.println("listen!!");
        // 2. Accept
        while (true) {
            Socket socket = listener.accept();
            System.out.println("accept!!");
            // 3. Request
            String request = getRequest(socket);
            System.out.println("request!!");
            // 4. Response
            String message = process(tasks, request);
            writeMessage(socket, message);
            System.out.println("response!!");
        }
    }

    private String getRequest(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();
        System.out.println("After flip charBuffer  : " + charBuffer);
        return charBuffer.toString();

    }

    private static String process(Map<Long, String> tasks, String request) {
        String method = request.substring(0, request.indexOf("HTTP")-1);
        if (method.startsWith("GET /tasks")) {
            return processGetTask(tasks);
        }
        if(method.startsWith("POST /tasks")) {
            return processPostTask(tasks, request);
        }
        if(method.startsWith("PATCH /tasks")){
            return processPatchTask(tasks, request, method);
        }
        if(method.startsWith("DELETE /tasks")){
            return processDeleteTask(tasks, request, method);
        }


        return "";
    }


    private static String processGetTask(Map<Long, String> tasks) {
        String content = new Gson().toJson(tasks);
        return generateMessage(content, "200 OK");

    }

    private static String processPostTask(Map<Long, String> tasks, String request){
        String task = parsePayload(request, "task");

        if(task.equals("")){
            return generateMessage("", "400 Bad Request");
        }
        tasks.put(num++, task);
        String content = new Gson().toJson(tasks);
        return generateMessage(content, "201 created");
    }

    private static String processPatchTask(Map<Long, String> tasks, String request, String method){
        Long id = parseTaskId(method);

        if(!tasks.containsKey(id)){
            return generateMessage("", "404 Not Found");
        }

        String task = parsePayload(request, "task");

        if (task.equals("")) {
            return generateMessage("", "400 Bad Request");
        }

        tasks.put(id, task);
        String content = new Gson().toJson(tasks);
        return generateMessage(content, "200 OK");
    }

    private static String processDeleteTask(Map<Long, String> tasks, String request, String method){
        Long id = parseTaskId(method);

        if(!tasks.containsKey(id)){
            return generateMessage("", "404 Not Found");
        }

        tasks.remove(id);
        String content = new Gson().toJson(tasks);
        return generateMessage(content, "200 OK");
    }

    private static Long parseTaskId(String method) {
        String[] parts = method.split("/");

        return Long.parseLong(parts[2].trim());
    }


    private static String parsePayload(String request, String value) {
        String[] lines = request.split("\n");
        String lastLine = lines[lines.length - 1];

        try {
            JsonElement jsonElement = JsonParser.parseString(lastLine);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
//            System.out.println(jsonObject.get("task").getAsString());
//            System.out.println(jsonObject.toString());
            return jsonObject.get(value).getAsString();
        } catch (Exception e) {
            return "";
        }
    }


    private static String generateMessage(String body, String statusCode) {
        byte[] bytes = body.getBytes();

        System.out.println("statusCode : " + statusCode);

        return "" +
                "HTTP/1.1 " + statusCode + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Host: localhost:8080" + "\n" +
                "\n" +
                body;
    }


    private void writeMessage(Socket socket, String message) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }
}