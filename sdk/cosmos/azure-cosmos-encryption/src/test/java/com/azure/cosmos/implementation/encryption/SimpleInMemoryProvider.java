// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.encryption.api.DataEncryptionKey;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKeyProvider;

import java.util.HashMap;
import java.util.Map;

public class SimpleInMemoryProvider implements DataEncryptionKeyProvider {
    private final Map<String, DataEncryptionKey> keyMap = new HashMap<>();

    public void addKey(String keyId, DataEncryptionKey key) {
        keyMap.put(keyId, key);
    }

    @Override
    public DataEncryptionKey getDataEncryptionKey(String id, String algorithm) {
        return keyMap.get(id);
    }
}
