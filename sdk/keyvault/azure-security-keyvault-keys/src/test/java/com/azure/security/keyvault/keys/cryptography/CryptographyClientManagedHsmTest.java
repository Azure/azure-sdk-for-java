// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assumptions;

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
}
