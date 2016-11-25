package com.sensorberg.sdk;

public class SensorbergServiceMessage {

    public static final int MSG_APPLICATION_IN_FOREGROUND = 1;

    public static final int MSG_APPLICATION_IN_BACKGROUND = 2;

    public static final int MSG_SET_API_TOKEN = 3;

    public static final int MSG_PRESENT_ACTION = 4;

    public static final int MSG_SHUTDOWN = 6;

    public static final int MSG_PING = 7;

    public static final int MSG_BLUETOOTH = 8;

    public static final int MSG_SDK_SCANNER_MESSAGE = 9;

    public static final int MSG_UPLOAD_HISTORY = 10;

    public static final int MSG_BEACON_LAYOUT_UPDATE = 11;

    public static final int MSG_LOCATION_SERVICES_IS_SET = 13;

    public static final int MSG_SET_API_ADVERTISING_IDENTIFIER = 12;

    public static final int GENERIC_TYPE_BEACON_ACTION = 1001;

    public static final int MSG_REGISTER_PRESENTATION_DELEGATE = 100;

    public static final int MSG_UNREGISTER_PRESENTATION_DELEGATE = 101;

    public static final int MSG_SETTINGS_UPDATE = 102;

    public static final int MSG_TYPE_DISABLE_LOGGING = 103;

    public static final int MSG_TYPE_ENABLE_LOGGING = 104;

    public static final int MSG_LOCATION_NOT_SET_WHEN_NEEDED = 106;

    public static final int MSG_LOCATION_SET = 107;

    public static final int MSG_CONVERSION = 200;

    public static final int MSG_ATTRIBUTES = 300;

    public static final String MSG_SET_API_TOKEN_TOKEN = "com.sensorberg.android.sdk.message.setApiToken.apiTokenString";

    public static final String MSG_PRESENT_ACTION_BEACONEVENT = "com.sensorberg.android.sdk.message.presentBeaconEvent.beaconEvent";

    public static final String MSG_SET_API_ADVERTISING_IDENTIFIER_ADVERTISING_IDENTIFIER
            = "com.sensorberg.android.sdk.message.setAdvertisingIdentifier.advertisingIdentifier";

    public static final String SERVICE_CONFIGURATION = "serviceConfiguration";

    public static final String EXTRA_API_KEY = "com.sensorberg.android.sdk.intent.apiKey";

    public static final String EXTRA_BLUETOOTH_STATE = "com.sensorberg.android.sdk.intent.bluetoothState";

    public static final String EXTRA_GENERIC_WHAT = "com.sensorberg.android.sdk.intent.generic.what";

    public static final String EXTRA_GENERIC_TYPE = "com.sensorberg.android.sdk.intent.generic.type";

    public static final String EXTRA_GENERIC_INDEX = "com.sensorberg.android.sdk.intent.generic.index";

    public static final String EXTRA_START_SERVICE = "com.sensorberg.android.sdk.intent.startService";

    public static final String EXTRA_MESSENGER = "com.sensorberg.android.sdk.intent.messenger";

    public static final String EXTRA_LOCATION_PERMISSION = "com.sensorberg.android.sdk.intent.permissionState";

    public static final String EXTRA_CONVERSION = "com.sensorberg.android.sdk.intent.conversion";

    public static final String EXTRA_ATTRIBUTES_SET = "com.sensorberg.android.sdk.intent.attributes.set";

    public static final String EXTRA_ATTRIBUTES_ADD = "com.sensorberg.android.sdk.intent.attributes.add";

    public static final String EXTRA_ATTRIBUTES_REMOVE = "com.sensorberg.android.sdk.intent.attributes.remove";

    private SensorbergServiceMessage() {
        throw new IllegalAccessError("Utility class");
    }

    @SuppressWarnings({"squid:S1142", "squid:MethodCyclomaticComplexity"})
    public static String stringFrom(int what) {
        switch (what) {
            case MSG_APPLICATION_IN_FOREGROUND:
                return "MSG_APPLICATION_IN_FOREGROUND";
            case MSG_APPLICATION_IN_BACKGROUND:
                return "MSG_APPLICATION_IN_BACKGROUND";
            case MSG_SET_API_TOKEN:
                return "MSG_SET_API_TOKEN";
            case MSG_PRESENT_ACTION:
                return "MSG_PRESENT_ACTION";
            case MSG_REGISTER_PRESENTATION_DELEGATE:
                return "MSG_REGISTER_PRESENTATION_DELEGATE";
            case MSG_UNREGISTER_PRESENTATION_DELEGATE:
                return "MSG_UNREGISTER_PRESENTATION_DELEGATE";
            case MSG_SHUTDOWN:
                return "MSG_SHUTDOWN";
            case MSG_PING:
                return "MSG_PING";
            case MSG_BLUETOOTH:
                return "MSG_BLUETOOTH";
            case MSG_SETTINGS_UPDATE:
                return "MSG_SETTINGS_UPDATE";
            case GENERIC_TYPE_BEACON_ACTION:
                return "GENERIC_TYPE_BEACON_ACTION";
            case MSG_TYPE_DISABLE_LOGGING:
                return "MSG_TYPE_DISABLE_LOGGING";
            case MSG_TYPE_ENABLE_LOGGING:
                return "MSG_TYPE_ENABLE_LOGGING";
            case MSG_SDK_SCANNER_MESSAGE:
                return "MSG_SDK_SCANNER_MESSAGE";
            case MSG_UPLOAD_HISTORY:
                return "MSG_UPLOAD_HISTORY";
            case MSG_BEACON_LAYOUT_UPDATE:
                return "MSG_BEACON_LAYOUT_UPDATE";
            case MSG_SET_API_ADVERTISING_IDENTIFIER:
                return "MSG_SET_API_ADVERTISING_IDENTIFIER";
            case MSG_LOCATION_SERVICES_IS_SET:
                return "MSG_LOCATION_SERVICES_IS_SET";
            case MSG_CONVERSION:
                return "MSG_CONVERSION";
            case MSG_ATTRIBUTES:
                return "MSG_ATTRIBUTES";
            default:
                return "unknown message" + what;
        }
    }
}
