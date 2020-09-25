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
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImpl;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImplBuilder;
import com.azure.digitaltwins.core.implementation.converters.EventRouteConverter;
import com.azure.digitaltwins.core.implementation.converters.EventRouteListOptionsConverter;
import com.azure.digitaltwins.core.implementation.converters.IncomingRelationshipConverter;
import com.azure.digitaltwins.core.implementation.converters.ModelDataConverter;
import com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions;
import com.azure.digitaltwins.core.implementation.models.QuerySpecification;
import com.azure.digitaltwins.core.implementation.serializer.DeserializationHelpers;
import com.azure.digitaltwins.core.implementation.serializer.DigitalTwinsStringSerializer;
import com.azure.digitaltwins.core.implementation.serializer.SerializationHelpers;
import com.azure.digitaltwins.core.models.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
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
@ServiceClient(builder = DigitalTwinsClientBuilder.class, isAsync = true)
public final class DigitalTwinsAsyncClient {
    private static final ClientLogger logger = new ClientLogger(DigitalTwinsAsyncClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final DigitalTwinsServiceVersion serviceVersion;
    private final AzureDigitalTwinsAPIImpl protocolLayer;
    private static final Boolean includeModelDefinitionOnGet = true;
    private JsonSerializer serializer = null;

    DigitalTwinsAsyncClient(String serviceEndpoint, HttpPipeline pipeline, DigitalTwinsServiceVersion serviceVersion, JsonSerializer jsonSerializer) {
        final SimpleModule stringModule = new SimpleModule("String Serializer");
        stringModule.addSerializer(new DigitalTwinsStringSerializer(String.class, mapper));

        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        jacksonAdapter.serializer().registerModule(stringModule);

        this.serviceVersion = serviceVersion;

        // Is null by default. If not null, then the user provided a custom json serializer for the convenience layer to use.
        // If null, then mapper will be used instead. See DeserializationHelpers for more details
        this.serializer = jsonSerializer;

        this.protocolLayer = new AzureDigitalTwinsAPIImplBuilder()
            .host(serviceEndpoint)
            .pipeline(pipeline)
            .serializerAdapter(jacksonAdapter)
            .buildClient();
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

    //region Digital twin APIs

    /**
     * Creates a digital twin.
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @return The deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> createDigitalTwin(String digitalTwinId, T digitalTwin, Class<T> clazz)
    {
        return createDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Creates a digital twin.
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> createDigitalTwinWithResponse(String digitalTwinId, T digitalTwin, Class<T> clazz) {
        return withContext(context -> createDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> createDigitalTwinWithResponse(String digitalTwinId, T digitalTwin, Class<T> clazz, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin, context)
            .flatMap(response -> {
                T genericResponse = null;
                try {
                    genericResponse = DeserializationHelpers.deserializeObject(mapper, response.getValue(), clazz, this.serializer);
                } catch (JsonProcessingException e) {
                    logger.error("JsonProcessingException occurred while deserializing the response: ", e);
                    return Mono.error(e);
                }

                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);

                return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
            });
    }

    /**
     * Gets a digital twin.
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @return The deserialized application/json object representing the digital twin
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getDigitalTwin(String digitalTwinId, Class<T> clazz)
    {
        return getDigitalTwinWithResponse(digitalTwinId, clazz)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Gets a digital twin.
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz)
    {
        return withContext(context -> getDigitalTwinWithResponse(digitalTwinId, clazz, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .getByIdWithResponseAsync(digitalTwinId, context)
            .flatMap(response -> {
                T genericResponse = null;
                try {
                    genericResponse = DeserializationHelpers.deserializeObject(mapper, response.getValue(), clazz, this.serializer);
                } catch (JsonProcessingException e) {
                    logger.error("JsonProcessingException occurred while deserializing the digital twin get response: ", e);
                    return Mono.error(e);
                }
                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
            });
    }

    /**
     * Updates a digital twin.
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin.
     *                                    This argument can be created using {@link UpdateOperationUtility}.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateDigitalTwin(String digitalTwinId, List<Object> digitalTwinUpdateOperations)
    {
        return updateDigitalTwinWithResponse(digitalTwinId, digitalTwinUpdateOperations, new UpdateDigitalTwinRequestOptions())
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Updates a digital twin.
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwinUpdateOperations The JSON patch to apply to the specified digital twin.
     *                                    This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional settings for this request
     * @return A {@link DigitalTwinsResponse}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateDigitalTwinWithResponse(String digitalTwinId, List<Object> digitalTwinUpdateOperations, UpdateDigitalTwinRequestOptions options)
    {
        return withContext(context -> updateDigitalTwinWithResponse(digitalTwinId, digitalTwinUpdateOperations, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateDigitalTwinWithResponse(String digitalTwinId, List<Object> digitalTwinUpdateOperations, UpdateDigitalTwinRequestOptions options, Context context) {
        String ifMatch = options != null ? options.getIfMatch() : null;
        return protocolLayer
            .getDigitalTwins()
            .updateWithResponseAsync(digitalTwinId, digitalTwinUpdateOperations, ifMatch, context)
            .map(response -> {
                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), response.getValue(), twinHeaders);
            });
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDigitalTwin(String digitalTwinId)
    {
        return deleteDigitalTwinWithResponse(digitalTwinId, new DeleteDigitalTwinRequestOptions())
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param options The optional settings for this request
     * @return The Http response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDigitalTwinWithResponse(String digitalTwinId, DeleteDigitalTwinRequestOptions options)
    {
        return withContext(context -> deleteDigitalTwinWithResponse(digitalTwinId, options, context));
    }

    Mono<Response<Void>> deleteDigitalTwinWithResponse(String digitalTwinId, DeleteDigitalTwinRequestOptions options, Context context) {
        String ifMatch = options != null ? options.getIfMatch() : null;
        return protocolLayer
            .getDigitalTwins()
            .deleteWithResponseAsync(digitalTwinId, ifMatch, context);
    }

    //endregion Digital twin APIs

    //region Relationship APIs

    /**
     * Creates a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The relationship to be created.
     * @param clazz The model class of the relationship.
     * @param <T> The generic type of the relationship.
     * @return The relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> createRelationship(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz) {
        return createRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Creates a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The relationship to be created.
     * @param clazz The model class of the relationship.
     * @param <T> The generic type of the relationship.
     * @return A {@link DigitalTwinsResponse} containing the relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> createRelationshipWithResponse(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz) {
        return withContext(context -> createRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> createRelationshipWithResponse(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .addRelationshipWithResponseAsync(digitalTwinId, relationshipId, relationship, context)
            .flatMap(response -> {
                T genericResponse = null;
                try {
                    genericResponse = DeserializationHelpers.deserializeObject(mapper, response.getValue(), clazz, this.serializer);
                } catch (JsonProcessingException e) {
                    logger.error("JsonProcessingException occurred while deserializing the create relationship response: ", e);
                    return Mono.error(e);
                }
                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
            });
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @param clazz The model class to deserialize the relationship into.
     * @param <T> The generic type to deserialize the relationship into.
     * @return The deserialized relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getRelationship(String digitalTwinId, String relationshipId, Class<T> clazz) {
        return getRelationshipWithResponse(digitalTwinId, relationshipId, clazz)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @param clazz The model class to deserialize the relationship into.
     * @param <T> The generic type to deserialize the relationship into.
     * @return A {@link DigitalTwinsResponse} containing the deserialized relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getRelationshipWithResponse(String digitalTwinId, String relationshipId, Class<T> clazz) {
        return withContext(context -> getRelationshipWithResponse(digitalTwinId, relationshipId, clazz, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getRelationshipWithResponse(String digitalTwinId, String relationshipId, Class<T> clazz, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .getRelationshipByIdWithResponseAsync(digitalTwinId, relationshipId, context)
            .flatMap(response -> {
                T genericResponse = null;
                try {
                    genericResponse = DeserializationHelpers.deserializeObject(mapper, response.getValue(), clazz, this.serializer);
                } catch (JsonProcessingException e) {
                    logger.error("JsonProcessingException occurred while deserializing the get relationship response: ", e);
                    return Mono.error(e);
                }
                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
            });
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param relationshipUpdateOperations The JSON patch to apply to the specified digital twin's relationship.
     *                                     This argument can be created using {@link UpdateOperationUtility}.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateRelationship(String digitalTwinId, String relationshipId, List<Object> relationshipUpdateOperations) {
        return updateRelationshipWithResponse(digitalTwinId, relationshipId, relationshipUpdateOperations, new UpdateRelationshipRequestOptions())
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param relationshipUpdateOperations The JSON patch to apply to the specified digital twin's relationship.
     *                                     This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional settings for this request.
     * @return A {@link DigitalTwinsResponse} containing no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateRelationshipWithResponse(String digitalTwinId, String relationshipId, List<Object> relationshipUpdateOperations, UpdateRelationshipRequestOptions options) {
        return withContext(context -> updateRelationshipWithResponse(digitalTwinId, relationshipId, relationshipUpdateOperations, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateRelationshipWithResponse(String digitalTwinId, String relationshipId, List<Object> relationshipUpdateOperations, UpdateRelationshipRequestOptions options, Context context) {
        String ifMatch = options != null ? options.getIfMatch() : null;

        return protocolLayer
            .getDigitalTwins()
            .updateRelationshipWithResponseAsync(digitalTwinId, relationshipId, ifMatch, relationshipUpdateOperations, context)
            .map(response -> {
                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), response.getValue(), twinHeaders);
            });
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRelationship(String digitalTwinId, String relationshipId) {
        return deleteRelationshipWithResponse(digitalTwinId, relationshipId, new DeleteRelationshipRequestOptions())
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @param options The optional settings for this request.
     * @return A {@link Response} containing no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, DeleteRelationshipRequestOptions options) {
        return withContext(context -> deleteRelationshipWithResponse(digitalTwinId, relationshipId, options, context));
    }

    Mono<Response<Void>> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, DeleteRelationshipRequestOptions options, Context context) {
        String ifMatch = options != null ? options.getIfMatch() : null;

        return protocolLayer
            .getDigitalTwins()
            .deleteRelationshipWithResponseAsync(digitalTwinId, relationshipId, ifMatch, context);
    }

    /**
     * Gets all the relationships on a digital twin by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param clazz The model class to convert the relationship to. Since a digital twin might have relationships conforming to different models, it is advisable to convert them to a generic model like {@link BasicRelationship}.
     * @param <T> The generic type to convert the relationship to.
     * @return A {@link PagedFlux} of relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> listRelationships(String digitalTwinId, Class<T> clazz) {
        return listRelationships(digitalTwinId, null, clazz);
    }

    /**
     * Gets all the relationships on a digital twin filtered by the relationship name, by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipName The name of a relationship to filter to.
     * @param clazz The model class to convert the relationship to.
     * @param <T> The generic type to convert the relationship to.
     * @return A {@link PagedFlux} of relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> listRelationships(String digitalTwinId, String relationshipName, Class<T> clazz) {
        return new PagedFlux<>(
            () -> withContext(context -> listRelationshipsFirstPage(digitalTwinId, relationshipName, clazz, context)),
            nextLink -> withContext(context -> listRelationshipsNextPage(this.protocolLayer.getHost() + nextLink, clazz, context)));
    }

    <T> Mono<PagedResponse<T>> listRelationshipsFirstPage(String digitalTwinId, String relationshipName, Class<T> clazz, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .listRelationshipsSinglePageAsync(digitalTwinId, relationshipName, context)
            .map(
                objectPagedResponse -> {
                    List<T> list = objectPagedResponse.getValue().stream()
                        .map(object -> {
                            try {
                                return DeserializationHelpers.deserializeObject(mapper, object, clazz, this.serializer);
                            } catch (JsonProcessingException e) {
                                logger.error("JsonProcessingException occurred while deserializing the list relationship response: ", e);
                                throw new RuntimeException("JsonProcessingException occurred while deserializing the list relationship response", e);
                            }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                    return new PagedResponseBase<>(
                        objectPagedResponse.getRequest(),
                        objectPagedResponse.getStatusCode(),
                        objectPagedResponse.getHeaders(),
                        list,
                        objectPagedResponse.getContinuationToken(),
                        ((PagedResponseBase) objectPagedResponse).getDeserializedHeaders());
                }
            );
    }

    <T> Mono<PagedResponse<T>> listRelationshipsNextPage(String nextLink, Class<T> clazz, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .listRelationshipsNextSinglePageAsync(nextLink, context)
            .map(objectPagedResponse -> {
                List<T> stringList = objectPagedResponse.getValue().stream()
                    .map(object -> {
                        try {
                            return DeserializationHelpers.deserializeObject(mapper, object, clazz, this.serializer);
                        } catch (JsonProcessingException e) {
                            logger.error("JsonProcessingException occurred while deserializing the list relationship response: ", e);
                            throw new RuntimeException("JsonProcessingException occurred while deserializing the list relationship response", e);
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

    <T> PagedFlux<T> listRelationships(String digitalTwinId, String relationshipName, Class<T> clazz, Context context) {
        return new PagedFlux<>(
            () -> listRelationshipsFirstPage(digitalTwinId, relationshipName, clazz, context),
            nextLink -> listRelationshipsNextPage(this.protocolLayer.getHost() + nextLink, clazz, context));
    }

    /**
     * Gets all the relationships referencing a digital twin as a target by iterating through a collection.
     *
     * @param digitalTwinId The Id of the target digital twin.
     * @return A {@link PagedFlux} of relationships directed towards the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<IncomingRelationship> listIncomingRelationships(String digitalTwinId) {
        return new PagedFlux<>(
            () -> withContext(context -> listIncomingRelationshipsFirstPageAsync(digitalTwinId, context)),
            nextLink -> withContext(context -> listIncomingRelationshipsNextSinglePageAsync(this.protocolLayer.getHost() + nextLink, context)));
    }

    PagedFlux<IncomingRelationship> listIncomingRelationships(String digitalTwinId, Context context) {
        return new PagedFlux<>(
            () -> listIncomingRelationshipsFirstPageAsync(digitalTwinId, context),
            nextLink -> listIncomingRelationshipsNextSinglePageAsync(this.protocolLayer.getHost() + nextLink, context));
    }

    Mono<PagedResponse<IncomingRelationship>> listIncomingRelationshipsFirstPageAsync(String digitalTwinId, Context context){
        return protocolLayer.getDigitalTwins().listIncomingRelationshipsSinglePageAsync(digitalTwinId, context)
            .map(pagedIncomingRelationshipMappingFunction);
    }

    Mono<PagedResponse<IncomingRelationship>> listIncomingRelationshipsNextSinglePageAsync(String nextLink, Context context){
        return protocolLayer.getDigitalTwins().listIncomingRelationshipsNextSinglePageAsync(nextLink, context)
            .map(pagedIncomingRelationshipMappingFunction);
    }

    private Function<PagedResponse<com.azure.digitaltwins.core.implementation.models.IncomingRelationship>, PagedResponse<IncomingRelationship>> pagedIncomingRelationshipMappingFunction = (pagedIncomingRelationshipResponse) -> {
        List<IncomingRelationship> convertedList = pagedIncomingRelationshipResponse.getValue().stream()
            .map(IncomingRelationshipConverter::map)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return new PagedResponseBase<>(
            pagedIncomingRelationshipResponse.getRequest(),
            pagedIncomingRelationshipResponse.getStatusCode(),
            pagedIncomingRelationshipResponse.getHeaders(),
            convertedList,
            pagedIncomingRelationshipResponse.getContinuationToken(),
            ((PagedResponseBase)pagedIncomingRelationshipResponse).getDeserializedHeaders());
    };

    //endregion Relationship APIs

    //region Model APIs

    /**
     * Creates one or many models.
     * @param models The list of models to create. Each string corresponds to exactly one model.
     * @return A List of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Iterable<DigitalTwinsModelData>> createModels(Iterable<String> models) {
        return createModelsWithResponse(models)
            .map(Response::getValue);
    }

    /**
     * Creates one or many models.
     * @param models The list of models to create. Each string corresponds to exactly one model.
     * @return A {@link Response} containing the list of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Response<Iterable<DigitalTwinsModelData>>> createModelsWithResponse(Iterable<String> models) {
        return withContext(context -> createModelsWithResponse(models, context));
    }

    Mono<Response<Iterable<DigitalTwinsModelData>>> createModelsWithResponse(Iterable<String> models, Context context) {
        List<Object> modelsPayload = new ArrayList<>();
        for (String model : models) {
            try {
                modelsPayload.add(mapper.readValue(model, Object.class));
            }
            catch (JsonProcessingException e) {
                logger.error("Could not parse the model payload [%s]: %s", model, e);
                return Mono.error(e);
            }
        }

        return protocolLayer.getDigitalTwinModels().addWithResponseAsync(modelsPayload, context)
            .map(listResponse -> {
                Iterable<DigitalTwinsModelData> convertedList = listResponse.getValue().stream()
                    .map(ModelDataConverter::map)
                    .collect(Collectors.toList());

                return new SimpleResponse<>(listResponse.getRequest(), listResponse.getStatusCode(), listResponse.getHeaders(), convertedList);
            });
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     * @param modelId The Id of the model.
     * @return A {@link DigitalTwinsModelData} instance that contains the model and its metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsModelData> getModel(String modelId) {
        return getModelWithResponse(modelId)
            .map(Response::getValue);
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     * @param modelId The Id of the model.
     * @return A {@link Response} containing a {@link DigitalTwinsModelData} instance that contains the model and its metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DigitalTwinsModelData>> getModelWithResponse(String modelId) {
        return withContext(context -> getModelWithResponse(modelId, context));
    }

    Mono<Response<DigitalTwinsModelData>> getModelWithResponse(String modelId, Context context){
        return protocolLayer
            .getDigitalTwinModels()
            .getByIdWithResponseAsync(modelId, includeModelDefinitionOnGet, context)
            .map(response -> {
                com.azure.digitaltwins.core.implementation.models.ModelData modelData = response.getValue();
                return new SimpleResponse<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    ModelDataConverter.map(modelData));
            });
    }

    /**
     * List all of the models in this digital twins instance.
     * @return A {@link PagedFlux} of {@link DigitalTwinsModelData} that enumerates all the models.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DigitalTwinsModelData> listModels() {
        return listModels(new ModelsListOptions());
    }

    /**
     * List the models in this digital twins instance based on some options.
     * @param modelsListOptions The options to follow when listing the models.
     * @return A {@link PagedFlux} containing the retrieved {@link DigitalTwinsModelData} instances.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DigitalTwinsModelData> listModels(ModelsListOptions modelsListOptions) {
        return new PagedFlux<>(
            () -> withContext(context -> listModelsSinglePageAsync(modelsListOptions, context)),
            nextLink -> withContext(context -> listModelsNextSinglePageAsync(this.protocolLayer.getHost() + nextLink, context)));
    }

    PagedFlux<DigitalTwinsModelData> listModels(ModelsListOptions modelsListOptions, Context context){
        return new PagedFlux<>(
            () -> listModelsSinglePageAsync(modelsListOptions, context),
            nextLink -> listModelsNextSinglePageAsync(this.protocolLayer.getHost() + nextLink, context));
    }

    Mono<PagedResponse<DigitalTwinsModelData>> listModelsSinglePageAsync(ModelsListOptions modelsListOptions, Context context){
        return protocolLayer.getDigitalTwinModels().listSinglePageAsync(
            modelsListOptions.getDependenciesFor(),
            modelsListOptions.getIncludeModelDefinition(),
            new DigitalTwinModelsListOptions().setMaxItemCount(modelsListOptions.getMaxItemCount()),
            context)
            .map(
                objectPagedResponse -> {
                    List<DigitalTwinsModelData> convertedList = objectPagedResponse.getValue().stream()
                        .map(ModelDataConverter::map)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
                    return new PagedResponseBase<>(
                        objectPagedResponse.getRequest(),
                        objectPagedResponse.getStatusCode(),
                        objectPagedResponse.getHeaders(),
                        convertedList,
                        objectPagedResponse.getContinuationToken(),
                        ((PagedResponseBase) objectPagedResponse).getDeserializedHeaders());
                }
            );
    }

    Mono<PagedResponse<DigitalTwinsModelData>> listModelsNextSinglePageAsync(String nextLink, Context context){
        return protocolLayer.getDigitalTwinModels().listNextSinglePageAsync(nextLink, context)
            .map(objectPagedResponse -> {
            List<DigitalTwinsModelData> convertedList = objectPagedResponse.getValue().stream()
                .map(ModelDataConverter::map)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
            return new PagedResponseBase<>(
                objectPagedResponse.getRequest(),
                objectPagedResponse.getStatusCode(),
                objectPagedResponse.getHeaders(),
                convertedList,
                objectPagedResponse.getContinuationToken(),
                ((PagedResponseBase)objectPagedResponse).getDeserializedHeaders());
        });
    }

    /**
     * Deletes a model.
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteModel(String modelId) {
        return deleteModelWithResponse(modelId)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Deletes a model.
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     * @return A {@link Response} with no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteModelWithResponse(String modelId) {
        return withContext(context -> deleteModelWithResponse(modelId, context));
    }

    Mono<Response<Void>> deleteModelWithResponse(String modelId, Context context){
        return protocolLayer.getDigitalTwinModels().deleteWithResponseAsync(modelId, context);
    }

    /**
     * Decommissions a model.
     * @param modelId The Id of the model to decommission.
     * @return an empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> decommissionModel(String modelId) {
        return decommissionModelWithResponse(modelId)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Decommissions a model.
     * @param modelId The Id of the model to decommission.
     * @return A {@link Response} with no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> decommissionModelWithResponse(String modelId) {
        return withContext(context -> decommissionModelWithResponse(modelId, context));
    }

    Mono<Response<Void>> decommissionModelWithResponse(String modelId, Context context) {
        List<Object> updateOperation = new UpdateOperationUtility()
            .appendReplaceOperation("/decommissioned", true)
            .getUpdateOperations();

        return protocolLayer.getDigitalTwinModels().updateWithResponseAsync(modelId, updateOperation, context);
    }

    //endregion Model APIs

    //region Component APIs

    /**
     * Get a component of a digital twin.
     * @param digitalTwinId The Id of the digital twin to get the component from.
     * @param componentPath The path of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize application/json the component into.
     * @return The deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getComponent(String digitalTwinId, String componentPath, Class<T> clazz) {
        return getComponentWithResponse(digitalTwinId, componentPath, clazz)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Get a component of a digital twin.
     * @param digitalTwinId The Id of the digital twin to get the component from.
     * @param componentPath The path of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize the application/json component into.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getComponentWithResponse(String digitalTwinId, String componentPath, Class<T> clazz) {
        return withContext(context -> getComponentWithResponse(digitalTwinId, componentPath, clazz, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getComponentWithResponse(String digitalTwinId, String componentPath, Class<T> clazz, Context context) {
        return protocolLayer.getDigitalTwins().getComponentWithResponseAsync(digitalTwinId, componentPath, context)
            .flatMap(response -> {
                T genericResponse = null;
                try {
                    genericResponse = DeserializationHelpers.deserializeObject(mapper, response.getValue(), clazz, this.serializer);
                } catch (JsonProcessingException e) {
                    logger.error("JsonProcessingException occurred while deserializing the get component response: ", e);
                    return Mono.error(e);
                }
                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return Mono.just(new DigitalTwinsResponse<T>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
            });
    }

    /**
     * Patch a component on a digital twin.
     * @param digitalTwinId The Id of the digital twin that has the component to patch.
     * @param componentPath The path of the component on the digital twin.
     * @param componentUpdateOperations The JSON patch to apply to the specified digital twin's relationship.
     *                                  This argument can be created using {@link UpdateOperationUtility}.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateComponent(String digitalTwinId, String componentPath, List<Object> componentUpdateOperations) {
        return updateComponentWithResponse(digitalTwinId, componentPath, componentUpdateOperations, new UpdateComponentRequestOptions())
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Patch a component on a digital twin.
     * @param digitalTwinId The Id of the digital twin that has the component to patch.
     * @param componentPath The path of the component on the digital twin.
     * @param componentUpdateOperations The JSON patch to apply to the specified digital twin's relationship.
     *                                  This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional parameters for this request.
     * @return A {@link DigitalTwinsResponse} containing an empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateComponentWithResponse(String digitalTwinId, String componentPath, List<Object> componentUpdateOperations, UpdateComponentRequestOptions options) {
        return withContext(context -> updateComponentWithResponse(digitalTwinId, componentPath, componentUpdateOperations, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateComponentWithResponse(String digitalTwinId, String componentPath, List<Object> componentUpdateOperations, UpdateComponentRequestOptions options, Context context) {
        String ifMatch = options != null ? options.getIfMatch() : null;

        return protocolLayer.getDigitalTwins().updateComponentWithResponseAsync(digitalTwinId, componentPath, ifMatch, componentUpdateOperations, context)
            .flatMap(response -> {
                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null, twinHeaders));
            });
    }

    //endregion Component APIs

    //region Query APIs

    /**
     * Query digital twins.
     * @param query The query string, in SQL-like syntax.
     * @param clazz The model class to deserialize each queried digital twin into. Since the queried twins may not all
     *              have the same model class, it is recommended to use a common denominator class such as {@link BasicDigitalTwin}.
     * @param <T> The generic type to deserialize each queried digital twin into.
     * @return A {@link PagedFlux} of deserialized digital twins.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> query(String query, Class<T> clazz) {
        return new PagedFlux<T>(
            () -> withContext(context -> queryFirstPage(query, clazz, context)),
            nextLink -> withContext(context -> queryNextPage(nextLink, clazz, context)));
    }

    <T> PagedFlux<T> query(String query, Class<T> clazz, Context context) {
        return new PagedFlux<>(
            () -> queryFirstPage(query, clazz, context),
            nextLink -> queryNextPage(nextLink, clazz, context));
    }

    <T> Mono<PagedResponse<T>> queryFirstPage(String query, Class<T> clazz, Context context) {
        QuerySpecification querySpecification = new QuerySpecification().setQuery(query);

        return protocolLayer
            .getQueries()
            .queryTwinsWithResponseAsync(querySpecification, context)
            .map(objectPagedResponse -> new PagedResponseBase<>(
                objectPagedResponse.getRequest(),
                objectPagedResponse.getStatusCode(),
                objectPagedResponse.getHeaders(),
                objectPagedResponse.getValue().getItems().stream()
                    .map(object -> {
                        try {
                            return DeserializationHelpers.deserializeObject(mapper, object, clazz, this.serializer);
                        } catch (JsonProcessingException e) {
                            logger.error("JsonProcessingException occurred while deserializing the query response: ", e);
                            throw new RuntimeException("JsonProcessingException occurred while deserializing the query response: ", e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()),
                SerializationHelpers.serializeContinuationToken(objectPagedResponse.getValue().getContinuationToken()),
                objectPagedResponse.getDeserializedHeaders()));
    }

    <T> Mono<PagedResponse<T>> queryNextPage(String nextLink, Class<T> clazz, Context context) {
        QuerySpecification querySpecification = new QuerySpecification().setContinuationToken(nextLink);

        return protocolLayer
            .getQueries()
            .queryTwinsWithResponseAsync(querySpecification, context)
            .map(objectPagedResponse -> new PagedResponseBase<>(
                objectPagedResponse.getRequest(),
                objectPagedResponse.getStatusCode(),
                objectPagedResponse.getHeaders(),
                objectPagedResponse.getValue().getItems().stream()
                    .map(object -> {
                        try {
                            return DeserializationHelpers.deserializeObject(mapper, object, clazz, this.serializer);
                        } catch (JsonProcessingException e) {
                            logger.error("JsonProcessingException occurred while deserializing the query response: ", e);
                            throw new RuntimeException("JsonProcessingException occurred while deserializing the query response: ", e);
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()),
                SerializationHelpers.serializeContinuationToken(objectPagedResponse.getValue().getContinuationToken()),
                objectPagedResponse.getDeserializedHeaders()));
    }

    //endregion Query APIs

    //region Event Route APIs

    /**
     * Create an event route.
     * @param eventRouteId The Id of the event route to create.
     * @param eventRoute The event route to create.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createEventRoute(String eventRouteId, EventRoute eventRoute)
    {
        return createEventRouteWithResponse(eventRouteId, eventRoute)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Create an event route.
     * @param eventRouteId The Id of the event route to create.
     * @param eventRoute The event route to create.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createEventRouteWithResponse(String eventRouteId, EventRoute eventRoute)
    {
        return withContext(context -> createEventRouteWithResponse(eventRouteId, eventRoute, context));
    }

    Mono<Response<Void>> createEventRouteWithResponse(String eventRouteId, EventRoute eventRoute, Context context)
    {
        return this.protocolLayer.getEventRoutes().addWithResponseAsync(eventRouteId, EventRouteConverter.map(eventRoute), context);
    }

    /**
     * Get an event route.
     * @param eventRouteId The Id of the event route to get.
     * @return The retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EventRoute> getEventRoute(String eventRouteId)
    {
        return getEventRouteWithResponse(eventRouteId)
            .map(Response::getValue);
    }

    /**
     * Get an event route.
     * @param eventRouteId The Id of the event route to get.
     * @return A {@link Response} containing the retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EventRoute>> getEventRouteWithResponse(String eventRouteId)
    {
        return withContext(context -> getEventRouteWithResponse(eventRouteId, context));
    }

    Mono<Response<EventRoute>> getEventRouteWithResponse(String eventRouteId, Context context)
    {
        return this.protocolLayer.getEventRoutes().getByIdWithResponseAsync(eventRouteId, context)
            .map(eventRouteResponse -> new SimpleResponse<>(
                eventRouteResponse.getRequest(),
                eventRouteResponse.getStatusCode(),
                eventRouteResponse.getHeaders(),
                EventRouteConverter.map(eventRouteResponse.getValue())));
    }

    /**
     * Delete an event route.
     * @param eventRouteId The Id of the event route to delete.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEventRoute(String eventRouteId)
    {
        return deleteEventRouteWithResponse(eventRouteId)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Delete an event route.
     * @param eventRouteId The Id of the event route to delete.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEventRouteWithResponse(String eventRouteId)
    {
        return withContext(context -> deleteEventRouteWithResponse(eventRouteId, context));
    }

    Mono<Response<Void>> deleteEventRouteWithResponse(String eventRouteId, Context context)
    {
        return this.protocolLayer.getEventRoutes().deleteWithResponseAsync(eventRouteId, context);
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     * @return A {@link PagedFlux} that contains all the event routes that exist in your digital twins instance.
     * This PagedFlux may take multiple service requests to iterate over all event routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<EventRoute> listEventRoutes()
    {
        return listEventRoutes(new EventRoutesListOptions());
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     * @param options The optional parameters to use when listing event routes. See {@link EventRoutesListOptions} for more details
     * on what optional parameters can be set.
     * @return A {@link PagedFlux} that contains all the event routes that exist in your digital twins instance.
     * This PagedFlux may take multiple service requests to iterate over all event routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<EventRoute> listEventRoutes(EventRoutesListOptions options)
    {
        return new PagedFlux<>(
            () -> withContext(context -> listEventRoutesFirstPage(options, context)),
            nextLink -> withContext(context -> listEventRoutesNextPage(this.protocolLayer.getHost() + nextLink, context)));
    }

    PagedFlux<EventRoute> listEventRoutes(EventRoutesListOptions options, Context context)
    {
        return new PagedFlux<>(
            () -> listEventRoutesFirstPage(options, context),
            nextLink -> listEventRoutesNextPage(this.protocolLayer.getHost() + nextLink, context));
    }

    Mono<PagedResponse<EventRoute>> listEventRoutesFirstPage(EventRoutesListOptions options, Context context) {
        return protocolLayer
            .getEventRoutes()
            .listSinglePageAsync(EventRouteListOptionsConverter.map(options), context)
            .map(pagedEventRouteMappingFunction);
    }

    Mono<PagedResponse<EventRoute>> listEventRoutesNextPage(String nextLink, Context context) {
        return protocolLayer
            .getEventRoutes()
            .listNextSinglePageAsync(nextLink, context)
            .map(pagedEventRouteMappingFunction);
    }

    private Function<PagedResponse<com.azure.digitaltwins.core.implementation.models.EventRoute>, PagedResponse<EventRoute>> pagedEventRouteMappingFunction = (pagedEventRouteResponse) -> {
        List<EventRoute> convertedList = pagedEventRouteResponse.getValue().stream()
            .map(EventRouteConverter::map)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return new PagedResponseBase<>(
            pagedEventRouteResponse.getRequest(),
            pagedEventRouteResponse.getStatusCode(),
            pagedEventRouteResponse.getHeaders(),
            convertedList,
            pagedEventRouteResponse.getContinuationToken(),
            ((PagedResponseBase) pagedEventRouteResponse).getDeserializedHeaders());
    };

    //endregion Event Route APIs

    //region Telemetry APIs

    /**
     * Publishes telemetry from a digital twin
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> publishTelemetry(String digitalTwinId, Object payload) {
        PublishTelemetryRequestOptions publishTelemetryRequestOptions = new PublishTelemetryRequestOptions();
        return withContext(context -> publishTelemetryWithResponse(digitalTwinId, payload, publishTelemetryRequestOptions, context))
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Publishes telemetry from a digital twin
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @param publishTelemetryRequestOptions The additional information to be used when processing a telemetry request.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishTelemetryWithResponse(String digitalTwinId, Object payload, PublishTelemetryRequestOptions publishTelemetryRequestOptions) {
        return withContext(context -> publishTelemetryWithResponse(digitalTwinId, payload, publishTelemetryRequestOptions, context));
    }

    Mono<Response<Void>> publishTelemetryWithResponse(String digitalTwinId, Object payload, PublishTelemetryRequestOptions publishTelemetryRequestOptions, Context context) {
        return protocolLayer.getDigitalTwins().sendTelemetryWithResponseAsync(
            digitalTwinId,
            publishTelemetryRequestOptions.getMessageId(),
            payload,
            publishTelemetryRequestOptions.getTimestamp().toString(),
            context);
    }

    /**
     * Publishes telemetry from a digital twin's component
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> publishComponentTelemetry(String digitalTwinId, String componentName, Object payload) {
        PublishTelemetryRequestOptions publishTelemetryRequestOptions = new PublishTelemetryRequestOptions();
        return withContext(context -> publishComponentTelemetryWithResponse(digitalTwinId, componentName, payload, publishTelemetryRequestOptions, context))
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Publishes telemetry from a digital twin's component
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @param publishTelemetryRequestOptions The additional information to be used when processing a telemetry request.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishComponentTelemetryWithResponse(String digitalTwinId, String componentName, Object payload, PublishTelemetryRequestOptions publishTelemetryRequestOptions) {
        return withContext(context -> publishComponentTelemetryWithResponse(digitalTwinId, componentName, payload, publishTelemetryRequestOptions, context));
    }

    Mono<Response<Void>> publishComponentTelemetryWithResponse(String digitalTwinId, String componentName, Object payload, PublishTelemetryRequestOptions publishTelemetryRequestOptions, Context context) {
        return protocolLayer.getDigitalTwins().sendComponentTelemetryWithResponseAsync(
            digitalTwinId,
            componentName,
            publishTelemetryRequestOptions.getMessageId(),
            payload,
            publishTelemetryRequestOptions.getTimestamp().toString(),
            context);
    }

    //endregion Telemetry APIs
}
