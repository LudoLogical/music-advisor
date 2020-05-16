package advisor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    @SuppressWarnings("SpellCheckingInspection") // this is not a word you fool IDE
    public static final String clientID = "509a92c099904dfeac8e86cf945749f2";
    public static final String clientSecret = "3b6d44bb0bea428b9cc52a93f261d59c";

    public static final int port = 8080;
    public static final String redirectURI = "http://localhost:" + port;

    // default values; become final upon evaluation of command-line args
    public static String authPath = "https://accounts.spotify.com";
    public static String apiPath = "https://api.spotify.com";
    public static int entriesPerPage = 5;

    public static final List<String> queryCommands =
            Arrays.asList("new", "featured", "categories", "playlists", "next", "prev");
    public static volatile AtomicReference<HashMap<String, String>> PLAYLIST_IDS =
            new AtomicReference<>(new HashMap<>());

    private static void parseCommandLineArgs (String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("-access".equals(args[i])) {
                try {
                    authPath = args[i+1];
                    i++;
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Invalid command syntax: -access PATH");
                }
            } else if ("-resource".equals(args[i])) {
                try {
                    apiPath = args[i+1];
                    i++;
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Invalid command syntax: -resource PATH");
                }
            } else if ("-page".equals(args[i])) {
                try {
                    entriesPerPage = Integer.parseInt(args[i+1]);
                    i++;
                } catch (IndexOutOfBoundsException e) {
                    throw new IllegalArgumentException("Invalid command syntax: -page INTEGER");
                }
            }
        }
    }

    private static void respondToUserQuery(SpotifyAPIManager spotifyAPIManager, String keyword, String cName) {

        ContentRequest request;

        if ("new".equals(keyword)) {
            request = new NewRequest(entriesPerPage);
        } else if ("featured".equals(keyword)) {
            request = new FeaturedRequest(entriesPerPage);
        } else if ("categories".equals(keyword)) {
            request = new CategoriesRequest(entriesPerPage);
        } else if ("playlists".equals(keyword)) {
            if (cName == null) {
                System.out.println("Invalid syntax: playlists [C_NAME]");
                return;
            } else {
                String id = PLAYLIST_IDS.get().get(cName.toLowerCase());
                if (id == null) {
                    System.out.println("Unknown category name.");
                    return;
                }
                request = new PlaylistsRequest(id, entriesPerPage);
            }
        } else { // keyword must be prev or next
            String link;
            if ("prev".equals(keyword)) {
                link = ViewManager.getPreviousOfLastShown();
            } else { // keyword must be next
                link = ViewManager.getNextOfLastShown();
            }
            if (link == null) {
                System.out.println("No more pages.");
                return; // do not make request or display further output
            } else {
                request = new RequestFromLink(link);
                keyword = ViewManager.getTypeOfLastShown(); // override to ensure formatting of output
            }
        }

        String rawResponse = spotifyAPIManager.getUnformattedContent(request);
        if (rawResponse != null) {
            ViewManager.showFormattedResponse(keyword, rawResponse, entriesPerPage, PLAYLIST_IDS);
        }

    }

    public static void main(String[] args) {

        parseCommandLineArgs(args);

        Scanner scan = new Scanner(System.in);
        SpotifyAPIManager spotifyAPIManager = SpotifyAPIManager.getManager();
        boolean authorized = false;

        while (true) {

            String input = scan.nextLine().trim().toLowerCase();
            String cName = null;
            if (input.split(" ").length >= 2) {
                int firstSpaceIndex = input.indexOf(' ');
                cName = input.substring(firstSpaceIndex + 1);
                input = input.substring(0, firstSpaceIndex);
            }

            if ("exit".equals(input)) {
                break;
            } else {
                if (!authorized) {
                    if ("auth".equals(input)) {
                        authorized = spotifyAPIManager.setup(port, authPath, apiPath,
                                                             redirectURI, clientID, clientSecret);
                    } else {
                        System.out.println("Please, provide access for application.");
                    }
                } else if (queryCommands.contains(input)) {
                    respondToUserQuery(spotifyAPIManager, input, cName);
                } else if ("auth".equals(input)) {
                    System.out.println("Already authorized.");
                } else {
                    System.out.println("Unrecognized command.");
                }
            }

        }

    }

}