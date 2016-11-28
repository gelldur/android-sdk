package com.sensorberg.sdk.test;

import com.sensorberg.utils.AttributeValidator;

import org.junit.Test;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestAttributesValidator {
    @Test
    public void apiservice_should_validate_user_target_input() throws Exception{
        SortedMap<String, String> attributes = new TreeMap<>();

        attributes.put("qwerty", "asdfgh");
        assertTrue("Valid input failed", AttributeValidator.isInputValid(attributes));

        attributes.clear();
        attributes.put("qwe-rty", "asdfgh");
        assertFalse("Minus symbol should not be allowed", AttributeValidator.isInputValid(attributes));

        attributes.clear();
        attributes.put("qwe.rty", "asdfgh");
        assertFalse("dot symbol should not be allowed", AttributeValidator.isInputValid(attributes));

        attributes.clear();
        attributes.put("qwe rty", "asdfgh");
        assertFalse("space symbol should not be allowed", AttributeValidator.isInputValid(attributes));

        attributes.clear();
        attributes.put("qwe&rty", "asdfgh");
        assertFalse("ampersand symbol should not be allowed", AttributeValidator.isInputValid(attributes));

        attributes.clear();
        attributes.put("qwe?rty", "asdfgh");
        assertFalse("question mark should not be allowed", AttributeValidator.isInputValid(attributes));

        attributes.clear();
        attributes.put("qwe\\rty", "asdfgh");
        assertFalse("\\ symbol should not be allowed", AttributeValidator.isInputValid(attributes));

        attributes.clear();
        attributes.put("qwerty", "!\"£$%^&*()_}{]#'/.=");
        assertFalse("Special symbols should not be allowed", AttributeValidator.isInputValid(attributes));
    }
}
