package kr.megaptera.assignment;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpRequest {

    private final Socket socket;
    private final String request;
    private final String method;
    private final String path;
    private final Map<String, String> headers;
    private final String body;

    public HttpRequest(Socket socket) throws IOException {
        this.socket = socket;
        this.request = readRequest();
        this.method = getMethod();
        this.path = getPath();
        this.headers = getHeaders();
        this.body = getBody();
    }

    private String readRequest() throws IOException {
        Reader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();
        return charBuffer.toString();
    }

    private String getMethod() {
        return request.split("\r\n")[0].split(" ")[0];
    }

    private String getPath() {
        return request.split("\r\n")[0].split(" ")[1];
    }

    private Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        String[] requestLines = request.split("\r\n");
        for (int i = 1; i < requestLines.length; i++) {
            if (requestLines[i].equals("")) {
                break;
            }
            String[] headerParts = requestLines[i].split(": ");
            headers.put(headerParts[0], headerParts[1]);
        }
        return headers;
    }

    private String getBody() {
        return request.substring(request.lastIndexOf("\r\n") + 2);
    }

    public boolean isBodyNotEmpty() {
        return body != null && !body.trim().isEmpty();
    }

    public long getTaskIdFromPath() {
        String[] pathParts = path.split("/tasks/");
        return Long.parseLong(pathParts[1]);
    }

    public boolean isMethodAllowed() {
        List<String> allowedMethods = Arrays.asList("GET", "POST", "PATCH", "DELETE");
        return allowedMethods.stream().anyMatch(allowedMethod -> allowedMethod.equalsIgnoreCase(method));
    }

    public boolean isPostMethod() {
        return method.equals("POST");
    }

    public boolean isGetMethod() {
        return method.equals("GET");
    }

    public boolean isPatchMethod() {
        return method.equals("PATCH");
    }

    public boolean isDeleteMethod() {
        return method.equals("DELETE");
    }

    public boolean isPathEquals(String targetPath) {
        return path.equals(targetPath);
    }

    public boolean isPathStartsWith(String targetPath) {
        return path.startsWith(targetPath);
    }

    public String getTaskFromBody() {
        JsonElement jsonElement = JsonParser.parseString(body);
        return jsonElement.getAsJsonObject().get("task").getAsString();
    }
}
