// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.iot.modelsrepository.implementation.ModelsRepositoryAPIImpl;
import com.azure.iot.modelsrepository.implementation.ModelsRepositoryAPIImplBuilder;
import com.azure.iot.modelsrepository.implementation.RepositoryHandler;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * This class provides a client for interacting asynchronously with a ModelsRepository instance.
 * This client is instantiated through {@link ModelsRepositoryClientBuilder}.
 */
@ServiceClient(builder = ModelsRepositoryClientBuilder.class, isAsync = true)
public final class ModelsRepositoryAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(ModelsRepositoryAsyncClient.class);
    private static final String MODELS_REPOSITORY_TRACING_NAMESPACE_VALUE = "Azure.IoT.ModelsRepository";
    private final ModelsRepositoryServiceVersion serviceVersion;
    private final RepositoryHandler repositoryHandler;
    private final ModelDependencyResolution defaultDependencyResolutionOption;
    private final URI repositoryEndpoint;

    ModelsRepositoryAsyncClient(
        URI repositoryEndpoint,
        HttpPipeline pipeline,
        ModelsRepositoryServiceVersion serviceVersion,
        ModelDependencyResolution dependencyResolutionOption) {

        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        this.serviceVersion = serviceVersion;

        this.defaultDependencyResolutionOption = dependencyResolutionOption;
        this.repositoryEndpoint = repositoryEndpoint;

        ModelsRepositoryAPIImpl protocolLayer = new ModelsRepositoryAPIImplBuilder()
            .apiVersion(this.serviceVersion.toString())
            .host(repositoryEndpoint.toString())
            .pipeline(pipeline)
            .serializerAdapter(jacksonAdapter)
            .buildClient();

        this.repositoryHandler = new RepositoryHandler(repositoryEndpoint, protocolLayer);
    }

    /**
     * Gets the repository endpoint that the client has been initialized with.
     * @return The target repository endpoint.
     */
    public String getRepositoryEndpoint() {
        return this.repositoryEndpoint.toString();
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
     *
     * @param dtmi A well-formed DTDL model Id. See <a href="https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md">DTDL specs</a>. For example 'dtmi:com:example:Thermostat;1'.
     * @return A Map containing the model definition(s) and dependencies (if applicable) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Map<String, String>> getModels(String dtmi) {
        return getModels(dtmi, this.defaultDependencyResolutionOption);
    }

    /**
     * Gets a collection of model definitions.
     *
     * @param dtmi A well-formed DTDL model Id. See <a href="https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md">DTDL specs</a>. For example 'dtmi:com:example:Thermostat;1'.
     * @param dependencyResolution A {@link ModelDependencyResolution} value to dictate model resolution behavior.
     * @return A Map containing the model definition(s) and dependencies (if applicable) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Map<String, String>> getModels(String dtmi, ModelDependencyResolution dependencyResolution) {
        return withContext(context -> getModels(dtmi, dependencyResolution, context));
    }

    Mono<Map<String, String>> getModels(String dtmi, ModelDependencyResolution dependencyResolution, Context context) {
        context = context.addData(AZ_TRACING_NAMESPACE_KEY, MODELS_REPOSITORY_TRACING_NAMESPACE_VALUE);
        return repositoryHandler.processAsync(dtmi, dependencyResolution, context);
    }

    /**
     * Gets a collection of model definitions.
     *
     * @param dtmis Collection of well-formed DTDL model Ids.
     * @return A Map containing the model definition(s) and dependencies (if applicable) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Map<String, String>> getModels(Iterable<String> dtmis) {
        return getModels(dtmis, this.defaultDependencyResolutionOption);
    }

    /**
     * Gets a collection of model definitions.
     *
     * @param dtmis An Iterable of well-formed DTDL model Ids. See <a href="https://github.com/Azure/opendigitaltwins-dtdl/blob/master/DTDL/v2/dtdlv2.md">DTDL specs</a>. For example 'dtmi:com:example:Thermostat;1'.
     * @param dependencyResolution A {@link ModelDependencyResolution} value to dictate model resolution behavior.
     * @return A Map containing the model definition(s) and dependencies (if applicable) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Map<String, String>> getModels(Iterable<String> dtmis, ModelDependencyResolution dependencyResolution) {
        return withContext(context -> getModels(dtmis, dependencyResolution, context));
    }

    Mono<Map<String, String>> getModels(Iterable<String> dtmis, ModelDependencyResolution dependencyResolution, Context context) {
        context = context.addData(AZ_TRACING_NAMESPACE_KEY, MODELS_REPOSITORY_TRACING_NAMESPACE_VALUE);
        return repositoryHandler.processAsync(dtmis, dependencyResolution, context);
    }
}
