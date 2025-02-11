// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ClientBuilderConnectionPolicyTests {
    @DataProvider(name = "endpointArgProvider")
    public static Object[][] endpointArgProvider() {
        return new Object[][]{
            { "https://localhost", true },
            { "https://Localhost", true },
            { "http://Localhost", true },
            { "https://127.0.0.1", true },
            { "https://[::1]", true },
            { "https://[0:0:0:0:0:0:0:1]", true },
            { "https://random", false}
        };
    }

    @Test(groups = "emulator", dataProvider = "endpointArgProvider")
    public void clientWithServerCertValidationDisabled(String endPoint, boolean isEmulatorHost) {
        System.setProperty("COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED", "true");

        try {
            CosmosClientBuilder clientBuilder = new CosmosClientBuilder().endpoint(endPoint).key("key");
            clientBuilder.validateConfig();
            clientBuilder.buildConnectionPolicy();
            assertThat(clientBuilder.getConnectionPolicy().isServerCertValidationDisabled()).isEqualTo(isEmulatorHost);
        } finally {
            System.clearProperty("COSMOS.EMULATOR_SERVER_CERTIFICATE_VALIDATION_DISABLED");
        }
    }
}
