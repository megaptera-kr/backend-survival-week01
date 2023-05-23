package kr.megaptera.assignment;

import com.google.gson.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;


        Map<Long, String> tasks = new HashMap<>();

        Long seq = 1L;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        ServerSocket listener = new ServerSocket(port, 0);


        // 2. Accept
        while (true) {
            Socket socket = listener.accept();

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());

            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();

            String requestHeaders= charBuffer.toString();

            System.out.println("requestHeaders :::" + requestHeaders);
            String responseMessage = "";
            String ErrorPage1= "" +
                    "HTTP/1.1 400 BAD REQUEST\n" +
                    "Content-Type: application/json; charset=UTF-8\n" +
                    "Content-Length: 0\n" +
                    "Host: localhost:8080\n";

            String ErrorPage2= "" +
                    "HTTP/1.1 404 Not Found\n" +
                    "Content-Type: application/json; charset=UTF-8\n" +
                    "Content-Length: 0\n" +
                    "Host: localhost:8080\n";


            if(requestHeaders.contains("GET")) {
                responseMessage = makeGetResponseMessage(tasks,gson);

            } else if(requestHeaders.contains("POST")) {
                if(isNullBody(requestHeaders)){
                    responseMessage = ErrorPage1;
                } else {
                    tasks.put(seq++,getTask(requestHeaders));
                    responseMessage = makeCreateResponseMessage(tasks,gson);

                }
            } else if(requestHeaders.contains("PATCH")) {
                if(existTask(requestHeaders,tasks)){
                    responseMessage = ErrorPage2;
                } else if(isNullBody(requestHeaders)){
                    responseMessage = ErrorPage1;
                } else {
                    updateTask(requestHeaders,tasks);
                    responseMessage = makeGetResponseMessage(tasks,gson);
                }
            } else if(requestHeaders.contains("DELETE")) {
                if(existTask(requestHeaders,tasks)){
                    responseMessage = ErrorPage2;
                } else {
                    deleteTask(requestHeaders,tasks);
                    responseMessage = makeGetResponseMessage(tasks,gson);
                }
            }

            // 4. Response


            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(responseMessage);
            System.out.println("response message ::" + responseMessage);
            writer.flush();

            socket.close();
        }
    }

    private static String makeGetResponseMessage(Map<Long, String> tasks, Gson gson) {
        String body = gson.toJson(tasks).replace("\\\"","\"");
        return "" +
                "HTTP/1.1 200 OK\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Content-Length: " + body.getBytes().length + "\n" +
                "Host: localhost:8080\n" +
                "\n" + body;
    }

    private static String makeCreateResponseMessage(Map<Long, String> tasks, Gson gson) {
        String body = gson.toJson(tasks).replace("\\\"","\"");
        return "" +
                "HTTP/1.1 201 Created\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Content-Length: " + body.getBytes().length + "\n" +
                "Host: localhost:8080\n" +
                "\n" + body;
    }

    private String getTask(String requestHeaders) {
        String body = requestHeaders.substring(requestHeaders.indexOf("{"));
        JsonElement jsonElement = JsonParser.parseString(body);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject.get("task").getAsString();
    }

    private boolean isNullBody (String requestHeaders) {
        return !requestHeaders.contains("{");
    }

    private boolean existTask (String requestHeaders, Map<Long, String> tasks) {
        Long taskNum = getTaskNum(requestHeaders);
        String existValue = tasks.getOrDefault(taskNum, "false");
        return existValue.equals("false");
    }

    private static Long getTaskNum(String requestHeaders) {
        String[] split = requestHeaders.split("/");
        String split2 = split[2];
        Long taskNum = Long.parseLong(split2.split(" ")[0]);
        return taskNum;
    }

    private void updateTask(String requestHeaders, Map<Long, String> tasks) {
        Long taskNum = getTaskNum(requestHeaders);
        String task = getTask(requestHeaders);
        tasks.put(taskNum,task);
    }

    private void deleteTask(String requestHeaders, Map<Long, String > tasks) {
        Long taskNum = getTaskNum(requestHeaders);
        tasks.remove(taskNum);
    }

}
