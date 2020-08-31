// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.*;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImpl;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImplBuilder;
import com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions;
import com.azure.digitaltwins.core.models.ModelData;
import com.azure.digitaltwins.core.util.DigitalTwinsResponse;
import com.azure.digitaltwins.core.util.DigitalTwinsResponseHeaders;
import com.azure.digitaltwins.core.implementation.serializer.DigitalTwinsStringSerializer;
import com.azure.digitaltwins.core.util.ListModelOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;


/**
 * This class provides a client for interacting asynchronously with an Azure Digital Twins instance.
 *
 * <p>
 * This client is instantiated through {@link DigitalTwinsClientBuilder}.
 * </p>
 *
 * <p>
 * This client allows for management of digital twins, their components, and their relationships. It also allows for managing
 * the digital twin models and event routes tied to your Azure Digital Twins instance.
 * </p>
 */
@ServiceClient(builder = DigitalTwinsClientBuilder.class)
public final class DigitalTwinsAsyncClient {
    private static final ClientLogger logger = new ClientLogger(DigitalTwinsAsyncClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final DigitalTwinsServiceVersion serviceVersion;
    private final AzureDigitalTwinsAPIImpl protocolLayer;
    private static final Boolean includeModelDefinition = true;

    DigitalTwinsAsyncClient(HttpPipeline pipeline, DigitalTwinsServiceVersion serviceVersion, String host) {
        final SimpleModule stringModule = new SimpleModule("String Serializer");
        stringModule.addSerializer(new DigitalTwinsStringSerializer(String.class, mapper));

        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        jacksonAdapter.serializer().registerModule(stringModule);

        this.protocolLayer = new AzureDigitalTwinsAPIImplBuilder()
            .host(host)
            .pipeline(pipeline)
            .serializerAdapter(jacksonAdapter)
            .buildClient();
        this.serviceVersion = serviceVersion;
    }

    /**
     * Gets the Azure Digital Twins service API version that this client is configured to use for all service requests.
     * Unless configured while building this client through {@link DigitalTwinsClientBuilder#serviceVersion(DigitalTwinsServiceVersion)},
     * this value will be equal to the latest service API version supported by this client.
     *
     * @return The Azure Digital Twins service API version.
     */
    public DigitalTwinsServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    /**
     * Gets the {@link HttpPipeline} that this client is configured to use for all service requests. This pipeline can
     * be customized while building this client through {@link DigitalTwinsClientBuilder#httpPipeline(HttpPipeline)}.
     *
     * @return The {@link HttpPipeline} that this client uses for all service requests.
     */
    public HttpPipeline getHttpPipeline() {
        return this.protocolLayer.getHttpPipeline();
    }

    // TODO: This is a temporary implementation for sample purposes. This should be spruced up/replaced once this API is actually designed.
    // Input is String and output is Response<String>.
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<String>> createDigitalTwinWithResponse(String digitalTwinId, String digitalTwin) {
        return withContext(context -> createDigitalTwinWithResponse(digitalTwinId, digitalTwin, context));
    }

    // TODO: This is a temporary implementation for sample purposes. This should be spruced up/replaced once this API is actually designed.
    // Input is Object and output is Response<T>.
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> createDigitalTwinWithResponse(String digitalTwinId, Object digitalTwin, Class<T> klazz) {
        return withContext(context -> createDigitalTwinWithResponse(digitalTwinId, digitalTwin, klazz, context));
    }

    Mono<DigitalTwinsResponse<String>> createDigitalTwinWithResponse(String digitalTwinId, String digitalTwin, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin, context)
            .flatMap(
                response -> {
                    try {
                        String jsonResponse = mapper.writeValueAsString(response.getValue());
                        DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                        return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), jsonResponse, twinHeaders));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    <T> Mono<DigitalTwinsResponse<T>> createDigitalTwinWithResponse(String digitalTwinId, Object digitalTwin, Class<T> klazz, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin, context)
            .flatMap(
                response -> {
                    T genericResponse = mapper.convertValue(response.getValue(), klazz);
                    DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                    return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
                });
    }

