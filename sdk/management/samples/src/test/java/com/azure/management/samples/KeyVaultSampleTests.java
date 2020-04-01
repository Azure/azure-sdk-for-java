/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.samples;

import com.azure.management.ApplicationTokenCredential;
import com.azure.management.keyvault.samples.ManageKeyVault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.io.File;
import java.io.IOException;

public class KeyVaultSampleTests extends SamplesTestBase {
    @Test
    @Disabled("Some RBAC related issue with current credentials")
    public void testManageKeyVault() {
        String clientId = "";
        if (!isPlaybackMode()) {
            final File credFile = new File(System.getenv("AZURE_AUTH_LOCATION"));
            try {
                clientId = ApplicationTokenCredential.fromFile(credFile).getClientId();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Assertions.assertTrue(ManageKeyVault.runSample(azure, clientId));
    }
}
