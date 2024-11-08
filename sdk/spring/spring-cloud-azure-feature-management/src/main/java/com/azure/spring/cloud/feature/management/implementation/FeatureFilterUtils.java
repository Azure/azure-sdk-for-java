// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.feature.management.implementation;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.azure.spring.cloud.feature.management.models.TargetingException;

public class FeatureFilterUtils {
    /**
     * Looks at the given key in the parameters and coverts it to a list if it is currently a map.
     *
     * @param parameters map of generic objects
     * @param key key of object int the parameters map
     */
    @SuppressWarnings("unchecked")
    public static void updateValueFromMapToList(Map<String, Object> parameters, String key) {
        Object objectMap = parameters.get(key);
        if (objectMap instanceof Map) {
            Collection<Object> toType = ((Map<String, Object>) objectMap).values();
            parameters.put(key, toType);
        }
    }

    public static String getKeyCase(Map<String, Object> parameters, String key) {
        if (parameters != null && parameters.containsKey(key)) {
            return key;
        }
        return StringUtils.uncapitalize(key);
    }

    /**
     * Computes the percentage that the contextId falls into.
     * 
     * @param contextId Id of the context being targeted
     * @return the bucket value of the context id
     * @throws TargetingException Unable to create hash of target context
     */
    public static double isTargetedPercentage(String contextId) {
        byte[] hash = null;

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            hash = digest.digest(contextId.getBytes(Charset.defaultCharset()));
        } catch (NoSuchAlgorithmException e) {
            throw new TargetingException("Unable to find SHA-256 for targeting.", e);
        }

        if (hash == null) {
            throw new TargetingException("Unable to create Targeting Hash for " + contextId);
        }

        ByteBuffer wrapped = ByteBuffer.wrap(hash);
        int contextMarker = Math.abs(wrapped.getInt());

        return (contextMarker / (double) Integer.MAX_VALUE) * 100;
    }
}
