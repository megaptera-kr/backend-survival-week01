package kr.megaptera.assignment.requestType;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

import static kr.megaptera.assignment.requestType.Method.MethodType.valueOf;

public abstract class Method {
    public abstract String process(Map<Long, String> tasks);
    public static String requestString;

    public enum MethodType {
        GET, POST, PATCH, DELETE
    }
    public static Method MethodFactory(String request) {
        requestString = request;
        switch (valueOf(request.split(" ")[0])) {
            case GET -> {
                return new GetMethod();
            }
            case POST -> {
                return new PostMethod();
            }
            case PATCH -> {
                return new PatchMethod();
            }
            case DELETE -> {
                return new DeleteMethod();
            }
        }
        throw new RuntimeException("Your request is unsupported method. we accept only GET, POST, PATCH, DELETE.");
    }

    public String generateMessage(String body, String statusCode) {
        byte[] bytes = body.getBytes();
        String message = "" +
                "HTTP/1.1 " + statusCode + "\n" +
                "Content-Type: application/json; charset=UTF-8\n" +
                "Content-Length: " + bytes.length + "\n" +
                "\n" +
                body;

        return message;
    }

    public String parsePayload(String request, String value) {
        String[] lines = request.split("\n");
        String lastLine = lines[lines.length - 1];

        try {
            JsonElement jsonElement = JsonParser.parseString(lastLine);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            return jsonObject.get(value).getAsString();
        } catch (IllegalStateException e) {
            System.out.println("IllegalStateException occurred");
            return "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public Long parseTaskId(String method) {
        System.out.println(method);
        String[] parts = method.split("/");

        return Long.parseLong(parts[2].trim());
    }

    public String getRequestMethod(String request) {
        return request.substring(0, request.indexOf("HTTP"));
    }

}
