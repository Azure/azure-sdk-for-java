package com.azure.identity.implementation.util;

import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for validating parameters.
 */
public class ValidationUtil {
    public static void validate(String className, Map<String, Object> parameters) {
        ClientLogger logger = new ClientLogger(className);
        List<String> missing = new ArrayList<>();
        for (String key : parameters.keySet()) {
            if (parameters.get(key) == null) {
                missing.add(key);
            }
        }
        if (missing.size() > 0) {
            logger.logAndThrow(new IllegalArgumentException("Must provide non-null values for "
                + String.join(", ", missing) + " properties in " + className));
        }
    }
}
