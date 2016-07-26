package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.AndroidFileManager;

import android.content.Context;

import java.io.File;
import java.io.IOException;

public class TestFileManager extends AndroidFileManager {

    public TestFileManager(Context ctx) {
        super(ctx);
    }

    @Override
    public File getFile(String fileName) {
        try {
            return File.createTempFile(fileName, null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
