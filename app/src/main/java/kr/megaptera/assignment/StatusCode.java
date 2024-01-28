package kr.megaptera.assignment;

public enum StatusCode {
    OK(200, "OK"),
    CREATED(201, "Created"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found");

    private final int code;
    private final String message;

    StatusCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public static StatusCode of(int code) {
        for (StatusCode statusCode : values()) {
            if (statusCode.code == code) {
                return statusCode;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
