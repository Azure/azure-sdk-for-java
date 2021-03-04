// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.iot.modelsrepository.implementation.ModelsRepositoryAPIImpl;
import com.azure.iot.modelsrepository.implementation.ModelsRepositoryAPIImplBuilder;
import com.azure.iot.modelsrepository.implementation.RepositoryHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

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
    private final RepositoryHandler repositoryHandler;
    private final DependencyResolutionOptions defaultDependencyResolutionOption;

    private static final String MODELS_REPOSITORY_TRACING_NAMESPACE_VALUE = "Azure.Iot.ModelsRepository";

    ModelsRepositoryAsyncClient(
        String repositoryEndpoint,
        HttpPipeline pipeline,
        ModelsRepositoryServiceVersion serviceVersion,
        DependencyResolutionOptions dependencyResolutionOption,
        JsonSerializer jsonSerializer) {

        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        mapper = jacksonAdapter.serializer(); // Use the same mapper in this layer that the generated layer will use
        this.serviceVersion = serviceVersion;

        this.defaultDependencyResolutionOption = dependencyResolutionOption;

        // Is null by default. If not null, then the user provided a custom json serializer for the convenience layer to use.
        // If null, then mapper will be used instead. See DeserializationHelpers for more details
        this.serializer = jsonSerializer;

        this.protocolLayer = new ModelsRepositoryAPIImplBuilder()
            .host(repositoryEndpoint)
            .pipeline(pipeline)
            .serializerAdapter(jacksonAdapter)
            .buildClient();

        this.repositoryHandler = new RepositoryHandler(repositoryEndpoint, protocolLayer);
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

    /**
     * Gets a collection of model definitions.
     * @param dtmi A well-formed DTDL model Id. For example 'dtmi:com:example:Thermostat;1'.
     * @return A Map containing the model definition(s) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    public Mono<Map<String, String>> GetModels(String dtmi) {
        return GetModels(dtmi, this.defaultDependencyResolutionOption);
    }

    /**
     * Gets a collection of model definitions.
     * @param dtmi A well-formed DTDL model Id. For example 'dtmi:com:example:Thermostat;1'.
     * @param dependencyResolutionOption A DependencyResolutionOption value to force model resolution behavior.
     * @return A Map containing the model definition(s) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    public Mono<Map<String, String>> GetModels(String dtmi, DependencyResolutionOptions dependencyResolutionOption) {
        return withContext(context -> GetModels(dtmi, dependencyResolutionOption, context));
    }

    private Mono<Map<String, String>> GetModels(String dtmi, DependencyResolutionOptions dependencyResolutionOption, Context context) {
        context.addData(AZ_TRACING_NAMESPACE_KEY, MODELS_REPOSITORY_TRACING_NAMESPACE_VALUE);
        return repositoryHandler.ProcessAsync(dtmi, dependencyResolutionOption, context);
    }

    /**
     * Gets a collection of model definitions.
     * @param dtmis Collection of well-formed DTDL model Ids
     * @return A Map containing the model definition(s) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    public Mono<Map<String, String>> GetModels(Iterable<String> dtmis) {
        return GetModels(dtmis, this.defaultDependencyResolutionOption);
    }

    /**
     * Gets a collection of model definitions.
     * @param dtmis Collection of well-formed DTDL model Ids.
     * @param dependencyResolutionOption A DependencyResolutionOption value to force model resolution behavior.
     * @return A Map containing the model definition(s) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    public Mono<Map<String, String>> GetModels(Iterable<String> dtmis, DependencyResolutionOptions dependencyResolutionOption) {
        return withContext(context -> GetModels(dtmis, dependencyResolutionOption, context));
    }

    private Mono<Map<String, String>> GetModels(Iterable<String> dtmis, DependencyResolutionOptions dependencyResolutionOption, Context context) {
        context.addData(AZ_TRACING_NAMESPACE_KEY, MODELS_REPOSITORY_TRACING_NAMESPACE_VALUE);
        return repositoryHandler.ProcessAsync(dtmis, dependencyResolutionOption, context);
    }
}
