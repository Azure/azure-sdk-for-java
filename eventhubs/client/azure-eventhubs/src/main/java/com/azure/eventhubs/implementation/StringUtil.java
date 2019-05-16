// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs.implementation;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public final class StringUtil {
    public static String getRandomString(String prefix) {
        return String.format(Locale.US, "%s_%s_%s", prefix, UUID.randomUUID().toString().substring(0, 6), Instant.now().toEpochMilli());
    }
}
