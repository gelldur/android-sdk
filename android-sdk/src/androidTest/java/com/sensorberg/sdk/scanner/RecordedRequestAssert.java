package com.sensorberg.sdk.scanner;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.fest.assertions.api.AbstractAssert;
import org.json.JSONException;

import android.content.Context;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.mockwebserver.RecordedRequest;
import util.Utils;


public class RecordedRequestAssert extends AbstractAssert<RecordedRequestAssert, RecordedRequest> {

    private RecordedRequestAssert(RecordedRequest actual) {
        super(actual, AbstractAssert.class);
    }

    public static RecordedRequestAssert assertThat(RecordedRequest request) {
        return new RecordedRequestAssert(request);
    }

    public RecordedRequestAssert matchesRawResourceRequest(int rawResourceID, Context context) throws IOException, JSONException {
        JsonParser parser = new JsonParser();
        JsonObject expectedRequest = Utils.getRawResourceAsJSON(rawResourceID, context);
        JsonObject body = parser.parse(actual.getBody().toString()).getAsJsonObject();
        jsonObjsAreEqual(expectedRequest.get("body").getAsJsonObject(), body);

        return this;
    }

    public static boolean jsonObjsAreEqual(JsonObject js1, JsonObject js2) throws JSONException {
        if (js1 == null || js2 == null) {
            return (js1 == js2);
        }

        List<String> l1 = setKeysAsList(js1.entrySet().iterator());
        Collections.sort(l1);
        List<String> l2 = setKeysAsList(js1.entrySet().iterator());
        Collections.sort(l2);
        if (!l1.equals(l2)) {
            return false;
        }
        for (String key : l1) {
            Object val1 = js1.get(key);
            Object val2 = js2.get(key);
            if (val1 instanceof JsonObject) {
                if (!(val2 instanceof JsonObject)) {
                    return false;
                }
                if (!jsonObjsAreEqual((JsonObject) val1, (JsonObject) val2)) {
                    return false;
                }
            }

            if (val1 == null) {
                if (val2 != null) {
                    return false;
                }
            } else if (!val1.equals(val2)) {
                return false;
            }
        }
        return true;
    }

    private static List<java.lang.String> setKeysAsList(Iterator<Map.Entry<String, JsonElement>> iterator) {
        List<String> value = new ArrayList<String>();
        while (iterator.hasNext()) {
            value.add(iterator.next().getKey());
        }
        return value;
    }

}
