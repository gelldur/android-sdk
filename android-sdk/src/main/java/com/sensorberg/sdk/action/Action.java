package com.sensorberg.sdk.action;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import lombok.Getter;
import lombok.ToString;

/**
 * Class {@link Action} implements an action for a {@link com.sensorberg.sdk.resolver.BeaconEvent}; this is the action put into the sensorberg
 * backend.
 */
@ToString
public abstract class Action implements Parcelable {

    public static final long NO_DELAY = 0L;

    public static final String INTENT_KEY = "com.sensorberg.sdk.Action";


    /**
     * -- GETTER --
     * Returns the type of the {@link Action}; types below 0x10000000 are reserved for the sensorberg API, i.e. use 0x1000000 or above for custom
     * types.
     *
     * @return the type of the {@link Action}
     */
    @Getter private final ActionType type;

    /**
     * -- GETTER --
     * Get the time that was specified by the backend, which should be used to delay the message.
     * If you implement you own Action handling, you need to take care of this! Default is @{NO_DELAY} when
     * no delay was set.
     *
     * @return time in milli seconds
     */

    @Getter private final long delayTime;

    /**
     *  -- GETTER --
     * the server uuid of this action. can be  used to identify an action
     *
     * @return the uuid
     */
    @Getter private final UUID uuid;

    /**
     * -- GETTER --
     * get the raw Payload, serialized as a String as entered in the campaign management.
     *
     * Note: This could be any kind of JSON value. Encoded you may find a @{org.json.JSONArray},
     *
     * @return Payload or null if none was set
     * @{org.json.JSONObject}, @{java.lang.String}, @{java.lang.Double} ... See <a href="http://json.org/">http://json.org/</a>
     */

    @Getter private final String payload;

    /**
     *  -- GETTER --
     * the local instance uuid of this action. it is used to match BeaconAction and ActionConversion.
     *
     * @return this action instance uuid
     */
    @Getter private final String instanceUuid;

    protected Action(ActionType type, long delayTime, UUID uuid, String payload, String instanceUuid) {
        this.type = type;
        this.delayTime = delayTime;
        this.uuid = uuid;
        this.payload = payload;
        this.instanceUuid = instanceUuid;
    }

    protected Action(Parcel source) {
        this.type = ActionType.fromId(source.readInt());
        this.delayTime = source.readLong();
        this.uuid = UUID.fromString(source.readString());
        this.payload = source.readString();
        this.instanceUuid = source.readString();
    }

    public int describeContents() {
        return (1);
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeInt(type.getId());
        destination.writeLong(delayTime);
        destination.writeString(uuid.toString());
        destination.writeString(payload);
        destination.writeString(instanceUuid);
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

        boolean uuidEquals =
                (uuid == null && action.uuid == null) ||
                        (uuid != null && action.uuid != null && uuid.equals(action.uuid));
        boolean instanceUuidEquals =
                (instanceUuid == null && action.instanceUuid == null) ||
                        (instanceUuid != null && action.instanceUuid != null && instanceUuid.equals(action.instanceUuid));

        return uuidEquals && instanceUuidEquals;
    }

    @Override
    public int hashCode() {
        return uuid != null ? uuid.hashCode() : 0;
    }
}
