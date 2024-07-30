// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.perf.core;

import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.resourcemanager.AzureResourceManager;

import java.util.Objects;

public abstract class AzureResourceManagerTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    protected final AzureResourceManager azureResourceManager;
    public AzureResourceManagerTest(TOptions options) {
        super(options);

        Configuration configuration = Configuration.getGlobalConfiguration();
        String tenantId = Objects.requireNonNull(
            configuration.get(Configuration.PROPERTY_AZURE_TENANT_ID),
            "'AZURE_TENANT_ID' environment variable cannot be null.");
        String subscriptionId = Objects.requireNonNull(
            configuration.get(Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID),
            "'AZURE_SUBSCRIPTION_ID' environment variable cannot be null.");

        AzureProfile profile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
        azureResourceManager = AzureResourceManager
            .authenticate(new DefaultAzureCredentialBuilder().build(), profile)
            .withDefaultSubscription();
    }
}
