package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;
        Map<Long, String> tasks = new HashMap<>();

        ServerSocket listener = new ServerSocket(port, 0);

        while(true) {
            Socket socket = listener.accept();
            CharBuffer charBuffer = accept(socket);

            Map<String, String> request = readHttpMessage(charBuffer);

            String httpMethod = request.get("httpMethod");
            String path = request.get("path");
            String requestBody = request.get("requestBody");

            Map<Long, String> responseBody = null;

            if(httpMethod.equals("GET")){
                get(path,tasks,socket);
            }

            if(httpMethod.equals("POST")){
                post(path,tasks,requestBody,socket);
            }

            if(httpMethod.equals("PATCH")){
                patch(path,tasks, requestBody, socket);
            }

            if(httpMethod.equals("DELETE")){
                delete(path,tasks,socket);
            }

        }
    }

    private Map<String, String> readHttpMessage(CharBuffer charBuffer) {
        Map<String, String> request = new HashMap<>();

        String httpMessage = charBuffer.toString();
        String[] httpMessageByLine = httpMessage.split("\n");
        String[] httpMessageFirstLine = httpMessageByLine[0].split(" ");

        String httpMethod = httpMessageFirstLine[0];
        String path = httpMessageFirstLine[1];
        String requestBody = "";

        for(int i = 0; i < httpMessageByLine.length; i++){
            if(httpMessageByLine[i].length() == 1) {
                requestBody = String.join("", Arrays.copyOfRange(httpMessageByLine, i + 1, httpMessageByLine.length));
            }

            if(requestBody.indexOf(":") != -1){
                requestBody = requestBody.split(":")[1].trim();
                requestBody = requestBody.substring(1, requestBody.length() - 2);
            }
        }

        request.put("httpMethod", httpMethod);
        request.put("path", path);
        request.put("requestBody", requestBody);

        return request;
    }

    private CharBuffer accept(Socket socket) throws IOException {;
        Reader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        reader.read(charBuffer);
        charBuffer.flip();

        return charBuffer;
    }

    private void get(String path, Map<Long, String> tasks, Socket socket) throws IOException {
        if(path.equals("/tasks")){
            send200(tasks, socket);
        }
    }

    private void post(String path, Map<Long, String> tasks, String requestBody, Socket socket) throws IOException {

        if(requestBody.isEmpty()){
            send400(socket);
            return;
        }

        if(path.equals("/tasks")){
            long id = tasks.size() + 1;
            tasks.put(id, requestBody);
            send201(tasks, socket);
        }
    }

    private void patch(String path, Map<Long, String> tasks, String requestBody, Socket socket) throws IOException {
        Long id = Long.parseLong(path.split("tasks/")[1],10);

        if(requestBody.isEmpty()){
            send400(socket);
            return;
        }

        if(tasks.get(id) == null){
            send404(socket);
            return;
        }

        tasks.put(id, requestBody);
        send200(tasks, socket);
    }

    private void delete(String path, Map<Long, String> tasks, Socket socket) throws IOException {
        Long id = Long.parseLong(path.split("tasks/")[1],10);

        if(tasks.get(id) == null){
            send404(socket);
            return;
        }

        tasks.remove(id);
        send200(tasks, socket);
    }

    private void send200(Map<Long, String> responseBody, Socket socket) throws IOException {
        String json = new Gson().toJson(responseBody);
        int length = json.length();

        String message = """
                    HTTP/1.1 200 OK
                    Content-Type: application/json; charset=UTF-8
                    Content-Length: """ + length + "\n" +
                "\n" + json;

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

    private void send201(Map<Long, String> responseBody, Socket socket) throws IOException {
        String json = new Gson().toJson(responseBody);
        int length = json.length();

        String message = """
                    HTTP/1.1 201 Created
                    Content-Type: application/json; charset=UTF-8
                    Content-Length: """ + length + "\n" +
                "\n" + json;

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

    private void send400(Socket socket) throws IOException {
        String message = """
                    HTTP/1.1 400 Bad Request
                    Content-Type: application/json; charset=UTF-8
                    Content-Length: 0""" + "\n" +
                "\n";

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

    private void send404(Socket socket) throws IOException{
        String message = """
                    HTTP/1.1 404 Not Found
                    Content-Type: application/json; charset=UTF-8
                    Content-Length: 0""" + "\n" +
                "\n";

        Writer writer = new OutputStreamWriter(socket.getOutputStream());
        writer.write(message);
        writer.flush();
    }

}
