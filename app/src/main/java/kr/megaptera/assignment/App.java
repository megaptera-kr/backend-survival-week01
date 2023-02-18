package kr.megaptera.assignment;

import com.google.gson.*;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.util.*;

public class App {
    public static Long num = 0L;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {

        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen
        ServerSocket serverSocket = new ServerSocket(port, 5);
        System.out.println("Server Listen!");

        while (true) {
            // 2. Accept

            Socket socket = serverSocket.accept();

            System.out.println("Accept!");

            // 3. Request
            String request = getRequest(socket);

            System.out.println(request);


            // 4. Response

            process(request, tasks, socket);
        }

    }

    private static void writeMessage(Socket socket, String rm) throws IOException {
        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(rm);
        writer.flush();

    }

    private static String getRequest(Socket socket) throws IOException {
        InputStream is = socket.getInputStream();
        Reader reader = new InputStreamReader(is);

        CharBuffer cbuf = CharBuffer.allocate(1_000_000);
        reader.read(cbuf);
        cbuf.flip();


        String request = cbuf.toString();

        return request;
    }

    private String getJson(String request, String value) {
        String[] lines = request.split("\n");
        String data = lines[lines.length - 1];

        try {
            JsonElement element = JsonParser.parseString(data);
            JsonObject object = element.getAsJsonObject();

            return object.get(value).getAsString();
        } catch (Exception e) {
            return "";
        }
    }

    private void process(String request, Map<Long, String> tasks, Socket socket) throws IOException {
        String method = request.split("\n")[0].split(" ")[0];

        switch (method) {
            case ("GET"): {
                String rm = processGet(tasks);
                writeMessage(socket, rm);
                break;
            }
            case ("POST"): {
                String json = getJson(request, "task");

                if (json == "") {
                    String rm = processPost(tasks, json);
                    writeMessage(socket, rm);
                    break;
                } else {
                    tasks.put(++num, json);
                    String rm = processPost(tasks, json);
                    writeMessage(socket, rm);
                    break;
                }
            }
            case ("DELETE"): {
                Long id = Long.valueOf(request.split("\n")[0].split(" ")[1].split("/")[2]);
                String status = "";
                if (tasks.containsKey(id)) {
                    status = "200";
                    String rm = processDelete(tasks, id, status);
                    writeMessage(socket, rm);
                    break;
                } else {
                    status = "404";
                    String rm = processDelete(tasks, id, status);
                    writeMessage(socket, rm);
                    break;
                }
            }
            case ("PATCH"): {
                Long id = Long.valueOf(request.split("\n")[0].split(" ")[1].split("/")[2]);
                String json = getJson(request, "task");
                String status = "";
                if (tasks.containsKey(id)) {
                    if (json == "") {
                        status = "400";
                        String rm = processPatch(tasks, id, json, status);
                        writeMessage(socket, rm);
                        break;
                    } else {
                        status = "200";
                        tasks.replace(id, json);
                        String rm = processPatch(tasks, id, json, status);
                        writeMessage(socket, rm);
                        break;
                    }
                } else {
                    status = "404";
                    String rm = processPatch(tasks, id, json, status);
                    writeMessage(socket, rm);
                    break;
                }
            }
        }
    }

    private String processGet(Map<Long, String> tasks) {
        String message = "" +
                "HTTP/1.1 200 OK\n" +
                "Content-Length: " + new Gson().toJson(tasks).length() + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "\n" +
                new Gson().toJson(tasks);

        return message;
    }

    private String processPost(Map<Long, String> tasks, String json) {
        String message = "";
        byte[] bytes = new Gson().toJson(tasks).getBytes();

        if (json == "") {
            message = "" +
                    "HTTP/1.1 400 Bad Request\n" +
                    "Content-Length: " + json.length() + "\n" +
                    "Content-Type: application/json; charset=UTF-8\n" +
                    "\n";
        } else {
            message = "" +
                    "HTTP/1.1 201 Created\n" +
                    "Content-Length: " + bytes.length + "\n" +
                    "Content-Type: application/json; charset=UTF-8\n" +
                    "\n" +
                    new Gson().toJson(tasks);
        }
        return message;
    }

    private String processPatch(Map<Long, String> tasks, Long id, String json, String status) {
        String message = "";
        switch (status) {
            case ("200"): {
                tasks.replace(id, json);
                byte[] bytes = new Gson().toJson(tasks).getBytes();
                message = "" +
                        "HTTP/1.1 200 OK\n" +
                        "Content-Length: " + bytes.length + "\n" +
                        "Content-Type: application/json; charset=UTF-8\n" +
                        "\n" +
                        new Gson().toJson(tasks);
                break;
            }
            case ("400"): {
                // Bad Request
                message = "" +
                        "HTTP/1.1 400 Bad Request\n" +
                        "Content-Length: 0\n" +
                        "Content-Type: application/json; charset=UTF-8\n" +
                        "\n";
                break;
            }
            case ("404"): {
                // Not Found
                message = "" +
                        "HTTP/1.1 404 Not Found\n" +
                        "Content-Length: 0\n" +
                        "Content-Type: application/json; charset=UTF-8\n" +
                        "\n";
                break;
            }
        }
        return message;
    }

    private String processDelete(Map<Long, String> tasks, Long id, String status) {
        String message = "";
        if (status == "200") {
            tasks.remove(id);
            message = "" +
                    "HTTP/1.1 200 OK\n" +
                    "Content-Length: " + new Gson().toJson(tasks).length() + "\n" +
                    "Content-Type: application/json; charset=UTF-8\n" +
                    "\n" +
                    new Gson().toJson(tasks);
        } else {
            message = "" +
                    "HTTP/1.1 404 Not Found\n" +
                    "Content-Length: 0\n" +
                    "Content-Type: application/json; charset=UTF-8\n" +
                    "\n";
        }
        return message;
    }
}

