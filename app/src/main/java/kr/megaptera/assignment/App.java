package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import kr.megaptera.assignment.factories.HttpRequestSourceFactory;
import kr.megaptera.assignment.factories.TodoItemJsonFactory;
import kr.megaptera.assignment.managers.HttpPathBindingManager;
import kr.megaptera.assignment.managers.TodoItemManager;
import kr.megaptera.assignment.models.HttpMethodType;
import kr.megaptera.assignment.models.HttpPath;
import kr.megaptera.assignment.models.HttpPathType;
import kr.megaptera.assignment.models.HttpResponseSource;
import kr.megaptera.assignment.utils.PayloadConverter;

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

        pathBindingManager.Add(new HttpPath(
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

        pathBindingManager.Add(new HttpPath(
                HttpMethodType.Post,
                "/tasks",
                HttpPathType.Normal,
                requestSource -> {
                    var reqBody = requestSource.getBody();
                    var responseSource = new HttpResponseSource();

                    var task = PayloadConverter.Convert(reqBody, "task");
                    if(task == ""){
                        responseSource.setStatusCode(400);
                        responseSource.setStatusMessage("Bad Request");
                        responseSource.setBody("");

                        return responseSource;
                    }

                    todoItemManager.add(task);

                    var todoItems = todoItemManager.getAll();
                    var resBody = todoItemJsonFactory.Create(todoItems);

                    responseSource.setStatusCode(201);
                    responseSource.setStatusMessage("Created");
                    responseSource.setBody(resBody);

                    return responseSource;
                }));

        pathBindingManager.Add(new HttpPath(
                HttpMethodType.Patch,
                "/tasks",
                HttpPathType.HasValue,
                requestSource -> {
                    // TODO : (dh) Get by util
                    var path = requestSource.getStartLine().getPath();
                    var lastParameter = path.lastIndexOf('/');
                    var idString = path.substring(lastParameter + 1, path.length());
                    var id = Integer.parseInt(idString);

                    var responseSource = new HttpResponseSource();
                    var reqBody = requestSource.getBody();

                    var task = PayloadConverter.Convert(reqBody, "task");
                    if(task == ""){
                        responseSource.setStatusCode(400);
                        responseSource.setStatusMessage("Bad Request");
                        responseSource.setBody("");

                        return responseSource;
                    }

                    var isUpdated = todoItemManager.update(id, task);
                    if(!isUpdated){
                        responseSource.setStatusCode(404);
                        responseSource.setStatusMessage("Not Found");
                        responseSource.setBody("");
                        return responseSource;
                    }

                    var todoItems = todoItemManager.getAll();
                    var body = todoItemJsonFactory.Create(todoItems);

                    responseSource.setStatusCode(200);
                    responseSource.setStatusMessage("OK");
                    responseSource.setBody(body);

                    return responseSource;
                }));

        pathBindingManager.Add(new HttpPath(
                HttpMethodType.Delete,
                "/tasks",
                HttpPathType.HasValue,
                requestSource -> {
                    var path = requestSource.getStartLine().getPath();
                    var lastParameter = path.lastIndexOf('/');
                    var idString = path.substring(lastParameter + 1, path.length());
                    var id = Integer.parseInt(idString);

                    var responseSource = new HttpResponseSource();

                    var isSuccessRemove = todoItemManager.remove(id);
                    if(!isSuccessRemove){
                        responseSource.setStatusCode(404);
                        responseSource.setStatusMessage("Not Found");
                        responseSource.setBody("");
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
            HttpResponseSource responseSource;

            if (action == null) {
                responseSource = new HttpResponseSource();
                responseSource.setStatusCode(404);
                responseSource.setStatusMessage("Bad Request");
                responseSource.setBody("");
            }else{
                responseSource = action.execute(requestSource);
            }

            System.out.println("Request done");

            String responseBody = responseSource.getBody();
            byte[] bytes = responseBody.getBytes();
            String responseMessage = "" +
                    "HTTP/1.1 " + responseSource.getStatusCode() + " " + responseSource.getStatusMessage() + "\n" +
                    "Content-Type: application/json; charset=UTF-8\n" +
                    "Content-Length: " + bytes.length + "\n" +
                    "Host: localhost:8080\n" +
                    "\n";

             if(responseBody != ""){
                responseMessage += responseBody;
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
