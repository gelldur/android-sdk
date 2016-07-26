package com.sensorberg.sdk.action;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.UUID;

import lombok.ToString;

/**
 * Class {@link Action} implements an action for a {@link com.sensorberg.sdk.resolver.BeaconEvent}; this is the action put into the sensorberg
 * backend.
 */
@ToString
public abstract class Action implements Parcelable {

    public static final long NO_DELAY = 0L;

    public static final String INTENT_KEY = "com.sensorberg.sdk.Action";

    private final ActionType type;

    private final long delayTime;

    private final UUID uuid;

    private final String payload;

    protected Action(ActionType type, long delayTime, UUID uuid, String payload) {
        this.type = type;
        this.delayTime = delayTime;
        this.uuid = uuid;
        this.payload = payload;
    }

    protected Action(Parcel source) {
        this.type = ActionType.fromId(source.readInt());
        this.delayTime = source.readLong();
        this.uuid = UUID.fromString(source.readString());
        this.payload = source.readString();
    }

    public int describeContents() {
        return (1);
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeInt(type.getId());
        destination.writeLong(delayTime);
        destination.writeString(uuid.toString());
        destination.writeString(payload);
    }

    /**
     * Returns the type of the {@link Action}; types below 0x10000000 are reserved for the sensorberg API, i.e. use 0x1000000 or above for custom
     * types.
     *
     * @return the type of the {@link Action}
     */
    public ActionType getType() {
        return (type);
    }

    /**
     * the server uuid of this action. can be  used to identify an action
     *
     * @return the uuid
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the time that was specified by the backend, which should be used to delay the message.
     * If you implement you own Action handling, you need to take care of this! Default is @{NO_DELAY} when
     * no delay was set.
     *
     * @return time in milli seconds
     */
    public long getDelayTime() {
        return delayTime;
    }

    /**
     * get the raw Payload, serialized as a String as entered in the campaign management.
     *
     * Note: This could be any kind of JSON value. Encoded you may find a @{org.json.JSONArray},
     *
     * @return Payload or null if none was set
     * @{org.json.JSONObject}, @{java.lang.String}, @{java.lang.Double} ... See <a href="http://json.org/">http://json.org/</a>
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Convenience Method that returns the Payload as a @{org.json.JSONObject} if it is in fact an @{org.json.JSONObject}
     *
     * @return the payload as a parsed Object or null if @{getPayload} is null
     * @throws JSONException when the object was of any other JSON value type. For instance @org.json.{JSONArray} or @{java.langString}...
     */
    public JSONObject getPayloadJSONObject() throws JSONException {
        if (payload == null) {
            return null;
        }
        return new JSONObject(payload);
    }

    /**
     * Convenience Method that returns the Payload as a @{org.json.JSONArray} if it is in fact an @{org.json.JSONArray}
     *
     * @return the payload as a parsed Object or null if @{getPayload} is null
     * @throws JSONException when the object was of any other JSON value type. For instance @{org.json.JSONObject} or @{java.langString}...
     */
    public JSONArray getPayloadJSONArray() throws JSONException {
        if (payload == null) {
            return null;
        }
        return new JSONArray(payload);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Action)) {
            return false;
        }

        Action action = (Action) o;
        return !(uuid != null ? !uuid.equals(action.uuid) : action.uuid != null);
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }
}
