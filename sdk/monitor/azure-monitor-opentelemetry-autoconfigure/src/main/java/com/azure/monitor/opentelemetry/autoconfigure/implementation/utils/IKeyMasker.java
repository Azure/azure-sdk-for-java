// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure.implementation.utils;

public final class IKeyMasker {

    private static final int CHARACTERS_TO_KEEP_AT_END = 13;

    private IKeyMasker() {
    }

    public static String mask(String instrumentationKey) {
        // Tests could set the connection string with a short one
        if (instrumentationKey != null && instrumentationKey.length() > CHARACTERS_TO_KEEP_AT_END) {
            return "*" + instrumentationKey.substring(instrumentationKey.length() - CHARACTERS_TO_KEEP_AT_END);
        }
        return instrumentationKey;
    }
}
