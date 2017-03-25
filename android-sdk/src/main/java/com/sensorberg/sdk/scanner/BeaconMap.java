package com.sensorberg.sdk.scanner;

import com.sensorberg.sdk.internal.interfaces.FileManager;
import com.sensorberg.sdk.model.BeaconId;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BeaconMap {

    private FileManager fileManager;

    public interface Filter {

        boolean filter(EventEntry beaconEntry, BeaconId beaconId);
    }

    private final HashMap<BeaconId, EventEntry> storage;

    private final File fileForPersistance;

    public BeaconMap(FileManager fm, File file) {
        fileManager = fm;
        fileForPersistance = file;

        if (fileForPersistance != null) {
            storage = readBeaconEntriesFile(fileForPersistance);

        } else {
            storage = new HashMap<>();
        }
    }

    public int size() {
        return storage.size();
    }

    public void clear() {
        storage.clear();
        deleteFile();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void deleteFile() {
        if (fileForPersistance != null) {
            fileForPersistance.delete();
        }
    }

    public EventEntry get(BeaconId beaconId) {
        return storage.get(beaconId);
    }

    public void put(BeaconId beaconId, EventEntry entry) {
        storage.put(beaconId, entry);
        persist();
    }

    public void addScanPauseTime(long pause) {
        for(Map.Entry<BeaconId, EventEntry> entry : storage.entrySet()) {
            storage.put(entry.getKey(),
                    new EventEntry(
                            entry.getValue().getLastBeaconTime(),
                            entry.getValue().getScanPauseTime() + pause,
                            entry.getValue().getEventMask(),
                            entry.getValue().getPairingId()
                    )
            );
        }
    }

    public void filter(Filter filter) {
        boolean modified = false;
        Iterator<Map.Entry<BeaconId, EventEntry>> iterator = storage.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BeaconId, EventEntry> enteredBeacon = iterator.next();
            EventEntry beaconEntry = enteredBeacon.getValue();
            BeaconId beaconId = enteredBeacon.getKey();
            if (filter.filter(beaconEntry, beaconId)) {
                iterator.remove();
                modified = true;
            }
        }
        if (modified) {
            persist();
        }
    }

    private void persist() {
        if (fileForPersistance != null) {
            fileManager.write(storage, fileForPersistance);
        }
    }

    private HashMap<BeaconId, EventEntry> readBeaconEntriesFile(File file) {
        HashMap<BeaconId, EventEntry> value;
        try {
            //noinspection unchecked if it fails, see catch block
            value = (HashMap<BeaconId, EventEntry>) fileManager.getContentsOfFileOrNull(file);
            if (value == null) {
                return new HashMap<>();
            }
        } catch (ClassCastException e) {
            return new HashMap<>();
        }
        return value;
    }
}
