package kr.megaptera.assignment;

import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
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
        tasks.put(1L, "공부하기");

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen

        ServerSocket listener = new ServerSocket(8080, 0);

        System.out.println("Listen!");

        // 2. Accept

        Socket socket = listener.accept();

        System.out.println("Accept!");

        // 3. Request
        request(socket);

        // 4. Response

        int size = tasks.size();

        // parse
        String json = new Gson().toJson(tasks);

        String message = "" +
                "HTTP/1.1 200 OK\n" +
                "Content-Length: " + size + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: " + socket.getLocalAddress().getHostName() + ":" + socket.getLocalPort() +
                "\n" +
                json;

        Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
        writer.write(message);
        writer.flush();

    }

    private void request(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());
        socket.getInputStream();

        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);

        charBuffer.flip();
        System.out.println("charBuffer : " + charBuffer.toString());

        // GET /tasks


        // POST /tasks
        // Body data가 없을 경우 -> 400 Bad Request

        // PATCH /tasks/{id}
        // 존재하지 않는 id로 요청하는 경우 -> 404 Not Found
        // Body data가 없을 경우 -> 400 Bad Request

        // DELETE /tasks/{id}
        // 존재하지 않는 id로 요청하는 경우 -> 404 Not Found
    }

}
