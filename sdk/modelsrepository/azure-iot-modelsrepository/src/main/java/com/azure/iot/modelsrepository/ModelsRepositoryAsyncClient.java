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
import com.azure.core.util.serializer.JsonSerializer;
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
 *
 * <p><strong>Code Samples</strong></p>
 * <p>
 * {@codesnippet com.azure.iot.modelsrepository.asyncClient.instantiation}
 *
 * <p>
 * This client allows for TODO: azabbasi
 * </p>
 */
@ServiceClient(builder = ModelsRepositoryClientBuilder.class, isAsync = true)
public final class ModelsRepositoryAsyncClient {
    private static final ClientLogger logger = new ClientLogger(ModelsRepositoryAsyncClient.class);
    private static final String MODELS_REPOSITORY_TRACING_NAMESPACE_VALUE = "Azure.Iot.ModelsRepository";
    private final ModelsRepositoryServiceVersion serviceVersion;
    private final ModelsRepositoryAPIImpl protocolLayer;
    private final RepositoryHandler repositoryHandler;
    private final DependencyResolutionOptions defaultDependencyResolutionOption;

    ModelsRepositoryAsyncClient(
        URI repositoryUri,
        HttpPipeline pipeline,
        ModelsRepositoryServiceVersion serviceVersion,
        DependencyResolutionOptions dependencyResolutionOption,
        JsonSerializer jsonSerializer) {

        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        this.serviceVersion = serviceVersion;

        this.defaultDependencyResolutionOption = dependencyResolutionOption;

        this.protocolLayer = new ModelsRepositoryAPIImplBuilder()
            .host(repositoryUri.toString())
            .pipeline(pipeline)
            .serializerAdapter(jacksonAdapter)
            .buildClient();

        this.repositoryHandler = new RepositoryHandler(repositoryUri, protocolLayer);
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
     * @param dtmi A well-formed DTDL model Id. For example 'dtmi:com:example:Thermostat;1'.
     * @return A Map containing the model definition(s) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Map<String, String>> getModels(String dtmi) {
        return getModels(dtmi, this.defaultDependencyResolutionOption);
    }

    /**
     * Gets a collection of model definitions.
     *
     * @param dtmi                       A well-formed DTDL model Id. For example 'dtmi:com:example:Thermostat;1'.
     * @param dependencyResolutionOption A DependencyResolutionOption value to force model resolution behavior.
     * @return A Map containing the model definition(s) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Map<String, String>> getModels(String dtmi, DependencyResolutionOptions dependencyResolutionOption) {
        return withContext(context -> {
            try {
                return getModels(dtmi, dependencyResolutionOption, context);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    private Mono<Map<String, String>> getModels(String dtmi, DependencyResolutionOptions dependencyResolutionOption, Context context) throws Exception {
        context.addData(AZ_TRACING_NAMESPACE_KEY, MODELS_REPOSITORY_TRACING_NAMESPACE_VALUE);
        return repositoryHandler.processAsync(dtmi, dependencyResolutionOption, context);
    }

    /**
     * Gets a collection of model definitions.
     *
     * @param dtmis Collection of well-formed DTDL model Ids
     * @return A Map containing the model definition(s) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Map<String, String>> getModels(Iterable<String> dtmis) {
        return getModels(dtmis, this.defaultDependencyResolutionOption);
    }

    /**
     * Gets a collection of model definitions.
     *
     * @param dtmis                      Collection of well-formed DTDL model Ids.
     * @param dependencyResolutionOption A DependencyResolutionOption value to force model resolution behavior.
     * @return A Map containing the model definition(s) where the key is the dtmi
     * and the value is the raw model definition string.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Map<String, String>> getModels(Iterable<String> dtmis, DependencyResolutionOptions dependencyResolutionOption) {
        return withContext(context -> getModels(dtmis, dependencyResolutionOption, context));
    }

    private Mono<Map<String, String>> getModels(Iterable<String> dtmis, DependencyResolutionOptions dependencyResolutionOption, Context context) {
        context.addData(AZ_TRACING_NAMESPACE_KEY, MODELS_REPOSITORY_TRACING_NAMESPACE_VALUE);
        return repositoryHandler.processAsync(dtmis, dependencyResolutionOption, context);
    }
}
