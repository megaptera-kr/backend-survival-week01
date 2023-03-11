package kr.megaptera.assignment;

import kr.megaptera.assignment.factories.HttpStartLineFactory;
import kr.megaptera.assignment.factories.HttpRequestSourceFactory;
import kr.megaptera.assignment.managers.HttpPathBindingManager;
import kr.megaptera.assignment.models.HttpMethodType;
import kr.megaptera.assignment.models.HttpResponseSource;

import java.io.*;
import java.net.ServerSocket;
import java.nio.CharBuffer;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {

        // TODO : (dh) Move Initialize Location
        var pathBindingManager = new HttpPathBindingManager();

        pathBindingManager.Put(HttpMethodType.Get, "/tasks", (requestSource -> {
            return new HttpResponseSource();
        }));

        pathBindingManager.Put(HttpMethodType.Post, "/tasks", (requestSource -> {
            return new HttpResponseSource();
        }));

        pathBindingManager.Put(HttpMethodType.Patch, "/tasks", (requestSource -> {
            return new HttpResponseSource();
        }));

        pathBindingManager.Put(HttpMethodType.Delete, "/tasks", (requestSource -> {
            return new HttpResponseSource();
        }));

        int port = 8080;
        int backlog = 0;
        ServerSocket serverSocket = new ServerSocket(port, backlog);

        while (true) {
            System.out.println("Listen");

            var clientSocket = serverSocket.accept();
            System.out.println("Accept");

            Reader clientReader = new InputStreamReader(clientSocket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            clientReader.read(charBuffer);

            charBuffer.flip();

            String requestMessage = charBuffer.toString();

            var httpRequestSourceFactory = new HttpRequestSourceFactory();
            var requestSource = httpRequestSourceFactory.Create(requestMessage);
            var firstLine = requestSource.getStartLine();

            var action = pathBindingManager.Get(firstLine.getHttpMethodType(), firstLine.getPath());
            if(action == null){
                // TODO : (dh) Not Found..
            }

            var responseSource = action.execute(requestSource);

            System.out.println("Request done");

            String body = "Hello, world!";
            byte[] bytes = body.getBytes();
            String responseMessage = "" +
                    "HTTP/1.1 200 OK\n" +
                    "Content-Type: text/html; charset=UTF-8\n" +
                    "Content-Length: " + bytes.length + "\n" +
                    "\n" +
                    body;

            System.out.println("Process done");

            Writer clientWriter = new OutputStreamWriter(clientSocket.getOutputStream());
            clientWriter.write(responseMessage);
            System.out.println("Response done");

            // 4. Close
            clientWriter.flush();
            clientReader.close();
            clientSocket.close();
            System.out.println("Close");
        }
    }

}
