package com.sensorberg.sdk.action;

import android.os.Parcel;

import java.util.UUID;

public class SilentAction extends Action {

    public static final Creator<SilentAction> CREATOR = new Creator<SilentAction>() {
        public SilentAction createFromParcel(Parcel in) {
            return (new SilentAction(in));
        }

        public SilentAction[] newArray(int size) {
            return (new SilentAction[size]);
        }
    };

    protected SilentAction(UUID uuid) {
        super(ActionType.SILENT, 0, uuid, null);
    }

    protected SilentAction(Parcel source) {
        super(source);
    }
}
