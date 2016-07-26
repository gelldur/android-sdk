package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.BuildConfig;

import android.net.Uri;
import android.os.Build;

public class URLFactory {

    private static final String PRODUCTION_BASE_URL = "connect.sensorberg.com";

    private static final String PRODUCTION_RESOLVER_URL = "https://resolver.sensorberg.com/layout/";

    private static String SCHEME = "https";

    private static String customResolverURL = PRODUCTION_RESOLVER_URL;

    private static Uri.Builder BaseUri() {
        return new Uri.Builder()
                .scheme(SCHEME)
                .encodedAuthority(PRODUCTION_BASE_URL)
                .appendPath("api");
    }

    public static String getPingURL() {
        return BaseUri().appendEncodedPath("status/version").build().toString();
    }

    public static String getSettingsURLString(String apiKey) {
        return getSettingsURLString(null, apiKey);
    }

    public static String getSettingsURLString(Long revision, String apiKey) {
        Uri.Builder builder = BaseUri()
                .appendEncodedPath("applications/")
                .appendPath(apiKey)
                .appendPath("settings")
                .appendPath("android")
                .appendPath(BuildConfig.SDK_VERSION).appendPath(Build.VERSION.RELEASE).appendPath(Build.MANUFACTURER)
                .appendPath(android.os.Build.MODEL + ":" + android.os.Build.PRODUCT);

        if (revision != null) {
            builder.appendQueryParameter("revision", revision.toString());
        }
        return builder.toString();
    }

    public static String getResolveURLString() {
        if (customResolverURL != null) {
            return customResolverURL;
        }
        return BaseUri().appendPath("layout/").toString();
    }

    public static void setLayoutURL(String newResolverURL) {
        if (newResolverURL != null) {
            customResolverURL = newResolverURL;
        } else {
            customResolverURL = PRODUCTION_RESOLVER_URL;
        }
    }
}
