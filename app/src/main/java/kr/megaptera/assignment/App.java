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

        // 1. Listen
        try (ServerSocket listener = new ServerSocket(port)) {
            System.out.println("Listen!");

            int loop = 4096;
            while (loop > 0) {

                try (Socket socket = listener.accept();
                     Reader reader = new InputStreamReader(socket.getInputStream());
                     Writer writer = new OutputStreamWriter(socket.getOutputStream())) {

                    // 2. Accept
                    System.out.println("Accept!");

                    // 3. Request
                    CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
                    // TODO(the number of bytes actually read => 처리)
                    reader.read(charBuffer);
                    charBuffer.flip();  // 읽고 flip 필요

                    // Debug : request 메시지 출력
                    System.out.println(charBuffer);

                    // responseProvider
                    reponseProvider reponseProvider = new reponseProvider(String.valueOf(charBuffer), taskRepository);

                    writer.write(reponseProvider.getResponseMessage());
                    writer.flush();
                } catch (IOException e) {
                    // TODO(예외 출력 경고 해결)
                    e.printStackTrace();
                }
                loop--;
            }
        }
    }

}
