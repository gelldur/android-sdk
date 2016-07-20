package com.sensorberg.sdk.action;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.model.ISO8601TypeAdapter;
import com.sensorberg.sdk.model.sugarorm.SugarAction;
import com.sensorberg.sdk.model.sugarorm.SugarScan;

import org.json.JSONException;

import android.Manifest;
import android.net.Uri;
import android.util.Log;
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

    private static final String DELAY_TIME = "delayTime";

    private static final String CONTENT = "content";

    private static final String TYPE = "type";

    private static final String PAYLOAD = "payload";


    public static Action actionFromJSONObject(JsonObject contentJSON) throws JSONException {
        int actionType = contentJSON.get(TYPE).getAsInt();
        UUID actionUUID = UUID.fromString(contentJSON.get("id").getAsString());

        long delayMilliseconds = (contentJSON.get(DELAY_TIME) != null ? contentJSON.get(DELAY_TIME).getAsLong() : Action.NO_DELAY) * 1000;

        String messageString = contentJSON.get(CONTENT).getAsString();
        JsonParser parser = new JsonParser();
        JsonObject message = parser.parse(messageString).getAsJsonObject();

        return getAction(actionType, message, actionUUID, delayMilliseconds);
    }

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
        String url = message.get(URL) == null ? "" : validateUri(message.get(URL).getAsString());

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
                    .registerTypeAdapter(SugarScan.class, new SugarScan.SugarScanObjectTypeAdapter())
                    .registerTypeAdapter(SugarAction.class, new SugarAction.SugarActionTypeAdapter())
                    .setLenient()
                    .create();
        }

        return gson;
    }

    /**
     * Checks the URI string received.
     *
     * @param uriToParse - The URI string to parse.
     * @return - Returns the verified URI string. If not valid or empty will return an empty string.
     *
     */
    private static String validateUri(String uriToParse) {
        String toReturnUri;

            if (!URLUtil.isValidUrl(uriToParse)) {
                Logger.log.logError("URL is invalid, please change in the campaign settings.");
                toReturnUri = "";
            } else {
                toReturnUri = uriToParse;
            }

        return toReturnUri;
    }
}
