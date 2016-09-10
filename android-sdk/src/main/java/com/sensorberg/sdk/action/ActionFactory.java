package com.sensorberg.sdk.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.model.ISO8601TypeAdapter;

import org.json.JSONException;

import android.net.Uri;
import android.webkit.URLUtil;

import java.util.Date;
import java.util.UUID;

public class ActionFactory {

    private static Gson gson;

    public interface ServerType {

        int URL_MESSAGE = 1;
        int VISIT_WEBSITE = 2;
        int IN_APP = 3;
    }

    private static final String SUBJECT = "subject";

    private static final String BODY = "body";

    private static final String URL = "url";

    private static final String PAYLOAD = "payload";


    public static Action getAction(int actionType, JsonObject message, UUID actionUUID, long delay) throws JSONException {
        if (message == null) {
            return null;
        }
        Action value = null;
        String payload = null;
        JsonElement payloadElement = message.get(PAYLOAD);
        if (payloadElement != null && !payloadElement.isJsonNull()) {
            if (payloadElement.isJsonArray() || payloadElement.isJsonObject()) {
                payload = getGson().toJson(message.get(PAYLOAD));
            } else {
                payload = payloadElement.getAsString();
            }
        }

        String subject = message.get(SUBJECT) == null ? null : message.get(SUBJECT).getAsString();
        String body = message.get(BODY) == null ? null : message.get(BODY).getAsString();
        String url = getUriFromJson(message.get(URL), actionType);

        switch (actionType) {
            case ServerType.URL_MESSAGE: {
                value = new UriMessageAction(
                        actionUUID,
                        subject,
                        body,
                        url,
                        payload,
                        delay
                );
                break;
            }
            case ServerType.VISIT_WEBSITE: {
                value = new VisitWebsiteAction(
                        actionUUID,
                        subject,
                        body,
                        Uri.parse(url),
                        payload,
                        delay
                );
                break;
            }
            case ServerType.IN_APP: {
                value = new InAppAction(
                        actionUUID,
                        subject,
                        body,
                        payload,
                        Uri.parse(url),
                        delay
                );
            }
        }
        return value;
    }

    private static Gson getGson() {
        //TODO see how to inject this statically with dagger!
        if (gson == null) {
            gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .registerTypeAdapter(Date.class, ISO8601TypeAdapter.DATE_ADAPTER)
                    .setLenient()
                    .create();
        }

        return gson;
    }

    /**
     * Gets URL parameter from JSON and validates it.
     *
     * @param jsonElement - JsonElement that contains the url string.
     * @param messageType - Message type.
     * @return - Returns the verified URI string. If not valid or empty will return an empty string.
     */
    public static String getUriFromJson(JsonElement jsonElement, int messageType) {
        String urlToCheck = jsonElement == null ? "" : jsonElement.getAsString();
        String returnUrl = "";

        //we allow deep links for in app actions and URL messages; we enforce valid network URLs
        // for the visit website action
        if ((messageType == ServerType.IN_APP && validatedUrl(urlToCheck))
                || (messageType == ServerType.URL_MESSAGE && validatedUrl(urlToCheck))
                || URLUtil.isNetworkUrl(urlToCheck)) {
            returnUrl = urlToCheck;
        }

        if (returnUrl.isEmpty()) {
            Logger.log.logError("URL is invalid, please change in the campaign settings.");
        }

        return returnUrl;
    }

    //this allows for deep links, we just check for URL syntax conformity
    public static boolean validatedUrl(String urlToCheck) {
        Uri uri;

        try {
            uri = Uri.parse(urlToCheck);
        } catch (Exception e) {
            uri = Uri.parse("");
        }

        return uri.getScheme() != null || uri.getPath() != null;
    }

}
