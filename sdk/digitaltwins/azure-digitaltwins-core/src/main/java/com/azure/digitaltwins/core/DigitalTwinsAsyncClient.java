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
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.JsonPatchDocument;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImpl;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImplBuilder;
import com.azure.digitaltwins.core.implementation.converters.DigitalTwinsModelDataConverter;
import com.azure.digitaltwins.core.implementation.converters.EventRouteConverter;
import com.azure.digitaltwins.core.implementation.converters.IncomingRelationshipConverter;
import com.azure.digitaltwins.core.implementation.converters.OptionsConverter;
import com.azure.digitaltwins.core.implementation.models.QuerySpecification;
import com.azure.digitaltwins.core.implementation.serializer.DeserializationHelpers;
import com.azure.digitaltwins.core.implementation.serializer.DigitalTwinsStringSerializer;
import com.azure.digitaltwins.core.implementation.serializer.SerializationHelpers;
import com.azure.digitaltwins.core.models.CreateOrReplaceDigitalTwinOptions;
import com.azure.digitaltwins.core.models.CreateOrReplaceRelationshipOptions;
import com.azure.digitaltwins.core.models.DeleteDigitalTwinOptions;
import com.azure.digitaltwins.core.models.DeleteRelationshipOptions;
import com.azure.digitaltwins.core.models.DigitalTwinsEventRoute;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.azure.digitaltwins.core.models.DigitalTwinsResponse;
import com.azure.digitaltwins.core.models.DigitalTwinsResponseHeaders;
import com.azure.digitaltwins.core.models.IncomingRelationship;
import com.azure.digitaltwins.core.models.ListDigitalTwinsEventRoutesOptions;
import com.azure.digitaltwins.core.models.ListModelsOptions;
import com.azure.digitaltwins.core.models.PublishComponentTelemetryOptions;
import com.azure.digitaltwins.core.models.PublishTelemetryOptions;
import com.azure.digitaltwins.core.models.QueryOptions;
import com.azure.digitaltwins.core.models.UpdateComponentOptions;
import com.azure.digitaltwins.core.models.UpdateDigitalTwinOptions;
import com.azure.digitaltwins.core.models.UpdateRelationshipOptions;
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
import static com.azure.core.util.tracing.Tracer.AZ_TRACING_NAMESPACE_KEY;

/**
 * This class provides a client for interacting asynchronously with an Azure Digital Twins instance.
 * This client is instantiated through {@link DigitalTwinsClientBuilder}.
 *
 * <p><strong>Code Samples</strong></p>
 *
 * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.instantiation}
 *
 * <p>
 * This client allows for management of digital twins, their components, and their relationships. It also allows for managing
 * the digital twin models and event routes tied to your Azure Digital Twins instance.
 * </p>
 */
@ServiceClient(builder = DigitalTwinsClientBuilder.class, isAsync = true)
public final class DigitalTwinsAsyncClient {
    private static final ClientLogger LOGGER = new ClientLogger(DigitalTwinsAsyncClient.class);
    private static final Boolean INCLUDE_MODEL_DEFINITION_ON_GET = true;
    private static final String DIGITAL_TWINS_TRACING_NAMESPACE_VALUE = "Microsoft.DigitalTwins";

    private static final SerializerAdapter SERIALIZER_ADAPTER;
    private static final ObjectMapper MAPPER;

    private final DigitalTwinsServiceVersion serviceVersion;
    private final AzureDigitalTwinsAPIImpl protocolLayer;
    private final JsonSerializer serializer;

    static {
        final SimpleModule stringModule = new SimpleModule("String Serializer");
        JacksonAdapter jacksonAdapter = new JacksonAdapter();
        MAPPER = jacksonAdapter.serializer(); // Use the same mapper in this layer that the generated layer will use
        stringModule.addSerializer(new DigitalTwinsStringSerializer(String.class, MAPPER));
        jacksonAdapter.serializer().registerModule(stringModule);

        SERIALIZER_ADAPTER = jacksonAdapter;
    }

