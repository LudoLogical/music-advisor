package advisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("Convert2Lambda")
public class SpotifyAPIManager {

    private static final SpotifyAPIManager manager = new SpotifyAPIManager();

    private final HttpClient client;
    private final AtomicReference<String> AUTH_CODE;
    private final AtomicReference<String> ACCESS_TOKEN;
    private final AtomicBoolean AUTH_CODE_RETRIEVED = new AtomicBoolean(false);

    private String apiPath;

    private SpotifyAPIManager() {
        client = HttpClient.newBuilder().build();
        AUTH_CODE = new AtomicReference<>();
        ACCESS_TOKEN = new AtomicReference<>();
    }

    public static SpotifyAPIManager getManager() {
        return manager;
    }

    private void getAuthCode(int port, String authPath, String clientID,
                             String redirectURI) throws IOException, InterruptedException {

        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(port), 0);
        server.createContext("/",
                new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        if (!AUTH_CODE_RETRIEVED.get()) { // prevents wrong data from leaking in after success
                            AUTH_CODE.set(exchange.getRequestURI().getQuery());
                            String reply;
                            int responseCode;
                            if (AUTH_CODE.get() != null && AUTH_CODE.get().matches("code=.*")) {
                                reply = "Got the code. Return back to your program.";
                                responseCode = 200;
                                AUTH_CODE_RETRIEVED.set(true);
                            } else {
                                reply = "Not found authorization code. Try again.";
                                responseCode = 400;
                                AUTH_CODE.set(null); // bad input, scratching and trying again
                            }
                            exchange.sendResponseHeaders(responseCode, reply.length());
                            exchange.getResponseBody().write(reply.getBytes());
                            exchange.getResponseBody().close();
                        }
                    }
                }
        );
        server.start();

        System.out.println("use this link to request the access code:\n" + authPath +
                "/authorize?client_id=" + clientID + "&redirect_uri=" + redirectURI +
                "&response_type=code");
        System.out.println("waiting for code...");

        while (!AUTH_CODE_RETRIEVED.get()) {
            Thread.onSpinWait();
        }
        Thread.sleep(10); // For JBA; ensures Atomic data transfer is complete

        AUTH_CODE.set(AUTH_CODE.get().substring(5));
        server.stop(1);
    }

    private void getAccessToken(String authPath, String redirectURI, String clientID,
                                String clientSecret) throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(authPath + "/api/token"))
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=authorization_code" +
                        "&code=" + AUTH_CODE.get() + "&redirect_uri=" + redirectURI + "&client_id=" +
                        clientID + "&client_secret=" + clientSecret))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject accessTokenJson = JsonParser.parseString(response.body()).getAsJsonObject();

        ACCESS_TOKEN.set(accessTokenJson.get("access_token").getAsString());
        /*
        String tokenType = accessTokenJson.get("token_type").getAsString();
        int timeToExpiry = accessTokenJson.get("expires_in").getAsInt();
        String refreshToken = accessTokenJson.get("refresh_token").getAsString();
        // String scope = accessTokenJson.get("scope").getAsString();
        */

    }

    public boolean setup(int serverPort, String authPath, String apiPath,
                         String redirectURI, String clientID, String clientSecret) {

        try {
            getAuthCode(serverPort, authPath, clientID, redirectURI);
        } catch (IOException | InterruptedException e) {
            System.out.println("Server creation failed.");
            return false;
        }

        System.out.println("code received");
        System.out.println("Making http request for access_token...");

        try {
            getAccessToken(authPath, redirectURI, clientID, clientSecret);
        } catch (IOException | InterruptedException e) {
            System.out.println("Access token request failed.");
            return false;
        }

        this.apiPath = apiPath; // save for later, but only if successful

        System.out.println("Success!");
        return true;

    }

    public String getUnformattedContent(ContentRequest form) {
        try {
            HttpRequest request = form.generateRequest(ACCESS_TOKEN.get(), apiPath);
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            System.out.println("Request failed.");
            return null;
        }
    }

}
