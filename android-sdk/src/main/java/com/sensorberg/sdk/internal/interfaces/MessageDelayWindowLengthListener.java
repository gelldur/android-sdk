package com.sensorberg.sdk.internal.interfaces;

public interface MessageDelayWindowLengthListener {

    MessageDelayWindowLengthListener NONE = new MessageDelayWindowLengthListener() {

        @Override
        public void setMessageDelayWindowLength(long messageDelayWindowLength) {
            //do nothing
        }
    };

    void setMessageDelayWindowLength(long messageDelayWindowLength);
}
