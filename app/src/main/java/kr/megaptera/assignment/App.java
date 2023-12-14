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

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen
        ServerSocket listener = new ServerSocket(port);
        System.out.println("Listen!");

        // 2. Accept
        Socket socket = listener.accept();
        System.out.println("Accept!");

//        // 3. Request
//        Reader reader = new InputStreamReader(socket.getInputStream());
//        CharBuffer charBuffer = CharBuffer.allocate(1024);
//        StringBuilder stringBuilder = new StringBuilder();
//        //  메서드 read returns The number of characters added to the buffer, or -1 if this source of characters is at its end
//        int bytesRead;
//        while ((bytesRead = reader.read(charBuffer)) != -1) {
//            stringBuilder.append(charBuffer, 0, bytesRead);
//        }
//        String request = stringBuilder.toString();
//        System.out.println(request);

        // 3. Request
        Reader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();  // 읽고 flip 필요

        // Request Message Parsing
        String[] requestMessage = String.valueOf(charBuffer).split("\n");

        // Request Message의 첫줄이 startLine이다. Http method 그리고 path를 알 수 있다.
        String startLine = requestMessage[0];
        String[] splitStartLine = startLine.split(" ");

        //  httpMethod
        String httpMethod = splitStartLine[0];

        // target path
        String target = splitStartLine[1];

        // Request Message의 두번째 줄이 헤더의 Host에 대한 정보를 가지고 있다.
        String host = requestMessage[1].split(" ")[1];

        // 4. Response
        String body = new Gson().toJson(tasks);
        byte[] bytes = body.getBytes();
        String message = "HTTP/1.1 200 OK\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: " + host + "\n" +
                "\n" +
                body;   // Http message

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();

        // 5. Close
        socket.close();
        listener.close();
        reader.close();
        writer.close();
    }

}
