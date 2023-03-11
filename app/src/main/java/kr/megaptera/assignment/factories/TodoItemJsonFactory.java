package kr.megaptera.assignment.factories;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kr.megaptera.assignment.models.TodoItem;

import java.util.HashMap;
import java.util.Map;

public class TodoItemJsonFactory {
    public String Create(TodoItem[] todoItems){
        if(todoItems.length == 0){
            return "";
        }

        var gson = new Gson();

        Map<Long, String> tasks = new HashMap<>();
        for (var todoItem : todoItems) {
            tasks.put(todoItem.getId(), todoItem.getContent());
        }

        String jsonStr = gson.toJson(tasks);
        return jsonStr;
    }
}
