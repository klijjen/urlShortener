package com.example.urlshortener;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;

public class Utils {
    public static UUID getRandomUUID() {
        return UUID.randomUUID();
    }

    public static boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            return scheme != null &&
                    (scheme.equalsIgnoreCase("http") ||
                            scheme.equalsIgnoreCase("https") ||
                            scheme.equalsIgnoreCase("ftp"));
        } catch (URISyntaxException e) {
            return false;
        }
    }
}
