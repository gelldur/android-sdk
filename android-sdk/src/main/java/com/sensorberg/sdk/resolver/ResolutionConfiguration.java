package com.sensorberg.sdk.resolver;

import com.sensorberg.sdk.scanner.ScanEvent;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class {@link ResolutionConfiguration} implements configuration functionality for a {@link Resolution}.
 */
public final class ResolutionConfiguration implements Parcelable{

    public int retry;
    public int maxRetries;
    public long millisBetweenRetries;

    private ScanEvent scanEvent;

    /**
     * Creates and initializes a new {@link ResolutionConfiguration}.
     */
    public ResolutionConfiguration() {
        this.scanEvent = null;
        retry = 0;
        maxRetries = 0;
        millisBetweenRetries = 0;
    }

    public ResolutionConfiguration(ScanEvent scanEvent) {
        this();
        this.scanEvent = scanEvent;
    }

    /**
     * Returns the {@link ScanEvent} of the {@link ResolutionConfiguration}.
     *
     * @return the {@link ScanEvent} of the {@link ResolutionConfiguration}
     */
    public ScanEvent getScanEvent() {
        return (scanEvent);
    }

    /**
     * Sets the {@link ScanEvent} of the {@link ResolutionConfiguration}.
     *
     * @param scanEvent the {@link ScanEvent} to be set
     */
    public void setScanEvent(ScanEvent scanEvent) {
        this.scanEvent = scanEvent;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.retry);
        dest.writeInt(this.maxRetries);
        dest.writeLong(this.millisBetweenRetries);
        dest.writeParcelable(this.scanEvent, 0);
    }

    private ResolutionConfiguration(Parcel in) {
        this.retry = in.readInt();
        this.maxRetries = in.readInt();
        this.millisBetweenRetries = in.readLong();
        this.scanEvent = in.readParcelable(ScanEvent.class.getClassLoader());
    }

    public static final Creator<ResolutionConfiguration> CREATOR = new Creator<ResolutionConfiguration>() {
        public ResolutionConfiguration createFromParcel(Parcel source) {
            return new ResolutionConfiguration(source);
        }

        public ResolutionConfiguration[] newArray(int size) {
            return new ResolutionConfiguration[size];
        }
    };

    public boolean canRetry() {
        return maxRetries >= retry + 1;
    }

    public boolean canTry() {
        return maxRetries >= retry;
    }

}
