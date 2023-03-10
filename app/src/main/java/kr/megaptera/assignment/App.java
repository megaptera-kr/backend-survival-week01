package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
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
        ServerSocket listener = new ServerSocket(port);


        while (true) {
            // 2. Accept
            Socket socket = listener.accept();

            // 3. Request
            InputStreamReader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);        //charBuffer에 읽어온다
            charBuffer.flip();

            //Request 메시지 파싱
            String[] messageLines = charBuffer.toString().split("\n");
            int linesSize = messageLines.length;
            String requestBody = messageLines[linesSize - 1];
            System.out.println("requestBody = " + requestBody);
            System.out.println("requestBody.isBlank() = " + requestBody.isBlank());
            System.out.println("requestBody.isEmpty() = " + requestBody.isEmpty());

            String startLine = messageLines[0];
            String httpMethod = startLine.split(" ")[0];
            String path = startLine.split(" ")[1];
            String[] paths = path.split("/");
            System.out.println("path = " + path);


            try {
                // path가 /tasks인 경우
                if (path.equals("/tasks")) {
                    if (httpMethod.equals("GET")) {
                        System.out.println("httpMethod = " + httpMethod);
                        String body = new Gson().toJson(tasks);
                        make_response(socket, "200 OK", body);

                    } else if (httpMethod.equals("POST")) {
                        //request 메시지 바디가 비어있으면 예외발생
                        if (!requestBody.isBlank()) {
                            JsonObject jsonObject = JsonParser.parseString(requestBody).getAsJsonObject();
                            System.out.println("jsonObject = " + jsonObject);
                            JsonElement jsonElement = jsonObject.get("task");
                            tasks.put(1L, jsonElement.getAsString());

                            System.out.println("tasks = " + tasks);
                        } else {
                            //예외 발생시키기
                            //https://cheershennah.tistory.com/147 참고
                            Exception e = new Exception("고의 발생");
                            throw e;
                        }

                    }
                }
                // path가 /tasks/{id}인 경우
                else if (paths.length >= 3) {
                    if (httpMethod.equals("PATCH")) {

                    } else if (httpMethod.equals("DELETE")) {

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //close
            socket.close();
        }


        // 4. Response
    }

    private void make_response(Socket socket, String state, String body) throws IOException {
        System.out.println("body = " + body);
        byte[] bytes = body.getBytes();
        //content-type 구하기 -> 메서드가 있는건지? 아니면 application/json으로 직접 쓰는건지?
        String message = "" +
                "HTTP/1.1 " + state + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Host: localhost:8080\n" +
                "\n" +
                body;
        OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

}
