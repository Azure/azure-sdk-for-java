// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry.perf.core;

import com.azure.containers.containerregistry.ContainerRegistryAsyncClient;
import com.azure.containers.containerregistry.ContainerRegistryClient;
import com.azure.containers.containerregistry.ContainerRegistryClientBuilder;
import com.azure.containers.containerregistry.ContainerRegistryContentAsyncClient;
import com.azure.containers.containerregistry.ContainerRegistryContentClient;
import com.azure.containers.containerregistry.ContainerRegistryContentClientBuilder;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.netty.NettyAsyncHttpClientProvider;
import com.azure.core.http.okhttp.OkHttpAsyncClientProvider;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.HttpClientOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.perf.test.core.PerfStressTest;
import com.azure.perf.test.core.RepeatingInputStream;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.models.ImportImageParameters;
import com.azure.resourcemanager.containerregistry.models.ImportMode;
import com.azure.resourcemanager.containerregistry.models.ImportSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.containers.containerregistry.implementation.UtilsImpl.CHUNK_SIZE;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_ID;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_CLIENT_SECRET;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_RESOURCE_GROUP;
import static com.azure.core.util.Configuration.PROPERTY_AZURE_SUBSCRIPTION_ID;

/**
 * Base class for Azure Container Registry performance tests.
 */
public abstract class ServiceTest<TOptions extends PerfStressOptions> extends PerfStressTest<TOptions> {
    static final String REGISTRY_URI = "registry.hub.docker.com";
    protected static final String REPOSITORY_NAME = "library/node";
    protected static final String TEST_PERF_TAG1_NAME = "test-perf-tag1";
    protected static final String TEST_PERF_TAG2_NAME = "test-perf-tag2";
    protected static final String TEST_PERF_TAG3_NAME = "test-perf-tag3";
    protected static final String TEST_PERF_TAG4_NAME = "test-perf-tag4";
    private final TokenCredential tokenCredential;

    /**
     * The ContainerRegistryClient used in a performance test.
     */
    protected ContainerRegistryClient containerRegistryClient;

    /**
     * The ContainerRegistryAsyncClient used in an asynchronous performance test.
     */
    protected ContainerRegistryAsyncClient containerRegistryAsyncClient;

    protected ContainerRegistryContentClient blobClient;
    protected ContainerRegistryContentAsyncClient blobAsyncClient;

    /**
     * The base class for Azure Container Registry performance tests.
     *
     * @param options the configurable options for performing perf testing on this class.
     *
     * @throws RuntimeException if "AZURE_CONTAINERREGISTRY_ENDPOINT", "AZURE_CLIENT_ID", "AZURE_SUBSCRIPTION_ID" or "AZURE_CLIENT_SECRET" is null or empty.
     */
    public ServiceTest(TOptions options) {
        super(options);

        // DefaultCredentials rely on these values, so ensure that they have been set up.
        getConfigurationValue(PROPERTY_AZURE_CLIENT_ID);
        getConfigurationValue(PROPERTY_AZURE_CLIENT_SECRET);

        String registryEndpoint = getConfigurationValue("CONTAINERREGISTRY_ENDPOINT");

        HttpClientOptions httpOptions = new HttpClientOptions()
            .setHttpClientProvider(options.getHttpClient() == PerfStressOptions.HttpClientType.OKHTTP ? OkHttpAsyncClientProvider.class : NettyAsyncHttpClientProvider.class);

        tokenCredential = new DefaultAzureCredentialBuilder().build();
        ContainerRegistryClientBuilder builder = new ContainerRegistryClientBuilder()
            .endpoint(registryEndpoint)
            .clientOptions(httpOptions)
            .credential(tokenCredential);

        this.containerRegistryClient = builder.buildClient();
        this.containerRegistryAsyncClient  = builder.buildAsyncClient();

        ContainerRegistryContentClientBuilder blobClientBuilder = new ContainerRegistryContentClientBuilder()
            .credential(tokenCredential)
            .clientOptions(httpOptions)
            .endpoint(registryEndpoint)
            .repositoryName("oci-artifact");

        this.blobClient = blobClientBuilder.buildClient();
        this.blobAsyncClient = blobClientBuilder.buildAsyncClient();
    }

    private String getConfigurationValue(String configurationName) {
        String configurationValue = Configuration.getGlobalConfiguration().get(configurationName);
        if (CoreUtils.isNullOrEmpty(configurationValue)) {
            throw new RuntimeException(String.format("Configuration %s must be set in either environment variables "
                + "or system properties.%n", configurationName));
        }

        return configurationValue;
    }

    /**
     * Imports an image into a Container Registry.
     *
     * @param repositoryName The Container Registry repository name.
     * @param tags Tags to associate with the image.
     * @return An asynchronous response that only indicates completion.
     */
    protected Mono<Void> importImageAsync(String repositoryName, List<String> tags) {
        String rgName = getConfigurationValue(PROPERTY_AZURE_RESOURCE_GROUP);
        String registryName = getConfigurationValue("CONTAINERREGISTRY_REGISTRY_NAME");
        // to check if env var is set and output if missing
        // needed for AzureProfile.
        getConfigurationValue(PROPERTY_AZURE_SUBSCRIPTION_ID);

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

    protected void importImage(String repositoryName, List<String> tags) {
        String rgName = getConfigurationValue(PROPERTY_AZURE_RESOURCE_GROUP);
        String registryName = getConfigurationValue("CONTAINERREGISTRY_REGISTRY_NAME");
        // to check if env var is set and output if missing
        // needed for AzureProfile.
        getConfigurationValue(PROPERTY_AZURE_SUBSCRIPTION_ID);

        tags = tags.stream().map(tag -> repositoryName + ":" + tag).collect(Collectors.toList());

        ContainerRegistryManager manager = ContainerRegistryManager.authenticate(tokenCredential,
            new AzureProfile(AzureEnvironment.AZURE));

        manager.serviceClient().getRegistries().importImage(rgName, registryName, new ImportImageParameters()
            .withMode(ImportMode.FORCE)
            .withSource(new ImportSource().withSourceImage(repositoryName)
                .withRegistryUri(REGISTRY_URI))
            .withTargetTags(tags));
    }

    protected static Flux<ByteBuffer> generateAsyncStream(long size) {
        RepeatingInputStream input = new RepeatingInputStream(size);
        byte[] chunk = new byte[CHUNK_SIZE];
        return Flux.create(sink -> {
            int read;
            while((read = input.read(chunk, 0, CHUNK_SIZE)) >= 0) {
                ByteBuffer buffer = ByteBuffer.wrap(chunk);
                buffer.limit(read);
                sink.next(buffer);
            }

            sink.complete();
        });
    }
}
