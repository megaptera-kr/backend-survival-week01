package kr.megaptera.assignment;

import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;
        long idx = 0;
        Map<Long, String> tasks = new HashMap<>();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        ServerSocket listener = new ServerSocket(port, 0);      // Blocking

        while (true) {
            Gson gson = new Gson();
            String defaultURL = "/tasks";
            Socket socket = listener.accept();

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());

            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);

            charBuffer.flip();

            String[] inputArr = charBuffer.toString().split("\n");

            String httpMethod = inputArr[0].split(" ")[0];
            String requestUrl = inputArr[0].split(" ")[1];

            Map<String, String> requestBody = new HashMap<>();
            if (inputArr.length > 9) {
                requestBody = gson.fromJson(inputArr[9], Map.class);
            }
            //System.out.println("httpMethod : " + httpMethod);
            //System.out.println("requestUrl : " + requestUrl);
            //System.out.println("requestBody : " + requestBody.toString());

            String responseBody = "";
            String statusCode = "";

            switch (httpMethod) {
                case "GET":
                    if (requestUrl.equals(defaultURL)) {
                        // Todo 목록 보내기
                        statusCode = "200 OK";
                    } else {
                        statusCode = "404 Not Found";
                    }
                    break;
                case "POST":
                    if (requestUrl.indexOf(defaultURL) == 0) {
                        // Todo 생성하기
                        idx++;
                        if (requestBody.containsKey("task")) {
                            statusCode = "201 Created";
                            tasks.put(Long.valueOf(idx), requestBody.get("task"));
                        } else {
                            statusCode = "400 Bad Request";
                        }
                    }
                    break;
                case "PATCH":
                    if (requestUrl.indexOf(defaultURL) == 0) {
                        // Todo 제목 수정하기
                        long id = Long.valueOf(Integer.parseInt(requestUrl.substring(defaultURL.length() + 1).replaceAll("\\s", "")));
                        if (tasks.get(id) == null) {
                            statusCode = "404 Not Found";
                            break;
                        } else if (requestBody.get("task") == null) {
                            statusCode = "400 Bad Request";
                            break;
                        } else {
                            statusCode = "200 OK";
                            tasks.replace(id, requestBody.get("task"));
                        }
                    }
                    break;
                case "DELETE":
                    if (requestUrl.indexOf(defaultURL) == 0) {
                        // Todo 삭제하기
                        long id = Integer.parseInt(requestUrl.substring(defaultURL.length() + 1).replaceAll("\\s", ""));
                        if (tasks.get(id) == null) {
                            statusCode = "404 Not Found";
                        } else {
                            statusCode = "200 OK";
                            tasks.remove(id);
                        }
                    }
                    break;
                default:
                    statusCode = "404 Not Found";
                    break;
            }

            System.out.println("TODOS : " + tasks);
            System.out.println("statusCode : " + statusCode);
            System.out.println("resBody : " + responseBody);
            // 4. Response

            responseBody = gson.toJson(tasks);
            String message = "" +
                    "HTTP/1.1 " + statusCode + "\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n" +
                    "Content-Length: " + responseBody.length() + "\r\n" +
                    "\r\n" +
                    responseBody;

            System.out.println(message);

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();

            // 5. Close
            socket.close();
        }
    }

}
