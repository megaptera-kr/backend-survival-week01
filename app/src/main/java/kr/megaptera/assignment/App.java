package kr.megaptera.assignment;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class App {

    public static void main(String[] args) throws IOException {
        App app = new App();
        app.run();
    }


    private void run() throws IOException {
        int port = 8080;

        Map<Long, String> tasks = new HashMap<>();

        // TODO: 요구사항에 맞게 과제를 진행해주세요.

        // 1. Listen

        ServerSocket listener = new ServerSocket(port, 0);

        while (true) {

            // 2. Accept

            Socket socket = listener.accept();


            // 3. Request

            Reader reader = new InputStreamReader(socket.getInputStream());

            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);

            reader.read(charBuffer);

            charBuffer.flip();

            String req = charBuffer.toString();

            Gson gson = new Gson();
            Map map = new HashMap();
            int startIndexOfBody = req.indexOf("{");
            int lastIndexOfBody = req.lastIndexOf("}");

            if (startIndexOfBody != -1 || lastIndexOfBody != -1) {
                map = (Map) gson.fromJson(req.substring(startIndexOfBody, lastIndexOfBody + 1), map.getClass());

            }


            String req2 = req.split("\n")[0];

            String[] bs = req2.split(" ");

            String method = "";
            String path = "";
            for (int i = 0; i < bs.length; i++) {
                if (i == 0) {
                    method = bs[i];
                }
                if (i == 1) {
                    path = bs[i];
                }
            }


            String path1 = "";
            String path2 = "";

            String[] bs2 = path.split("/");
            ;

            for (int i = 0; i < bs2.length; i++) {
                if (i == 1) {
                    path1 = bs2[i];
                }
                if (i == 2) {
                    path2 = bs2[i];
                }
            }


            String message = "";


            if (path1.equals("tasks")) {

                Long longPath = 0L;
                if (!path2.equals("")) {
                    longPath = Long.parseLong(path2);
                }

                if (method.equals("GET")) {
                    // 4.Response

                    String body = gson.toJson(tasks);

                    byte[] bytes = body.getBytes();

                    message = "" +
                            "HTTP/1.1 200 OK\n" +
                            "Content-Type: application/json; charset=UTF-8\n" +
                            "Content-Length: " + bytes.length + "\n" +
                            "Host: localhost:" + port + "\n" +
                            "\n" +
                            body;


                } else if (method.equals("POST")) {
                    // POST 에 바디에 task에 글을넣어서 보내주면 해쉬맵 tasks에 추가해줌
                    // 근데 바디에 task가 없으면 400 Bad Requst 리스폰스해줌

                    if (map.get("task") == null) {
                        message = "" +
                                "HTTP/1.1 400 Bad Request\n" +
                                "Content-Type: application/json; charset=UTF-8\n" +
                                "Content-Length: 0\n" +
                                "Host: localhost:" + port + "\n" +
                                "\n";


                    } else {

                        String getTask = (String) map.get("task");
                        if (tasks.isEmpty()) {
                            tasks.put(1L, getTask);
                        } else {
                            List<Long> keyList = new ArrayList<>(tasks.keySet());
                            keyList.sort((s1, s2) -> s1.compareTo(s2));
                            Long newKey = keyList.get(keyList.size() - 1);
                            tasks.put(newKey + 1, getTask);
                        }

                        String body = gson.toJson(tasks);

                        byte[] bytes = body.getBytes();

                        message = "" +
                                "HTTP/1.1 201 Created\n" +
                                "Content-Type: application/json; charset=UTF-8\n" +
                                "Content-Length: " + bytes.length + "\n" +
                                "Host: localhost:" + port + "\n" +
                                "\n" +
                                body;

                    }


                } else if (method.equals("PATCH")) {

                    // PATCH에 task가 없으면 400
                    // PATCH에 tasks에 없는 key값이 들어오면 404

                    String getTask = (String) map.get("task");


                    if (getTask == null) {
                        message = "" +
                                "HTTP/1.1 400 Bad Request\n" +
                                "Content-Type: application/json; charset=UTF-8\n" +
                                "Content-Length: 0\n" +
                                "Host: localhost:" + port + "\n" +
                                "\n";
                    } else if (path2.equals("") || !tasks.containsKey(longPath)) {

                        message = "" +
                                "HTTP/1.1 404 Not Found\n" +
                                "Content-Type: application/json; charset=UTF-8\n" +
                                "Content-Length: 0\n" +
                                "Host: localhost:" + port + "\n" +
                                "\n";
                    } else {
                        tasks.put(longPath, getTask);

                        String body = gson.toJson(tasks);

                        byte[] bytes = body.getBytes();

                        message = "" +
                                "HTTP/1.1 200 OK\n" +
                                "Content-Type: application/json; charset=UTF-8\n" +
                                "Content-Length: " + bytes.length + "\n" +
                                "Host: localhost:" + port + "\n" +
                                "\n" +
                                body;
                    }


                } else if (method.equals("DELETE")) {
                    // DELETE tasks에 없는 key값이 들어오면 404

                    if (path2.equals("") || !tasks.containsKey(longPath)) {

                        message = "" +
                                "HTTP/1.1 404 Not Found\n" +
                                "Content-Type: application/json; charset=UTF-8\n" +
                                "Content-Length: 0\n" +
                                "Host: localhost:" + port + "\n" +
                                "\n";
                    } else {
                        tasks.remove(longPath);

                        String body = gson.toJson(tasks);

                        byte[] bytes = body.getBytes();

                        message = "" +
                                "HTTP/1.1 200 OK\n" +
                                "Content-Type: application/json; charset=UTF-8\n" +
                                "Content-Length: " + bytes.length + "\n" +
                                "Host: localhost:" + port + "\n" +
                                "\n" +
                                body;
                    }


                }

                Writer writer = new OutputStreamWriter(socket.getOutputStream());
                writer.write(message);
                writer.flush();

            }


            // 5. Close
            socket.close();
        }
    }

}
