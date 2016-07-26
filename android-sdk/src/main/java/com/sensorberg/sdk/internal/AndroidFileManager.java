package com.sensorberg.sdk.internal;

import com.sensorberg.sdk.Logger;
import com.sensorberg.sdk.internal.interfaces.FileManager;

import android.content.Context;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class AndroidFileManager implements FileManager {

    protected final Context context;

    public AndroidFileManager(Context ctx) {
        context = ctx;
    }

    @Override
    public File getFile(String fileName) {
        return new File(context.getFilesDir() + File.separator + fileName);
    }

    @Override
    public void write(Serializable serializableObject, String fileName) {
        write(serializableObject, getFile(fileName));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void removeFile(String fileName) {
        getFile(fileName).delete();
    }

    @Override
    public boolean write(Serializable object, File file) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
            return true;
        } catch (Exception e) {
            Logger.log.logError("error writing file", e);
        } finally {
            Closeables.close(fos);
            Closeables.close(oos);
        }
        return false;
    }

    @Override
    public Object getContentsOfFileOrNull(File file) {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            return ois.readObject();
        } catch (Exception e) {
            Logger.log.logError("error reading file", e);
        } finally {
            Closeables.close(fis);
            Closeables.close(ois);
        }

        return null;
    }

    static class Closeables {

        @SuppressWarnings("EmptyCatchBlock")
        public static void close(Closeable closeable) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
