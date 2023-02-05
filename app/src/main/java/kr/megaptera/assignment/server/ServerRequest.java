package kr.megaptera.assignment.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

public class ServerRequest {

    public CharBuffer readRequest(Socket socket) throws IOException {
        InputStream inputStream = socket.getInputStream();
        Reader reader = new InputStreamReader(inputStream);

        CharBuffer buffer = CharBuffer.allocate(1_000_000);

        reader.read(buffer);
        buffer.flip();
        System.out.println("buffer.toString() = " + buffer.toString());
        return buffer;
    }

    public Map<String, String> parseElement(CharBuffer buffer) {
        String requestMethod;
        String path1;
        String path2;
        String host;
        String requestBody;
        Map<String, String> Elements = new HashMap<>();

        String[] splitBuffer = buffer.toString().split("/");
        String[] splitBuffer2 = buffer.toString().split("\\n");

        // 1. requestMethod
        requestMethod = splitBuffer[0].trim();
        Elements.put("method", requestMethod);

        // 2. path1
        path1 = splitBuffer[1].substring(0, 5);
        Elements.put("path1", path1);

        // 3. path2
        path2 = splitBuffer[2].toUpperCase().replaceAll("[^0-9]", "");
        Elements.put("path2", path2);

        // 4. host
        host = splitBuffer2[1].substring(5).trim();
        Elements.put("host", host);

        // 5. requestBody
        if (!requestMethod.equals("GET")) {
            requestBody = splitBuffer2[splitBuffer2.length - 1].trim();
            Elements.put("body", requestBody);
        }

        return Elements;
    }
}
