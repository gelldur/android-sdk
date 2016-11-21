package com.sensorberg.sdk.model.persistence;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

public class ActionConversion implements Parcelable {

    public static final String SHARED_PREFS_TAG = "com.sensorberg.sdk.InternalActionConversion";

    /**
     * Action was given to the app, but app did not return cofirmation
     * that it made attempt to show it to the user. This is the situation where e.g.
     * app delays showing notification to the user for whatever reason.
     */
    public static final int TYPE_SUPPRESSED = -1;

    /**
     * App has confirmed via {@link com.sensorberg.SensorbergSdk#notifyActionShowAttempt(UUID, Context) SensorbergSdk.notifyActionShowAttempt}
     * that the action was shown to  the user by notification or otherwise.
     */
    public static final int TYPE_IGNORED = 0;

    /**
     * App has confirmed via {@link com.sensorberg.SensorbergSdk#notifyActionSuccess(UUID, Context)}  SensorbergSdk.notifyActionSuccess}
     * that the user acknowledged the action (e.g. user opened notification).
     */
    public static final int TYPE_SUCCESS = 1;

    @Expose
    @Getter
    @Setter
    @SerializedName("action")
    private final String action;

    @Expose
    @Getter
    @Setter
    @SerializedName("dt")
    private final long date;

    @Expose
    @Getter
    @Setter
    @SerializedName("type")
    private final int type;

    @Expose
    @Getter
    @Setter
    @SerializedName("location")
    private String geohash;

    public ActionConversion(UUID uuid, int type) {
        this.action = uuid.toString();
        this.type = type;
        this.date = System.currentTimeMillis();
    }

    protected ActionConversion(Parcel in) {
        action = in.readString();
        date = in.readLong();
        type = in.readInt();
        geohash = in.readString();
    }

    public static final Creator<ActionConversion> CREATOR = new Creator<ActionConversion>() {
        @Override
        public ActionConversion createFromParcel(Parcel in) {
            return new ActionConversion(in);
        }

        @Override
        public ActionConversion[] newArray(int size) {
            return new ActionConversion[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(action);
        parcel.writeLong(date);
        parcel.writeInt(type);
        parcel.writeString(geohash);
    }
}
