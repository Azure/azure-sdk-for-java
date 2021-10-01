// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.Assumptions;

public class CryptographyClientManagedHsmTest extends CryptographyClientTest {
    public CryptographyClientManagedHsmTest() {
        this.isManagedHsmTest = Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT") != null;
    }

    @Override
    protected void beforeTest() {
        Assumptions.assumeTrue(isManagedHsmTest || getTestMode() == TestMode.PLAYBACK);

        super.beforeTest();
    }
}
