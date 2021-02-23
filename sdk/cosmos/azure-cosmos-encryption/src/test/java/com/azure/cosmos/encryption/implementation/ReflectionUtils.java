// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import org.apache.commons.lang3.reflect.FieldUtils;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;

public class ReflectionUtils {

    private static <T> void set(Object object, T newValue, String fieldName) {
        try {
            FieldUtils.writeField(object, fieldName, newValue, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T get(Object object, String fieldName) {
        try {
            return (T) FieldUtils.readField(object, fieldName, true);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static AtomicBoolean isEncryptionSettingsInitDone(EncryptionProcessor encryptionProcessor) {
        return get(encryptionProcessor, "isEncryptionSettingsInitDone");
    }
}
