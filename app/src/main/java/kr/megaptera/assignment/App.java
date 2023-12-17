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
        int port = 8080;

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

        // Debug : request 메시지 출력
        System.out.println(charBuffer);

        // responseProvider
        reponseProvider reponseProvider = new reponseProvider(String.valueOf(charBuffer));

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
