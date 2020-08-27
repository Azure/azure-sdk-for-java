// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImpl;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImplBuilder;
import com.azure.digitaltwins.core.implementation.models.IncomingRelationship;
import com.azure.digitaltwins.core.implementation.serializer.DigitalTwinsStringSerializer;
import com.azure.digitaltwins.core.util.DigitalTwinsResponse;
import com.azure.digitaltwins.core.util.DigitalTwinsResponseHeaders;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;


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
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin)
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

    // TODO: This is a temporary implementation for sample purposes. This should be spruced up/replaced once this API is actually designed.
    // Input is Object and output is Response<T>.
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> createDigitalTwinWithResponse(String digitalTwinId, Object digitalTwin, Class<T> klazz) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin)
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
     * @return The application/json relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> createRelationship(String digitalTwinId, String relationshipId, String relationship) {
        return protocolLayer
            .getDigitalTwins()
            .addRelationshipWithResponseAsync(digitalTwinId, relationshipId, relationship)
            .flatMap(
                response -> {
                    try {
                        String jsonResponse = mapper.writeValueAsString(response.getValue());
                        return Mono.just(jsonResponse);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
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
    public Mono<DigitalTwinsResponse<String>> createRelationshipWithResponse(String digitalTwinId, String relationshipId, String relationship) {
        return protocolLayer
            .getDigitalTwins()
            .addRelationshipWithResponseAsync(digitalTwinId, relationshipId, relationship)
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

    /**
     * Creates a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The relationship to be created.
     * @param modelClass The model class to convert the relationship to.
     * @return The relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> createRelationship(String digitalTwinId, String relationshipId, Object relationship, Class<T> modelClass) {
        return protocolLayer
            .getDigitalTwins()
            .addRelationshipWithResponseAsync(digitalTwinId, relationshipId, relationship)
            .flatMap(
                response -> Mono.just(mapper.convertValue(response.getValue(), modelClass)));
    }

    /**
     * Creates a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The relationship to be created.
     * @param modelClass The model class to convert the relationship to.
     * @return A REST response containing the relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> createRelationshipWithResponse(String digitalTwinId, String relationshipId, Object relationship, Class<T> modelClass) {
        return protocolLayer
            .getDigitalTwins()
            .addRelationshipWithResponseAsync(digitalTwinId, relationshipId, relationship)
            .flatMap(
                response -> {
                    T genericResponse = mapper.convertValue(response.getValue(), modelClass);
                    DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                    return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
                });
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @return The application/json relationship corresponding to the provided relationshipId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> getRelationship(String digitalTwinId, String relationshipId) {
        return protocolLayer.getDigitalTwins().getRelationshipByIdWithResponseAsync(digitalTwinId, relationshipId)
            .flatMap(
                response -> {
                    try {
                        String jsonResponse = mapper.writeValueAsString(response.getValue());
                        return Mono.just(jsonResponse);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @return A REST response containing the application/json relationship corresponding to the provided relationshipId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<String>> getRelationshipWithResponse(String digitalTwinId, String relationshipId) {
        return protocolLayer.getDigitalTwins().getRelationshipByIdWithResponseAsync(digitalTwinId, relationshipId)
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

    /**
     * Gets a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @param modelClass The model class to convert the relationship to.
     * @return The relationship corresponding to the provided relationshipId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getRelationship(String digitalTwinId, String relationshipId, Class<T> modelClass) {
        return protocolLayer.getDigitalTwins().getRelationshipByIdWithResponseAsync(digitalTwinId, relationshipId)
            .flatMap(
                response -> Mono.just(mapper.convertValue(response.getValue(), modelClass)));
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @param modelClass The model class to convert the relationship to.
     * @return A REST response containing the relationship corresponding to the provided relationshipId.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getRelationshipWithResponse(String digitalTwinId, String relationshipId, Class<T> modelClass) {
        return protocolLayer.getDigitalTwins().getRelationshipByIdWithResponseAsync(digitalTwinId, relationshipId)
            .flatMap(
                response -> {
                    T genericResponse = mapper.convertValue(response.getValue(), modelClass);
                    DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                    return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
                });
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param relationshipUpdateOperations The application/json-patch+json operations to be performed on the specified digital twin's relationship.
     * @return An empty response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateRelationship(String digitalTwinId, String relationshipId, List<Object> relationshipUpdateOperations) {
        return protocolLayer.getDigitalTwins().updateRelationshipWithResponseAsync(digitalTwinId, relationshipId, null, relationshipUpdateOperations)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param relationshipUpdateOperations The application/json-patch+json operations to be performed on the specified digital twin's relationship.
     * @param options The optional settings for this request.
     * @return A REST response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateRelationshipWithResponse(String digitalTwinId, String relationshipId, List<Object> relationshipUpdateOperations, RequestOptions options) {
        return protocolLayer.getDigitalTwins().updateRelationshipWithResponseAsync(digitalTwinId, relationshipId, options.getIfMatch(), relationshipUpdateOperations)
            .flatMap(
                response -> {
                    DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                    return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), response.getValue(), twinHeaders));
                });
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRelationship(String digitalTwinId, String relationshipId) {
        return protocolLayer.getDigitalTwins().deleteRelationshipWithResponseAsync(digitalTwinId, relationshipId, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @param options The optional settings for this request.
     * @return A REST response.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, RequestOptions options) {
        return protocolLayer.getDigitalTwins().deleteRelationshipWithResponseAsync(digitalTwinId, relationshipId, options.getIfMatch());
    }

    /**
     * Gets all the relationships on a digital twin by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @return A {@link PagedFlux} of application/json relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<String> listRelationships(String digitalTwinId) {
        return listRelationships(digitalTwinId, (String) null);
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
        Supplier<Mono<PagedResponse<String>>> firstPage = () -> protocolLayer.getDigitalTwins().listRelationshipsSinglePageAsync(digitalTwinId, relationshipName)
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

        Function<String, Mono<PagedResponse<String>>> nextPage = nextLink -> protocolLayer.getDigitalTwins().listRelationshipsNextSinglePageAsync(nextLink)
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

        return new PagedFlux<>(firstPage, nextPage);
    }

    /**
     * Gets all the relationships on a digital twin by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param modelClass The model class to convert the relationship to. Since a digital twin might have relationships conforming to different models, it is advisable to convert them to a generic model.
     * @return A {@link PagedFlux} of relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> listRelationships(String digitalTwinId, Class<T> modelClass) {
        return listRelationships(digitalTwinId, null, modelClass);
    }

    /**
     * Gets all the relationships on a digital twin filtered by the relationship name, by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipName The name of a relationship to filter to.
     * @param modelClass The model class to convert the relationship to.
     * @return A {@link PagedFlux} of relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> listRelationships(String digitalTwinId, String relationshipName, Class<T> modelClass) {
        Supplier<Mono<PagedResponse<T>>> firstPage = () -> protocolLayer.getDigitalTwins().listRelationshipsSinglePageAsync(digitalTwinId, relationshipName)
            .map(
                objectPagedResponse -> {
                    List<T> list = objectPagedResponse.getValue().stream()
                        .map(object -> mapper.convertValue(object, modelClass))
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

        Function<String, Mono<PagedResponse<T>>> nextPage = nextLink -> protocolLayer.getDigitalTwins().listRelationshipsNextSinglePageAsync(nextLink)
            .map(objectPagedResponse -> {
                List<T> stringList = objectPagedResponse.getValue().stream()
                    .map(object -> mapper.convertValue(object, modelClass))
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

        return new PagedFlux<>(firstPage, nextPage);
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
            () -> protocolLayer.getDigitalTwins().listIncomingRelationshipsSinglePageAsync(digitalTwinId),
            nextLink -> protocolLayer.getDigitalTwins().listIncomingRelationshipsNextSinglePageAsync(nextLink)
        );
    }

}
