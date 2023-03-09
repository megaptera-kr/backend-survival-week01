package kr.megaptera.assignment.factories;

import kr.megaptera.assignment.models.HttpMethodType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpStartLineFactoryTest {
    @Test
    void getRequestMessage_withGetMethod_makeCorrectFirstLine() {
        var message = "GET /get-method HTTP/1.1";

        var firstLine = HttpStartLineFactory.Create(message);

        assertEquals(HttpMethodType.Get, firstLine.getHttpMethodType());
        assertEquals("/get-method", firstLine.getPath());
        assertEquals("HTTP/1.1", firstLine.getVersion());
    }

    @Test
    void getRequestMessage_withPostMethod_makeCorrectFirstLine() {
        var message = "POST /post-method HTTP/1.1";

        var firstLine = HttpStartLineFactory.Create(message);

        assertEquals(HttpMethodType.Post, firstLine.getHttpMethodType());
        assertEquals("/post-method", firstLine.getPath());
        assertEquals("HTTP/1.1", firstLine.getVersion());
    }

    @Test
    void getRequestMessage_withPatchMethod_makeCorrectFirstLine() {
        var message = "Patch /patch-method HTTP/1.1";

        var firstLine = HttpStartLineFactory.Create(message);

        assertEquals(HttpMethodType.Patch, firstLine.getHttpMethodType());
        assertEquals("/patch-method", firstLine.getPath());
        assertEquals("HTTP/1.1", firstLine.getVersion());
    }

    @Test
    void getRequestMessage_withDeleteMethod_makeCorrectFirstLine() {
        var message = "Delete /delete-method HTTP/1.1";

        var firstLine = HttpStartLineFactory.Create(message);

        assertEquals(HttpMethodType.Delete, firstLine.getHttpMethodType());
        assertEquals("/delete-method", firstLine.getPath());
        assertEquals("HTTP/1.1", firstLine.getVersion());
    }
}
