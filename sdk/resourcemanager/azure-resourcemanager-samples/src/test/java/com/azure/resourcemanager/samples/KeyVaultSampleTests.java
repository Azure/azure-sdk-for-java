// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.util.Configuration;
import com.azure.resourcemanager.keyvault.samples.ManageKeyVault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

public class KeyVaultSampleTests extends SamplesTestBase {
    @Test
    @Disabled("Some RBAC related issue with current credentials")
    public void testManageKeyVault() {
        String clientId = "";
        if (!isPlaybackMode()) {
            final Configuration configuration = Configuration.getGlobalConfiguration();
            clientId = configuration.get(Configuration.PROPERTY_AZURE_CLIENT_ID);
        }
        Assertions.assertTrue(ManageKeyVault.runSample(azureResourceManager, clientId));
    }
}
