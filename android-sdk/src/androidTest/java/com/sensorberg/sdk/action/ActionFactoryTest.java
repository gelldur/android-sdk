package com.sensorberg.sdk.action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import org.fest.assertions.api.Assertions;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.io.IOException;

import util.Utils;

@RunWith(AndroidJUnit4.class)
public class ActionFactoryTest {

    private static final int[] payloadSamples = new int[]{
            com.sensorberg.sdk.test.R.raw.action_factory_payload_001_array,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_002_object,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_004_integer,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_003_boolean,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_005_string,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_006_double,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_008_integer_with_exponent,
            com.sensorberg.sdk.test.R.raw.action_factory_payload_009_empty_string,
    };

    public static final JsonElement JSON_ELEMENT_HTTP_LINK = new JsonPrimitive("http://google.com");
    public static final JsonElement JSON_ELEMENT_HTTPS_LINK = new JsonPrimitive("https://google.com");
    public static final JsonElement JSON_ELEMENT_HTTP_LINK_WITH_PARAMS = new JsonPrimitive("http://google.com?test=test&test2=test2");
    public static final JsonElement JSON_ELEMENT_HTTP_LINK_WITH_PAGE = new JsonPrimitive("http://google.com/test.html");
    public static final JsonElement JSON_ELEMENT_DEEPLINK = new JsonPrimitive("android-app://com.sensorberg.bconfig");

    @Test
    public void should_parse_server_output() {
        try {
            JsonObject URI_JSON_OBJECT = Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_001,
                    InstrumentationRegistry.getContext());
            UriMessageAction result = (UriMessageAction) ActionFactory.actionFromJSONObject(URI_JSON_OBJECT);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result.getContent()).isEqualTo("This is a message");
            Assertions.assertThat(result.getTitle()).isEqualTo("this is a subject");
            Assertions.assertThat(result.getUri()).isEqualTo("");
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_parse_all_non_null_values() throws Exception {
        for (int i : payloadSamples) {
            Action action = ActionFactory.actionFromJSONObject(Utils.getRawResourceAsJSON(i, InstrumentationRegistry.getContext()));
            Assertions.assertThat(action.getPayload()).isNotNull();
        }
    }

