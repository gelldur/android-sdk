package com.sensorberg.sdk.testUtils;

import android.content.Context;

import com.sensorberg.sdk.internal.http.helper.RawJSONMockResponse;

import org.fest.assertions.api.Assertions;
import org.json.JSONException;
import org.junit.After;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

/**
 * Created by falkorichter on 02/08/16.
 */
public abstract class TestWithMockServer {
    protected MockWebServer server;

    @After
    public void tearDown() throws Exception {
        if (server != null){
            server.shutdown();
        }
    }

    protected String getBaseUrl() {
        return server.url("").toString();
    }

    protected void startWebserver(int... rawRequestsResourceIds) throws IOException, JSONException {
        server = new MockWebServer();
        enqueue(rawRequestsResourceIds);
        server.start();

    }

    protected String getUrl(String path) {
        return server.url(path).toString();
    }

    public void enqueue(int... rawRequestsResourceIds) throws IOException, JSONException {
        for (int rawRequestId : rawRequestsResourceIds) {
            server.enqueue(fromRaw(rawRequestId));
        }
    }

    protected MockResponse fromRaw(int resourceID) throws IOException, JSONException {
        return RawJSONMockResponse.fromRawResource(getContext().getResources().openRawResource(resourceID)) ;
    }

    protected abstract Context getContext();


    protected List<RecordedRequest> waitForRequests(int i) throws InterruptedException {
        List<RecordedRequest> recordedRequests = new ArrayList<>();
        for (int i1 = i; i1 > 0; i1--) {
            recordedRequests.add(server.takeRequest(10, TimeUnit.SECONDS));
        }
        Assertions.assertThat(server.getRequestCount()).overridingErrorMessage("There should have been %d requests. Only %d requests were recorded.", i, server.getRequestCount()).isEqualTo(i);
        return recordedRequests;
    }
}
