package com.sensorberg.sdk.internal;

public class URLFactory {

    private static final String PRODUCTION_RESOLVER_BASE_URL = "https://resolver.sensorberg.com";

    private static String customResolverBaseURL = PRODUCTION_RESOLVER_BASE_URL;

    public static String getSettingsURLString(String apiKey) {
        StringBuilder builder = new StringBuilder(customResolverBaseURL)
                .append("/applications/").append(apiKey)
                .append("/settings")
                .append("/android");
        return builder.toString();
    }

    public static String getResolveURLString() {
        return customResolverBaseURL + "/layout";
    }

    public static void setCustomLayoutURL(String newResolverURL) {
        if (newResolverURL != null) {
            customResolverBaseURL = newResolverURL;
        } else {
            customResolverBaseURL = PRODUCTION_RESOLVER_BASE_URL;
        }
    }
}
