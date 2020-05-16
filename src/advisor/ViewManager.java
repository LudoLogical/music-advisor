package advisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ViewManager {

    private static String typeOfLastShown = null;
    private static String previousOfLastShown = null;
    private static String nextOfLastShown = null;

    private static void showNew(String rawResponse) {
        JsonObject responseJson = JsonParser.parseString(rawResponse).getAsJsonObject().getAsJsonObject("albums");
        for (JsonElement album : responseJson.getAsJsonArray(("items"))) {
            JsonObject albumObject = album.getAsJsonObject();
            System.out.println(albumObject.get("name").getAsString());
            StringBuilder artistsOutput = new StringBuilder("[");
            for (JsonElement artist : albumObject.get("artists").getAsJsonArray()) {
                JsonObject artistObject = artist.getAsJsonObject();
                artistsOutput.append(artistObject.get("name").getAsString()).append(", ");

            }
            // remove trailing space and comma, replace with closing ]
            artistsOutput.deleteCharAt(artistsOutput.length() - 1);
            artistsOutput.setCharAt(artistsOutput.length() - 1, ']');
            System.out.println(artistsOutput);
            System.out.println(albumObject.get("external_urls").getAsJsonObject().get("spotify").getAsString());
            System.out.println(); // make pretty
        }
    }

    private static void showAndUpdateCategories(String rawResponse,
                                                AtomicReference<HashMap<String, String>> PLAYLIST_IDS) {

        HashMap<String, String> idMap = PLAYLIST_IDS.get();
        JsonObject responseJson = JsonParser.parseString(rawResponse).getAsJsonObject().getAsJsonObject("categories");
        for (JsonElement category : responseJson.getAsJsonArray(("items"))) {
            JsonObject categoryObject = category.getAsJsonObject();
            String nowName = categoryObject.get("name").getAsString();
            if (idMap.get(nowName.toLowerCase()) == null) {
                String nowID = categoryObject.get("id").getAsString();
                idMap.put(nowName.toLowerCase(), nowID);
            }
            System.out.println(nowName);
        }
        PLAYLIST_IDS.set(idMap);

    }

    private static void showPlaylists(String rawResponse) {
        JsonObject responseJson = JsonParser.parseString(rawResponse).getAsJsonObject().getAsJsonObject("playlists");
        for (JsonElement playlist : responseJson.getAsJsonArray(("items"))) {
            JsonObject playlistObject = playlist.getAsJsonObject();
            System.out.println(playlistObject.get("name").getAsString());
            System.out.println(playlistObject.get("external_urls").getAsJsonObject().get("spotify").getAsString());
            System.out.println(); // make pretty
        }
    }

    public static void showFormattedResponse (String type, String rawResponse, int limit,
                                              AtomicReference<HashMap<String, String>> PLAYLIST_IDS) {

        String topObjectName;

        switch (type) {
            case "new":
                topObjectName = "albums";
                showNew(rawResponse);
                break;
            case "featured":
            case "playlists":
                topObjectName = "playlists";
                showPlaylists(rawResponse);
                break;
            case "categories":
                topObjectName = "categories";
                showAndUpdateCategories(rawResponse, PLAYLIST_IDS);
                break;
            default:
                System.out.println("Displaying of this type of content is not yet implemented.");
                return;
        }

        // General output and storage operations that must be performed regardless of the value of String type
        JsonObject responseJson = JsonParser.parseString(rawResponse).getAsJsonObject().getAsJsonObject(topObjectName);
        int offset = responseJson.get("offset").getAsInt();
        int total = responseJson.get("total").getAsInt();
        int currentPage = (offset / limit) + 1;
        int totalPages = total/limit;
        if (total % limit != 0) {
            totalPages++;
        }
        System.out.println("---PAGE " + currentPage + " OF " + totalPages + "---");
        JsonElement prevJson = responseJson.get("previous");
        previousOfLastShown = prevJson.isJsonNull() ? null : prevJson.getAsString();
        JsonElement nextJson = responseJson.get("next");
        nextOfLastShown = nextJson.isJsonNull() ? null : nextJson.getAsString();
        typeOfLastShown = type;

    }

    public static String getPreviousOfLastShown() {
        return previousOfLastShown;
    }

    public static String getNextOfLastShown() {
        return nextOfLastShown;
    }

    public static String getTypeOfLastShown() {
        return typeOfLastShown;
    }

}
