package com.sensorberg.sdk.model;

import com.sensorberg.utils.Objects;
import com.sensorberg.utils.UUIDUtils;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

/**
 * Class {@link BeaconId} represents the id of a beacon.
 */
public class BeaconId implements Parcelable, Serializable {

    public static final String INTENT_KEY = "com.sensorberg.sdk.BeaconId";

    private static final long serialVersionUID = 1L;

    /**
     * {@link android.os.Parcelable.Creator} for the {@link android.os.Parcelable} interface
     */
    public static final Creator<BeaconId> CREATOR = new Creator<BeaconId>() {
        public BeaconId createFromParcel(Parcel in) {
            return (new BeaconId(in));
        }

        public BeaconId[] newArray(int size) {
            return (new BeaconId[size]);
        }
    };

    private final byte[] beaconId;

    /**
     * Hack for enabling geofences. BeaconId will have either valid beaconId or geofenceId.
     * Non-valid beaconId is all 0's.
     */
    private final String geofenceId;
    transient private UUID uuid = null;
    /**
     * Creates and initializes a new {@link BeaconId}.
     *
     * @param beaconId the beacon id (a concatenation of UUID, major id, and minor id)
     */
    public BeaconId(byte[] beaconId) {
        if ((beaconId == null) || (beaconId.length != 20)) {
            throw (new IllegalArgumentException("Illegal beacon id"));
        }
        this.beaconId = new byte[20];
        System.arraycopy(beaconId, 0x00, this.beaconId, 0, 20);
        geofenceId = null;
    }

    /**
     * Creates and initializes a new {@link BeaconId}.
     *
     * @param data   a block of data containing the beacon id (a concatenation of UUID, major id, and minor id)
     * @param offset the offset of the beacon id in the byte array
     */
    public BeaconId(byte[] data, int offset) {
        if ((data == null) || (data.length < offset + 20)) {
            throw (new IllegalArgumentException("Illegal beacon id"));
        }
        this.beaconId = new byte[20];
        System.arraycopy(data, offset, this.beaconId, 0, 20);
        geofenceId = null;
    }

    protected BeaconId(Parcel source) {
        this.beaconId = new byte[20];
        source.readByteArray(beaconId);
        geofenceId = source.readString();
    }

    /**
     * Creates and initializes a new {@link BeaconId}.
     *
     * @param beaconId the beacon id as a hexadecimal {@link String} of length 40
     */
    public BeaconId(String beaconId) {
        if (beaconId.length() != 40) {
            throw (new IllegalArgumentException("Invalid beacon id"));
        }
        this.beaconId = hexToByteArray(beaconId);
        this.geofenceId = null;
    }

    /**
     * Creates and initializes a new {@link BeaconId}.
     *
     * @param beaconId the beacon id as a hexadecimal {@link String} of length 40
     */
    public BeaconId(String beaconId, String geofenceId) {
        if (beaconId.length() != 40) {
            throw (new IllegalArgumentException("Invalid beacon id"));
        }
        this.beaconId = hexToByteArray(beaconId);
        this.geofenceId = geofenceId;
    }

