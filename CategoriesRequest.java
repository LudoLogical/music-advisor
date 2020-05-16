package advisor;

import java.net.http.HttpRequest;

public class CategoriesRequest extends ContentRequest {

    private static final String queryPath = "/v1/browse/categories";

    private final int limit;

    public CategoriesRequest(int limit) {
        this.limit = limit;
    }

    public HttpRequest generateRequest(String accessToken, String apiPath) {
        return super.generateRequestHelper(accessToken, apiPath, queryPath, limit);
    }

}
