// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf.core;

import com.azure.containers.containerregistry.ContainerRegistryAsyncClient;
import com.azure.containers.containerregistry.ContainerRegistryClient;
import com.azure.containers.containerregistry.ContainerRegistryClientBuilder;
import com.azure.containers.containerregistry.models.ContainerRegistryAudience;
import com.azure.core.credential.TokenCredential;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.models.ImportImageParameters;
import com.azure.resourcemanager.containerregistry.models.ImportMode;
import com.azure.resourcemanager.containerregistry.models.ImportSource;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

import static com.azure.containers.containerregistry.perf.core.Utils.CONFIGURATION;
import static com.azure.containers.containerregistry.perf.core.Utils.PROPERTY_CONTAINERREGISTRY_ENDPOINT;
import static com.azure.containers.containerregistry.perf.core.Utils.PROPERTY_CONTAINERREGISTRY_NAME;
import static com.azure.containers.containerregistry.perf.core.Utils.PROPERTY_CONTAINERREGISTRY_RESOURCE_GROUP;
import static com.azure.containers.containerregistry.perf.core.Utils.PROPERTY_CONTAINERREGISTRY_SUBSCRIPTION_ID;
import static com.azure.containers.containerregistry.perf.core.Utils.REGISTRY_URI;

/**
 * Base class for Azure Container Registry performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    private static final String CONFIGURATION_ERROR = "Configuration %s must be set in either environment variables "
        + "or system properties.%n";

    final String registryEndpoint;
    final String registryName;
    final String rgName;
    final String subscriptionId;
    final TokenCredential tokenCredential;

    protected ContainerRegistryClient containerRegistryClient;
    protected ContainerRegistryAsyncClient containerRegistryAsyncClient;



    /**
     * The base class for Azure Container Registry performance tests.
     *
     * @param options the configurable options for performing perf testing on this class.
     *
     * @throws RuntimeException if "AZURE_CONTAINERREGISTRY_ENDPOINT", "AZURE_CLIENT_ID", "AZURE_SUBSCRIPTION_ID" or "AZURE_CLIENT_SECRET" is null or empty.
     */
    public ServiceTest(TOptions options) {
        super(options);

        registryEndpoint = getConfigurationValue(PROPERTY_CONTAINERREGISTRY_ENDPOINT);
        registryName = getConfigurationValue(PROPERTY_CONTAINERREGISTRY_NAME);

        // DefaultCredentials rely on these values, so ensure that they have been set up.
        getConfigurationValue(Configuration.PROPERTY_AZURE_CLIENT_ID);
        getConfigurationValue(Configuration.PROPERTY_AZURE_CLIENT_SECRET);

        // importImage relies on these values, so ensure that they have been set up.
        subscriptionId = getConfigurationValue(PROPERTY_CONTAINERREGISTRY_SUBSCRIPTION_ID);
        rgName = getConfigurationValue(PROPERTY_CONTAINERREGISTRY_RESOURCE_GROUP);

        tokenCredential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryClientBuilder builder = new ContainerRegistryClientBuilder()
            .endpoint(registryEndpoint)
            .audience(ContainerRegistryAudience.AZURERESOURCEMANAGERPUBLICCLOUD)
            .credential(tokenCredential);

        this.containerRegistryClient = builder.buildClient();
        this.containerRegistryAsyncClient  = builder.buildAsyncClient();
    }


    private String getConfigurationValue(String configurationName) {
        String configurationValue = CONFIGURATION.get(configurationName);
        if (CoreUtils.isNullOrEmpty(configurationValue)) {
            throw new RuntimeException(String.format(CONFIGURATION_ERROR, configurationName));
        }

        return configurationValue;
    }

    protected Mono<Void> importImageAsync(String repositoryName, List<String> tags) {
        tags = tags.stream().map(tag -> String.format("%1$s:%2$s", repositoryName, tag)).collect(Collectors.toList());

        ContainerRegistryManager manager = ContainerRegistryManager.authenticate(tokenCredential, new AzureProfile(AzureEnvironment.AZURE));

        return manager.serviceClient().getRegistries().importImageAsync(
            rgName,
            registryName,
            new ImportImageParameters()
                .withMode(ImportMode.FORCE)
                .withSource(new ImportSource().withSourceImage(repositoryName)
                    .withRegistryUri(REGISTRY_URI))
                .withTargetTags(tags));
    }
}
