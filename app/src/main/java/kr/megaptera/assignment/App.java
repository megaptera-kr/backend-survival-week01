package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class App {

    Long id = 1L;   //insert id

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // 1. Listen
        try(ServerSocket listener = new ServerSocket(port)){

            System.out.println("Listen!");

            while (true) {
                // 2. Accept
                Socket socket = listener.accept();

                System.out.println("Accept!");

                // 3. Request
                Reader reader = new InputStreamReader(socket.getInputStream());

                CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
                reader.read(charBuffer);

                charBuffer.flip();
                String req = charBuffer.toString();
                System.out.println(req);

                // 4. Response
                String method = getMethod(req);
                System.out.println("getMethod :" + method);

                String message = getMessage(req, method, tasks);

                Writer writer = new OutputStreamWriter(socket.getOutputStream());
                writer.write(message);
                writer.flush();
            }
        }catch (IOException e){
            System.out.println(e);
        }
    }

    private String getMessage(String req, String method, Map<Long, String> tasks) {
        String resBody = "";
        String resMessage = "";
        String reqBody = "";

        if(method.equals("GET")){
            resBody = new Gson().toJson(tasks);
            resMessage = createMessage(resBody, "200 OK");
            return resMessage;
        }
        if(method.equals("POST")){
            reqBody = getReqBody(req);
            System.out.println("POST-reqBody: "+ reqBody);

            if(reqBody.equals("")){
                resMessage = createMessage(resBody, "400 Bad Request");
            }else {
                // id 1증가
                tasks.put(id++, reqBody);
                resBody = new Gson().toJson(tasks);
                resMessage = createMessage(resBody, "201 Created");
            }
            return resMessage;
        }
        if(method.equals("PATCH")){
            Long targetId = Long.parseLong(getId(req));
            System.out.println("PATCH-targetId: "+ targetId);

            if(!tasks.containsKey(targetId)){
                resMessage = createMessage(resBody, "404 Not Found");
            }else{
                reqBody = getReqBody(req);
                if(reqBody.equals("")){
                    resMessage = createMessage(resBody,"400 Bad Request");
                }else {
                    tasks.put(targetId, reqBody);
                    resBody = new Gson().toJson(tasks);
                    resMessage = createMessage(resBody, "200 OK");
                }
            }
            return resMessage;
        }
        if(method.equals("DELETE")){
            Long targetId = Long.parseLong(getId(req));
            System.out.println("DELETE-targetId: "+ targetId);

            if(!tasks.containsKey(targetId)){
                resMessage = createMessage(resBody, "404 Not Found");
            }else {
                tasks.remove(targetId);
                resBody = new Gson().toJson(tasks);
                resMessage = createMessage(resBody, "200 OK");
            }
            return resMessage;
        }

        return "defaultMessage";
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
            System.out.println("reqBody : "+ reqBody);
            JsonElement jsonElement = JsonParser.parseString(reqBody);  //com.google.gson.JsonSyntaxException
            JsonObject jsonObject = jsonElement.getAsJsonObject();

            return  jsonObject.get("task").getAsString();
        }
    }

    private String createMessage(String resBody, String status) {
        return "" +
                "HTTP/1.1 " + status +"\n"+
                "Content-Length: " + resBody.getBytes().length + "\n"+
                "Content-Type : application/json; charset=UTF-8\n"+
                "Host : localhost:8080\n"+
                "\n"+
                resBody;
    }

    private String getMethod(String req) {
        if(req.startsWith("GET")){
            return "GET";
        } else if (req.startsWith("POST")){
            return "POST";
        } else if (req.startsWith("PATCH")) {
            return  "PATCH";
        } else if (req.startsWith("DELETE")){
            return "DELETE";
        } else {
            return "none";
        }
    }

}
