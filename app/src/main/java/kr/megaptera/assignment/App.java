package kr.megaptera.assignment;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class App {

    public static void main(String[] args) throws IOException{
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();
        int sequence = 1;
        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen
        ServerSocket listener = new ServerSocket(port, 0);
        while (true) {
            System.out.println("---listen---");
            // 2. Accept
            Socket socket = listener.accept();
            System.out.println("accept");

            // 3. Request
            InputStreamReader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            reader.read(charBuffer);
            charBuffer.flip();
            System.out.println(charBuffer.toString());

            String[] requestParam = charBuffer.toString().split("\n");

            String resultCode = "200";
            String resultMsg = "OK";
            String body = "";
            String bodyData = "";
            //if(requestParam.length != 1) { //postman 오류 임시처리
            if(true) { //postman 오류 임시처리
                String requestMethod = requestParam[0].split(" ")[0].toUpperCase();
                String URIPath = requestParam[0].split(" ")[1];
                String[] pathSplit = URIPath.split("/");
                String paramId = "";
                System.out.println("request header:"+requestMethod+" "+URIPath);
                //URI ID값 파싱
                try {
                    paramId = pathSplit[2];
                }catch (Exception e){
                }

                //body data 파싱
                boolean bodyDataExist = false;
                for (int i = 0; i < requestParam.length; i++) {
                    if (requestParam[i].indexOf("{") > -1) {
                        bodyData += requestParam[i];
                        bodyDataExist = true;
                    } else if (bodyDataExist == true) {
                        bodyData += requestParam[i];
                    } else if (requestParam[i].indexOf("}") > -1) {
                        bodyData += "}";
                        break;
                    }
                }

                Gson gson = new Gson();
                switch (requestMethod) {
                    case "GET":
                        body = gson.toJson(tasks);
                        break;

                    case "POST":
                        if(bodyData.equals("")){
                            resultCode = "400";
                            resultMsg = "Bad Request";
                            break;
                        }else{
                            resultCode = "201";
                            resultMsg = "Created";
                        }
                        HashMap<String, String> hashMaps = gson.fromJson(bodyData, new HashMap<String, String>().getClass());
                        for (Object o: hashMaps.keySet()) {
                            Long k = (long)sequence++;
                            String put = tasks.put(k, hashMaps.get(o));

                        }

                        body = gson.toJson(tasks);

                        break;

                    case "PATCH":
                        if(bodyData.equals("")){
                            resultCode = "400";
                            resultMsg = "Bad Request";
                            break;
                        }
                        try {
                            if (!paramId.equals("")) {
                                String value = tasks.get(Long.parseLong(paramId));
                                if (value != null) {
                                    HashMap<String, String> hashMaps1 = gson.fromJson(bodyData, new HashMap<String, String>().getClass());
                                    for (Object o: hashMaps1.keySet()) {
                                        String put = tasks.put(Long.parseLong(paramId), hashMaps1.get(o));

                                    }

                                    body = gson.toJson(tasks);
                                }else{
                                    throw new Exception();
                                }
                            } else {
                                throw new Exception();
                            }
                        }catch (Exception e){
                            resultCode = "404";
                            resultMsg = "Not Found";
                        }
                        break;
                    case "DELETE":
                        try {
                            if (!paramId.equals("")) {
                                String value = tasks.get(Long.parseLong(paramId));
                                if (value != null) {
                                    tasks.remove(Long.parseLong(paramId));
                                    body = gson.toJson(tasks);
                                }else{
                                    throw new Exception();
                                }
                            } else {
                                throw new Exception();
                            }
                        }catch (Exception e){
                            resultCode = "404";
                            resultMsg = "Not Found";
                        }
                        break;
                }
            }

            // 4. Response

            byte[] bytes = body.getBytes();
            String responseMessage = null;

            responseMessage =
                    "HTTP/1.1 " + resultCode + " " + resultMsg + "\n"+
                            "Content-Length: " + bytes.length + " \n"+
                            "Content-Type: application/json; charset=UTF-8\n" +
                            "\n";
            if(body != null){
                responseMessage+= body +"\n\n";
            }



            System.out.println(responseMessage);
            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(responseMessage);
            writer.flush();

            System.out.println("message: ");
            System.out.println(responseMessage);
            System.out.println("----close----");
            //close
            socket.close();
        }

    }

}

