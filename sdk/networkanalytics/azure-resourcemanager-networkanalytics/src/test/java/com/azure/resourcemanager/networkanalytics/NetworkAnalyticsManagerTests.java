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
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.resources.models.Provider;
import com.azure.resourcemanager.test.utils.TestUtilities;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

public class NetworkAnalyticsManagerTests extends TestProxyTestBase {
    private static final String PROVIDER_NAMESPACE = "Microsoft.NetworkAnalytics";
    private static final Duration REGISTRATION_POLL_INTERVAL = Duration.ofSeconds(30);
    private static final Duration REGISTRATION_TIMEOUT = Duration.ofMinutes(5);

    private NetworkAnalyticsManager networkAnalyticsManager = null;

    @Override
    public void beforeTest() {
        final TokenCredential credential = TestUtilities.getTokenCredentialForTest(getTestMode());
        final AzureProfile profile = new AzureProfile(AzureEnvironment.AZURE);

        ResourceManager resourceManager = ResourceManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile)
            .withDefaultSubscription();

        Provider provider = resourceManager.providers().register(PROVIDER_NAMESPACE);
        Instant deadline = Instant.now().plus(REGISTRATION_TIMEOUT);
        while (!"Registered".equalsIgnoreCase(provider.registrationState()) && Instant.now().isBefore(deadline)) {
            ResourceManagerUtils.sleep(REGISTRATION_POLL_INTERVAL);
            provider = resourceManager.providers().getByName(PROVIDER_NAMESPACE);
        }
        if (!"Registered".equalsIgnoreCase(provider.registrationState())) {
            throw new IllegalStateException("Provider " + PROVIDER_NAMESPACE + " was not registered within "
                + REGISTRATION_TIMEOUT + "; current state: " + provider.registrationState());
        }

        networkAnalyticsManager = NetworkAnalyticsManager.configure()
            .withLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC))
            .authenticate(credential, profile);
    }

    @Test
    @LiveOnly
    public void testListDataProducts() {
        networkAnalyticsManager.dataProducts().list().stream().count();
    }
}