    @Test
    public void should_parse_null_payloads() throws IOException, JSONException {
        Action action = ActionFactory.actionFromJSONObject(
                Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_payload_007_null, InstrumentationRegistry.getContext()));
        Assertions.assertThat(action.getPayload()).isNull();
    }

    @Test
    public void should_parse_action_type_url_message() {
        try {
            JsonObject URI_JSON_OBJECT = Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_001,
                    InstrumentationRegistry.getContext());
            Action result = ActionFactory.actionFromJSONObject(URI_JSON_OBJECT);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result).isInstanceOf(UriMessageAction.class);
            Assertions.assertThat(((UriMessageAction) result).getContent()).isNotEmpty();
            Assertions.assertThat(((UriMessageAction) result).getTitle()).isNotEmpty();
            Assertions.assertThat(((UriMessageAction) result).getUri()).isEmpty();

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_parse_action_type_visit_website() {
        try {
            JsonObject URI_JSON_OBJECT = Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_002,
                    InstrumentationRegistry.getContext());
            Action result = ActionFactory.actionFromJSONObject(URI_JSON_OBJECT);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result).isInstanceOf(VisitWebsiteAction.class);
            Assertions.assertThat(((VisitWebsiteAction) result).getSubject()).isNotEmpty();
            Assertions.assertThat(((VisitWebsiteAction) result).getBody()).isNotEmpty();
            Assertions.assertThat(((VisitWebsiteAction) result).getUri().toString()).isEqualTo("http://www.google.com");

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_parse_action_type_visit_website_with_just_url() {
        try {
            JsonObject URI_JSON_OBJECT = Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_003,
                    InstrumentationRegistry.getContext());
            Action result = ActionFactory.actionFromJSONObject(URI_JSON_OBJECT);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result).isInstanceOf(VisitWebsiteAction.class);
            Assertions.assertThat(((VisitWebsiteAction) result).getSubject()).isNull();
            Assertions.assertThat(((VisitWebsiteAction) result).getBody()).isNull();
            Assertions.assertThat(((VisitWebsiteAction) result).getUri().toString()).isEqualTo("http://www.google.com");

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_parse_action_type_inapp_action() {
        try {
            JsonObject URI_JSON_OBJECT = Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_004,
                    InstrumentationRegistry.getContext());
            Action result = ActionFactory.actionFromJSONObject(URI_JSON_OBJECT);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result).isInstanceOf(InAppAction.class);
            Assertions.assertThat(((InAppAction) result).getSubject()).isNotEmpty();
            Assertions.assertThat(((InAppAction) result).getBody()).isNotEmpty();
            Assertions.assertThat(((InAppAction) result).getUri().toString()).isEqualTo("http://www.google.com");

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_parse_action_type_inapp_action_with_just_url() {
        try {
            JsonObject URI_JSON_OBJECT = Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_005,
                    InstrumentationRegistry.getContext());
            Action result = ActionFactory.actionFromJSONObject(URI_JSON_OBJECT);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result).isInstanceOf(InAppAction.class);
            Assertions.assertThat(((InAppAction) result).getSubject()).isNull();
            Assertions.assertThat(((InAppAction) result).getBody()).isNull();
            Assertions.assertThat(((InAppAction) result).getUri().toString()).isEqualTo("http://www.google.com");

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_parse_action_type_inapp_action_null_url() {
        try {
            JsonObject URI_JSON_OBJECT = Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_null_url,
                    InstrumentationRegistry.getContext());
            Action result = ActionFactory.actionFromJSONObject(URI_JSON_OBJECT);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result).isInstanceOf(InAppAction.class);
            Assertions.assertThat(((InAppAction) result).getSubject()).isNotNull();
            Assertions.assertThat(((InAppAction) result).getBody()).isNull();
            Assertions.assertThat(((InAppAction) result).getUri().toString()).isEqualTo("");

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_parse_action_type_inapp_action_empty_url() {
        try {
            JsonObject URI_JSON_OBJECT = Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_empty_url,
                    InstrumentationRegistry.getContext());
            Action result = ActionFactory.actionFromJSONObject(URI_JSON_OBJECT);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result).isInstanceOf(InAppAction.class);
            Assertions.assertThat(((InAppAction) result).getSubject()).isNotEmpty();
            Assertions.assertThat(((InAppAction) result).getBody()).isNotEmpty();
            Assertions.assertThat(((InAppAction) result).getUri().toString()).isEqualTo("");

        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_parse_action_type_inapp_action_with_deeplink() {
        try {
            JsonObject URI_JSON_OBJECT = Utils.getRawResourceAsJSON(com.sensorberg.sdk.test.R.raw.action_factory_deeplink_001,
                    InstrumentationRegistry.getContext());
            Action result = ActionFactory.actionFromJSONObject(URI_JSON_OBJECT);

            Assertions.assertThat(result).isNotNull();
            Assertions.assertThat(result).isInstanceOf(InAppAction.class);
            Assertions.assertThat(result.getPayload()).isNotNull();
            Assertions.assertThat(((InAppAction) result).getSubject()).isNotEmpty();
            Assertions.assertThat(((InAppAction) result).getBody()).isNotEmpty();
            Assertions.assertThat(((InAppAction) result).getUri()).isNotNull();
            Assertions.assertThat(((InAppAction) result).getUri().toString()).isNotEmpty();
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
    }

    @Test
    public void should_validate_valid_only_network_urls_for_url_message() {
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK, ActionFactory.ServerType.URL_MESSAGE)).isEqualTo("http://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTPS_LINK, ActionFactory.ServerType.URL_MESSAGE)).isEqualTo("https://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PARAMS, ActionFactory.ServerType.URL_MESSAGE)).isEqualTo("http://google.com?test=test&test2=test2");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PAGE, ActionFactory.ServerType.URL_MESSAGE)).isEqualTo("http://google.com/test.html");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_DEEPLINK, ActionFactory.ServerType.URL_MESSAGE)).isEmpty();
    }

    @Test
    public void should_validate_valid_only_network_urls_for_visit_website_message() {
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK, ActionFactory.ServerType.VISIT_WEBSITE)).isEqualTo("http://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTPS_LINK, ActionFactory.ServerType.VISIT_WEBSITE)).isEqualTo("https://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PARAMS, ActionFactory.ServerType.VISIT_WEBSITE)).isEqualTo("http://google.com?test=test&test2=test2");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PAGE, ActionFactory.ServerType.VISIT_WEBSITE)).isEqualTo("http://google.com/test.html");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_DEEPLINK, ActionFactory.ServerType.VISIT_WEBSITE)).isEmpty();
    }

    @Test
    public void should_validate_valid_all_urls_for_in_app_message() {
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK, ActionFactory.ServerType.IN_APP)).isEqualTo("http://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTPS_LINK, ActionFactory.ServerType.IN_APP)).isEqualTo("https://google.com");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PARAMS, ActionFactory.ServerType.IN_APP)).isEqualTo("http://google.com?test=test&test2=test2");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_HTTP_LINK_WITH_PAGE, ActionFactory.ServerType.IN_APP)).isEqualTo("http://google.com/test.html");
        Assertions.assertThat(ActionFactory.getUriFromJson(JSON_ELEMENT_DEEPLINK, ActionFactory.ServerType.IN_APP)).isEqualTo("android-app://com.sensorberg.bconfig");
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
