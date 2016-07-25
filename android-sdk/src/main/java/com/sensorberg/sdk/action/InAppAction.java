package com.sensorberg.sdk.action;

import android.net.Uri;
import android.os.Parcel;

import java.util.UUID;

import lombok.ToString;

@ToString
public class InAppAction extends Action implements android.os.Parcelable {

    private final String subject;

    private final String body;

    private final Uri uri;

    public InAppAction(UUID uuid, String subject, String body, String payload, Uri uri, long delayTime) {
        super(ActionType.MESSAGE_IN_APP, delayTime, uuid, payload);
        this.subject = subject;
        this.body = body;
        this.uri = uri;
    }

    /**
     * the subject of this action as entered on the web interface. This field is optional!
     *
     * @return the subject or null
     */
    public String getSubject() {
        return subject;
    }

    /**
     * the body of the action as entered in the web interface. This field is optional!
     *
     * @return the body as a string or null
     */
    public String getBody() {
        return body;
    }

    /**
     * the URL of the website as entered in the web interface. This field is mandatory.
     *
     * @return the url
     */
    public Uri getUri() {
        return uri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.subject);
        dest.writeString(this.body);
        dest.writeParcelable(this.uri, 0);
    }

    private InAppAction(Parcel in) {
        super(in);
        this.subject = in.readString();
        this.body = in.readString();
        this.uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<InAppAction> CREATOR = new Creator<InAppAction>() {
        public InAppAction createFromParcel(Parcel source) {
            return new InAppAction(source);
        }

        public InAppAction[] newArray(int size) {
            return new InAppAction[size];
        }
    };
}
