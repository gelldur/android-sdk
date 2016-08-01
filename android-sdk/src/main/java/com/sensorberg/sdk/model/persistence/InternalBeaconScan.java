package com.sensorberg.sdk.model.persistence;

import com.google.gson.annotations.Expose;
import com.sensorberg.sdk.scanner.ScanEvent;
import com.sensorberg.utils.ListUtils;

import lombok.Getter;
import lombok.Setter;

public class InternalBeaconScan {

    public static final String SHARED_PREFS_TAG = "com.sensorberg.sdk.InternalBeaconScans";

    public static final ListUtils.Mapper<InternalBeaconScan, BeaconScan> TO_BEACON_SCAN = new ListUtils.Mapper<InternalBeaconScan, BeaconScan>() {
        @Override
        public BeaconScan map(InternalBeaconScan internalBeaconScan) {
            return internalBeaconScan.getBeaconScan();
        }
    };

    @Expose
    @Getter
    @Setter
    BeaconScan beaconScan;

    @Expose
    @Getter
    @Setter
    private Long sentToServerTimestamp;

    public static InternalBeaconScan from(ScanEvent scanEvent) {
        InternalBeaconScan value = new InternalBeaconScan();
        value.setSentToServerTimestamp(null);
        value.setBeaconScan(BeaconScan.from(scanEvent));
        return value;
    }

}
