package com.sensorberg.utils;

import java.util.Map;
import java.util.regex.Pattern;

public class AttributeValidator {

    private static final String VALID_INPUT_REG = "^[a-zA-Z0-9_]+";

    public static boolean isInputValid(Map<String, String> attributes) {
        Pattern pattern = Pattern.compile(VALID_INPUT_REG);
        for (String key : attributes.keySet()) {
            if (!pattern.matcher(key).matches()) {
                return false;
            }
        }
        for (String val : attributes.values()) {
            if (!pattern.matcher(val).matches()) {
                return false;
            }
        }
        return true;
    }
}
