package com.sensorberg.sdk.action;

import android.os.Parcel;

import com.sensorberg.utils.Objects;

import java.util.UUID;

import lombok.Getter;
import lombok.ToString;

/**
 * Class {@link UriMessageAction} extends {@link Action} for holding title, content, and a URI.
 */
@ToString
public class UriMessageAction extends Action {

    /**
     * {@link android.os.Parcelable.Creator} for the {@link android.os.Parcelable} interface
     */
    @SuppressWarnings("hiding")
    public static final Creator<UriMessageAction> CREATOR = new Creator<UriMessageAction>() {
        public UriMessageAction createFromParcel(Parcel in) {
            return (new UriMessageAction(in));
        }

        public UriMessageAction[] newArray(int size) {
            return (new UriMessageAction[size]);
        }
    };

    /**
     * -- GETTER --
     * Returns the title
     *
     * @return the title
     */
    @Getter private final String title;

    /**
     * -- GETTER --
     * Returns the content
     *
     * @return the content
     */
    @Getter private final String content;

    /**
     * -- GETTER --
     * Returns the URI of the {@link UriMessageAction}.
     *
     * @return the URI of the {@link UriMessageAction}
     */
    @Getter private final String uri;

    /**
     * Creates and initializes a new {@link UriMessageAction}.
     *
     * @param title     the title of the {@link com.sensorberg.sdk.action.UriMessageAction}
     * @param content   the message of the {@link com.sensorberg.sdk.action.UriMessageAction}
     * @param uri       the URI of the {@link com.sensorberg.sdk.action.UriMessageAction}
     * @param payload   payload from the server
     * @param delayTime delay in millis
     */
    public UriMessageAction(UUID actionUUID, String title, String content, String uri, String payload, long delayTime, String instanceUuid) {
        super(ActionType.MESSAGE_URI, delayTime, actionUUID, payload, instanceUuid);
        this.title = title;
        this.content = content;
        this.uri = uri;
    }

    private UriMessageAction(Parcel source) {
        super(source);
        this.title = source.readString();
        this.content = source.readString();
        this.uri = source.readString();
    }

    /**
     * Returns a hash code bases on the actual contents.
     *
     * @return hashCode
     */
    @Override
    public int hashCode() {
        return (title.hashCode() + content.hashCode() + uri.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || ((Object) this).getClass() != o.getClass()) {
            return false;
        }

        UriMessageAction that = (UriMessageAction) o;

        return Objects.equals(content, that.content) &&
                Objects.equals(title, that.title) &&
                Objects.equals(uri, that.uri);
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        super.writeToParcel(destination, flags);
        destination.writeString(title);
        destination.writeString(content);
        destination.writeString(uri);
    }
}
