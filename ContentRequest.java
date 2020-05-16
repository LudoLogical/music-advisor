package advisor;

import java.net.URI;
import java.net.http.HttpRequest;

public abstract class ContentRequest {

    public abstract HttpRequest generateRequest(String accessToken, String apiPath);

    HttpRequest generateRequestHelper(String accessToken, String apiPath, String queryPath, int limit) {
        String limitString = limit == -1 ? "" : "?limit=" + limit;
        return HttpRequest.newBuilder()
                .header("Authorization", "Bearer " + accessToken)
                .uri(URI.create(apiPath + queryPath + limitString))
                .GET()
                .build();
    }

}
