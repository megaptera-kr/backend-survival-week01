package kr.megaptera.assignment.factories;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kr.megaptera.assignment.models.TodoItem;

public class TodoItemJsonFactory {
    public String Create(TodoItem[] todoItems){
        if(todoItems.length == 0){
            return "";
        }

        var gson = new Gson();
        var jsonObject = new JsonObject();

        for (var todoItem : todoItems) {
            jsonObject.addProperty(Integer.toString(todoItem.getId()), todoItem.getContent());
        }
        String jsonStr = gson.toJson(jsonObject);
        return jsonStr;
    }
}
