// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static com.azure.security.keyvault.keys.cryptography.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;

public class CryptographyClientManagedHsmTest extends CryptographyClientTest {
    public CryptographyClientManagedHsmTest() {
        this.isHsmEnabled = Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT") != null;
        this.runManagedHsmTest = isHsmEnabled || getTestMode() == TestMode.PLAYBACK;
    }

    @Override
    protected void beforeTest() {
        Assumptions.assumeTrue(runManagedHsmTest);

        super.beforeTest();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.security.keyvault.keys.cryptography.TestHelper#getTestParameters")
    public void signVerifyOkp(HttpClient httpClient, CryptographyServiceVersion serviceVersion) throws Exception {
        super.signVerifyOkp(httpClient, serviceVersion);
    }
}
