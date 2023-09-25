package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {
    Long id = 1L;
    Map<Long, String> tasks = new HashMap<>();

    public static Long taskId = 0L;

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run(){
        int port = 8080;

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        try(ServerSocket listener = new ServerSocket(port);){
            while (true) {
                Socket socket = listener.accept();
                // 2. Accept
                System.out.println("accept!!");


                // 3. Request
                String req = getRequest(socket);
                System.out.println(req);

                //4. response
                String method = getMethod(req);
                System.out.println("Request Method : " + method);
                String message = getMessage(req, method, tasks);
                sendResponse(socket, message);
            }
        }catch (IOException e){
            System.out.println(e);
        }
    }

    private void sendResponse(Socket socket, String message) {
        try {
            OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();
            writer.close();
        }catch (IOException e){
            System.out.println(e);
        }

    }

    private String createMessage(String body, String status){
        return "" +
            "HTTP/1.1 " + status + "\n" +
            "Content-Length: " + body.getBytes().length + "\n" +
            "Content-type: application/json; charset=UTF-8\n" +
            "Host: localhost:8080\n" +
            "\n" + body;
    }

    private String getMessage(String req, String method, Map<Long, String> tasks) {
        String message = "";
        String reqBody = "";
        String resBody = "";
        if(method.equals("GET")){
            resBody = new Gson().toJson(tasks);
            message = createMessage(resBody,"200 OK");
        }else if(method.equals("POST")){
            reqBody = getReqBody(req);
            if(reqBody.equals("")){
                message = createMessage(resBody,"400 Bad Request");
            }else{
                tasks.put(id++,reqBody);
                resBody = new Gson().toJson(tasks);
                message = createMessage(resBody,"201 Created");
            }
        }else if(method.equals("PATCH")){
            Long targetId = Long.parseLong(getId(req));
            if(!tasks.containsKey(targetId)){
                message = createMessage(resBody,"404 Not Found");
            }else{
                reqBody = getReqBody(req);
                if(reqBody.equals("")){
                    message = createMessage(resBody,"400 Bad Request");
                }else{
                    tasks.put(targetId,reqBody);
                    resBody = new Gson().toJson(tasks);
                    message = createMessage(resBody,"200 OK");
                }
            }
        }else if(method.equals("DELETE")){
            Long targetId = Long.parseLong(getId(req));
            if(!tasks.containsKey(targetId)){
                message = createMessage(resBody,"404 Not Found");
            }else{
                tasks.remove(targetId);
                resBody = new Gson().toJson(tasks);
                message = createMessage(resBody,"200 OK");
            }
        }

        return message;
    }

    private String getId(String req) {
        String[] strArr = req.split("\n")[0].split(" ")[1].split("/");
        return strArr[strArr.length-1];
    }

    private String getReqBody(String req) {
        String[] strArr = req.split("\n");
        String reqBody = strArr[strArr.length -1];

        if(reqBody.trim().equals("")){
            return "";
        }else{
            JsonElement jsonElement = JsonParser.parseString(reqBody);
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            return  jsonObject.get("task").getAsString();
        }
    }


    private String getMethod(String req) {
        if(req.startsWith("GET")){
            return "GET";
        }else if(req.startsWith("POST")){
            return "POST";
        }else if(req.startsWith("PATCH")){
            return "PATCH";
        }else {
            return "DELETE";
        }
    }

    private String getRequest(Socket socket) throws IOException {
        InputStreamReader reader = new InputStreamReader(socket.getInputStream());
        CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
        int size = reader.read(charBuffer);
        charBuffer.flip();

        return charBuffer.toString();
    }
}
