package kr.megaptera.assignment;

import com.google.gson.Gson;

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

        // TODO: 요구사항에 맞게 과제를 진행해주세요.


        // 1. Listen
        ServerSocket listener = new ServerSocket(port, 0);

        System.out.println("listen");


            // 2. Accept
            System.out.println("creat Socket");
            Socket socket = listener.accept();

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();

            System.out.println(charBuffer);
            String request = charBuffer.toString();
            String method = request.substring(0,request.indexOf("HTTP"));

            String message = "";

            // 4. Response
            switch (method){
                case "GET /tasks ":
                    message = getTodo(tasks); break;
                case "POST /tasks ":
                    message = createTodo(tasks); break;
                case "PATCH /tasks ":
                    message = updateTodo(tasks); break;
                case "DELETE /tasks ":
                    message = deleteTodo(tasks);break;
                default:
                    System.out.println("이상함"); break;
            }

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();

            socket.close();

    }

    private String getTodo(Map<Long, String> tasks){
        String body = new Gson().toJson(tasks);
        return getMessage(body, "200 OK");

    }
    private String createTodo(Map<Long, String> tasks){

        if(tasks.isEmpty()){
            return getMessage("","400 Bad Request");
        }
        return null;
    }
    private String updateTodo(Map<Long, String> tasks){

        return null;
    }
    private String deleteTodo(Map<Long, String> tasks) {
        return null;
    }
    private String getMessage(String body, String status){
        byte[] bytes = body.getBytes();
        String message = "" +
                "HTTP/1.1 " + status + "\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: localhost:8080\n" +
                "\n" + body;

        System.out.println(message);
        return message;
    }
}
