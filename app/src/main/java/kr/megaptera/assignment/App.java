package kr.megaptera.assignment;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class App {

	public static Long seq = 0L;

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
		System.out.println("Listen!");

		// 2. Accept
		while (true) {
			Socket socket = listener.accept();
			System.out.println("Accept!");
			// 3. Request
			String request = getRequest(socket);

			// 4. Response
			String responseMessage = getResponse(request, tasks);

			writerMessage(socket, responseMessage);
		}

	}

	private String getRequest(Socket socket) throws IOException {
		Reader reader = new InputStreamReader(socket.getInputStream());
		CharBuffer charBuffer = CharBuffer.allocate(1_000_000);
		reader.read(charBuffer);
		charBuffer.flip();

		return charBuffer.toString();
	}

	private String getResponse(String request, Map<Long, String> tasks) {
		String requestMethod = request.substring(0, request.indexOf("HTTP"));

		if (requestMethod.startsWith("GET /tasks")) {
			return requestGet(tasks);
		}

		if (requestMethod.startsWith("POST /tasks")) {
			return requestPost(request, tasks);
		}

		if (requestMethod.startsWith("PATCH /tasks")) {
			return requestPatch(request, tasks);
		}

		if (requestMethod.startsWith("DELETE /tasks")) {
			return requestDelete(request, tasks);
		}

		return "";
	}

	private String requestGet(Map<Long, String> tasks) {
		return getMessage(new Gson().toJson(tasks), "200 OK");
	}

	private String requestPost(String request, Map<Long, String> tasks) {
		String task = requestParse(request, tasks);

		if ("".equals(task)) {
			return getMessage("", "400 Bad Request");
		}
		seq = seq + 1;
		tasks.put(seq, task);
		return getMessage(new Gson().toJson(tasks), "201 Created");
	}

	private String requestPatch(String request, Map<Long, String> tasks) {

		Long requestId = getRequestId(request);
		if (!tasks.containsKey(requestId)) {
			return getMessage("", "404 Not Found");
		}

		String task = requestParse(request, tasks);
		if ("".equals(task)) {
			return getMessage("", "400 Bad Request");
		}

		tasks.put(requestId, task);
		return getMessage(new Gson().toJson(tasks), "200 OK");
	}

	private String requestDelete(String request, Map<Long, String> tasks) {
		Long requestId = getRequestId(request);
		if (!tasks.containsKey(requestId)) {
			return getMessage("", "404 Not Found");
		}

		tasks.remove(requestId);

		return getMessage(new Gson().toJson(tasks), "200 OK");
	}

	private String requestParse(String request, Map<Long, String> tasks) {
		String[] requestSplit = request.split("\n");
		String lastLine = requestSplit[requestSplit.length - 1];

		try {
			JsonElement jsonElement = JsonParser.parseString(lastLine);
			JsonObject jsonObject = jsonElement.getAsJsonObject();

			return jsonObject.get("task").getAsString();
		} catch (Exception e) {
			return "";
		}
	}

	private Long getRequestId(String request) {
		String[] requestSplit = request.split("/");
		String[] requestId = requestSplit[2].split(" ");
		return Long.parseLong(requestId[0]);
	}

	private String getMessage(String body, String status) {
		byte[] bytes = body.getBytes();

		String response = ""
			+ "HTTP/1.1 " + status + " \n"
			+ "Content-Length: " + bytes.length + "\n"
			+ "Content-TypeL: application/json; charset=UTF-8\n"
			+ "HOST: localhost:8080\n"
			+ "\n"
			+ body;

		return response;
	}

	private void writerMessage(Socket socket, String responseMessage) throws IOException {
		Writer writer = new OutputStreamWriter(socket.getOutputStream());
		writer.write(responseMessage);

		writer.flush();
	}
}
