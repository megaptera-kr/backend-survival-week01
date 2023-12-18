package kr.megaptera.assignment;

import com.google.gson.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static Long newId = 0L;
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
        System.out.println("Listen!");

        while(true){
            // 2. Accept
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();

            System.out.println("Request :" + charBuffer.toString());

            // 정보추출
            String[] requestLines = charBuffer.toString().split("\n");
            String requestMethod = requestLines[0].split(" ")[0];
            String requestPath = requestLines[0].split(" ")[1];
            String requestBody = requestLines[requestLines.length-1];
            JsonObject requestBodyJson = new JsonObject();
            if(requestBody.length() > 1){
                JsonElement jsonElement = JsonParser.parseString(requestBody);
                requestBodyJson = jsonElement.getAsJsonObject();
            }

            // 처리
            Map<String, String> bodyMap= processBusinessLogic(tasks, requestMethod, requestPath, requestBodyJson);

            // 4. Response
            String message = processRespMessage(bodyMap);
            System.out.println("message: "+ message);

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();
        }
    }

    private String processRespMessage(Map bodyMap) {
        String body = bodyMap.get("body").toString().replace("\\\"","");
        byte[] bytes = body.getBytes();
        String message = "" +
                "HTTP/1.1 "+ bodyMap.get("status").toString()+"\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Host: localhost:8080" + "\n" +
                "\n" +
                body;
        return message;
    }

    private Map<String, String> processBusinessLogic(Map tasks, String requestMethod, String requestPath, JsonObject requestBodyJson) {
        String body = "";
        if(requestMethod.equals("GET") && requestPath.equals("/tasks")){
            body = new Gson().toJson(tasks);

            return Map.of(
                    "status","200 OK",
                    "body",body);
        }

        if(requestMethod.equals("POST") && requestPath.startsWith("/tasks")){
            if(requestBodyJson.get("task") == null){
                return Map.of(
                        "status", "400 Bad Request",
                        "body","");
            }

            String value = requestBodyJson.get("task").toString();
            tasks.put(generateTaskId(),value);

            body = new Gson().toJson(tasks);
            return Map.of(
                    "status","201 Created",
                    "body",body);
        }

        if(requestMethod.equals("PATCH") && requestPath.startsWith("/tasks")){

            Long secondPath = Long.valueOf(requestPath.split("/")[2]);
            if(tasks.get(secondPath) == null){
                return Map.of(
                        "status", "404 Not Found",
                        "body","");
            }

            if(requestBodyJson.get("task") == null){
                return Map.of(
                        "status", "400 Bad Request",
                        "body","");
            }

            String inputValue = requestBodyJson.get("task").toString();
            tasks.put(secondPath,inputValue);

            body = new Gson().toJson(tasks);
            return Map.of(
                    "status","200 OK",
                    "body",body);
        }

        if(requestMethod.equals("DELETE") && requestPath.startsWith("/tasks")){

            Long secondPath = Long.valueOf(requestPath.split("/")[2]);
            if(!tasks.containsKey(secondPath)){
                return Map.of(
                        "status", "404 Not Found",
                        "body","");
            }

            tasks.remove(secondPath);

            body = new Gson().toJson(tasks);
            return Map.of(
                    "status","200 OK",
                    "body",body);
        }

        return Map.of(
                "status","200 OK",
                "body",body);
    }

    private Long generateTaskId() {
        newId += 1;
        return newId;
    }

}
