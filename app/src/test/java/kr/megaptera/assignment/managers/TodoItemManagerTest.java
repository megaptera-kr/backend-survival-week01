package kr.megaptera.assignment.managers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TodoItemManagerTest {
    @Test
    void content_toAddTodoItem_shouldReturnTrue() {
        var manager = new TodoItemManager();
        var content = "demo content";

        manager.add(content);

        var todoItems = manager.getAll();

        assertEquals(1, todoItems.length);

        var firstItem = todoItems[0];
        assertEquals(1, firstItem.getId());
        assertEquals(content, firstItem.getContent());
    }

    @Test
    void content_toAddTodoItems_shouldReturnTrue() {
        var manager = new TodoItemManager();
        var content1 = "demo content 1";
        var content2 = "demo content 2";

        manager.add(content1);
        manager.add(content2);

        var todoItems = manager.getAll();

        assertEquals(2, todoItems.length);

        var firstItem = todoItems[0];
        assertEquals(1, firstItem.getId());
        assertEquals(content1, firstItem.getContent());

        var secondItem = todoItems[1];
        assertEquals(2, secondItem.getId());
        assertEquals(content2, secondItem.getContent());
    }

    @Test
    void content_toRemoveTodoItem_shouldReturnTrue() {
        var manager = new TodoItemManager();
        manager.add("demo");

        var isSuccessRemove = manager.remove(1);
        assertTrue(isSuccessRemove);
    }

    // ...
}