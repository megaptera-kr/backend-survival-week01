package kr.megaptera.assignment;

import kr.megaptera.assignment.server.ServerJsonParser;
import kr.megaptera.assignment.server.ServerRequest;
import kr.megaptera.assignment.server.ServerResponse;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {
    long count = 0;

    ServerJsonParser serverJsonParser = new ServerJsonParser();
    ServerRequest serverRequest = new ServerRequest();
    ServerResponse serverResponse = new ServerResponse(serverJsonParser);


    public static void main(String[] args) throws IOException {

        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;
        Map<String, String> Elements;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen
        ServerSocket listener = new ServerSocket(port, 0);
        System.out.println("listener!");

        // 2. Accept
        while (true) {
            Socket socket = listener.accept();
            System.out.println("Accept!");

            // 3. Request

            // 3-1. readRequest
            CharBuffer buffer = serverRequest.readRequest(socket);

            // 3-2 parseElement
            Elements = serverRequest.parseElement(buffer);

            // 3.3 method 분기
            if (Elements.get("method").equals("GET")) {
                System.out.println("GET Method!");

                serverResponse.sendMessage(Elements, tasks, socket, 200);

            } else if (Elements.get("method").equals("POST")) {
                System.out.println("POST Method!");

                if (Elements.get("body").equals("")) {
                    serverResponse.sendMessage(Elements, socket, 400);

                } else if (!Elements.get("body").equals("")) {
                    String taskValue = serverJsonParser.returnTask(Elements.get("body"));
                    tasks.put(++count, taskValue);

                    serverResponse.sendMessage(Elements, tasks, socket, 201);
                }

            } else if (Elements.get("method").equals("PATCH")) {
                System.out.println("PATCH Method!");

                String path2 = Elements.get("path2");
                String path2Value = tasks.get(Long.parseLong(path2));

                if (path2Value == null) {
                    serverResponse.sendMessage(Elements, socket, 404);
                }

                if (Elements.get("body").equals("")) {
                    serverResponse.sendMessage(Elements, socket, 400);
                } else if (!Elements.get("body").equals("")) {
                    String taskValue = serverJsonParser.returnTask(Elements.get("body"));
                    tasks.put(Long.parseLong(path2), taskValue);

                    serverResponse.sendMessage(Elements, tasks, socket, 200);
                }


            } else if (Elements.get("method").equals("DELETE")) {
                System.out.println("DELETE Method!");

                String path2 = Elements.get("path2");
                String path2Value = tasks.get(Long.parseLong(path2));

                if (path2Value == null) {
                    serverResponse.sendMessage(Elements, socket, 404);
                }

                tasks.remove(Long.parseLong(path2));

                serverResponse.sendMessage(Elements, tasks, socket, 200);

            }

            socket.close();
            System.out.println("Close!");

        }
    }
}
