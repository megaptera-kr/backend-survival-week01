package kr.megaptera.assignment.server;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Map;

public class ServerResponse {
    ServerJsonParser serverJsonParser;

    public ServerResponse(ServerJsonParser serverJsonParser) {
        this.serverJsonParser = serverJsonParser;
    }

    public void sendMessage(Map<String, String> Elements, Socket socket, int statusCode) throws IOException {

        String message = "" +
                "HTTP/1.1 " + String.valueOf(statusCode) + " OK\n" +
                "Content-Length: 0" + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: " + Elements.get("host") + "\n" +
                "\n";

        OutputStream outputStream = socket.getOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);

        writer.write(message);
        writer.flush();
    }

    public void sendMessage(Map<String, String> Elements, Map<Long, String> tasks, Socket socket, int statusCode) throws IOException {

        String body = serverJsonParser.getTasksToJson(tasks);
        byte[] bytes = body.getBytes();
        String message = "" +
                "HTTP/1.1 " + String.valueOf(statusCode) + " OK\n" +
                "Content-Length: " + bytes.length + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Host: " + Elements.get("host") + "\n" +
                "\n" +
                body;

        OutputStream outputStream = socket.getOutputStream();
        Writer writer = new OutputStreamWriter(outputStream);

        writer.write(message);
        writer.flush();
    }
}
