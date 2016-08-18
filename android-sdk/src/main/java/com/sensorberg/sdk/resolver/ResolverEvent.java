package com.sensorberg.sdk.resolver;

import android.os.Message;

final class ResolverEvent {

    public static final int RESOLUTION_START_REQUESTED = 1;

    public static Message asMessage(int type, Object data0){
        Message message = Message.obtain();
        message.arg1 = type;
        message.obj = data0;
        return message;
    }
}
