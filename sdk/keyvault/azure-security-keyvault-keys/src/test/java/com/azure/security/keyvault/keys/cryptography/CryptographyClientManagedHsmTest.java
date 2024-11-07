// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.condition.EnabledIf;

import static com.azure.security.keyvault.keys.KeyClientTestBase.TEST_MODE;

@EnabledIf("shouldRunHsmTest")
public class CryptographyClientManagedHsmTest extends CryptographyClientTest {
    public CryptographyClientManagedHsmTest() {
        this.isHsmEnabled = Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT") != null;
        this.runManagedHsmTest = shouldRunHsmTest();
    }

    public static boolean shouldRunHsmTest() {
        return Configuration.getGlobalConfiguration().get("AZURE_MANAGEDHSM_ENDPOINT") != null
            || TEST_MODE == TestMode.PLAYBACK;
    }
}
