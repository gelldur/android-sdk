package com.sensorberg.sdk.action;

import android.net.Uri;
import android.os.Parcel;

import java.util.UUID;

import lombok.Getter;
import lombok.ToString;

@ToString
public class VisitWebsiteAction extends Action implements android.os.Parcelable {

    /**
     * -- GETTER --
     * the subject of this action as entered on the web interface. This field is optional!
     *
     * @return the subject or null
     */
    @Getter private final String subject;

    /**
     * -- GETTER --
     * the body of the action as entered in the web interface. This field is optional!
     *
     * @return the body as a string or null
     */
    @Getter private final String body;

    /**
     * -- GETTER --
     * the URL of the website as entered in the web interface. This field is mandatory.
     *
     * @return the http or https uri
     */
    @Getter private final Uri uri;

    public VisitWebsiteAction(UUID actionUUID, String subject, String body, Uri uri, String payload, long delayTime, String instanceUuid) {
        super(ActionType.MESSAGE_WEBSITE, delayTime, actionUUID, payload, instanceUuid);
        this.subject = subject;
        this.body = body;
        this.uri = uri;
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
        dest.writeParcelable(this.uri, flags);
    }

    private VisitWebsiteAction(Parcel in) {
        super(in);
        this.subject = in.readString();
        this.body = in.readString();
        this.uri = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<VisitWebsiteAction> CREATOR = new Creator<VisitWebsiteAction>() {
        public VisitWebsiteAction createFromParcel(Parcel source) {
            return new VisitWebsiteAction(source);
        }

        public VisitWebsiteAction[] newArray(int size) {
            return new VisitWebsiteAction[size];
        }
    };
}
