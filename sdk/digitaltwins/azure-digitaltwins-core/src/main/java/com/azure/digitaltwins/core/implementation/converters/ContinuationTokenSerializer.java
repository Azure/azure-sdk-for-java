package com.azure.digitaltwins.core.implementation.converters;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;

public class ContinuationTokenSerializer {
    public static String serialize(String continueationToken) {
        try {
            return new JacksonAdapter().serialize(continueationToken, SerializerEncoding.JSON);
        }
        catch (Exception e){
            throw new IllegalArgumentException("Invalid continuation token");
        }
    }
}
