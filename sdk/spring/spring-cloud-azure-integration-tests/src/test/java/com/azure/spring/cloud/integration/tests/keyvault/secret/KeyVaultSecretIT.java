// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.keyvault.secret;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("keyvault-secret")
public class KeyVaultSecretIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultSecretIT.class);
    private static final String NAME = "sample-key";
    private static final String VALUE = "sample-value";

    @Autowired
    private SecretClient client;

    @Test
    public void testKeyVaultSecretOperation() {
        LOGGER.info("testKeyVaultSecretOperation begin.");
        client.setSecret(NAME, VALUE);
        KeyVaultSecret secret = client.getSecret(NAME);
        Assertions.assertEquals(VALUE, secret.getValue());
        LOGGER.info("testKeyVaultSecretOperation end.");
    }
}