    /**
     * Creates a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The application/json relationship to be created.
     * @return A REST response containing the application/json relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> createRelationshipWithResponse(String digitalTwinId, String relationshipId, String relationship) {
        return withContext(context -> createRelationshipWithResponse(digitalTwinId, relationshipId, relationship, context));
    }

    Mono<Response<String>> createRelationshipWithResponse(String digitalTwinId, String relationshipId, String relationship, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .addRelationshipWithResponseAsync(digitalTwinId, relationshipId, relationship, context)
            .flatMap(
                response -> {
                    try {
                        String jsonResponse = mapper.writeValueAsString(response.getValue());
                        return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), jsonResponse));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    /**
     * Gets all the relationships on a digital twin filtered by the relationship name, by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipName The name of a relationship to filter to.
     * @return A {@link PagedFlux} of application/json relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listRelationships(String digitalTwinId, String relationshipName) {

        return new PagedFlux<>(
            () -> withContext(context -> listRelationshipsSinglePageAsync(digitalTwinId, relationshipName, context)),
            nextLink -> withContext(context -> listRelationshipsNextSinglePageAsync(nextLink, context)));
    }

    PagedFlux<String> listRelationships(String digitalTwinId, String relationshipName, Context context) {

        return new PagedFlux<>(
            () -> listRelationshipsSinglePageAsync(digitalTwinId, relationshipName, context),
            nextLink -> listRelationshipsNextSinglePageAsync(nextLink, context));
    }

    Mono<PagedResponse<String>> listRelationshipsSinglePageAsync(String digitalTwinId, String relationshipName, Context context) {
        return protocolLayer.getDigitalTwins().listRelationshipsSinglePageAsync(digitalTwinId, relationshipName, context)
            .map(
                objectPagedResponse -> {
                    List<String> stringList = objectPagedResponse.getValue().stream()
                        .map(object -> {
                            try {
                                return mapper.writeValueAsString(object);
                            } catch (JsonProcessingException e) {
                                logger.error("Could not parse the returned relationship [%s]: %s", object, e);
                                return null;
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                    return new PagedResponseBase<>(
                        objectPagedResponse.getRequest(),
                        objectPagedResponse.getStatusCode(),
                        objectPagedResponse.getHeaders(),
                        stringList,
                        objectPagedResponse.getContinuationToken(),
                        ((PagedResponseBase) objectPagedResponse).getDeserializedHeaders());

                }
            );
    }

    Mono<PagedResponse<String>> listRelationshipsNextSinglePageAsync(String nextLink, Context context) {
        return protocolLayer.getDigitalTwins().listRelationshipsNextSinglePageAsync(nextLink, context)
            .map(objectPagedResponse -> {
                List<String> stringList = objectPagedResponse.getValue().stream()
                    .map(object -> {
                        try {
                            return mapper.writeValueAsString(object);
                        } catch (JsonProcessingException e) {
                            logger.error("Could not parse the returned relationship [%s]: %s", object, e);
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                return new PagedResponseBase<>(
                    objectPagedResponse.getRequest(),
                    objectPagedResponse.getStatusCode(),
                    objectPagedResponse.getHeaders(),
                    stringList,
                    objectPagedResponse.getContinuationToken(),
                    ((PagedResponseBase)objectPagedResponse).getDeserializedHeaders());
            });
    }


    //==================================================================================================================================================
    // Models APIs
    //==================================================================================================================================================

    /**
     * Creates one or many models.
     * @param models The list of models to create. Each string corresponds to exactly one model.
     * @return A {@link PagedFlux} of created models and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ModelData> createModels(List<String> models) {
        return new PagedFlux<>(
            () -> withContext(context -> createModelsSinglePageAsync(models, context)),
            nextLink -> withContext(context -> Mono.empty()));
    }

    PagedFlux<ModelData> createModels(List<String> models, Context context){
        return new PagedFlux<>(
            () -> createModelsSinglePageAsync(models, context),
            nextLink -> Mono.empty());
    }

    Mono<PagedResponse<ModelData>> createModelsSinglePageAsync(List<String> models, Context context)
    {
        List<Object> modelsPayload = new ArrayList<>();
        for (String model: models) {
            try {
                modelsPayload.add(mapper.readValue(model, Object.class));
            }
            catch (JsonProcessingException e) {
                logger.error("Could not parse the model payload [%s]: %s", model, e);
                return Mono.error(e);
            }
        }

        return protocolLayer.getDigitalTwinModels().addWithResponseAsync(modelsPayload, context)
            .map(
                listResponse -> new PagedResponseBase<>(
                    listResponse.getRequest(),
                    listResponse.getStatusCode(),
                    listResponse.getHeaders(),
                    listResponse.getValue(),
                    null,
                    ((ResponseBase)listResponse).getDeserializedHeaders()));
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     * @param modelId The Id of the model.
     * @return The ModelData
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ModelData> getModel(String modelId) {
        return withContext(context -> getModelWithResponse(modelId, context))
            .flatMap(response -> Mono.just(response.getValue()));
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     * @param modelId The Id of the model.
     * @return The ModelData and the http response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ModelData>> getModelWithResponse(String modelId) {
        return withContext(context -> getModelWithResponse(modelId, context));
    }

    Mono<Response<ModelData>> getModelWithResponse(String modelId, Context context){
        return protocolLayer
            .getDigitalTwinModels()
            .getByIdWithResponseAsync(modelId, includeModelDefinition, context);
    }

    /**
     * Gets the list of models by iterating through a collection.
     * @param listModelOptions The options to follow when listing the models. For example, the page size hint can be specified.
     * @return A {@link PagedFlux} of ModelData and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ModelData> listModels(ListModelOptions listModelOptions) {
        return new PagedFlux<>(
            () -> withContext(context -> listModelsSinglePageAsync(listModelOptions, context)),
            nextLink -> withContext(context -> listModelsNextSinglePageAsync(nextLink, context)));
    }

    /**
     * Gets the list of models by iterating through a collection.
     * @return A {@link PagedFlux} of ModelData and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<ModelData> listModels() {
        return listModels(new ListModelOptions());
    }

    PagedFlux<ModelData> listModels(Context context){
        return new PagedFlux<>(
            () -> listModelsSinglePageAsync(new ListModelOptions(), context),
            nextLink -> listModelsNextSinglePageAsync(nextLink, context));
    }

    PagedFlux<ModelData> listModels(ListModelOptions listModelOptions, Context context){
        return new PagedFlux<>(
            () -> listModelsSinglePageAsync(listModelOptions, context),
            nextLink -> listModelsNextSinglePageAsync(nextLink, context));
    }

    Mono<PagedResponse<ModelData>> listModelsSinglePageAsync(ListModelOptions listModelOptions, Context context){
        return protocolLayer.getDigitalTwinModels().listSinglePageAsync(
            listModelOptions.getDependenciesFor(),
            listModelOptions.getIncludeModelDefinition(),
            new DigitalTwinModelsListOptions().setMaxItemCount(listModelOptions.getMaxItemCount()),
            context);
    }

    Mono<PagedResponse<ModelData>> listModelsNextSinglePageAsync(String nextLink, Context context){
        return protocolLayer.getDigitalTwinModels().listNextSinglePageAsync(nextLink, context);
    }

    /**
     * Deletes a model.
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteModel(String modelId) {
        return withContext(context -> deleteModelWithResponse(modelId, context))
            .flatMap(response -> Mono.just(response.getValue()));
    }

    /**
     * Deletes a model.
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     * @return The http response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteModelWithResponse(String modelId) {
        return withContext(context -> deleteModelWithResponse(modelId, context));
    }

    Mono<Response<Void>> deleteModelWithResponse(String modelId, Context context){
        return protocolLayer.getDigitalTwinModels().deleteWithResponseAsync(modelId, context);
    }

    //TODO: Decommission Model APIs (waiting for Abhipsa's change to come in)
}
