package kr.megaptera.assignment;

import kr.megaptera.assignment.factories.HttpRequestSourceFactory;
import kr.megaptera.assignment.factories.TodoItemJsonFactory;
import kr.megaptera.assignment.managers.HttpPathBindingManager;
import kr.megaptera.assignment.managers.TodoItemManager;
import kr.megaptera.assignment.models.HttpMethodType;
import kr.megaptera.assignment.models.HttpPath;
import kr.megaptera.assignment.models.HttpPathType;
import kr.megaptera.assignment.models.HttpResponseSource;

import java.io.*;
import java.net.ServerSocket;
import java.nio.CharBuffer;

public class App {

    public static void main(String[] args){
        App app = new App();

        try{
            app.run();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void run() throws Exception {
        var pathBindingManager = new HttpPathBindingManager();
        var todoItemManager = new TodoItemManager();
        var todoItemJsonFactory = new TodoItemJsonFactory();

        pathBindingManager.Put(new HttpPath(
                HttpMethodType.Get,
                "/tasks",
                HttpPathType.Normal,
                requestSource -> {
                    var todoItems = todoItemManager.getAll();
                    var body = todoItemJsonFactory.Create(todoItems);

                    var responseSource = new HttpResponseSource();
                    responseSource.setStatusCode(200);
                    responseSource.setStatusMessage("OK");
                    responseSource.setBody(body);

                    return responseSource;
                }));

        pathBindingManager.Put(new HttpPath(
                HttpMethodType.Post,
                "/tasks",
                HttpPathType.Normal,
                requestSource -> {
                    var parameters = requestSource.getStartLine().getParameters();
                    var task = parameters.get("task");

                    todoItemManager.add(task);

                    var todoItems = todoItemManager.getAll();
                    var body = todoItemJsonFactory.Create(todoItems);

                    var responseSource = new HttpResponseSource();
                    responseSource.setStatusCode(201);
                    responseSource.setStatusMessage("Created");
                    responseSource.setBody(body);

                    return new HttpResponseSource();
                }));

        pathBindingManager.Put(new HttpPath(
                HttpMethodType.Patch,
                "/tasks",
                HttpPathType.HasValue,
                requestSource -> {
                    var path = requestSource.getStartLine().getPath();
                    var lastParameter = path.lastIndexOf('/');
                    var idString = path.substring(lastParameter, path.length());
                    var id = Integer.parseInt(idString);

                    var parameters = requestSource.getStartLine().getParameters();
                    var task = parameters.get("task");

                    var responseSource = new HttpResponseSource();

                    var isUpdated = todoItemManager.update(id, task);
                    if(!isUpdated){
                        responseSource.setStatusCode(404);
                        responseSource.setStatusMessage("Not Found");
                        return responseSource;
                    }

                    var todoItems = todoItemManager.getAll();
                    var body = todoItemJsonFactory.Create(todoItems);

                    responseSource.setStatusCode(200);
                    responseSource.setStatusMessage("OK");
                    responseSource.setBody(body);

                    return responseSource;
                }));

        pathBindingManager.Put(new HttpPath(
                HttpMethodType.Delete,
                "/tasks",
                HttpPathType.HasValue,
                requestSource -> {
                    var path = requestSource.getStartLine().getPath();
                    var lastParameter = path.lastIndexOf('/');
                    var idString = path.substring(lastParameter, path.length());
                    var id = Integer.parseInt(idString);

                    var responseSource = new HttpResponseSource();

                    var isSuccessRemove = todoItemManager.remove(id);
                    if(!isSuccessRemove){
                        responseSource.setStatusCode(404);
                        responseSource.setStatusMessage("Not Found");
                        return responseSource;
                    }

                    var todoItems = todoItemManager.getAll();
                    var body = todoItemJsonFactory.Create(todoItems);

                    responseSource.setStatusCode(200);
                    responseSource.setStatusMessage("OK");
                    responseSource.setBody(body);

                    return responseSource;
                }));


        int port = 8080;
        int backlog = 0;
        ServerSocket serverSocket = new ServerSocket(port, backlog);

        while (true) {
            System.out.println("Listen");

            var clientSocket = serverSocket.accept();
            System.out.println("Accept");

            Reader clientReader = new InputStreamReader(clientSocket.getInputStream());
            CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
            clientReader.read(charBuffer);

            charBuffer.flip();

            String requestMessage = charBuffer.toString();

            var httpRequestSourceFactory = new HttpRequestSourceFactory();
            var requestSource = httpRequestSourceFactory.Create(requestMessage);
            var firstLine = requestSource.getStartLine();

            var action = pathBindingManager.Get(firstLine.getHttpMethodType(), firstLine.getPath());
            if (action == null) {
                // TODO : (dh) Not Found..
            }

            var responseSource = action.execute(requestSource);

            System.out.println("Request done");

            String body = requestSource.getBody();
            byte[] bytes = body.getBytes();
            String responseMessage = "" +
                    "HTTP/1.1 " + responseSource.getStatusCode() + " " + responseSource.getStatusMessage() + "\r\n" +
                    "Content-Type: application/json; charset=UTF-8\r\n" +
                    "Content-Length: " + bytes.length + "\r\n" +
                    "\r\n";

             if(body != ""){
                responseMessage += body + "\r\n";
             }

            System.out.println("Process done");

            Writer clientWriter = new OutputStreamWriter(clientSocket.getOutputStream());
            clientWriter.write(responseMessage);
            System.out.println("Response done");

            // 4. Close
            clientWriter.flush();
            clientReader.close();
            clientSocket.close();
            System.out.println("Close");
        }
    }

}
