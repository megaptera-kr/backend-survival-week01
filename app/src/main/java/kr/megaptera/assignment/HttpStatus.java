package kr.megaptera.assignment;

public enum HttpStatus {
    OK(200, "OK"),
    CREATE(201, "Created"),
    BAD_REQUEST(400, "Bad Request"),
    NOT_FOUND(404, "Not Found");

    int code;
    String message;

    HttpStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
