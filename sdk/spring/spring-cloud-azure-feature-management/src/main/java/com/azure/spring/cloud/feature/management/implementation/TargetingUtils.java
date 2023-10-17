package com.azure.spring.cloud.feature.management.implementation;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.azure.spring.cloud.feature.management.models.TargetingException;

public class TargetingUtils {
    
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
