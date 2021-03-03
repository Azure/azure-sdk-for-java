// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.iot.modelsrepository.implementation.ModelsRepositoryAPIImpl;
import com.azure.iot.modelsrepository.implementation.ModelsRepositoryAPIImplBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client for interacting asynchronously with a ModelsRepository instance.
 * This client is instantiated through {@link ModelsRepositoryClientBuilder}.
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.iot.modelsrepository.asyncClient.instantiation}
 *
 * <p>
 * This client allows for TODO: azabbasi
 * </p>
 */
@ServiceClient(builder = ModelsRepositoryClientBuilder.class, isAsync = true)
public final class ModelsRepositoryAsyncClient {
    private static final ClientLogger logger = new ClientLogger(ModelsRepositoryAsyncClient.class);
    private final ObjectMapper mapper;
    private final ModelsRepositoryServiceVersion serviceVersion;
    private final ModelsRepositoryAPIImpl protocolLayer;
    private final JsonSerializer serializer;
    private static final String MODELS_REPOSITORY_TRACING_NAMESPACE_VALUE = "Azure.Iot.ModelsRepository";

    ModelsRepositoryAsyncClient(String serviceEndpoint, HttpPipeline pipeline, ModelsRepositoryServiceVersion serviceVersion, JsonSerializer jsonSerializer) {

        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        mapper = jacksonAdapter.serializer(); // Use the same mapper in this layer that the generated layer will use
        this.serviceVersion = serviceVersion;

        // Is null by default. If not null, then the user provided a custom json serializer for the convenience layer to use.
        // If null, then mapper will be used instead. See DeserializationHelpers for more details
        this.serializer = jsonSerializer;

        this.protocolLayer = new ModelsRepositoryAPIImplBuilder()
            .host(serviceEndpoint)
            .pipeline(pipeline)
            .serializerAdapter(jacksonAdapter)
            .buildClient();
    }

    /**
     * Gets the Models Repository service API version that this client is configured to use for all service requests.
     * Unless configured while building this client through {@link ModelsRepositoryClientBuilder#serviceVersion(ModelsRepositoryServiceVersion)},
     * this value will be equal to the latest service API version supported by this client.
     *
     * @return The ModelsRepository service API version.
     */
    public ModelsRepositoryServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }
}
