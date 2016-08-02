package com.sensorberg.sdk.internal.http.helper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import okhttp3.mockwebserver.MockResponse;
import util.Utils;

public class RawJSONMockResponse {
    public static MockResponse fromRawResource(InputStream inputStream) throws IOException, JSONException {

        String theString = Utils.toString(inputStream);
        JSONObject json = new JSONObject(theString);
        MockResponse value = new MockResponse();

        JSONObject body = json.optJSONObject("body");
        if (body != null) {
            value.setBody(body.toString());
        }
        value.setResponseCode(json.optInt("statusCode", 200));

        JSONObject headers = json.optJSONObject("headers");
        if (headers != null) {
            Iterator<String> keys = headers.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                value.addHeader(key, headers.get(key));
            }
        }

        return value;
    }
}
