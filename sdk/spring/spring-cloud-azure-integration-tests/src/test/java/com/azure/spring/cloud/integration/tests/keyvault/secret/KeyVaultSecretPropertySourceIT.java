// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.integration.tests.keyvault.secret;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Disabled("Auth by workload identity is not supported now. Track issue: https://github.com/Azure/azure-sdk-for-java/issues/40897")
@ActiveProfiles("keyvault-secret-property-source")
public class KeyVaultSecretPropertySourceIT {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyVaultSecretPropertySourceIT.class);

    @Value("${AZURE_KEYVAULT_SECRET_VALUE}")
    private String expectedVal;
    @Value("${AZURE_KEYVAULT_SECRET_NAME}")
    private String secretKey;

    @Autowired
    private Environment environment;

    @Test
    public void testKeyVaultSecretOperation() {
        LOGGER.info("KeyVaultSecretPropertySourceIT begin.");
        Assertions.assertEquals(expectedVal, environment.getProperty(secretKey.replace('-', '.')));
        LOGGER.info("KeyVaultSecretPropertySourceIT end.");
    }
}
