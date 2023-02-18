package kr.megaptera.assignment;

import static kr.megaptera.assignment.HttpStatus.*;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
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
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class App {

    private static Map<Long, String> TEMP_DB = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        // 1. Listen
        try (ServerSocket listener = new ServerSocket(port)) {

            while (true) {
                // 2. Accept
                Socket socket = listener.accept();

                // 3. Request
                String requestInfo = getRequest(socket);
                Request request = parseRequest(requestInfo);

                // 4. Response
                Runnable runnable = () -> {
                    try {
                        String message = makeResponse(request);
                        System.out.println("TEMP_DB = " + TEMP_DB);

                        Writer writer = new OutputStreamWriter(socket.getOutputStream());
                        writer.write(message);
                        writer.flush();
                        socket.close();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                };

                Thread thread = new Thread(runnable);
                thread.start();
            }
        }
    }

    private String makeResponse(Request request) throws IOException {
        HttpMethod httpMethod = request.startLine().httpMethod();

        if (HttpMethod.GET == httpMethod) {
            return getMessage(OK.code, OK.message);
        }

        if (HttpMethod.POST == httpMethod) {
            Set<Long> longs = TEMP_DB.keySet();
            long max = longs.stream()
                .mapToLong(Long::longValue)
                .max()
                .orElseGet(() -> 0);
            System.out.println("max = " + max);
            max += 1;
            String body = request.body();
            if (body == null) {
                return getMessage(BAD_REQUEST.code, BAD_REQUEST.message);
            }
            body = body.replaceAll("\"", "");
            TEMP_DB.put(max, body);

            return getMessage(CREATE.code, CREATE.message);
        }
        if (HttpMethod.PATCH == httpMethod) {
            String path = request.startLine().requestUriPath();
            Long id = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
            String oldValue = TEMP_DB.get(id);
            if (oldValue == null) {
                return getMessage(NOT_FOUND.code, NOT_FOUND.message);
            }

            String body = request.body();
            if (body == null) {
                return getMessage(BAD_REQUEST.code, BAD_REQUEST.message);
            }
            body = body.replaceAll("\"", "");
            TEMP_DB.put(id, body);

            return getMessage(OK.code, OK.message);
        }

        if (HttpMethod.DELETE == httpMethod) {
            String path = request.startLine().requestUriPath();
            Long id = Long.parseLong(path.substring(path.lastIndexOf("/") + 1));
            String value = TEMP_DB.get(id);
            System.out.println("value = " + value);

            if (value == null) {
                return getMessage(NOT_FOUND.code, NOT_FOUND.message);
            }
            TEMP_DB.remove(id);

            return getMessage(OK.code, OK.message);
        }

        return "";
    }


    private static String getMessage(int statusCode, String statusMessage) {
        String body = new Gson().toJson(TEMP_DB);

        byte[] bodyBytes = body.getBytes();
        String message = "HTTP/1.1 " + statusCode + " " + statusMessage + " " + "\n" +
            "Host: localhost:8080\n" +
            "Content-Length: " + bodyBytes.length + "\n" +
            "Content-Type: application/json; charset=UTF-8\n" +
            "\n" +
            body;
        return message;
    }

    private Request parseRequest(String request) {
        System.out.println(request);
        int index = request.indexOf(System.lineSeparator());
        String requestFirstLine = request.substring(0, index);
        request = request.replace(requestFirstLine + "\n", "");
        StringTokenizer st = new StringTokenizer(requestFirstLine);
        String httpMethod = st.nextToken();
        HttpMethod method = HttpMethod.valueOf(httpMethod);

        String requestUriPath = st.nextToken();
        String httpVersion = st.nextToken();
        StartLine startLine = new StartLine(method, httpVersion, requestUriPath);

        String header = "";
        Map<String, String> headers = new HashMap<>();
        while (!request.equals("")) {
            int i = request.indexOf(System.lineSeparator());
            header = request.substring(0, i);
            if (header.equals("\r")) {
                break;
            }

            String key = header.substring(0, header.indexOf(":"));
            String value = header.substring(header.indexOf(":"), header.length() - 1);

            headers.put(key, value);
            request = request.replace(header + "\n", "");
        }

        String body = getBody(request);
        return new Request(startLine, headers, body);
    }

    private String getBody(String request) {
        if (!request.isBlank()) {
            JsonElement jsonElement = JsonParser.parseString(request);
            JsonObject asJsonObject = jsonElement.getAsJsonObject();
            JsonElement task = asJsonObject.get("task");
            return task.toString();
        }

        return null;
    }

    private String getRequest(Socket socket) throws IOException {
        Reader reader = new InputStreamReader(new BufferedInputStream(socket.getInputStream()));

        CharBuffer buffer = CharBuffer.allocate(1_000_000);
        reader.read(buffer);
        buffer.flip();

        return buffer.toString();
    }

}
