package kr.megaptera.assignment;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        // port
        int port = 8080;

        // taskRepository
        taskRepository taskRepository = new taskRepository();

        while (true) {
            // 1. Listen
            ServerSocket listener = new ServerSocket(port);
            System.out.println("Listen!");

            // 2. Accept
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();  // 읽고 flip 필요

            // Debug : request 메시지 출력
            System.out.println(charBuffer);


            // responseProvider
            reponseProvider reponseProvider = new reponseProvider(String.valueOf(charBuffer), taskRepository);

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(reponseProvider.getResponseMessage());
            writer.flush();

            // 5. Close
            socket.close();
            listener.close();
            reader.close();
            writer.close();
        }
    }

}
