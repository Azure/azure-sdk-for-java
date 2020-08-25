// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.encryption.DataEncryptionKey;
import com.azure.cosmos.encryption.DataEncryptionKeyProvider;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class SimpleInMemoryProvider implements DataEncryptionKeyProvider {
    private final Map<String, DataEncryptionKey> keyMap = new HashMap<>();

    public void addKey(String keyId, DataEncryptionKey key) {
        keyMap.put(keyId, key);
    }

    @Override
    public Mono<DataEncryptionKey> getDataEncryptionKey(String id, String algorithm) {
        return Mono.just(keyMap.get(id));
    }
}
