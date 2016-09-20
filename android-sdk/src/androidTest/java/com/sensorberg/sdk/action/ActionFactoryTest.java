package com.sensorberg.sdk.action;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.sensorberg.sdk.SensorbergTestApplication;
import com.sensorberg.sdk.di.TestComponent;
import com.sensorberg.sdk.model.server.ResolveAction;
import com.sensorberg.sdk.resolver.BeaconEvent;

import org.fest.assertions.api.Assertions;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import javax.inject.Inject;

import util.Utils;

import static com.sensorberg.sdk.test.R.raw.action_factory_001;
import static com.sensorberg.sdk.test.R.raw.action_factory_002;
import static com.sensorberg.sdk.test.R.raw.action_factory_003;
import static com.sensorberg.sdk.test.R.raw.action_factory_004;
import static com.sensorberg.sdk.test.R.raw.action_factory_005;
import static com.sensorberg.sdk.test.R.raw.action_factory_deeplink_001;
import static com.sensorberg.sdk.test.R.raw.action_factory_empty_url;
import static com.sensorberg.sdk.test.R.raw.action_factory_null_url;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_001_array;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_002_object;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_003_boolean;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_004_integer;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_005_string;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_006_double;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_007_null;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_008_integer_with_exponent;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_009_empty_string;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_010_silent_campaign;

import static com.sensorberg.sdk.test.R.raw.action_factory_payload_010_silent_campaign_no_content_map;
import static com.sensorberg.sdk.test.R.raw.action_factory_payload_010_silent_campaign_null_map;

@RunWith(AndroidJUnit4.class)
public class ActionFactoryTest {

    private static final int[] payloadSamples = new int[]{
            action_factory_payload_001_array,
            action_factory_payload_002_object,
            action_factory_payload_004_integer,
            action_factory_payload_003_boolean,
            action_factory_payload_005_string,
            action_factory_payload_006_double,
            action_factory_payload_008_integer_with_exponent,
            action_factory_payload_009_empty_string,
    };

    public static final JsonElement JSON_ELEMENT_HTTP_LINK = new JsonPrimitive("http://google.com");

    public static final JsonElement JSON_ELEMENT_HTTPS_LINK = new JsonPrimitive("https://google.com");

    public static final JsonElement JSON_ELEMENT_HTTP_LINK_WITH_PARAMS = new JsonPrimitive("http://google.com?test=test&test2=test2");

    public static final JsonElement JSON_ELEMENT_HTTP_LINK_WITH_PAGE = new JsonPrimitive("http://google.com/test.html");

    public static final JsonElement JSON_ELEMENT_DEEPLINK = new JsonPrimitive("android-app://com.sensorberg.bconfig");

    @Inject
    Gson gson;

    private Context context;

    @Before
    public void setUp() throws Exception {
        ((TestComponent) SensorbergTestApplication.getComponent()).inject(this);
        context = InstrumentationRegistry.getContext();
    }

