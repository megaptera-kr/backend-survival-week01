package kr.megaptera.assignment.models;

import java.util.Objects;

public class HttpParameter {
    private String key;
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var other = (HttpParameter) o;

        return Objects.equals(key, other.getKey()) && Objects.equals(value, other.getValue()) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }
}
