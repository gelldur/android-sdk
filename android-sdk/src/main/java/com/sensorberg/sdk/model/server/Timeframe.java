package com.sensorberg.sdk.model.server;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Date;

@SuppressWarnings("WeakerAccess") //gson serialization
public class Timeframe implements Serializable {

    private static final long serialVersionUID = 5L;

    /**
     * can be null, if so, only the end is important.
     */
    @Expose
    public Date start;
    /**
     * can be null, if so, only the start is important.
     */
    @Expose
    public Date end;

    public Timeframe(Long startMillis, Long endMillis) {
        if(startMillis != null){
            start = new Date(startMillis);
        }
        if (endMillis != null){
            end = new Date(endMillis);
        }
    }

    public boolean valid(long now) {
        return (start == null || now >= start.getTime()) &&
                 (end == null || now <= end.getTime());
    }
}
