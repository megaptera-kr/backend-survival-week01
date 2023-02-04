package kr.megaptera.assignment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

public class TodoService {
  static Map<Integer, String> todoDataMap = new HashMap<>();
  static int nextIdx = 1;

  public static String getTodo(){
    return getToJson();
  }
  public static String createTodo(JsonElement body){
    String taskItem = getTask(body);
    todoDataMap.put(nextIdx, taskItem);
    nextIdx++;
    return getToJson();
  }

  public static String updateTodo(int id, JsonElement body){
    String taskItem = getTask(body);
    todoDataMap.replace(id, taskItem);
    return getToJson();
  }
  public static String deleteTodo(int id){
    todoDataMap.remove(id);
    return getToJson();
  }

  public static boolean hasTodoItem(String  todoId){
    return todoDataMap.containsKey(Integer.valueOf(todoId));
  }


  private static String getToJson() {
    return new Gson().toJson(todoDataMap);
  }

  private static String getTask(JsonElement body) {
    String rawValue = body.getAsJsonObject().get("task").toString();
    return rawValue.substring(1, rawValue.length()-1);
  }
}
