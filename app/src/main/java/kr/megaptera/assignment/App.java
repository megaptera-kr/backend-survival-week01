package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {
    public static Long taskId = 0L;

    public static void main(String[] args) throws IOException {

        App app = new App();
        app.run();
    }

    private void run() throws IOException {

        int port = 8080;
        Map<Long, String> tasks = new HashMap<>();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.


        // 1. Listen
        ServerSocket listener = new ServerSocket(port, 0);
        System.out.println("listen");

        while(true) {
            // 2. Accept
            System.out.println("creat Socket");
            Socket socket = listener.accept();

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();

            String request = charBuffer.toString();

            System.out.println("request: " + request);

            // 4. Response
            String message = getMessage(tasks, request);
            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();
        }


    }

    private String getMessage(Map<Long, String> tasks, String request) {
        String method = request.substring(0,request.indexOf("HTTP"));
        if (method.startsWith("GET /tasks")) {
            return getMethod(tasks);
        }

        if (method.startsWith("POST /tasks")) {
            return postMethod(tasks, request);
        }

        if (method.startsWith("PATCH /tasks/")) {
            return patchMethod(tasks, request, method);
        }

        if (method.startsWith("DELETE /tasks/")) {
            return deleteMethod(tasks, method);
        }

        return createMessage("", "400 Bad Request");
    }

    private String getMethod(Map<Long, String> tasks) {
        String body = new Gson().toJson(tasks);
        return createMessage(body, "200 OK");
    }
    private String postMethod(Map<Long, String> tasks, String request) {
        System.out.println(request);
        String task = parseTask(request,"task");

        if("".equals(task)){
            System.out.println("여기들어오니?1");
            return createMessage("","400 Bad Request");
        }
        tasks.put(taskId+1,task);
        String body = new Gson().toJson(tasks);

        return createMessage(body, "201 Created");
    }
    private String patchMethod(Map<Long, String> tasks, String request, String method) {
        Long id = parseTaskId(method);

        if (!tasks.containsKey(id)) {
            return createMessage("", "404 Not Found");
        }

        String task = parseTask(request, "task");

        if (task.equals("")) {
            return createMessage("", "400 Bad Request");
        }
        tasks.put(id, task);
        String body = new Gson().toJson(tasks);

        return createMessage(body, "200 OK");
    }
    private String deleteMethod(Map<Long, String> tasks, String method) {
        Long id = parseTaskId(method);
        if(!tasks.containsKey(id)){
            return createMessage("","404 Not Found");
        }
        tasks.remove(id);
        String body = new Gson().toJson(tasks);

        return createMessage(body, "200 OK");
    }

    private String createMessage(String body, String status) {
        byte[] bytes = body.getBytes();
        String message = "" +
                "HTTP/1.1 " + status + "\n" +
                "Host: localhost:8080\n"+
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "\n" +
                body;
        return message;
    }

    private Long parseTaskId(String method) {
        String[] parts = method.split("/");
        return Long.parseLong(parts[2].trim());
    }

    private String parseTask(String request, String task) {
        String[] lines = request.split("\n"); //라인마다 저장
        String lastLine = lines[lines.length-1];    //마지막라인(json데이터) 저장

        try {
            JsonElement jsonElement = JsonParser.parseString(lastLine);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            return jsonObject.get("task").getAsString(); //task값 리턴
        }catch (Exception e){
            System.out.println(e.getMessage());
            return ""; //에러발생시 null
        }
    }

}
