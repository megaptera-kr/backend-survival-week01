package kr.megaptera.assignment.managers;

import kr.megaptera.assignment.models.TodoItem;

import java.util.HashSet;

public class TodoItemManager {

    private int index = 0;
    private HashSet<TodoItem> todoItems = new HashSet<TodoItem>();

    public void add(String content) {
        var item = new TodoItem();
        item.setId(++index);
        item.setContent(content);

        todoItems.add(item);
    }

    public TodoItem[] getAll() {
        if (todoItems.size() == 0) {
            return new TodoItem[0];
        }

        return todoItems.toArray(new TodoItem[todoItems.size()]);
    }

    public TodoItem get(int id) {
        for (var todoItem : todoItems) {
            if (todoItem.getId() == id) {
                return todoItem;
            }
        }

        return null;
    }

    public boolean update(long id, String content) {
        for (var todoItem : todoItems) {
            if (todoItem.getId() == id) {
                todoItem.setContent(content);
                return true;
            }
        }

        return false;
    }

    public boolean remove(int id) {
        for (var todoItem : todoItems) {
            if (todoItem.getId() == id) {
                todoItems.remove(todoItem);
                return true;
            }
        }

        return false;
    }
}
