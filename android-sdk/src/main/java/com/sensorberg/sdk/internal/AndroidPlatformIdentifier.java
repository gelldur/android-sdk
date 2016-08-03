package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.BuildConfig;
import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.PlatformIdentifier;
import com.sensorberg.sdk.settings.SharedPreferencesKeys;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.UUID;

import static com.sensorberg.utils.UUIDUtils.uuidWithoutDashesString;

public class AndroidPlatformIdentifier implements PlatformIdentifier {

    private static final String SENSORBERG_PREFERENCE_INSTALLATION_IDENTIFIER = "com.sensorberg.preferences.installationUuidIdentifier";

    private final Context context;

    private final SharedPreferences sharedPreferences;

    private String userAgentString;

    private String deviceInstallationIdentifier;

    private String advertiserIdentifier;

    public AndroidPlatformIdentifier(Context ctx, SharedPreferences sharedPrefs) {
        context = ctx;
        sharedPreferences = sharedPrefs;

        if (sharedPrefs.contains(SharedPreferencesKeys.Network.ADVERTISING_IDENTIFIER)) {
            advertiserIdentifier = sharedPrefs.getString(SharedPreferencesKeys.Network.ADVERTISING_IDENTIFIER, null);
        }
        deviceInstallationIdentifier = getOrCreateInstallationIdentifier();
    }

    @SuppressWarnings({"StringConcatenationInsideStringBufferAppend", "StringBufferReplaceableByString"})
    @Override
    public String getUserAgentString() {
        if (userAgentString == null) {
            String packageName = context.getPackageName();
            String appLabel = URLEncoder.encode(getAppLabel(context));
            String appVersion = getAppVersionString(context);

            StringBuilder userAgent = new StringBuilder();
            userAgent.append(appLabel + "/" + packageName + "/" + appVersion);
            userAgent.append(" ");
            //noinspection deprecation old API compatability
            userAgent.append("(Android " + Build.VERSION.RELEASE + " " + Build.CPU_ABI + ")");
            userAgent.append(" ");
            userAgent.append("(" + Build.MANUFACTURER + ":" + android.os.Build.MODEL + ":" + android.os.Build.PRODUCT + ")");
            userAgent.append(" ");
            userAgent.append("Sensorberg SDK " + BuildConfig.VERSION_NAME);
            userAgentString = userAgent.toString();
        }
        return userAgentString;
    }

    @Override
    public String getDeviceInstallationIdentifier() {


        return deviceInstallationIdentifier;
    }

    @Override
    public String getAdvertiserIdentifier() {
        return advertiserIdentifier;
    }

    @Override
    public void setAdvertisingIdentifier(String advertisingIdentifier) {
        advertiserIdentifier = advertisingIdentifier;
        persistAdvertiserIdentifier(advertiserIdentifier);
    }


    private String getOrCreateInstallationIdentifier() {
        String value;

        String uuidString = sharedPreferences.getString(SENSORBERG_PREFERENCE_INSTALLATION_IDENTIFIER, null);
        if (uuidString != null) {
            value = uuidString;
        } else {
            value = uuidWithoutDashesString(UUID.randomUUID());
            persistInstallationIdentifier(value);
        }
        return value;
    }

    /**
     * Persists the installation identifier value to preferences.
     *
     * @param value - Value to save.
     */
    @SuppressLint("CommitPrefEdits")
    private void persistInstallationIdentifier(String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SENSORBERG_PREFERENCE_INSTALLATION_IDENTIFIER, value);
        editor.commit();
    }

    /**
     * Persists the advertiser identifier value to preferences.
     *
     * @param advertisingIdentifier - Value to save.
     */
    @SuppressLint("CommitPrefEdits")
    private void persistAdvertiserIdentifier(String advertisingIdentifier) {
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (advertisingIdentifier == null) {
            editor.remove(SharedPreferencesKeys.Network.ADVERTISING_IDENTIFIER);
        } else {
            editor.putString(SharedPreferencesKeys.Network.ADVERTISING_IDENTIFIER, advertisingIdentifier);
            editor.apply();
        }
    }

    private static String getAppVersionString(Context context) {
        try {
            PackageInfo myInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return URLEncoder.encode(myInfo.versionName) + "/" + myInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            return "<unknown>";
        } catch (NullPointerException e) {
            return "<unknown>";
        }
    }

    @TargetApi(Build.VERSION_CODES.DONUT)
    private static String getAppLabel(Context application) {
        PackageManager pm = application.getPackageManager();
        ApplicationInfo ai = application.getApplicationInfo();
        return String.valueOf(pm.getApplicationLabel(ai));
    }
}
