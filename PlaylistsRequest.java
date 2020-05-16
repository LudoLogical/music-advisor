package advisor;

import java.net.http.HttpRequest;

public class PlaylistsRequest extends ContentRequest {

    private final String queryPath;

    private final int limit;

    public PlaylistsRequest(String validCategoryID, int limit) {
        queryPath = "/v1/browse/categories/" + validCategoryID + "/playlists";
        this.limit = limit;
    }

    public HttpRequest generateRequest(String accessToken, String apiPath) {
        return super.generateRequestHelper(accessToken, apiPath, queryPath, limit);
    }

}
