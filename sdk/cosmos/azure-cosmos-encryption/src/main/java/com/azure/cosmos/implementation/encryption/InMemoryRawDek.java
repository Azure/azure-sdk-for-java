// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.DataEncryptionKey;

import java.time.Duration;
import java.time.Instant;

class InMemoryRawDek {
    private final DataEncryptionKey DataEncryptionKey;
    private final Instant RawDekExpiry;

    public InMemoryRawDek(DataEncryptionKey dataEncryptionKey, Duration clientCacheTimeToLive) {
        this.DataEncryptionKey = dataEncryptionKey;
        this.RawDekExpiry = Instant.now().plus(clientCacheTimeToLive);
    }

    public DataEncryptionKey getDataEncryptionKey() {
        return DataEncryptionKey;
    }
    public Instant getRawDekExpiry() {
        return RawDekExpiry;
    }
}
