package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {
    public static Long newId = 0L;

    public static void main(String[] args) throws IOException {
        System.out.println("test");
        App app = new App();
        app.run();
    }

    private String run() throws IOException {
        int port = 8080;
        int backlog = 0;
        Map<Long, String> tasks = new HashMap<>();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        ServerSocket listener = new ServerSocket(port,backlog);
        System.out.println("Listen!");

        while (true){
            // 2. Accept
            Socket socket =  listener.accept();
            System.out.println("Accept!");

            // 3. Request
            String requestString = getRequest(socket);

            // 4. Response
            String responseMessage = process(requestString,tasks);
            sendResponse(socket, responseMessage);
        }
    }

    private static String getRequest(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer =CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();
        String request = charBuffer.toString();
        return request;
    }

    private String process(String requestString,Map<Long, String> tasks){
        String[] lines = requestString.split("\n");
        System.out.println(lines[0]);
        String[] firstLineParts = lines[0].split("/");

        String httpMethod = firstLineParts[0].trim();
        String path = firstLineParts[1].split(" ")[0];
        if(!path.equals("tasks")){
            System.out.println("not available path: "+path);
            return generateMessage("","404");
        }
        switch (httpMethod){
            case "GET":{
                String content = new Gson().toJson(tasks);
                return generateMessage(content,"200 OK");
            }
            case "POST":{
                String task = parsePayload(requestString, "task");

                if (task.equals("")) {
                    return generateMessage("", "400 Bad Request");
                }

                tasks.put(generateTaskId(), task);
                String content = new Gson().toJson(tasks);

                return generateMessage(content, "201 Created");
            }
            case "PATCH":{
                Long id = parseTaskId(firstLineParts);
                if (!tasks.containsKey(id)) {
                    return generateMessage("", "404 Not Found");
                }

                String task = parsePayload(requestString, "task");

                if (task.equals("")) {
                    return generateMessage("", "400 Bad Request");
                }

                tasks.put(id, task);
                String content = new Gson().toJson(tasks);

                return generateMessage(content, "200 OK");
            }
            case "DELETE":{
                Long id = parseTaskId(firstLineParts);

                if (!tasks.containsKey(id)) {
                    return generateMessage("", "404 Not Found");
                }

                tasks.remove(id);
                String content = new Gson().toJson(tasks);

                return generateMessage(content, "200 OK");
            }
            default:
                return generateMessage("", "400");
        }

    }

    private String generateMessage(String body,String statusCode){
        byte[] bytes = body.getBytes();
        return  "" +
                "HTTP/1.1 " + statusCode + "\n" +
                "Host: localhost:8080\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "\n"+
                body;
    }

    private String parsePayload(String request, String value) {
        String[] lines = request.split("\n");
        String lastLine = lines[lines.length - 1];
        try {
            JsonElement jsonElement = JsonParser.parseString(lastLine);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.get(value).getAsString();
        } catch (Exception e) {
            return "";
        }
    }
    private Long generateTaskId() {
        newId += 1;
        return newId;
    }
    private void sendResponse(Socket socket, String message) throws IOException {
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }
    private Long parseTaskId(String[] firstLineParts) {
        return Long.parseLong(firstLineParts[2].split(" ")[0]);
    }

}
