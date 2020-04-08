// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.encryption.api.CosmosEncryptionAlgorithm;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKey;
import com.azure.cosmos.implementation.encryption.api.DataEncryptionKeyProvider;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class JavaKeyProvider implements DataEncryptionKeyProvider {
    private Map<String, DataEncryptionKey> keyMap = new HashMap<>();
    public void addKey(DataEncryptionKey key) {
        keyMap.put(key.getId(), key);
    }

    @Override
    public DataEncryptionKey loadDataEncryptionKey(String id, CosmosEncryptionAlgorithm algorithm) {
        return keyMap.get(id);
    }
}
