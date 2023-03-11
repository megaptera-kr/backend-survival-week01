package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }

    private void run() throws IOException {
        int port = 8080;

        Map<Integer, String> tasks = new HashMap<>();
        Map<Integer, String> statusCode = new HashMap();
        statusCode.put(200, "OK");
        statusCode.put(201, "Created");
        statusCode.put(400, "Bad request");
        statusCode.put(404, "Not Found");

        int key = 1;

        // 1. Listen
        ServerSocket listener = new ServerSocket(port, 0);


        while(true)
        {
            // 2. Accept
            Socket socket = listener.accept();

            // 3. Request
            Reader reader = new InputStreamReader(socket.getInputStream());
            CharBuffer cbuff = CharBuffer.allocate(1_000_000);
            reader.read(cbuff);
            cbuff.flip();
            String request = cbuff.toString();

            // request header parsing

            String pattern = "^([A-Z]+)\\s(.+?)\\s.*\\r?\\n((.|\\n|\\r)*)Host:\\s(.+)((.|\\n|\\r)*)\\r?\\n\\r?\\n((.|\\n|\\r)*)$";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(request);
            m.find();

            String method = m.group(1);
            String path = m.group(2);
            String beforeSlash = "";
            String afterSlash = "";
            int last = path.lastIndexOf("/");
            if (last == 0)
            {
                beforeSlash = path;
            }
            else
            {
                beforeSlash = path.substring(0, last);
                afterSlash = path.substring(last + 1);
            }
            String host = m.group(5);
            String body = m.group(8);

            int code = 0;
            String status = "";
            JsonElement jsonbody = JsonParser.parseString(body);
            String resbody = "";

            // if - else for each method

                if (method.equals("GET"))
                {
                    code = 200;
                    status = statusCode.get(200);
                    resbody = new Gson().toJson(tasks);
                }

                else if (method.equals("POST"))
                {
                    if (body.isEmpty())
                    {
                        code = 400;
                        status = statusCode.get(400);
                    }
                    else
                    {
                        code = 201;
                        status = statusCode.get(201);

                        String value = jsonbody.getAsJsonObject().get("task").toString();
                        String item = value.substring(1, value.length() - 1);
                        tasks.put(key, item);
                        key++;
                        resbody = new Gson().toJson(tasks);
                    }
                }

                else if (method.equals("PATCH"))
                {
                    if (body.isEmpty())
                    {
                        code = 400;
                        status = statusCode.get(400);
                    }
                    else if (afterSlash == null || tasks.containsKey(Integer.valueOf(afterSlash)) == false)
                    {
                        code = 404;
                        status = statusCode.get(404);
                    }
                    else
                    {
                        code = 200;
                        status = statusCode.get(200);

                        String value = jsonbody.getAsJsonObject().get("task").toString();
                        String item = value.substring(1, value.length() - 1);
                        tasks.replace(Integer.valueOf(afterSlash), item);
                        resbody = new Gson().toJson(tasks);
                    }
                }

                else if (method.equals("DELETE"))
                {
                    if (afterSlash == null || tasks.containsKey(Integer.valueOf(afterSlash)) == false)
                    {
                        code = 404;
                        status = statusCode.get(404);
                    }
                    else
                    {
                        code = 200;
                        status = statusCode.get(200);

                        tasks.remove(Integer.valueOf(afterSlash));
                        resbody = new Gson().toJson(tasks);
                    }
                }


           // 4. Response


            byte[] bytes = resbody.getBytes();
            String message = "" +
                    "HTTP/1.1 " + code + " " + status + "\n" +
                    "Content-Length: " + bytes.length + "\n" +
                    "Content-Type: text/html; charset=UTF-8\n" +
                    "Host: " + host + "\n" +
                    "\n" +
                    resbody;

            Writer writer = new OutputStreamWriter(socket.getOutputStream());
            writer.write(message);
            writer.flush();

            socket.close();
        }


    }






}



