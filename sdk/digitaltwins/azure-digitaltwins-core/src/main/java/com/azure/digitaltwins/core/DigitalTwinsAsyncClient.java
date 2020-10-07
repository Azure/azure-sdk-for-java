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
import com.azure.digitaltwins.core.implementation.converters.IncomingRelationshipConverter;
import com.azure.digitaltwins.core.implementation.converters.DigitalTwinsModelDataConverter;
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
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client for interacting asynchronously with an Azure Digital Twins instance.
 * This client is instantiated through {@link DigitalTwinsClientBuilder}.
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.digitaltwins.core.asyncClient.instantiation}
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
    private final JsonSerializer serializer;

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
     * <p><strong>Code Samples</strong></p>
     *
     * <p> You can provide a strongly typed digital twin object such as {@link BasicDigitalTwin} as the input parameter:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createDigitalTwins#String-Object-Class#String}
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
        return createDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, null)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Creates a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p> You can provide a strongly typed digital twin object such as {@link BasicDigitalTwin} as the input parameter:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#String}
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> createDigitalTwinWithResponse(String digitalTwinId, T digitalTwin, Class<T> clazz, DigitalTwinsAddOptions options) {
        return withContext(context -> createDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, options, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> createDigitalTwinWithResponse(String digitalTwinId, T digitalTwin, Class<T> clazz, DigitalTwinsAddOptions options, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin, options, context)
            .flatMap(response -> {
                T genericResponse;
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
     <p><strong>Code Samples</strong></p>
     *
     * <p>
     * A Strongly typed object type such as {@link BasicDigitalTwin} can be provided as an input parameter for {@code clazz}
     * to indicate what type is used to deserialize the response.
     * </p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getDigitalTwin#String-Class#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getDigitalTwin#String-Class#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @return The deserialized application/json object representing the digital twin
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getDigitalTwin(String digitalTwinId, Class<T> clazz)
    {
        return getDigitalTwinWithResponse(digitalTwinId, clazz, null)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Gets a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>
     * A Strongly typed object type such as {@link BasicDigitalTwin} can be provided as an input parameter for {@code clazz}
     * to indicate what type is used to deserialize the response.
     * </p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getDigitalTwinWithResponse#String-Class-Options#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getDigitalTwinWithResponse#String-Class-Options#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz, DigitalTwinsGetByIdOptions options)
    {
        return withContext(context -> getDigitalTwinWithResponse(digitalTwinId, clazz, options, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz, DigitalTwinsGetByIdOptions options, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .getByIdWithResponseAsync(digitalTwinId, options, context)
            .flatMap(response -> {
                T genericResponse;
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
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update digital twin by providing list of intended patch operations.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.updateDigitalTwin#String-List}
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param jsonPatch The JSON patch to apply to the specified digital twin.
     *                                    This argument can be created using {@link UpdateOperationUtility}.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateDigitalTwin(String digitalTwinId, List<Object> jsonPatch)
    {
        return updateDigitalTwinWithResponse(digitalTwinId, jsonPatch, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Updates a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update digital twin by providing list of intended patch operations.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.updateDigitalTwinWithResponse#String-List-Options}
     *
     * @param digitalTwinId The Id of the digital twin.
     * @param jsonPatch The JSON patch to apply to the specified digital twin.
     *                                    This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateDigitalTwinWithResponse(String digitalTwinId, List<Object> jsonPatch, DigitalTwinsUpdateOptions options)
    {
        return withContext(context -> updateDigitalTwinWithResponse(digitalTwinId, jsonPatch, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateDigitalTwinWithResponse(String digitalTwinId, List<Object> jsonPatch, DigitalTwinsUpdateOptions options, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .updateWithResponseAsync(digitalTwinId, jsonPatch, options, context)
            .map(response -> {
                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), response.getValue(), twinHeaders);
            });
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.deleteDigitalTwin#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDigitalTwin(String digitalTwinId)
    {
        return deleteDigitalTwinWithResponse(digitalTwinId, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.deleteDigitalTwinWithResponse#String-Options}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return The Http response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDigitalTwinWithResponse(String digitalTwinId, DigitalTwinsDeleteOptions options)
    {
        return withContext(context -> deleteDigitalTwinWithResponse(digitalTwinId, options, context));
    }

    Mono<Response<Void>> deleteDigitalTwinWithResponse(String digitalTwinId, DigitalTwinsDeleteOptions options, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .deleteWithResponseAsync(digitalTwinId, options, context);
    }

    //endregion Digital twin APIs

    //region Relationship APIs

    /**
     * Creates a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createRelationship#String-String-Object-Class#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createRelationship#String-String-Object-Class#String}
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
        return createRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz, null)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Creates a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createRelationshipWithResponse#String-String-Object-Class-Options#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createRelationshipWithResponse#String-String-Object-Class-Options#String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The relationship to be created.
     * @param clazz The model class of the relationship.
     * @param <T> The generic type of the relationship.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing the relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> createRelationshipWithResponse(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz, DigitalTwinsAddRelationshipOptions options) {
        return withContext(context -> createRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz, options, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> createRelationshipWithResponse(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz, DigitalTwinsAddRelationshipOptions options, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .addRelationshipWithResponseAsync(digitalTwinId, relationshipId, relationship, options, context)
            .flatMap(response -> {
                T genericResponse;
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
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getRelationship#String#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getRelationship#String#String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @param clazz The model class to deserialize the relationship into.
     * @param <T> The generic type to deserialize the relationship into.
     * @return The deserialized relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getRelationship(String digitalTwinId, String relationshipId, Class<T> clazz) {
        return getRelationshipWithResponse(digitalTwinId, relationshipId, clazz, null)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getRelationshipWithResponse#String-String-Class-Options#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getRelationshipWithResponse#String-String-Class-Options#String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to retrieve.
     * @param clazz The model class to deserialize the relationship into.
     * @param <T> The generic type to deserialize the relationship into.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing the deserialized relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getRelationshipWithResponse(String digitalTwinId, String relationshipId, Class<T> clazz, DigitalTwinsGetRelationshipByIdOptions options) {
        return withContext(context -> getRelationshipWithResponse(digitalTwinId, relationshipId, clazz, options, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getRelationshipWithResponse(String digitalTwinId, String relationshipId, Class<T> clazz, DigitalTwinsGetRelationshipByIdOptions options, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .getRelationshipByIdWithResponseAsync(digitalTwinId, relationshipId, options, context)
            .flatMap(response -> {
                T genericResponse;
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
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.updateRelationship#String-String-List}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                     This argument can be created using {@link UpdateOperationUtility}.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateRelationship(String digitalTwinId, String relationshipId, List<Object> jsonPatch) {
        return updateRelationshipWithResponse(digitalTwinId, relationshipId, jsonPatch, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.updateRelationshipWithResponse#String-String-List-Options}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                     This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateRelationshipWithResponse(String digitalTwinId, String relationshipId, List<Object> jsonPatch, DigitalTwinsUpdateRelationshipOptions options) {
        return withContext(context -> updateRelationshipWithResponse(digitalTwinId, relationshipId, jsonPatch, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateRelationshipWithResponse(String digitalTwinId, String relationshipId, List<Object> jsonPatch, DigitalTwinsUpdateRelationshipOptions options, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .updateRelationshipWithResponseAsync(digitalTwinId, relationshipId, jsonPatch, options, context)
            .map(response -> {
                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), response.getValue(), twinHeaders);
            });
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.deleteRelationship#String-String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRelationship(String digitalTwinId, String relationshipId) {
        return deleteRelationshipWithResponse(digitalTwinId, relationshipId, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.deleteRelationshipWithResponse#String-String-Options}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, DigitalTwinsDeleteRelationshipOptions options) {
        return withContext(context -> deleteRelationshipWithResponse(digitalTwinId, relationshipId, options, context));
    }

    Mono<Response<Void>> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, DigitalTwinsDeleteRelationshipOptions options, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .deleteRelationshipWithResponseAsync(digitalTwinId, relationshipId, options, context);
    }

    /**
     * Gets all the relationships on a digital twin by iterating through a collection.
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.listRelationships#String-Class-Options#BasicRelationship#IterateByItem}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.listRelationships#String-Class-Options#String#IterateByItem}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param clazz The model class to convert the relationship to. Since a digital twin might have relationships conforming to different models, it is advisable to convert them to a generic model like {@link BasicRelationship}.
     * @param <T> The generic type to convert the relationship to.
     * @return A {@link PagedFlux} of relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> listRelationships(String digitalTwinId, Class<T> clazz) {
        return listRelationships(digitalTwinId, null, clazz, null);
    }

    /**
     * Gets all the relationships on a digital twin filtered by the relationship name, by iterating through a collection.
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.listRelationships#String-String-Class-Options#BasicRelationship#IterateByItem}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.listRelationships#String-String-Class-Options#String#IterateByItem}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipName The name of a relationship to filter to.
     * @param clazz The model class to convert the relationship to.
     * @param <T> The generic type to convert the relationship to.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link PagedFlux} of relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> listRelationships(String digitalTwinId, String relationshipName, Class<T> clazz, DigitalTwinsListRelationshipsOptions options) {
        return new PagedFlux<>(
            () -> withContext(context -> listRelationshipsFirstPage(digitalTwinId, relationshipName, clazz, options, context)),
            nextLink -> withContext(context -> listRelationshipsNextPage(nextLink, clazz, options, context)));
    }

    <T> PagedFlux<T> listRelationships(String digitalTwinId, String relationshipName, Class<T> clazz, DigitalTwinsListRelationshipsOptions options, Context context) {
        return new PagedFlux<>(
            () -> listRelationshipsFirstPage(digitalTwinId, relationshipName, clazz, options, context),
            nextLink -> listRelationshipsNextPage(nextLink, clazz, options, context));
    }

    <T> Mono<PagedResponse<T>> listRelationshipsFirstPage(String digitalTwinId, String relationshipName, Class<T> clazz, DigitalTwinsListRelationshipsOptions options, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .listRelationshipsSinglePageAsync(digitalTwinId, relationshipName, options, context)
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

    <T> Mono<PagedResponse<T>> listRelationshipsNextPage(String nextLink, Class<T> clazz, DigitalTwinsListRelationshipsOptions options, Context context) {
        return protocolLayer
            .getDigitalTwins()
            .listRelationshipsNextSinglePageAsync(nextLink, options, context)
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

    /**
     * Gets all the relationships referencing a digital twin as a target by iterating through a collection.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.listIncomingRelationships#String-Options}
     *
     * @param digitalTwinId The Id of the target digital twin.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link PagedFlux} of relationships directed towards the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<IncomingRelationship> listIncomingRelationships(String digitalTwinId, DigitalTwinsListIncomingRelationshipsOptions options) {
        return new PagedFlux<>(
            () -> withContext(context -> listIncomingRelationshipsFirstPageAsync(digitalTwinId, options, context)),
            nextLink -> withContext(context -> listIncomingRelationshipsNextSinglePageAsync(nextLink, options, context)));
    }

    PagedFlux<IncomingRelationship> listIncomingRelationships(String digitalTwinId, DigitalTwinsListIncomingRelationshipsOptions options, Context context) {
        return new PagedFlux<>(
            () -> listIncomingRelationshipsFirstPageAsync(digitalTwinId, options, context),
            nextLink -> listIncomingRelationshipsNextSinglePageAsync(nextLink, options, context));
    }

    Mono<PagedResponse<IncomingRelationship>> listIncomingRelationshipsFirstPageAsync(String digitalTwinId, DigitalTwinsListIncomingRelationshipsOptions options, Context context){
        return protocolLayer.getDigitalTwins().listIncomingRelationshipsSinglePageAsync(digitalTwinId, options, context)
            .map(pagedIncomingRelationshipMappingFunction);
    }

    Mono<PagedResponse<IncomingRelationship>> listIncomingRelationshipsNextSinglePageAsync(String nextLink, DigitalTwinsListIncomingRelationshipsOptions options, Context context){
        return protocolLayer.getDigitalTwins().listIncomingRelationshipsNextSinglePageAsync(nextLink, options, context)
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
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createModels#Iterable}
     *
     * @param models The list of models to create. Each string corresponds to exactly one model.
     * @return A List of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Iterable<DigitalTwinsModelData>> createModels(Iterable<String> models) {
        return createModelsWithResponse(models, null)
            .map(Response::getValue);
    }

    /**
     * Creates one or many models.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createModelsWithResponse#Iterable-Options}
     *
     * @param models The list of models to create. Each string corresponds to exactly one model.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing the list of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Response<Iterable<DigitalTwinsModelData>>> createModelsWithResponse(Iterable<String> models, DigitalTwinModelsAddOptions options) {
        return withContext(context -> createModelsWithResponse(models, options, context));
    }

    Mono<Response<Iterable<DigitalTwinsModelData>>> createModelsWithResponse(Iterable<String> models, DigitalTwinModelsAddOptions options, Context context) {
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

        return protocolLayer.getDigitalTwinModels().addWithResponseAsync(modelsPayload, options, context)
            .map(listResponse -> {
                Iterable<DigitalTwinsModelData> convertedList = listResponse.getValue().stream()
                    .map(DigitalTwinsModelDataConverter::map)
                    .collect(Collectors.toList());

                return new SimpleResponse<>(listResponse.getRequest(), listResponse.getStatusCode(), listResponse.getHeaders(), convertedList);
            });
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getModel#String}
     *
     * @param modelId The Id of the model.
     * @return A {@link DigitalTwinsModelData} instance that contains the model and its metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsModelData> getModel(String modelId) {
        return getModelWithResponse(modelId, null)
            .map(Response::getValue);
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getModelWithResponse#String-Options}
     *
     * @param modelId The Id of the model.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing a {@link DigitalTwinsModelData} instance that contains the model and its metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DigitalTwinsModelData>> getModelWithResponse(String modelId, DigitalTwinModelsGetByIdOptions options) {
        return withContext(context -> getModelWithResponse(modelId, options, context));
    }

    Mono<Response<DigitalTwinsModelData>> getModelWithResponse(String modelId, DigitalTwinModelsGetByIdOptions options, Context context){
        return protocolLayer
            .getDigitalTwinModels()
            .getByIdWithResponseAsync(modelId, includeModelDefinitionOnGet, options, context)
            .map(response -> {
                com.azure.digitaltwins.core.implementation.models.DigitalTwinsModelData modelData = response.getValue();
                return new SimpleResponse<>(
                    response.getRequest(),
                    response.getStatusCode(),
                    response.getHeaders(),
                    DigitalTwinsModelDataConverter.map(modelData));
            });
    }

    /**
     * List all of the models in this digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.listModels}
     *
     * @return A {@link PagedFlux} of {@link DigitalTwinsModelData} that enumerates all the models.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DigitalTwinsModelData> listModels() {
        return listModels(null);
    }

    /**
     * List the models in this digital twins instance based on some options.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.listModels#Options}
     *
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link PagedFlux} containing the retrieved {@link DigitalTwinsModelData} instances.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DigitalTwinsModelData> listModels(DigitalTwinModelsListOptions options) {
        return new PagedFlux<>(
            () -> withContext(context -> listModelsSinglePageAsync(options, context)),
            nextLink -> withContext(context -> listModelsNextSinglePageAsync(nextLink, options, context)));
    }

    PagedFlux<DigitalTwinsModelData> listModels(DigitalTwinModelsListOptions options, Context context){
        return new PagedFlux<>(
            () -> listModelsSinglePageAsync(options, context),
            nextLink -> listModelsNextSinglePageAsync(nextLink, options, context));
    }

    Mono<PagedResponse<DigitalTwinsModelData>> listModelsSinglePageAsync(DigitalTwinModelsListOptions options, Context context){
        // default values for these options
        List<String> getDependenciesFor = null;
        boolean includeModelDefinition = true; //service default is false, but we expect customers to want the model definitions by default
        com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions protocolLayerOptions = null;

        if (options != null) {
            protocolLayerOptions = new com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions()
                .setMaxItemsPerPage(options.getMaxItemsPerPage())
                .setTraceparent(options.getTraceparent())
                .setTracestate(options.getTracestate());
            getDependenciesFor = options.getDependenciesFor();
            includeModelDefinition = options.getIncludeModelDefinition();
        }

        return protocolLayer.getDigitalTwinModels().listSinglePageAsync(
            getDependenciesFor,
            includeModelDefinition,
            protocolLayerOptions,
            context)
            .map(
                objectPagedResponse -> {
                    List<DigitalTwinsModelData> convertedList = objectPagedResponse.getValue().stream()
                        .map(DigitalTwinsModelDataConverter::map)
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

    Mono<PagedResponse<DigitalTwinsModelData>> listModelsNextSinglePageAsync(String nextLink, DigitalTwinModelsListOptions options, Context context){
        com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions protocolLayerOptions = null;
        if (options != null) {
            protocolLayerOptions = new com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions()
                .setMaxItemsPerPage(options.getMaxItemsPerPage())
                .setTraceparent(options.getTraceparent())
                .setTracestate(options.getTracestate());
        }

        return protocolLayer.getDigitalTwinModels().listNextSinglePageAsync(
            nextLink,
            protocolLayerOptions,
            context)
            .map(objectPagedResponse -> {
                List<DigitalTwinsModelData> convertedList = objectPagedResponse.getValue().stream()
                    .map(DigitalTwinsModelDataConverter::map)
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
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.deleteModel#String}
     *
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteModel(String modelId) {
        return deleteModelWithResponse(modelId, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Deletes a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.deleteModelWithResponse#String-Options}
     *
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} with no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteModelWithResponse(String modelId, DigitalTwinModelsDeleteOptions options) {
        return withContext(context -> deleteModelWithResponse(modelId, options, context));
    }

    Mono<Response<Void>> deleteModelWithResponse(String modelId, DigitalTwinModelsDeleteOptions options, Context context){
        return protocolLayer.getDigitalTwinModels().deleteWithResponseAsync(modelId, options, context);
    }

    /**
     * Decommissions a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.decommissionModel#String}
     *
     * @param modelId The Id of the model to decommission.
     * @return an empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> decommissionModel(String modelId) {
        return decommissionModelWithResponse(modelId, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Decommissions a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.decommissionModelWithResponse#String-Options}
     *
     * @param modelId The Id of the model to decommission.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} with no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> decommissionModelWithResponse(String modelId, DigitalTwinModelsUpdateOptions options) {
        return withContext(context -> decommissionModelWithResponse(modelId, options, context));
    }

    Mono<Response<Void>> decommissionModelWithResponse(String modelId, DigitalTwinModelsUpdateOptions options, Context context) {
        List<Object> updateOperation = new UpdateOperationUtility()
            .appendReplaceOperation("/decommissioned", true)
            .getUpdateOperations();

        return protocolLayer.getDigitalTwinModels().updateWithResponseAsync(modelId, updateOperation, options, context);
    }

    //endregion Model APIs

    //region Component APIs

    /**
     * Get a component of a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getComponent#String-String-Class}
     *
     * @param digitalTwinId The Id of the digital twin to get the component from.
     * @param componentPath The path of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize application/json the component into.
     * @return The deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getComponent(String digitalTwinId, String componentPath, Class<T> clazz) {
        return getComponentWithResponse(digitalTwinId, componentPath, clazz, null)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Get a component of a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getComponentWithResponse#String-String-Class-Options}
     *
     * @param digitalTwinId The Id of the digital twin to get the component from.
     * @param componentPath The path of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize the application/json component into.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getComponentWithResponse(String digitalTwinId, String componentPath, Class<T> clazz, DigitalTwinsGetComponentOptions options) {
        return withContext(context -> getComponentWithResponse(digitalTwinId, componentPath, clazz, options, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getComponentWithResponse(String digitalTwinId, String componentPath, Class<T> clazz, DigitalTwinsGetComponentOptions options, Context context) {
        return protocolLayer.getDigitalTwins().getComponentWithResponseAsync(digitalTwinId, componentPath, options, context)
            .flatMap(response -> {
                T genericResponse;
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
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.updateComponent#String-String-List}
     *
     * @param digitalTwinId The Id of the digital twin that has the component to patch.
     * @param componentPath The path of the component on the digital twin.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                  This argument can be created using {@link UpdateOperationUtility}.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateComponent(String digitalTwinId, String componentPath, List<Object> jsonPatch) {
        return updateComponentWithResponse(digitalTwinId, componentPath, jsonPatch, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Patch a component on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.updateComponentWithResponse#String-String-List-Options}
     *
     * @param digitalTwinId The Id of the digital twin that has the component to patch.
     * @param componentPath The path of the component on the digital twin.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                  This argument can be created using {@link UpdateOperationUtility}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing an empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateComponentWithResponse(String digitalTwinId, String componentPath, List<Object> jsonPatch, DigitalTwinsUpdateComponentOptions options) {
        return withContext(context -> updateComponentWithResponse(digitalTwinId, componentPath, jsonPatch, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateComponentWithResponse(String digitalTwinId, String componentPath, List<Object> jsonPatch, DigitalTwinsUpdateComponentOptions options, Context context) {
        return protocolLayer.getDigitalTwins().updateComponentWithResponseAsync(digitalTwinId, componentPath, jsonPatch, options, context)
            .flatMap(response -> {
                DigitalTwinsResponseHeaders twinHeaders = mapper.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), null, twinHeaders));
            });
    }

    //endregion Component APIs

    //region Query APIs

    /**
     * Query digital twins.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicDigitalTwin} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.query#String-Options#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.query#String-Options#String}
     *
     * @param query The query string, in SQL-like syntax.
     * @param clazz The model class to deserialize each queried digital twin into. Since the queried twins may not all
     *              have the same model class, it is recommended to use a common denominator class such as {@link BasicDigitalTwin}.
     * @param <T> The generic type to deserialize each queried digital twin into.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link PagedFlux} of deserialized digital twins.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> query(String query, Class<T> clazz, QueryTwinsOptions options) {
        return new PagedFlux<T>(
            () -> withContext(context -> queryFirstPage(query, clazz, options, context)),
            nextLink -> withContext(context -> queryNextPage(nextLink, clazz, options, context)));
    }

    <T> PagedFlux<T> query(String query, Class<T> clazz, QueryTwinsOptions options, Context context) {
        return new PagedFlux<T>(
            () -> queryFirstPage(query, clazz, options, context),
            nextLink -> queryNextPage(nextLink, clazz, options, context));
    }

    <T> Mono<PagedResponse<T>> queryFirstPage(String query, Class<T> clazz, QueryTwinsOptions options, Context context) {
        QuerySpecification querySpecification = new QuerySpecification().setQuery(query);

        return protocolLayer
            .getQueries()
            .queryTwinsWithResponseAsync(querySpecification, options, context)
            .map(objectPagedResponse -> new PagedResponseBase<>(
                objectPagedResponse.getRequest(),
                objectPagedResponse.getStatusCode(),
                objectPagedResponse.getHeaders(),
                objectPagedResponse.getValue().getValue().stream()
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

    <T> Mono<PagedResponse<T>> queryNextPage(String nextLink, Class<T> clazz, QueryTwinsOptions options, Context context) {
        QuerySpecification querySpecification = new QuerySpecification().setContinuationToken(nextLink);

        return protocolLayer
            .getQueries()
            .queryTwinsWithResponseAsync(querySpecification, options, context)
            .map(objectPagedResponse -> new PagedResponseBase<>(
                objectPagedResponse.getRequest(),
                objectPagedResponse.getStatusCode(),
                objectPagedResponse.getHeaders(),
                objectPagedResponse.getValue().getValue().stream()
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
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createEventRoute#String-EventRoute}
     *
     * @param eventRouteId The Id of the event route to create.
     * @param eventRoute The event route to create.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createEventRoute(String eventRouteId, EventRoute eventRoute)
    {
        return createEventRouteWithResponse(eventRouteId, eventRoute, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Create an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.createEventRouteWithResponse#String-EventRoute-Options}
     *
     * @param eventRouteId The Id of the event route to create.
     * @param eventRoute The event route to create.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createEventRouteWithResponse(String eventRouteId, EventRoute eventRoute, EventRoutesAddOptions options)
    {
        return withContext(context -> createEventRouteWithResponse(eventRouteId, eventRoute, options, context));
    }

    Mono<Response<Void>> createEventRouteWithResponse(String eventRouteId, EventRoute eventRoute, EventRoutesAddOptions options, Context context)
    {
        return this.protocolLayer.getEventRoutes().addWithResponseAsync(eventRouteId, EventRouteConverter.map(eventRoute), options, context);
    }

    /**
     * Get an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getEventRoute#String}
     *
     * @param eventRouteId The Id of the event route to get.
     * @return The retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EventRoute> getEventRoute(String eventRouteId)
    {
        return getEventRouteWithResponse(eventRouteId, null)
            .map(Response::getValue);
    }

    /**
     * Get an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.getEventRouteWithResponse#String-Options}
     *
     * @param eventRouteId The Id of the event route to get.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing the retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<EventRoute>> getEventRouteWithResponse(String eventRouteId, EventRoutesGetByIdOptions options)
    {
        return withContext(context -> getEventRouteWithResponse(eventRouteId, options, context));
    }

    Mono<Response<EventRoute>> getEventRouteWithResponse(String eventRouteId, EventRoutesGetByIdOptions options, Context context)
    {
        return this.protocolLayer.getEventRoutes().getByIdWithResponseAsync(eventRouteId, options, context)
            .map(eventRouteResponse -> new SimpleResponse<>(
                eventRouteResponse.getRequest(),
                eventRouteResponse.getStatusCode(),
                eventRouteResponse.getHeaders(),
                EventRouteConverter.map(eventRouteResponse.getValue())));
    }

    /**
     * Delete an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.deleteEventRoute#String}
     *
     * @param eventRouteId The Id of the event route to delete.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEventRoute(String eventRouteId)
    {
        return deleteEventRouteWithResponse(eventRouteId, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Delete an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.deleteEventRouteWithResponse#String-Options}
     *
     * @param eventRouteId The Id of the event route to delete.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEventRouteWithResponse(String eventRouteId, EventRoutesDeleteOptions options)
    {
        return withContext(context -> deleteEventRouteWithResponse(eventRouteId, options, context));
    }

    Mono<Response<Void>> deleteEventRouteWithResponse(String eventRouteId, EventRoutesDeleteOptions options, Context context)
    {
        return this.protocolLayer.getEventRoutes().deleteWithResponseAsync(eventRouteId, options, context);
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.listEventRoutes}
     *
     * @return A {@link PagedFlux} that contains all the event routes that exist in your digital twins instance.
     * This PagedFlux may take multiple service requests to iterate over all event routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<EventRoute> listEventRoutes()
    {
        return listEventRoutes(null);
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.listEventRoutes#Options}
     *
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
            nextLink -> withContext(context -> listEventRoutesNextPage(nextLink, options, context)));
    }

    PagedFlux<EventRoute> listEventRoutes(EventRoutesListOptions options, Context context)
    {
        return new PagedFlux<>(
            () -> listEventRoutesFirstPage(options, context),
            nextLink -> listEventRoutesNextPage(nextLink, options, context));
    }

    Mono<PagedResponse<EventRoute>> listEventRoutesFirstPage(EventRoutesListOptions options, Context context) {
        return protocolLayer
            .getEventRoutes()
            .listSinglePageAsync(options, context)
            .map(pagedEventRouteMappingFunction);
    }

    Mono<PagedResponse<EventRoute>> listEventRoutesNextPage(String nextLink, EventRoutesListOptions options, Context context) {
        return protocolLayer
            .getEventRoutes()
            .listNextSinglePageAsync(nextLink, options, context)
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
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.publishTelemetry#String-String-Object#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.publishTelemetry#String-String-Object#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> publishTelemetry(String digitalTwinId, String messageId, Object payload) {
        return withContext(context -> publishTelemetryWithResponse(digitalTwinId, messageId, payload, null, context))
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Publishes telemetry from a digital twin
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.publishTelemetryWithResponse#String-String-Object-Options#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.publishTelemetryWithResponse#String-String-Object-Options#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishTelemetryWithResponse(String digitalTwinId, String messageId, Object payload, DigitalTwinsSendTelemetryOptions options) {
        return withContext(context -> publishTelemetryWithResponse(digitalTwinId, messageId, payload, options, context));
    }

    Mono<Response<Void>> publishTelemetryWithResponse(String digitalTwinId, String messageId, Object payload, DigitalTwinsSendTelemetryOptions options, Context context) {
        if (messageId == null || messageId.isEmpty()) {
            messageId = UUID.randomUUID().toString();
        }

        com.azure.digitaltwins.core.implementation.models.DigitalTwinsSendTelemetryOptions protocolLayerOptions;
        if (options == null) {
            options = new DigitalTwinsSendTelemetryOptions();
            protocolLayerOptions = null;
        }
        else {
            protocolLayerOptions = new com.azure.digitaltwins.core.implementation.models.DigitalTwinsSendTelemetryOptions()
                .setTraceparent(options.getTraceparent())
                .setTracestate(options.getTracestate());
        }

        return protocolLayer.getDigitalTwins().sendTelemetryWithResponseAsync(
            digitalTwinId,
            messageId,
            payload,
            options.getTimestamp().toString(),
            protocolLayerOptions,
            context);
    }

    /**
     * Publishes telemetry from a digital twin's component
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.publishComponentTelemetry#String-String-String-Object#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.publishComponentTelemetry#String-String-String-Object#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> publishComponentTelemetry(String digitalTwinId, String componentName, String messageId, Object payload) {
        return withContext(context -> publishComponentTelemetryWithResponse(digitalTwinId, componentName, messageId, payload, null, context))
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Publishes telemetry from a digital twin's component
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.asyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link EventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishComponentTelemetryWithResponse(String digitalTwinId, String componentName, String messageId, Object payload, DigitalTwinsSendComponentTelemetryOptions options) {
        return withContext(context -> publishComponentTelemetryWithResponse(digitalTwinId, componentName, messageId, payload, options, context));
    }

    Mono<Response<Void>> publishComponentTelemetryWithResponse(String digitalTwinId, String componentName, String messageId, Object payload, DigitalTwinsSendComponentTelemetryOptions options, Context context) {
        if (messageId == null || messageId.isEmpty()) {
            messageId = UUID.randomUUID().toString();
        }

        com.azure.digitaltwins.core.implementation.models.DigitalTwinsSendComponentTelemetryOptions protocolLayerOptions;
        if (options == null) {
            options = new DigitalTwinsSendComponentTelemetryOptions();
            protocolLayerOptions = null;
        }
        else {
            protocolLayerOptions = new com.azure.digitaltwins.core.implementation.models.DigitalTwinsSendComponentTelemetryOptions()
                .setTraceparent(options.getTraceparent())
                .setTracestate(options.getTracestate());
        }

        return protocolLayer.getDigitalTwins().sendComponentTelemetryWithResponseAsync(
            digitalTwinId,
            componentName,
            messageId,
            payload,
            options.getTimestamp().toString(),
            protocolLayerOptions,
            context);
    }

    //endregion Telemetry APIs
}
