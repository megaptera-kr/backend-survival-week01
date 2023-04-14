package kr.megaptera.assignment;

import java.net.HttpURLConnection;

public enum HttpStatus {

    OK(HttpURLConnection.HTTP_OK, "OK"),
    CREATED(HttpURLConnection.HTTP_CREATED, "Created"),
    BAD_REQUEST(HttpURLConnection.HTTP_BAD_REQUEST, "Bad Request"),
    NOT_FOUND(HttpURLConnection.HTTP_NOT_FOUND, "Not Found"),
    METHOD_NOT_ALLOWED(HttpURLConnection.HTTP_BAD_METHOD, "Method Not Allowed");

    private final int code;
    private final String text;

    HttpStatus(int code, String text) {
        this.code = code;
        this.text = text;
    }

    public String createResponse(String responseBody) {
        if (this == OK || this == CREATED) {
            return String.format("""
                    HTTP/1.1 %d %s
                    Content-Type: application/json; charset=utf-8
                    Content-Length: %d

                    %s
                    """, this.code, this.text, responseBody.getBytes().length, responseBody);
        } else {
            return String.format("""
                    HTTP/1.1 %d %s
                    Content-Type: application/json; charset=utf-8
                    Content-Length: 0

                    """, this.code, this.text);
        }
    }
}
