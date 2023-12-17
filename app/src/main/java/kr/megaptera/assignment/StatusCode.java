package kr.megaptera.assignment;

public enum StatusCode {
    OK("200", "OK"),
    CREATED("201", "Created"),
    BAD_REQUEST("400", "Bad Request"),
    NON_FOUND("404", "Not Found");

    private final String code;
    private final String message;

    StatusCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
