package kr.megaptera.assignment;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class App {

    private static final String PATH = "/tasks";
    private static final int port = 8080;

    public static void main(String[] args){
        App app = new App();
        app.run();
    }

    private void run(){
        // todo: ServerSocket 객체로 교체
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext(PATH, new TaskHandler());

            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
