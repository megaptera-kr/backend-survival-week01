package kr.megaptera.assignment.factories;

import kr.megaptera.assignment.models.HttpRequestSource;

import java.util.Arrays;

public class HttpRequestSourceFactory {

    public static HttpRequestSource Create(String reqMessage){
        var lines = reqMessage.split("\n");
        var startLine = lines[0];

        // TODO : (dh) Get by method
        int emptyIndex = 0;

        for(int i = 0; i <lines.length; i++){
            var line = lines[i];
            if(line.length() == 0){
                emptyIndex = i;
                break;
            }
        }

        int headerStartIndex = 1;
        int headerEndIndex = emptyIndex;
        var headers = Arrays.copyOfRange(lines, headerStartIndex, headerEndIndex);

        int bodyStartIndex = emptyIndex + 1;
        int bodyEndIndex = lines.length;
        var bodies = Arrays.copyOfRange(lines, bodyStartIndex, bodyEndIndex);
        var body = String.join("\n", bodies);

        var source = new HttpRequestSource();
        source.setStartLine(startLine);
        source.setHeaders(headers);
        source.setBody(body);

        return source;
    }
}
