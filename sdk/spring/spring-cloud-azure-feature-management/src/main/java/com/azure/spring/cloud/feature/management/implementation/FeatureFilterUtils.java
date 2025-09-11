// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.models.TargetingException;

public class FeatureFilterUtils {

    public static void updateValueFromMapToList(Map<String, Object> parameters, String key) {
        updateValueFromMapToList(parameters, key, false);
    }

    /**
     * Looks at the given key in the parameters and converts it to a list if it is currently a map. Also handles empty
     * strings and null values based on the fixNull parameter.
     *
     * @param parameters map of generic objects
     * @param key key of object in the parameters map
     * @param fixNull whether to convert null values to empty lists
     */
    @SuppressWarnings("unchecked")
    public static void updateValueFromMapToList(Map<String, Object> parameters, String key, boolean fixNull) {
        Object objectMap = parameters.get(key);
        if (objectMap instanceof Map) {
            Collection<Object> toType = ((Map<String, Object>) objectMap).values();
            parameters.put(key, toType);
        } else if ((objectMap != null && objectMap.equals("")) || (objectMap == null && fixNull)) {
            parameters.put(key, new ArrayList<Object>());
        } else if (objectMap != null) {
            parameters.put(key, objectMap);
        }
    }

    public static String getKeyCase(Map<String, Object> parameters, String key) {
        if (parameters != null && parameters.containsKey(key)) {
            return key;
        }
        return StringUtils.uncapitalize(key);
    }

    /**
     * Computes the percentage bucket (0-100) that the contextId falls into. Uses SHA-256 hash for consistent
     * distribution across the percentage range.
     * 
     * @param contextId Id of the context being targeted
     * @return the bucket value of the context id
     * @throws TargetingException Unable to create hash of target context
     */
    public static double isTargetedPercentage(String contextId) {
        byte[] hash = null;
        if (contextId == null) {
            contextId = "\n";
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(contextId.getBytes());

        } catch (NoSuchAlgorithmException e) {
            throw new TargetingException("Unable to find SHA-256 for targeting.", e);
        }

        if (hash == null) {
            throw new TargetingException("Unable to create Targeting Hash for " + contextId);
        }

        BigInteger bi = bigEndianToLittleEndian(hash);

        return (bi.longValue() / (Math.pow(2, 32) - 1)) * 100;
    }

    public static BigInteger bigEndianToLittleEndian(byte[] bigEndian) {
        byte[] reversedBytes = new byte[4];
        for (int i = 0; i < 4; i++) {
            reversedBytes[i] = bigEndian[3 - i];
        }
        return new BigInteger(1, reversedBytes);
    }
}
