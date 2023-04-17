package kr.megaptera.assignment;

import kr.megaptera.assignment.requestType.Method;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

import static kr.megaptera.assignment.requestType.Method.MethodFactory;

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

        ServerSocket listener = new ServerSocket(8080, 0);
        System.out.println("listen!");

        // 2. Accept
        while (true) {
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());

            CharBuffer charBuffer = CharBuffer.allocate(1_000_00);
            reader.read(charBuffer);

            charBuffer.flip();

            Method method = MethodFactory(charBuffer.toString());
            String message = method.process(tasks);


            // 4. Response
            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();

            // 5. Close
            socket.close();
        }
    }
}