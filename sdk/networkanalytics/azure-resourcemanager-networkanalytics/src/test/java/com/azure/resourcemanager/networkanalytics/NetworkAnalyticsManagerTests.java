// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.networkanalytics;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import org.junit.jupiter.api.Test;

public class NetworkAnalyticsManagerTests extends TestBase {
    private NetworkAnalyticsManager networkAnalyticsManager = null;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        networkAnalyticsManager = NetworkAnalyticsManager
            .configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);
    }

    @Test
    @LiveOnly
    public void testListDataProducts() {
        networkAnalyticsManager.dataProducts().list().stream().count();
    }
}
