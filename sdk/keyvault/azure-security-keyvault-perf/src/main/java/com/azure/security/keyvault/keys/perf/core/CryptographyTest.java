// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.perf.core;

import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;

import java.util.UUID;

public abstract class CryptographyTest<TOptions extends PerfStressOptions> extends KeysTest<TOptions> {
    protected final CryptographyClient cryptographyClient;
    protected final CryptographyAsyncClient cryptographyAsyncClient;

    /**
     * Creates an instance of performance test.
     *
     * @param options the options configured for the test.
     */
    public CryptographyTest(TOptions options) {
        super(options);

        String keyName = "decryptPerfTest-" + UUID.randomUUID();
        KeyVaultKey key = keyClient.createKey(keyName, KeyType.RSA);

        // Setup the service client
        CryptographyClientBuilder builder = new CryptographyClientBuilder()
            .keyIdentifier(key.getId())
            .credential(new DefaultAzureCredentialBuilder().build());

        cryptographyClient = builder.buildClient();
        cryptographyAsyncClient = builder.buildAsyncClient();
    }
}
