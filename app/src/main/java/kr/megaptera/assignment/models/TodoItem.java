package kr.megaptera.assignment.models;

import java.util.Objects;

public class TodoItem {
    private int id;
    private String content;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var other = (TodoItem) o;

        return Objects.equals(id, other.getId()) && Objects.equals(content, other.getContent()) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