    private static byte[] hexToByteArray(String hex) throws IllegalArgumentException {
        int length = hex.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            int digitHigh = Character.digit(hex.charAt(i), 16);
            int digitLow = Character.digit(hex.charAt(i + 1), 16);
            if ((digitHigh == -1) || (digitLow == -1)) {
                throw (new IllegalArgumentException("Invalid beacon id"));
            }
            data[i / 2] = (byte) ((digitHigh << 4) + digitLow);
        }
        return (data);
    }

    /**
     * Creates and initializes a new {@link BeaconId}.
     *
     * @param uuid    the {@link UUID} of the {@link BeaconId}
     * @param majorId the major id of the {@link BeaconId} (delivered as {@link Integer} although it is 16-bits; this is due to Java's unsigned limitations)
     * @param minorId the minor id of the {@link BeaconId} (delivered as {@link Integer} although it is 16-bits; this is due to Java's unsigned limitations)
     */
    public BeaconId(UUID uuid, int majorId, int minorId) {
        this.uuid = uuid;
        ByteArrayOutputStream stream = new ByteArrayOutputStream(20);
        DataOutputStream streamOuter = new DataOutputStream(stream);
        try {
            streamOuter.writeLong(uuid.getMostSignificantBits());
            streamOuter.writeLong(uuid.getLeastSignificantBits());
            streamOuter.writeShort(majorId);
            streamOuter.writeShort(minorId);
        } catch (IOException exception) {
            // This cannot happen
        }
        this.beaconId = stream.toByteArray();
        this.geofenceId = null;
    }

    public int describeContents() {
        return (0);
    }

    public void writeToParcel(Parcel destination, int flags) {
        destination.writeByteArray(beaconId);
        destination.writeString(geofenceId);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return (true);
        }
        if (other == null) {
            return (false);
        }
        if (((Object) this).getClass() != other.getClass()) {
            return (false);
        }
        BeaconId otherId = (BeaconId) other;
        return Arrays.equals(beaconId, otherId.beaconId) &&
                Objects.equals(geofenceId, otherId.geofenceId);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(beaconId);
        if (geofenceId != null) {
            result = prime * result + geofenceId.hashCode();
        }
        return (result);
    }

    /**
     * Returns the beacon id (a concatenation of {@link UUID}, major id, and minor id).
     *
     * @return the beacon id
     */
    public byte[] getBeaconId() {
        byte[] beaconIdCopy = new byte[20];
        System.arraycopy(beaconId, 0, beaconIdCopy, 0, 20);
        return (beaconIdCopy);
    }

    public String getGeofenceId() {
        return geofenceId;
    }

    /**
     * Returns the traditional representation of the {@link BeaconId}.
     *
     * @return the traditional representation of the {@link BeaconId}
     */
    public String toTraditionalString() {
        return (String.format(Locale.US, "%s:%d:%d", getUuid(), getMajorId(), getMinorId()));
    }

    /**
     * Returns the major id of the {@link BeaconId}.
     *
     * @return the major id of the {@link BeaconId}
     */
    public int getMajorId() {
        return ((beaconId[0x11] & 0xFF) | (beaconId[0x10] & 0xFF) << 8);
    }

    /**
     * Returns the minor id of the {@link BeaconId}.
     *
     * @return the minor id of the {@link BeaconId}
     */
    public int getMinorId() {
        return ((beaconId[0x13] & 0xFF) | (beaconId[0x12] & 0xFF) << 8);
    }

    /**
     * Returns the {@link UUID} of the {@link BeaconId}.
     *
     * @return the {@link UUID} of the {@link BeaconId}
     */
    public UUID getUuid() {
        if (uuid == null) {
            byte[] uuidHigh = new byte[0x08];
            byte[] uuidLow = new byte[0x08];
            System.arraycopy(beaconId, 0x00, uuidHigh, 0, 0x08);
            System.arraycopy(beaconId, 0x08, uuidLow, 0, 0x08);
            uuid = new UUID(toLong(uuidHigh), toLong(uuidLow));
        }
        return (uuid);
    }

    private static long toLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip();
        return (buffer.getLong());
    }

    public String getNormalizedUUIDString() {
        return UUIDUtils.uuidWithoutDashesString(getUuid());
    }

    public String getPid(){
        return String.format("%1s%2$05d%3$05d", this.getProximityUUIDWithoutDashes(), this.getMajorId(), this.getMinorId());
    }

    @Override
    public String toString() {
        return "BeaconId{" +
                "uuid=" + getNormalizedUUIDString()+
                ", major=" + getMajorId()+
                ", minor=" + getMinorId()+
                ", geofence=" + getGeofenceId()+
                '}';
    }

    public String getProximityUUIDWithoutDashes() {
        return UUIDUtils.uuidWithoutDashesString(this.getUuid());
    }
}
