// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.networkanalytics;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.resourcemanager.resources.fluentcore.policy.ProviderRegistrationPolicy;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.models.Provider;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class NetworkAnalyticsManagerTests extends TestProxyTestBase {
    private ResourceManager resourceManager = null;
    private NetworkAnalyticsManager networkAnalyticsManager = null;

    @Override
    public void beforeTest() {
        final TokenCredential credential = new AzurePowerShellCredentialBuilder().build();
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        networkAnalyticsManager = NetworkAnalyticsManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .withPolicy(new ProviderRegistrationPolicy(resourceManager))
            .authenticate(credential, profile);
    }

    @Test
    @LiveOnly
    public void testListDataProducts() {
        networkAnalyticsManager.dataProducts().list().stream().count();
    }
}
