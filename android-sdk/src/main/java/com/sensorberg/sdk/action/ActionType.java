package com.sensorberg.sdk.action;


/**
 * Enumeration {@link ActionType} enumerates the various {@link Action} types; each {@link ActionType} has an id with ids below <code>0x1000000</code> being
 * reserved for the sensorberg API.
 */
public enum ActionType {

    /**
     * Constant indicating a message and URI {@link Action}
     */
    MESSAGE_URI(0x00000101),

    /**
     * Constant indicating a message with optional title and subject {@link Action}
     */
    MESSAGE_WEBSITE(0x00000102),

    /**
     * Constant indicating an action with an in-app URI {@link Action}
     */
    MESSAGE_IN_APP(0x00000103),

    /**
     * Constant indicating a "silent" campaign, which handled only internally in the SDK
     */
    SILENT(0x00000103);

    private final int id;

    ActionType(int id) {
        this.id = id;
    }

    /**
     * Returns the {@link ActionType} for a given id.
     *
     * @param id the id find the {@link ActionType} to
     * @return the {@link ActionType} found or null
     */
    public static ActionType fromId(int id) {
        for (ActionType actionType : ActionType.values()) {
            if (actionType.id == id) {
                return (actionType);
            }
        }
        return (null);
    }

    /**
     * Returns the id of the {@link ActionType}.
     *
     * @return the id of the {@link ActionType}
     */
    public int getId() {
        return (id);
    }
}
