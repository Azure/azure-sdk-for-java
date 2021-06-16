// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.test.TestMode;
import org.junit.jupiter.api.Assumptions;

public class CryptographyClientManagedHsmTest extends CryptographyClientTest {
    public CryptographyClientManagedHsmTest() {
        this.isManagedHsmTest = true;
    }

    @Override
    protected void beforeTest() {
        Assumptions.assumeTrue(isManagedHsmTest && getTestMode() != TestMode.PLAYBACK);

        super.beforeTest();
    }
}
