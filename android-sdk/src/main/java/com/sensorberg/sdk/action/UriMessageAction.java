package com.sensorberg.sdk.action;

import com.sensorberg.utils.Objects;

import android.os.Parcel;

import java.util.UUID;

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

    private final String title;

    private final String content;

    private final String uri;

    /**
     * Creates and initializes a new {@link UriMessageAction}.
     *
     * @param title     the title of the {@link com.sensorberg.sdk.action.UriMessageAction}
     * @param content   the message of the {@link com.sensorberg.sdk.action.UriMessageAction}
     * @param uri       the URI of the {@link com.sensorberg.sdk.action.UriMessageAction}
     * @param payload   payload from the server
     * @param delayTime delay in millis
     */
    public UriMessageAction(UUID actionUUID, String title, String content, String uri, String payload, long delayTime) {
        super(ActionType.MESSAGE_URI, delayTime, actionUUID, payload);
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
     * Returns the URI of the {@link UriMessageAction}.
     *
     * @return the URI of the {@link UriMessageAction}
     */
    public String getUri() {
        return (uri);
    }

    /**
     * Returns the content
     *
     * @return the content
     */
    public String getContent() {
        return (content);
    }

    /**
     * Returns the title
     *
     * @return the title
     */
    public String getTitle() {
        return (title);
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