    DigitalTwinsAsyncClient(String serviceEndpoint, HttpPipeline pipeline, DigitalTwinsServiceVersion serviceVersion, JsonSerializer jsonSerializer) {
        this.serviceVersion = serviceVersion;

        // Is null by default. If not null, then the user provided a custom json serializer for the convenience layer to use.
        // If null, then mapper will be used instead. See DeserializationHelpers for more details
        this.serializer = jsonSerializer;

        this.protocolLayer = new AzureDigitalTwinsAPIImplBuilder()
            .host(serviceEndpoint)
            .pipeline(pipeline)
            .serializerAdapter(SERIALIZER_ADAPTER)
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
     * Creates a digital twin. If the provided digital twin Id is already in use, then this will attempt
     * to replace the existing digital twin with the provided digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p> You can provide a strongly typed digital twin object such as {@link BasicDigitalTwin} as the input parameter:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwins#String-Object-Class#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @return The deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> createOrReplaceDigitalTwin(String digitalTwinId, T digitalTwin, Class<T> clazz) {
        return createOrReplaceDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, null)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Creates a digital twin. If the provided digital twin Id is already in use, then this will attempt
     * to replace the existing digital twin with the provided digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p> You can provide a strongly typed digital twin object such as {@link BasicDigitalTwin} as the input parameter:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> createOrReplaceDigitalTwinWithResponse(String digitalTwinId, T digitalTwin, Class<T> clazz, CreateOrReplaceDigitalTwinOptions options) {
        return withContext(context -> createOrReplaceDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, options, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> createOrReplaceDigitalTwinWithResponse(String digitalTwinId, T digitalTwin, Class<T> clazz, CreateOrReplaceDigitalTwinOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(
                digitalTwinId,
                digitalTwin,
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .flatMap(response -> {
                T genericResponse;
                try {
                    genericResponse = DeserializationHelpers.deserializeObject(MAPPER, response.getValue(), clazz, this.serializer);
                } catch (JsonProcessingException e) {
                    LOGGER.error("JsonProcessingException occurred while deserializing the response: ", e);
                    return Mono.error(e);
                }

                DigitalTwinsResponseHeaders twinHeaders = MAPPER.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);

                return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
            });
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwin#String-Class#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwin#String-Class#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @return The deserialized application/json object representing the digital twin
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getDigitalTwin(String digitalTwinId, Class<T> clazz) {
        return getDigitalTwinWithResponse(digitalTwinId, clazz)
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwinWithResponse#String-Class-Options#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwinWithResponse#String-Class-Options#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz) {
        return withContext(context -> getDigitalTwinWithResponse(digitalTwinId, clazz, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwins()
            .getByIdWithResponseAsync(
                digitalTwinId,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .flatMap(response -> {
                T genericResponse;
                try {
                    genericResponse = DeserializationHelpers.deserializeObject(MAPPER, response.getValue(), clazz, this.serializer);
                } catch (JsonProcessingException e) {
                    LOGGER.error("JsonProcessingException occurred while deserializing the digital twin get response: ", e);
                    return Mono.error(e);
                }
                DigitalTwinsResponseHeaders twinHeaders = MAPPER.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateDigitalTwin#String-JsonPatchDocument}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param jsonPatch The JSON patch to apply to the specified digital twin.
     *                                    This argument can be created using {@link JsonPatchDocument}.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateDigitalTwin(String digitalTwinId, JsonPatchDocument jsonPatch) {
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateDigitalTwinWithResponse#String-JsonPatchDocument-UpdateDigitalTwinOptions}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param jsonPatch The JSON patch to apply to the specified digital twin.
     *                                    This argument can be created using {@link JsonPatchDocument}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse}. This response object includes an HTTP header that gives you the updated
     * ETag for this resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateDigitalTwinWithResponse(String digitalTwinId, JsonPatchDocument jsonPatch, UpdateDigitalTwinOptions options) {
        return withContext(context -> updateDigitalTwinWithResponse(digitalTwinId, jsonPatch, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateDigitalTwinWithResponse(String digitalTwinId, JsonPatchDocument jsonPatch, UpdateDigitalTwinOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwins()
            .updateWithResponseAsync(
                digitalTwinId,
                jsonPatch,
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .map(response -> {
                DigitalTwinsResponseHeaders twinHeaders = MAPPER.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), response.getValue(), twinHeaders);
            });
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteDigitalTwin#String}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDigitalTwin(String digitalTwinId) {
        return deleteDigitalTwinWithResponse(digitalTwinId, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteDigitalTwinWithResponse#String-DeleteDigitalTwinOptions}
     *
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return The Http response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDigitalTwinWithResponse(String digitalTwinId, DeleteDigitalTwinOptions options) {
        return withContext(context -> deleteDigitalTwinWithResponse(digitalTwinId, options, context));
    }

    Mono<Response<Void>> deleteDigitalTwinWithResponse(String digitalTwinId, DeleteDigitalTwinOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwins()
            .deleteWithResponseAsync(
                digitalTwinId,
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE));
    }

    //endregion Digital twin APIs

    //region Relationship APIs

    /**
     * Creates a relationship on a digital twin. If the provided relationship Id is already in use, then this will
     * attempt to replace the existing relationship with the provided relationship.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationship#String-String-Object-Class#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationship#String-String-Object-Class#String}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be created.
     * @param relationship The relationship to be created.
     * @param clazz The model class of the relationship.
     * @param <T> The generic type of the relationship.
     * @return The relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> createOrReplaceRelationship(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz) {
        return createOrReplaceRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz, null)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Creates a relationship on a digital twin. If the provided relationship Id is already in use, then this will
     * attempt to replace the existing relationship with the provided relationship.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options#String}
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
    public <T> Mono<DigitalTwinsResponse<T>> createOrReplaceRelationshipWithResponse(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz, CreateOrReplaceRelationshipOptions options) {
        return withContext(context -> createOrReplaceRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz, options, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> createOrReplaceRelationshipWithResponse(String digitalTwinId, String relationshipId, T relationship, Class<T> clazz, CreateOrReplaceRelationshipOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwins()
            .addRelationshipWithResponseAsync(
                digitalTwinId,
                relationshipId,
                relationship,
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .flatMap(response -> {
                T genericResponse;
                try {
                    genericResponse = DeserializationHelpers.deserializeObject(MAPPER, response.getValue(), clazz, this.serializer);
                } catch (JsonProcessingException e) {
                    LOGGER.error("JsonProcessingException occurred while deserializing the create relationship response: ", e);
                    return Mono.error(e);
                }
                DigitalTwinsResponseHeaders twinHeaders = MAPPER.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationship#String#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationship#String#String}
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
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationshipWithResponse#String-String-Class-Options#BasicRelationship}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationshipWithResponse#String-String-Class-Options#String}
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
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwins()
            .getRelationshipByIdWithResponseAsync(
                digitalTwinId,
                relationshipId,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .flatMap(response -> {
                T genericResponse;
                try {
                    genericResponse = DeserializationHelpers.deserializeObject(MAPPER, response.getValue(), clazz, this.serializer);
                } catch (JsonProcessingException e) {
                    LOGGER.error("JsonProcessingException occurred while deserializing the get relationship response: ", e);
                    return Mono.error(e);
                }
                DigitalTwinsResponseHeaders twinHeaders = MAPPER.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
            });
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateRelationship#String-String-JsonPatchDocument}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                     This argument can be created using {@link JsonPatchDocument}.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateRelationship(String digitalTwinId, String relationshipId, JsonPatchDocument jsonPatch) {
        return updateRelationshipWithResponse(digitalTwinId, relationshipId, jsonPatch, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateRelationshipWithResponse#String-String-JsonPatchDocument-UpdateRelationshipOptions}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to be updated.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                     This argument can be created using {@link JsonPatchDocument}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing no parsed payload object. This response object includes an
     * HTTP header that gives you the updated ETag for this resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateRelationshipWithResponse(String digitalTwinId, String relationshipId, JsonPatchDocument jsonPatch, UpdateRelationshipOptions options) {
        return withContext(context -> updateRelationshipWithResponse(digitalTwinId, relationshipId, jsonPatch, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateRelationshipWithResponse(String digitalTwinId, String relationshipId, JsonPatchDocument jsonPatch, UpdateRelationshipOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwins()
            .updateRelationshipWithResponseAsync(
                digitalTwinId,
                relationshipId,
                jsonPatch,
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .map(response -> {
                DigitalTwinsResponseHeaders twinHeaders = MAPPER.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), response.getValue(), twinHeaders);
            });
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteRelationship#String-String}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteRelationshipWithResponse#String-String-DeleteRelationshipOptions}
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipId The Id of the relationship to delete.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, DeleteRelationshipOptions options) {
        return withContext(context -> deleteRelationshipWithResponse(digitalTwinId, relationshipId, options, context));
    }

    Mono<Response<Void>> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId, DeleteRelationshipOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwins()
            .deleteRelationshipWithResponseAsync(
                digitalTwinId,
                relationshipId,
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE));
    }

    /**
     * Gets all the relationships on a digital twin by iterating through a collection.
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-Class-Options#BasicRelationship#IterateByItem}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-Class-Options#String#IterateByItem}
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
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-String-Class-Options#BasicRelationship#IterateByItem}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-String-Class-Options#String#IterateByItem}
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
            nextLink -> withContext(context -> listRelationshipsNextPage(nextLink, clazz, context)));
    }

    <T> PagedFlux<T> listRelationships(String digitalTwinId, String relationshipName, Class<T> clazz, Context context) {
        return new PagedFlux<>(
            () -> listRelationshipsFirstPage(digitalTwinId, relationshipName, clazz, context != null ? context : Context.NONE),
            nextLink -> listRelationshipsNextPage(nextLink, clazz, context != null ? context : Context.NONE));
    }

    <T> Mono<PagedResponse<T>> listRelationshipsFirstPage(String digitalTwinId, String relationshipName, Class<T> clazz, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwins()
            .listRelationshipsSinglePageAsync(
                digitalTwinId, relationshipName,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .map(
                objectPagedResponse -> {
                    List<T> list = objectPagedResponse.getValue().stream()
                        .map(object -> {
                            try {
                                return DeserializationHelpers.deserializeObject(MAPPER, object, clazz, this.serializer);
                            } catch (JsonProcessingException e) {
                                LOGGER.error("JsonProcessingException occurred while deserializing the list relationship response: ", e);
                                throw LOGGER.logExceptionAsError(new RuntimeException("JsonProcessingException occurred while deserializing the list relationship response", e));
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
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwins()
            .listRelationshipsNextSinglePageAsync(
                nextLink,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .map(objectPagedResponse -> {
                List<T> stringList = objectPagedResponse.getValue().stream()
                    .map(object -> {
                        try {
                            return DeserializationHelpers.deserializeObject(MAPPER, object, clazz, this.serializer);
                        } catch (JsonProcessingException e) {
                            LOGGER.error("JsonProcessingException occurred while deserializing the list relationship response: ", e);
                            throw LOGGER.logExceptionAsError(new RuntimeException("JsonProcessingException occurred while deserializing the list relationship response", e));
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
            });
    }

    /**
     * Gets all the relationships referencing a digital twin as a target by iterating through a collection.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listIncomingRelationships#String}
     *
     * @param digitalTwinId The Id of the target digital twin.
     * @return A {@link PagedFlux} of relationships directed towards the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<IncomingRelationship> listIncomingRelationships(String digitalTwinId) {
        return new PagedFlux<>(
            () -> withContext(context -> listIncomingRelationshipsFirstPageAsync(digitalTwinId, context)),
            nextLink -> withContext(context -> listIncomingRelationshipsNextSinglePageAsync(nextLink, context)));
    }

    PagedFlux<IncomingRelationship> listIncomingRelationships(String digitalTwinId, Context context) {
        return new PagedFlux<>(
            () -> listIncomingRelationshipsFirstPageAsync(digitalTwinId, context != null ? context : Context.NONE),
            nextLink -> listIncomingRelationshipsNextSinglePageAsync(nextLink, context != null ? context : Context.NONE));
    }

    Mono<PagedResponse<IncomingRelationship>> listIncomingRelationshipsFirstPageAsync(String digitalTwinId, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .listIncomingRelationshipsSinglePageAsync(
                digitalTwinId,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .map(pagedIncomingRelationshipMappingFunction);
    }

    Mono<PagedResponse<IncomingRelationship>> listIncomingRelationshipsNextSinglePageAsync(String nextLink, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .listIncomingRelationshipsNextSinglePageAsync(
                nextLink,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .map(pagedIncomingRelationshipMappingFunction);
    }

    private final Function<PagedResponse<com.azure.digitaltwins.core.implementation.models.IncomingRelationship>, PagedResponse<IncomingRelationship>> pagedIncomingRelationshipMappingFunction = (pagedIncomingRelationshipResponse) -> {
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
            ((PagedResponseBase) pagedIncomingRelationshipResponse).getDeserializedHeaders());
    };

    //endregion Relationship APIs

    //region Model APIs

    /**
     * Creates one or many models.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createModels#Iterable}
     *
     * @param dtdlModels The list of models to create. Each string corresponds to exactly one model.
     * @return A List of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Iterable<DigitalTwinsModelData>> createModels(Iterable<String> dtdlModels) {
        return createModelsWithResponse(dtdlModels)
            .map(Response::getValue);
    }

    /**
     * Creates one or many models.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createModelsWithResponse#Iterable-Options}
     *
     * @param dtdlModels The list of models to create. Each string corresponds to exactly one model.
     * @return A {@link Response} containing the list of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Response<Iterable<DigitalTwinsModelData>>> createModelsWithResponse(Iterable<String> dtdlModels) {
        return withContext(context -> createModelsWithResponse(dtdlModels, context));
    }

    Mono<Response<Iterable<DigitalTwinsModelData>>> createModelsWithResponse(Iterable<String> dtdlModels, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        List<Object> modelsPayload = new ArrayList<>();
        for (String model : dtdlModels) {
            try {
                modelsPayload.add(MAPPER.readValue(model, Object.class));
            } catch (JsonProcessingException e) {
                LOGGER.error("Could not parse the model payload [{}]: {}", model, e);
                return Mono.error(e);
            }
        }

        return protocolLayer.getDigitalTwinModels().addWithResponseAsync(
            modelsPayload,
            null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getModel#String}
     *
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
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getModelWithResponse#String-Options}
     *
     * @param modelId The Id of the model.
     * @return A {@link Response} containing a {@link DigitalTwinsModelData} instance that contains the model and its metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DigitalTwinsModelData>> getModelWithResponse(String modelId) {
        return withContext(context -> getModelWithResponse(modelId, context));
    }

    Mono<Response<DigitalTwinsModelData>> getModelWithResponse(String modelId, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getDigitalTwinModels()
            .getByIdWithResponseAsync(
                modelId,
                INCLUDE_MODEL_DEFINITION_ON_GET,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listModels}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listModels#ListModelsOptions}
     *
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link PagedFlux} containing the retrieved {@link DigitalTwinsModelData} instances.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DigitalTwinsModelData> listModels(ListModelsOptions options) {
        return new PagedFlux<>(
            () -> withContext(context -> listModelsSinglePageAsync(options, context)),
            nextLink -> withContext(context -> listModelsNextSinglePageAsync(nextLink, options, context)));
    }

    PagedFlux<DigitalTwinsModelData> listModels(ListModelsOptions options, Context context) {
        return new PagedFlux<>(
            () -> listModelsSinglePageAsync(options, context != null ? context : Context.NONE),
            nextLink -> listModelsNextSinglePageAsync(nextLink, options, context != null ? context : Context.NONE));
    }

    Mono<PagedResponse<DigitalTwinsModelData>> listModelsSinglePageAsync(ListModelsOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        // default values for these options
        List<String> getDependenciesFor = null;
        boolean includeModelDefinition = true; //service default is false, but we expect customers to want the model definitions by default
        com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions protocolLayerOptions = OptionsConverter.toProtocolLayerOptions(options);

        if (options != null) {
            getDependenciesFor = options.getDependenciesFor();
            includeModelDefinition = options.getIncludeModelDefinition();
        }

        return protocolLayer.getDigitalTwinModels().listSinglePageAsync(
            getDependenciesFor,
            includeModelDefinition,
            protocolLayerOptions,
            context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
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

    Mono<PagedResponse<DigitalTwinsModelData>> listModelsNextSinglePageAsync(String nextLink, ListModelsOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions protocolLayerOptions = null;
        if (options != null) {
            protocolLayerOptions = new com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions()
                .setMaxItemsPerPage(options.getMaxItemsPerPage());
        }

        return protocolLayer.getDigitalTwinModels().listNextSinglePageAsync(
            nextLink,
            protocolLayerOptions,
            context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
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
                    ((PagedResponseBase) objectPagedResponse).getDeserializedHeaders());
            });
    }

    /**
     * Deletes a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteModel#String}
     *
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
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteModelWithResponse#String-Options}
     *
     * @param modelId The Id for the model. The Id is globally unique and case sensitive.
     * @return A {@link Response} with no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteModelWithResponse(String modelId) {
        return withContext(context -> deleteModelWithResponse(modelId, context));
    }

    Mono<Response<Void>> deleteModelWithResponse(String modelId, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwinModels()
            .deleteWithResponseAsync(
                modelId,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE));
    }

    /**
     * Decommissions a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.decommissionModel#String}
     *
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
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.decommissionModelWithResponse#String-Options}
     *
     * @param modelId The Id of the model to decommission.
     * @return A {@link Response} with no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> decommissionModelWithResponse(String modelId) {
        return withContext(context -> decommissionModelWithResponse(modelId, context));
    }

    Mono<Response<Void>> decommissionModelWithResponse(String modelId, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        JsonPatchDocument updateOperation = new JsonPatchDocument()
            .appendReplace("/decommissioned", true);

        return protocolLayer.getDigitalTwinModels().updateWithResponseAsync(
            modelId,
            updateOperation,
            null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE));
    }

    //endregion Model APIs

    //region Component APIs

    /**
     * Get a component of a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getComponent#String-String-Class}
     *
     * @param digitalTwinId The Id of the digital twin to get the component from.
     * @param componentName The name of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize application/json the component into.
     * @return The deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getComponent(String digitalTwinId, String componentName, Class<T> clazz) {
        return getComponentWithResponse(digitalTwinId, componentName, clazz)
            .map(DigitalTwinsResponse::getValue);
    }

    /**
     * Get a component of a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getComponentWithResponse#String-String-Class-Options}
     *
     * @param digitalTwinId The Id of the digital twin to get the component from.
     * @param componentName The name of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize the application/json component into.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getComponentWithResponse(String digitalTwinId, String componentName, Class<T> clazz) {
        return withContext(context -> getComponentWithResponse(digitalTwinId, componentName, clazz, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getComponentWithResponse(String digitalTwinId, String componentName, Class<T> clazz, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .getComponentWithResponseAsync(
                digitalTwinId,
                componentName,
                null,
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .flatMap(response -> {
                T genericResponse;
                try {
                    genericResponse = DeserializationHelpers.deserializeObject(MAPPER, response.getValue(), clazz, this.serializer);
                } catch (JsonProcessingException e) {
                    LOGGER.error("JsonProcessingException occurred while deserializing the get component response: ", e);
                    return Mono.error(e);
                }
                DigitalTwinsResponseHeaders twinHeaders = MAPPER.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
                return Mono.just(new DigitalTwinsResponse<T>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, twinHeaders));
            });
    }

    /**
     * Patch a component on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateComponent#String-String-JsonPatchDocument}
     *
     * @param digitalTwinId The Id of the digital twin that has the component to patch.
     * @param componentName The name of the component on the digital twin.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                  This argument can be created using {@link JsonPatchDocument}.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateComponent(String digitalTwinId, String componentName, JsonPatchDocument jsonPatch) {
        return updateComponentWithResponse(digitalTwinId, componentName, jsonPatch, null)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Patch a component on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateComponentWithResponse#String-String-JsonPatchDocument-UpdateComponentOptions}
     *
     * @param digitalTwinId The Id of the digital twin that has the component to patch.
     * @param componentName The name of the component on the digital twin.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     *                                  This argument can be created using {@link JsonPatchDocument}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing an empty Mono. This response object includes an HTTP header
     * that gives you the updated ETag for this resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateComponentWithResponse(String digitalTwinId, String componentName, JsonPatchDocument jsonPatch, UpdateComponentOptions options) {
        return withContext(context -> updateComponentWithResponse(digitalTwinId, componentName, jsonPatch, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateComponentWithResponse(String digitalTwinId, String componentName, JsonPatchDocument jsonPatch, UpdateComponentOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .updateComponentWithResponseAsync(
                digitalTwinId,
                componentName,
                jsonPatch,
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .flatMap(response -> {
                DigitalTwinsResponseHeaders twinHeaders = MAPPER.convertValue(response.getDeserializedHeaders(), DigitalTwinsResponseHeaders.class);
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String#String}
     *
     * Note that there may be a delay between before changes in your instance are reflected in queries.
     * For more details on query limitations, see
     * <a href="https://docs.microsoft.com/azure/digital-twins/how-to-query-graph#query-limitations">Query limitations</a>
     *
     * @param query The query string, in SQL-like syntax.
     * @param clazz The model class to deserialize each queried digital twin into. Since the queried twins may not all
     *              have the same model class, it is recommended to use a common denominator class such as {@link BasicDigitalTwin}.
     * @param <T> The generic type to deserialize each queried digital twin into.
     * @return A {@link PagedFlux} of deserialized digital twins.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> query(String query, Class<T> clazz) {
        return query(query, clazz, null);
    }

    /**
     * Query digital twins.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicDigitalTwin} can be provided as the input parameter to deserialize the response into.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String-Options#BasicDigitalTwin}
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String-Options#String}
     *
     * Note that there may be a delay between before changes in your instance are reflected in queries.
     * For more details on query limitations, see
     * <a href="https://docs.microsoft.com/azure/digital-twins/how-to-query-graph#query-limitations">Query limitations</a>
     *
     * @param query The query string, in SQL-like syntax.
     * @param clazz The model class to deserialize each queried digital twin into. Since the queried twins may not all
     *              have the same model class, it is recommended to use a common denominator class such as {@link BasicDigitalTwin}.
     * @param <T> The generic type to deserialize each queried digital twin into.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link PagedFlux} of deserialized digital twins.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> query(String query, Class<T> clazz, QueryOptions options) {
        return new PagedFlux<T>(
            () -> withContext(context -> queryFirstPage(query, clazz, options, context)),
            nextLink -> withContext(context -> queryNextPage(nextLink, clazz, options, context)));
    }

    <T> PagedFlux<T> query(String query, Class<T> clazz, QueryOptions options, Context context) {
        return new PagedFlux<T>(
            () -> queryFirstPage(query, clazz, options, context != null ? context : Context.NONE),
            nextLink -> queryNextPage(nextLink, clazz, options, context != null ? context : Context.NONE));
    }

    <T> Mono<PagedResponse<T>> queryFirstPage(String query, Class<T> clazz, QueryOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        QuerySpecification querySpecification = new QuerySpecification().setQuery(query);

        return protocolLayer
            .getQueries()
            .queryTwinsWithResponseAsync(
                querySpecification,
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .map(objectPagedResponse -> new PagedResponseBase<>(
                objectPagedResponse.getRequest(),
                objectPagedResponse.getStatusCode(),
                objectPagedResponse.getHeaders(),
                objectPagedResponse.getValue().getValue().stream()
                    .map(object -> {
                        try {
                            return DeserializationHelpers.deserializeObject(MAPPER, object, clazz, this.serializer);
                        } catch (JsonProcessingException e) {
                            LOGGER.error("JsonProcessingException occurred while deserializing the query response: ", e);
                            throw LOGGER.logExceptionAsError(new RuntimeException("JsonProcessingException occurred while deserializing the query response: ", e));
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()),
                SerializationHelpers.serializeContinuationToken(objectPagedResponse.getValue().getContinuationToken()),
                objectPagedResponse.getDeserializedHeaders()));
    }

    <T> Mono<PagedResponse<T>> queryNextPage(String nextLink, Class<T> clazz, QueryOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        QuerySpecification querySpecification = new QuerySpecification().setContinuationToken(nextLink);

        return protocolLayer
            .getQueries()
            .queryTwinsWithResponseAsync(
                querySpecification,
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .map(objectPagedResponse -> new PagedResponseBase<>(
                objectPagedResponse.getRequest(),
                objectPagedResponse.getStatusCode(),
                objectPagedResponse.getHeaders(),
                objectPagedResponse.getValue().getValue().stream()
                    .map(object -> {
                        try {
                            return DeserializationHelpers.deserializeObject(MAPPER, object, clazz, this.serializer);
                        } catch (JsonProcessingException e) {
                            LOGGER.error("JsonProcessingException occurred while deserializing the query response: ", e);
                            throw LOGGER.logExceptionAsError(new RuntimeException("JsonProcessingException occurred while deserializing the query response: ", e));
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
     * Create an event route. If the provided eventRouteId is already in use, then this will attempt to replace the
     * existing event route with the provided event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceEventRoute#String-DigitalTwinsEventRoute}
     *
     * @param eventRouteId The Id of the event route to create.
     * @param eventRoute The event route to create.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createOrReplaceEventRoute(String eventRouteId, DigitalTwinsEventRoute eventRoute) {
        return createOrReplaceEventRouteWithResponse(eventRouteId, eventRoute)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Create an event route. If the provided eventRouteId is already in use, then this will attempt to replace the
     * existing event route with the provided event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceEventRouteWithResponse#String-DigitalTwinsEventRoute}
     *
     * @param eventRouteId The Id of the event route to create.
     * @param eventRoute The event route to create.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createOrReplaceEventRouteWithResponse(String eventRouteId, DigitalTwinsEventRoute eventRoute) {
        return withContext(context -> createOrReplaceEventRouteWithResponse(eventRouteId, eventRoute, context));
    }

    Mono<Response<Void>> createOrReplaceEventRouteWithResponse(String eventRouteId, DigitalTwinsEventRoute eventRoute, Context context) {
        return this.protocolLayer
            .getEventRoutes()
            .addWithResponseAsync(
                eventRouteId,
                EventRouteConverter.map(eventRoute),
                null,
                context != null ? context : Context.NONE);
    }

    /**
     * Get an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getEventRoute#String}
     *
     * @param eventRouteId The Id of the event route to get.
     * @return The retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsEventRoute> getEventRoute(String eventRouteId) {
        return getEventRouteWithResponse(eventRouteId)
            .map(Response::getValue);
    }

    /**
     * Get an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getEventRouteWithResponse#String-Options}
     *
     * @param eventRouteId The Id of the event route to get.
     * @return A {@link Response} containing the retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DigitalTwinsEventRoute>> getEventRouteWithResponse(String eventRouteId) {
        return withContext(context -> getEventRouteWithResponse(eventRouteId, context));
    }

    Mono<Response<DigitalTwinsEventRoute>> getEventRouteWithResponse(String eventRouteId, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return this.protocolLayer.getEventRoutes().getByIdWithResponseAsync(
            eventRouteId,
            null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteEventRoute#String}
     *
     * @param eventRouteId The Id of the event route to delete.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEventRoute(String eventRouteId) {
        return deleteEventRouteWithResponse(eventRouteId)
            .flatMap(voidResponse -> Mono.empty());
    }

    /**
     * Delete an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteEventRouteWithResponse#String-Options}
     *
     * @param eventRouteId The Id of the event route to delete.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEventRouteWithResponse(String eventRouteId) {
        return withContext(context -> deleteEventRouteWithResponse(eventRouteId, context));
    }

    Mono<Response<Void>> deleteEventRouteWithResponse(String eventRouteId, Context context) {
        return this.protocolLayer.getEventRoutes().deleteWithResponseAsync(
            eventRouteId,
            null,
            context != null ? context : Context.NONE);
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listEventRoutes}
     *
     * @return A {@link PagedFlux} that contains all the event routes that exist in your digital twins instance.
     * This PagedFlux may take multiple service requests to iterate over all event routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DigitalTwinsEventRoute> listEventRoutes() {
        return listEventRoutes(null);
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listEventRoutes#ListDigitalTwinsEventRoutesOptions}
     *
     * @param options The optional parameters to use when listing event routes. See {@link ListDigitalTwinsEventRoutesOptions} for more details
     * on what optional parameters can be set.
     * @return A {@link PagedFlux} that contains all the event routes that exist in your digital twins instance.
     * This PagedFlux may take multiple service requests to iterate over all event routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DigitalTwinsEventRoute> listEventRoutes(ListDigitalTwinsEventRoutesOptions options) {
        return new PagedFlux<>(
            () -> withContext(context -> listEventRoutesFirstPage(options, context)),
            nextLink -> withContext(context -> listEventRoutesNextPage(nextLink, options, context)));
    }

    PagedFlux<DigitalTwinsEventRoute> listEventRoutes(ListDigitalTwinsEventRoutesOptions options, Context context) {
        return new PagedFlux<>(
            () -> listEventRoutesFirstPage(options, context != null ? context : Context.NONE),
            nextLink -> listEventRoutesNextPage(nextLink, options, context != null ? context : Context.NONE));
    }

    Mono<PagedResponse<DigitalTwinsEventRoute>> listEventRoutesFirstPage(ListDigitalTwinsEventRoutesOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getEventRoutes()
            .listSinglePageAsync(
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .map(pagedEventRouteMappingFunction);
    }

    Mono<PagedResponse<DigitalTwinsEventRoute>> listEventRoutesNextPage(String nextLink, ListDigitalTwinsEventRoutesOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer
            .getEventRoutes()
            .listNextSinglePageAsync(
                nextLink,
                OptionsConverter.toProtocolLayerOptions(options),
                context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE))
            .map(pagedEventRouteMappingFunction);
    }

    private final Function<PagedResponse<com.azure.digitaltwins.core.implementation.models.EventRoute>, PagedResponse<DigitalTwinsEventRoute>> pagedEventRouteMappingFunction = (pagedEventRouteResponse) -> {
        List<DigitalTwinsEventRoute> convertedList = pagedEventRouteResponse.getValue().stream()
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetry#String-String-Object#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetry#String-String-Object#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link DigitalTwinsEventRoute}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetryWithResponse#String-String-Object-Options#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetryWithResponse#String-String-Object-Options#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link DigitalTwinsEventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishTelemetryWithResponse(String digitalTwinId, String messageId, Object payload, PublishTelemetryOptions options) {
        return withContext(context -> publishTelemetryWithResponse(digitalTwinId, messageId, payload, options, context));
    }

    Mono<Response<Void>> publishTelemetryWithResponse(String digitalTwinId, String messageId, Object payload, PublishTelemetryOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        if (messageId == null || messageId.isEmpty()) {
            messageId = UUID.randomUUID().toString();
        }

        if (options == null) {
            options = new PublishTelemetryOptions();
        }

        return protocolLayer.getDigitalTwins().sendTelemetryWithResponseAsync(
            digitalTwinId,
            messageId,
            payload,
            options.getTimestamp().toString(),
            null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE));
    }

    /**
     * Publishes telemetry from a digital twin's component
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the telemetry payload.</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetry#String-String-String-Object#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetry#String-String-String-Object#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link DigitalTwinsEventRoute}
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
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#Object}
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * {@codesnippet com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#String}
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under {@link DigitalTwinsEventRoute}
     * These event routes need to be set before publishing a telemetry message, in order for the telemetry message to be consumed.
     * @param digitalTwinId The Id of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly typed object like a Dictionary.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishComponentTelemetryWithResponse(String digitalTwinId, String componentName, String messageId, Object payload, PublishComponentTelemetryOptions options) {
        return withContext(context -> publishComponentTelemetryWithResponse(digitalTwinId, componentName, messageId, payload, options, context));
    }

    Mono<Response<Void>> publishComponentTelemetryWithResponse(String digitalTwinId, String componentName, String messageId, Object payload, PublishComponentTelemetryOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        if (messageId == null || messageId.isEmpty()) {
            messageId = UUID.randomUUID().toString();
        }

        if (options == null) {
            options = new PublishComponentTelemetryOptions();
        }

        return protocolLayer.getDigitalTwins().sendComponentTelemetryWithResponseAsync(
            digitalTwinId,
            componentName,
            messageId,
            payload,
            options.getTimestamp().toString(),
            null,
            context.addData(AZ_TRACING_NAMESPACE_KEY, DIGITAL_TWINS_TRACING_NAMESPACE_VALUE));
    }

    //endregion Telemetry APIs
}
