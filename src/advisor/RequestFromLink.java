package advisor;

import java.net.http.HttpRequest;

public class RequestFromLink extends ContentRequest {

    private final String link;

    public RequestFromLink(String link) {
        this.link = link;
    }

    // Ignore input apiPath
    public HttpRequest generateRequest(String accessToken, String apiPath) {
        return super.generateRequestHelper(accessToken, link, "", -1);
    }

}
