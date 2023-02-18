package kr.megaptera.assignment;

import java.util.Map;

public record Request (StartLine startLine, Map<String, String> headers, String body) {

}
