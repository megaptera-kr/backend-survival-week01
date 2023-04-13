package kr.megaptera.assignment;

import com.google.gson.Gson;

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

// 소켓을 사용한 HTTP 서버
public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    // 필요 모듈정리 : ServerSocket, Socket
    private void run() throws IOException {
        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        int port = 8080;
        ServerSocket serverSocket = new ServerSocket(port, 0);
        Map<Long, String> tasks = new HashMap<>();
        tasks.put(1L, "dd");


        while (true) {
            // 2. Accept
            Socket socket = serverSocket.accept();

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();

            String[] arr = charBuffer.toString().split("\\s");
            System.out.println("charBuffer\n" + arr[1]);

            switch (arr[0]) {
                case "GET":
                    requestGet(arr[1], socket, tasks);
                    break;
                case "POST":
                    break;
                case "PATCH":
                    break;
                case "DELETE":
                    break;
            }

//            responseBody(socket);

            socket.close();
        }

    }

    private void requestGet(String path, Socket socket, Map<Long, String> tasks) throws IOException {
        if (path.equals("/tasks")) {
            responseBody(socket, tasks);
        }
    }

    private void responseBody(Socket socket, Map<Long, String> tasks) throws IOException {
        // 4. Response
        String body = new Gson().toJson(tasks);

        byte[] bytes = body.getBytes();

        String message = """
                HTTP/1.1 200 OK
                Content-Type: application/json; charset=UTF-8
                Content-Length:""" + bytes.length + "\n" + "\n" + body;


        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

}
