package advisor;

import java.net.http.HttpRequest;

public class NewRequest extends ContentRequest {

    private static final String queryPath = "/v1/browse/new-releases";

    private final int limit;

    public NewRequest(int limit) {
        this.limit = limit;
    }

    public HttpRequest generateRequest(String accessToken, String apiPath) {
        return super.generateRequestHelper(accessToken, apiPath, queryPath, limit);
    }

}
