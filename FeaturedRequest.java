package advisor;

import java.net.http.HttpRequest;

public class FeaturedRequest extends ContentRequest {

    private static final String queryPath = "/v1/browse/featured-playlists";

    private final int limit;

    public FeaturedRequest(int limit) {
        this.limit = limit;
    }

    public HttpRequest generateRequest(String accessToken, String apiPath) {
        return super.generateRequestHelper(accessToken, apiPath, queryPath, limit);
    }

}
