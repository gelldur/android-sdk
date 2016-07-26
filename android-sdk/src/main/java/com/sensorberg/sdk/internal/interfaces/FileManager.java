package com.sensorberg.sdk.internal.interfaces;

import java.io.File;
import java.io.Serializable;

public interface FileManager {

    File getFile(String fileName);

    void write(Serializable serializableObject, String fileName);

    boolean write(Serializable serializableObject, File file);

    Object getContentsOfFileOrNull(File file);

    void removeFile(String fileName);
}