    @Test
    public void should_parse_server_output() throws IOException {
        UriMessageAction result = (UriMessageAction) getAction(action_factory_001);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getContent()).isEqualTo("This is a message");
        Assertions.assertThat(result.getTitle()).isEqualTo("this is a subject");
        Assertions.assertThat(result.getUri()).isEqualTo("something://");

    }

    @Test
    public void should_parse_all_non_null_values() throws Exception {
        for (int i : payloadSamples) {
            Action result = getAction(i);
            Assertions.assertThat(result.getPayload()).isNotNull();
        }
    }

    @Test
    public void should_parse_null_payloads() throws IOException, JSONException {
        Action result = getAction(action_factory_payload_007_null);
        Assertions.assertThat(result.getPayload()).isNull();
    }

    @Test
    public void should_parse_action_type_url_message() throws IOException {
        Action result = getAction(action_factory_001);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isInstanceOf(UriMessageAction.class);
        Assertions.assertThat(((UriMessageAction) result).getContent()).isNotEmpty();
        Assertions.assertThat(((UriMessageAction) result).getTitle()).isNotEmpty();
        Assertions.assertThat(((UriMessageAction) result).getUri()).isEqualToIgnoringCase("something://");
    }

    @Test
    public void should_parse_action_type_visit_website() throws IOException {
        Action result = getAction(action_factory_002);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isInstanceOf(VisitWebsiteAction.class);
        Assertions.assertThat(((VisitWebsiteAction) result).getSubject()).isNotEmpty();
        Assertions.assertThat(((VisitWebsiteAction) result).getBody()).isNotEmpty();
        Assertions.assertThat(((VisitWebsiteAction) result).getUri().toString()).isEqualTo("http://www.google.com");
    }

    @Test
    public void should_parse_action_type_visit_website_with_just_url() throws IOException {
        Action result = getAction(action_factory_003);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isInstanceOf(VisitWebsiteAction.class);
        Assertions.assertThat(((VisitWebsiteAction) result).getSubject()).isNull();
        Assertions.assertThat(((VisitWebsiteAction) result).getBody()).isNull();
        Assertions.assertThat(((VisitWebsiteAction) result).getUri().toString()).isEqualTo("http://www.google.com");

    }

    @Test
    public void should_parse_action_type_inapp_action() throws IOException {
        Action result = getAction(action_factory_004);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isInstanceOf(InAppAction.class);
        Assertions.assertThat(((InAppAction) result).getSubject()).isNotEmpty();
        Assertions.assertThat(((InAppAction) result).getBody()).isNotEmpty();
        Assertions.assertThat(((InAppAction) result).getUri().toString()).isEqualTo("http://www.google.com");
    }

    @Test
    public void should_parse_action_type_inapp_action_with_just_url() throws IOException {
        Action result = getAction(action_factory_005);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isInstanceOf(InAppAction.class);
        Assertions.assertThat(((InAppAction) result).getSubject()).isNull();
        Assertions.assertThat(((InAppAction) result).getBody()).isNull();
        Assertions.assertThat(((InAppAction) result).getUri().toString()).isEqualTo("http://www.google.com");
    }

    @Test
    public void should_parse_action_type_inapp_action_null_url() throws IOException {
        Action result = getAction(action_factory_null_url);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isInstanceOf(InAppAction.class);
        Assertions.assertThat(((InAppAction) result).getSubject()).isNotNull();
        Assertions.assertThat(((InAppAction) result).getBody()).isNull();
        Assertions.assertThat(((InAppAction) result).getUri().toString()).isEqualTo("");

    }

    @Test
    public void should_parse_action_type_inapp_action_empty_url() throws IOException {
        Action result = getAction(action_factory_empty_url);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isInstanceOf(InAppAction.class);
        Assertions.assertThat(((InAppAction) result).getSubject()).isNotEmpty();
        Assertions.assertThat(((InAppAction) result).getBody()).isNotEmpty();
        Assertions.assertThat(((InAppAction) result).getUri().toString()).isEqualTo("");

    }

    @Test
    public void should_parse_action_type_inapp_action_with_deeplink() throws IOException {
        Action result = getAction(action_factory_deeplink_001);

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isInstanceOf(InAppAction.class);
        Assertions.assertThat(result.getPayload()).isNotNull();
        Assertions.assertThat(((InAppAction) result).getSubject()).isNotEmpty();
        Assertions.assertThat(((InAppAction) result).getBody()).isNotEmpty();
        Assertions.assertThat(((InAppAction) result).getUri()).isNotNull();
        Assertions.assertThat(((InAppAction) result).getUri().toString()).isNotEmpty();

    }

    @Test
    public void should_parse_action_type_silent() throws IOException {
        Action result = getAction(action_factory_payload_010_silent_campaign);
        Assertions.assertThat(result.getType()).isEqualTo(ActionType.SILENT);
    }

    @Test()
    public void succeeds_to_parse_a_map_with_null_values() throws IOException {
        Action result = getAction(action_factory_payload_010_silent_campaign_null_map);
        Assertions.assertThat(result.getType()).isEqualTo(ActionType.SILENT);
    }

    @Test
    public void should_parse_action_type_silent_without_content() throws IOException {
        Action result = getAction(action_factory_payload_010_silent_campaign_no_content_map);
        Assertions.assertThat(result.getType()).isEqualTo(ActionType.SILENT);
    }

    private Action getAction(int resourceID) throws IOException {
        String string = Utils.getRawResourceAsString(resourceID, context);
        ResolveAction input = gson.fromJson(string, ResolveAction.class);
        BeaconEvent beaconEvent = ResolveAction.BEACON_EVENT_MAPPER.map(input);
        return beaconEvent != null ? beaconEvent.getAction() : null;
    }


    @Test
    public void should_validate_valid_all_urls_for_url_message() {
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK, ActionFactory.ServerType.URL_MESSAGE))
                .isEqualTo("http://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTPS_LINK, ActionFactory.ServerType.URL_MESSAGE))
                .isEqualTo("https://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PARAMS, ActionFactory.ServerType.URL_MESSAGE))
                .isEqualTo("http://google.com?test=test&test2=test2");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PAGE, ActionFactory.ServerType.URL_MESSAGE))
                .isEqualTo("http://google.com/test.html");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_DEEPLINK, ActionFactory.ServerType.URL_MESSAGE))
                .isEqualTo("android-app://com.sensorberg.bconfig");
    }

    @Test
    public void should_validate_valid_only_network_urls_for_visit_website_message() {
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK, ActionFactory.ServerType.VISIT_WEBSITE))
                .isEqualTo("http://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTPS_LINK, ActionFactory.ServerType.VISIT_WEBSITE))
                .isEqualTo("https://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PARAMS, ActionFactory.ServerType.VISIT_WEBSITE))
                .isEqualTo("http://google.com?test=test&test2=test2");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PAGE, ActionFactory.ServerType.VISIT_WEBSITE))
                .isEqualTo("http://google.com/test.html");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_DEEPLINK, ActionFactory.ServerType.VISIT_WEBSITE)).isEmpty();
    }

    @Test
    public void should_validate_valid_all_urls_for_in_app_message() {
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK, ActionFactory.ServerType.IN_APP)).isEqualTo("http://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTPS_LINK, ActionFactory.ServerType.IN_APP)).isEqualTo("https://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PARAMS, ActionFactory.ServerType.IN_APP))
                .isEqualTo("http://google.com?test=test&test2=test2");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PAGE, ActionFactory.ServerType.IN_APP))
                .isEqualTo("http://google.com/test.html");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_DEEPLINK, ActionFactory.ServerType.IN_APP))
                .isEqualTo("android-app://com.sensorberg.bconfig");
    }

    @Test
    public void should_validate_urls_and_deeplinks() {
        Assertions.assertThat(ActionFactory.validatedUrl("http://google.com")).isTrue();
        Assertions.assertThat(ActionFactory.validatedUrl("https://google.com")).isTrue();
        Assertions.assertThat(ActionFactory.validatedUrl("http://google.com/test.html")).isTrue();
        Assertions.assertThat(ActionFactory.validatedUrl("http://google.com?test=test&test2=test2")).isTrue();
        Assertions.assertThat(ActionFactory.validatedUrl("android-app://com.sensorberg.bconfig")).isTrue();
    }
}
