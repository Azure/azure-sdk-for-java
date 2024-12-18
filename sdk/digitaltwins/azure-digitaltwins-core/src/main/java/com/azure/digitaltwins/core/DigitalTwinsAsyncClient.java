// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.JsonPatchDocument;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImpl;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImplBuilder;
import com.azure.digitaltwins.core.implementation.converters.DigitalTwinsModelDataConverter;
import com.azure.digitaltwins.core.implementation.converters.EventRouteConverter;
import com.azure.digitaltwins.core.implementation.converters.IncomingRelationshipConverter;
import com.azure.digitaltwins.core.implementation.converters.OptionsConverter;
import com.azure.digitaltwins.core.implementation.models.QuerySpecification;
import com.azure.digitaltwins.core.implementation.serializer.DeserializationHelpers;
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
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * This class provides a client for interacting asynchronously with an Azure Digital Twins instance.
 * This client is instantiated through {@link DigitalTwinsClientBuilder}.
 *
 * <p><strong>Code Samples</strong></p>
 *
 * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.instantiation -->
 * <pre>
 * DigitalTwinsAsyncClient digitalTwinsAsyncClient = new DigitalTwinsClientBuilder&#40;&#41;.credential&#40;
 *     new ClientSecretCredentialBuilder&#40;&#41;.tenantId&#40;tenantId&#41;
 *         .clientId&#40;clientId&#41;
 *         .clientSecret&#40;clientSecret&#41;
 *         .build&#40;&#41;&#41;.endpoint&#40;digitalTwinsEndpointUrl&#41;.buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.instantiation -->
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

    private final DigitalTwinsServiceVersion serviceVersion;
    private final AzureDigitalTwinsAPIImpl protocolLayer;
    private final JsonSerializer serializer;

    DigitalTwinsAsyncClient(String serviceEndpoint, HttpPipeline pipeline, DigitalTwinsServiceVersion serviceVersion,
        JsonSerializer jsonSerializer) {
        this.serviceVersion = serviceVersion;

        // Is null by default. If not null, then the user provided a custom json serializer for the convenience layer to use.
        // If null, then mapper will be used instead. See DeserializationHelpers for more details
        this.serializer = jsonSerializer;

        this.protocolLayer = new AzureDigitalTwinsAPIImplBuilder().host(serviceEndpoint)
            .pipeline(pipeline)
            .buildClient();
    }

    /**
     * Gets the Azure Digital Twins service API version that this client is configured to use for all service requests.
     * Unless configured while building this client through
     * {@link DigitalTwinsClientBuilder#serviceVersion(DigitalTwinsServiceVersion)}, this value will be equal to the
     * latest service API version supported by this client.
     *
     * @return The Azure Digital Twins service API version.
     */
    public DigitalTwinsServiceVersion getServiceVersion() {
        return this.serviceVersion;
    }

    //region Digital twin APIs

    /**
     * Creates a digital twin. If the provided digital twin ID is already in use, then this will attempt to replace the
     * existing digital twin with the provided digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>You can provide a strongly typed digital twin object such as {@link BasicDigitalTwin} as the input
     * parameter:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin -->
     * <pre>
     * String modelId = &quot;dtmi:com:samples:Building;1&quot;;
     *
     * BasicDigitalTwin basicTwin = new BasicDigitalTwin&#40;&quot;myDigitalTwinId&quot;&#41;.setMetadata&#40;
     *     new BasicDigitalTwinMetadata&#40;&#41;.setModelId&#40;modelId&#41;&#41;;
     *
     * digitalTwinsAsyncClient.createOrReplaceDigitalTwin&#40;basicTwin.getId&#40;&#41;, basicTwin, BasicDigitalTwin.class&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;&quot;Created digital twin Id: &quot; + response.getId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwins#String-Object-Class#BasicDigitalTwin -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwins#String-Object-Class#String -->
     * <pre>
     * digitalTwinsAsyncClient.createOrReplaceDigitalTwin&#40;&quot;myDigitalTwinId&quot;, digitalTwinStringPayload, String.class&#41;
     *     .subscribe&#40;stringResponse -&gt; System.out.println&#40;&quot;Created digital twin: &quot; + stringResponse&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwins#String-Object-Class#String -->
     *
     * @param digitalTwinId The ID of the digital twin. The ID is unique within the service and case-sensitive.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @return The deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> createOrReplaceDigitalTwin(String digitalTwinId, T digitalTwin, Class<T> clazz) {
        return createOrReplaceDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, null).flatMap(
            FluxUtil::toMono);
    }

    /**
     * Creates a digital twin. If the provided digital twin ID is already in use, then this will attempt to replace the
     * existing digital twin with the provided digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>You can provide a strongly typed digital twin object such as {@link BasicDigitalTwin} as the input
     * parameter:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#BasicDigitalTwin -->
     * <pre>
     * String modelId = &quot;dtmi:com:samples:Building;1&quot;;
     *
     * BasicDigitalTwin basicDigitalTwin = new BasicDigitalTwin&#40;&quot;myDigitalTwinId&quot;&#41;.setMetadata&#40;
     *     new BasicDigitalTwinMetadata&#40;&#41;.setModelId&#40;modelId&#41;&#41;;
     *
     * digitalTwinsAsyncClient.createOrReplaceDigitalTwinWithResponse&#40;basicDigitalTwin.getId&#40;&#41;, basicDigitalTwin,
     *         BasicDigitalTwin.class, new CreateOrReplaceDigitalTwinOptions&#40;&#41;&#41;
     *     .subscribe&#40;resultWithResponse -&gt; System.out.println&#40;
     *         &quot;Response http status: &quot; + resultWithResponse.getStatusCode&#40;&#41; + &quot; created digital twin Id: &quot;
     *             + resultWithResponse.getValue&#40;&#41;.getId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#BasicDigitalTwin -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#String -->
     * <pre>
     * digitalTwinsAsyncClient.createOrReplaceDigitalTwinWithResponse&#40;basicDigitalTwin.getId&#40;&#41;, stringPayload,
     *         String.class, new CreateOrReplaceDigitalTwinOptions&#40;&#41;&#41;
     *     .subscribe&#40;stringWithResponse -&gt; System.out.println&#40;
     *         &quot;Response http status: &quot; + stringWithResponse.getStatusCode&#40;&#41; + &quot; created digital twin: &quot;
     *             + stringWithResponse.getValue&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createDigitalTwinsWithResponse#String-Object-Class-Options#String -->
     *
     * @param digitalTwinId The ID of the digital twin. The ID is unique within the service and case-sensitive.
     * @param digitalTwin The application/json object representing the digital twin to create.
     * @param clazz The model class to serialize the request with and deserialize the response with.
     * @param <T> The generic type to serialize the request with and deserialize the response with.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the digital twin created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> createOrReplaceDigitalTwinWithResponse(String digitalTwinId, T digitalTwin,
        Class<T> clazz, CreateOrReplaceDigitalTwinOptions options) {
        return withContext(
            context -> createOrReplaceDigitalTwinWithResponse(digitalTwinId, digitalTwin, clazz, options, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> createOrReplaceDigitalTwinWithResponse(String digitalTwinId, T digitalTwin,
        Class<T> clazz, CreateOrReplaceDigitalTwinOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .addNoCustomHeadersWithResponseAsync(digitalTwinId, digitalTwin,
                OptionsConverter.toProtocolLayerOptions(options), context)
            .flatMap(response -> deserializeHelper(response, clazz));
    }

    /**
     * Gets a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>
     * A Strongly typed object type such as {@link BasicDigitalTwin} can be provided as an input parameter for
     * {@code clazz} to indicate what type is used to deserialize the response.
     * </p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwin#String-Class#BasicDigitalTwin -->
     * <pre>
     * digitalTwinsAsyncClient.getDigitalTwin&#40;&quot;myDigitalTwinId&quot;, BasicDigitalTwin.class&#41;
     *     .subscribe&#40;
     *         basicDigitalTwin -&gt; System.out.println&#40;&quot;Retrieved digital twin with Id: &quot; + basicDigitalTwin.getId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwin#String-Class#BasicDigitalTwin -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwin#String-Class#String -->
     * <pre>
     * digitalTwinsAsyncClient.getDigitalTwin&#40;&quot;myDigitalTwinId&quot;, String.class&#41;
     *     .subscribe&#40;stringResult -&gt; System.out.println&#40;&quot;Retrieved digital twin: &quot; + stringResult&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwin#String-Class#String -->
     *
     * @param digitalTwinId The ID of the digital twin. The ID is unique within the service and case-sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @return The deserialized application/json object representing the digital twin
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getDigitalTwin(String digitalTwinId, Class<T> clazz) {
        return getDigitalTwinWithResponse(digitalTwinId, clazz).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>
     * A Strongly typed object type such as {@link BasicDigitalTwin} can be provided as an input parameter for
     * {@code clazz} to indicate what type is used to deserialize the response.
     * </p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwinWithResponse#String-Class-Options#BasicDigitalTwin -->
     * <pre>
     * digitalTwinsAsyncClient.getDigitalTwinWithResponse&#40;&quot;myDigitalTwinId&quot;, BasicDigitalTwin.class&#41;
     *     .subscribe&#40;basicDigitalTwinWithResponse -&gt; System.out.println&#40;
     *         &quot;Retrieved digital twin with Id: &quot; + basicDigitalTwinWithResponse.getValue&#40;&#41;.getId&#40;&#41;
     *             + &quot; Http Status Code: &quot; + basicDigitalTwinWithResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwinWithResponse#String-Class-Options#BasicDigitalTwin -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwinWithResponse#String-Class-Options#String -->
     * <pre>
     * digitalTwinsAsyncClient.getDigitalTwinWithResponse&#40;&quot;myDigitalTwinId&quot;, String.class&#41;
     *     .subscribe&#40;basicDigitalTwinWithResponse -&gt; System.out.println&#40;
     *         &quot;Retrieved digital twin: &quot; + basicDigitalTwinWithResponse.getValue&#40;&#41; + &quot; Http Status Code: &quot;
     *             + basicDigitalTwinWithResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getDigitalTwinWithResponse#String-Class-Options#String -->
     *
     * @param digitalTwinId The ID of the digital twin. The ID is unique within the service and case-sensitive.
     * @param clazz The model class to deserialize the response with.
     * @param <T> The generic type to deserialize the digital twin with.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the
     * digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz) {
        return withContext(context -> getDigitalTwinWithResponse(digitalTwinId, clazz, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getDigitalTwinWithResponse(String digitalTwinId, Class<T> clazz,
        Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .getByIdNoCustomHeadersWithResponseAsync(digitalTwinId, null, context)
            .flatMap(response -> deserializeHelper(response, clazz));
    }

    /**
     * Updates a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update digital twin by providing list of intended patch operations.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateDigitalTwin#String-JsonPatchDocument -->
     * <pre>
     * JsonPatchDocument jsonPatchDocument = new JsonPatchDocument&#40;&#41;;
     * jsonPatchDocument.appendReplace&#40;&quot;Prop1&quot;, &quot;newValue&quot;&#41;;
     *
     * digitalTwinsAsyncClient.updateDigitalTwin&#40;&quot;myDigitalTwinId&quot;, jsonPatchDocument&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateDigitalTwin#String-JsonPatchDocument -->
     *
     * @param digitalTwinId The ID of the digital twin. The ID is unique within the service and case-sensitive.
     * @param jsonPatch The JSON patch to apply to the specified digital twin.
     * This argument can be created using {@link JsonPatchDocument}.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateDigitalTwin(String digitalTwinId, JsonPatchDocument jsonPatch) {
        return updateDigitalTwinWithResponse(digitalTwinId, jsonPatch, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Updates a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>Update digital twin by providing list of intended patch operations.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateDigitalTwinWithResponse#String-JsonPatchDocument-UpdateDigitalTwinOptions -->
     * <pre>
     * JsonPatchDocument jsonPatchDocument = new JsonPatchDocument&#40;&#41;;
     * jsonPatchDocument.appendReplace&#40;&quot;Prop1&quot;, &quot;newValue&quot;&#41;;
     *
     * digitalTwinsAsyncClient.updateDigitalTwinWithResponse&#40;&quot;myDigitalTwinId&quot;, jsonPatchDocument,
     *         new UpdateDigitalTwinOptions&#40;&#41;&#41;
     *     .subscribe&#40;updateResponse -&gt; System.out.println&#40;
     *         &quot;Update completed with HTTP status code: &quot; + updateResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateDigitalTwinWithResponse#String-JsonPatchDocument-UpdateDigitalTwinOptions -->
     *
     * @param digitalTwinId The ID of the digital twin. The ID is unique within the service and case-sensitive.
     * @param jsonPatch The JSON patch to apply to the specified digital twin.
     * This argument can be created using {@link JsonPatchDocument}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse}. This response object includes an HTTP header that gives you the updated
     * ETag for this resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateDigitalTwinWithResponse(String digitalTwinId,
        JsonPatchDocument jsonPatch, UpdateDigitalTwinOptions options) {
        return withContext(context -> updateDigitalTwinWithResponse(digitalTwinId, jsonPatch, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateDigitalTwinWithResponse(String digitalTwinId, JsonPatchDocument jsonPatch,
        UpdateDigitalTwinOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .updateNoCustomHeadersWithResponseAsync(digitalTwinId, jsonPatch,
                OptionsConverter.toProtocolLayerOptions(options), context)
            .map(response -> new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), response.getValue(), createDTResponseHeadersFromResponse(response)));
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteDigitalTwin#String -->
     * <pre>
     * digitalTwinsAsyncClient.deleteDigitalTwin&#40;&quot;myDigitalTwinId&quot;&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteDigitalTwin#String -->
     *
     * @param digitalTwinId The ID of the digital twin. The ID is unique within the service and case-sensitive.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteDigitalTwin(String digitalTwinId) {
        return deleteDigitalTwinWithResponse(digitalTwinId, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a digital twin. All relationships referencing the digital twin must already be deleted.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteDigitalTwinWithResponse#String-DeleteDigitalTwinOptions -->
     * <pre>
     * digitalTwinsAsyncClient.deleteDigitalTwinWithResponse&#40;&quot;myDigitalTwinId&quot;, new DeleteDigitalTwinOptions&#40;&#41;&#41;
     *     .subscribe&#40;deleteResponse -&gt; System.out.println&#40;
     *         &quot;Deleted digital twin. HTTP response status code: &quot; + deleteResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteDigitalTwinWithResponse#String-DeleteDigitalTwinOptions -->
     *
     * @param digitalTwinId The ID of the digital twin. The ID is unique within the service and case-sensitive.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return The Http response
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteDigitalTwinWithResponse(String digitalTwinId, DeleteDigitalTwinOptions options) {
        return withContext(context -> deleteDigitalTwinWithResponse(digitalTwinId, options, context));
    }

    Mono<Response<Void>> deleteDigitalTwinWithResponse(String digitalTwinId, DeleteDigitalTwinOptions options,
        Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .deleteWithResponseAsync(digitalTwinId, OptionsConverter.toProtocolLayerOptions(options), context);
    }

    //endregion Digital twin APIs

    //region Relationship APIs

    /**
     * Creates a relationship on a digital twin. If the provided relationship ID is already in use, then this will
     * attempt to replace the existing relationship with the provided relationship.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter
     * to deserialize the response into.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationship#String-String-Object-Class#BasicRelationship -->
     * <pre>
     * BasicRelationship buildingToFloorBasicRelationship = new BasicRelationship&#40;&quot;myRelationshipId&quot;,
     *     &quot;mySourceDigitalTwinId&quot;, &quot;myTargetDigitalTwinId&quot;, &quot;contains&quot;&#41;.addProperty&#40;&quot;Prop1&quot;, &quot;Prop1 value&quot;&#41;
     *     .addProperty&#40;&quot;Prop2&quot;, 6&#41;;
     *
     * digitalTwinsAsyncClient.createOrReplaceRelationship&#40;&quot;mySourceDigitalTwinId&quot;, &quot;myRelationshipId&quot;,
     *         buildingToFloorBasicRelationship, BasicRelationship.class&#41;
     *     .subscribe&#40;createdRelationship -&gt; System.out.println&#40;
     *         &quot;Created relationship with Id: &quot; + createdRelationship.getId&#40;&#41; + &quot; from: &quot;
     *             + createdRelationship.getSourceId&#40;&#41; + &quot; to: &quot; + createdRelationship.getTargetId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationship#String-String-Object-Class#BasicRelationship -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationship#String-String-Object-Class#String -->
     * <pre>
     * String relationshipPayload = getRelationshipPayload&#40;&#41;;
     *
     * digitalTwinsAsyncClient.createOrReplaceRelationship&#40;&quot;mySourceDigitalTwinId&quot;, &quot;myRelationshipId&quot;,
     *         relationshipPayload, String.class&#41;
     *     .subscribe&#40;
     *         createRelationshipString -&gt; System.out.println&#40;&quot;Created relationship: &quot; + createRelationshipString&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationship#String-String-Object-Class#String -->
     *
     * @param digitalTwinId The ID of the source digital twin.
     * @param relationshipId The ID of the relationship to be created.
     * @param relationship The relationship to be created.
     * @param clazz The model class of the relationship.
     * @param <T> The generic type of the relationship.
     * @return The relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> createOrReplaceRelationship(String digitalTwinId, String relationshipId, T relationship,
        Class<T> clazz) {
        return createOrReplaceRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz,
            null).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates a relationship on a digital twin. If the provided relationship ID is already in use, then this will
     * attempt to replace the existing relationship with the provided relationship.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter
     * to deserialize the response into.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options#BasicRelationship -->
     * <pre>
     * BasicRelationship buildingToFloorBasicRelationship = new BasicRelationship&#40;&quot;myRelationshipId&quot;,
     *     &quot;mySourceDigitalTwinId&quot;, &quot;myTargetDigitalTwinId&quot;, &quot;contains&quot;&#41;.addProperty&#40;&quot;Prop1&quot;, &quot;Prop1 value&quot;&#41;
     *     .addProperty&#40;&quot;Prop2&quot;, 6&#41;;
     *
     * digitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse&#40;&quot;mySourceDigitalTwinId&quot;, &quot;myRelationshipId&quot;,
     *         buildingToFloorBasicRelationship, BasicRelationship.class, new CreateOrReplaceRelationshipOptions&#40;&#41;&#41;
     *     .subscribe&#40;createdRelationshipWithResponse -&gt; System.out.println&#40;
     *         &quot;Created relationship with Id: &quot; + createdRelationshipWithResponse.getValue&#40;&#41;.getId&#40;&#41; + &quot; from: &quot;
     *             + createdRelationshipWithResponse.getValue&#40;&#41;.getSourceId&#40;&#41; + &quot; to: &quot;
     *             + createdRelationshipWithResponse.getValue&#40;&#41;.getTargetId&#40;&#41; + &quot; Http status code: &quot;
     *             + createdRelationshipWithResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options#BasicRelationship -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options#String -->
     * <pre>
     * String relationshipPayload = getRelationshipPayload&#40;&#41;;
     *
     * digitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse&#40;&quot;mySourceDigitalTwinId&quot;, &quot;myRelationshipId&quot;,
     *         relationshipPayload, String.class, new CreateOrReplaceRelationshipOptions&#40;&#41;&#41;
     *     .subscribe&#40;createdRelationshipStringWithResponse -&gt; System.out.println&#40;
     *         &quot;Created relationship: &quot; + createdRelationshipStringWithResponse + &quot; With HTTP status code: &quot;
     *             + createdRelationshipStringWithResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceRelationshipWithResponse#String-String-Object-Class-Options#String -->
     *
     * @param digitalTwinId The ID of the source digital twin.
     * @param relationshipId The ID of the relationship to be created.
     * @param relationship The relationship to be created.
     * @param clazz The model class of the relationship.
     * @param <T> The generic type of the relationship.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing the relationship created.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> createOrReplaceRelationshipWithResponse(String digitalTwinId,
        String relationshipId, T relationship, Class<T> clazz, CreateOrReplaceRelationshipOptions options) {
        return withContext(
            context -> createOrReplaceRelationshipWithResponse(digitalTwinId, relationshipId, relationship, clazz,
                options, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> createOrReplaceRelationshipWithResponse(String digitalTwinId,
        String relationshipId, T relationship, Class<T> clazz, CreateOrReplaceRelationshipOptions options,
        Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .addRelationshipNoCustomHeadersWithResponseAsync(digitalTwinId, relationshipId, relationship,
                OptionsConverter.toProtocolLayerOptions(options), context)
            .flatMap(response -> deserializeHelper(response, clazz));
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter
     * to deserialize the response into.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationship#String#BasicRelationship -->
     * <pre>
     * digitalTwinsAsyncClient.getRelationship&#40;&quot;myDigitalTwinId&quot;, &quot;myRelationshipName&quot;, BasicRelationship.class&#41;
     *     .subscribe&#40;retrievedRelationship -&gt; System.out.println&#40;
     *         &quot;Retrieved relationship with Id: &quot; + retrievedRelationship.getId&#40;&#41; + &quot; from: &quot;
     *             + retrievedRelationship.getSourceId&#40;&#41; + &quot; to: &quot; + retrievedRelationship.getTargetId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationship#String#BasicRelationship -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationship#String#String -->
     * <pre>
     * digitalTwinsAsyncClient.getRelationship&#40;&quot;myDigitalTwinId&quot;, &quot;myRelationshipName&quot;, String.class&#41;
     *     .subscribe&#40;retrievedRelationshipString -&gt; System.out.println&#40;
     *         &quot;Retrieved relationship: &quot; + retrievedRelationshipString&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationship#String#String -->
     *
     * @param digitalTwinId The ID of the source digital twin.
     * @param relationshipId The ID of the relationship to retrieve.
     * @param clazz The model class to deserialize the relationship into.
     * @param <T> The generic type to deserialize the relationship into.
     * @return The deserialized relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getRelationship(String digitalTwinId, String relationshipId, Class<T> clazz) {
        return getRelationshipWithResponse(digitalTwinId, relationshipId, clazz).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter
     * to deserialize the response into.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationshipWithResponse#String-String-Class-Options#BasicRelationship -->
     * <pre>
     * digitalTwinsAsyncClient.getRelationshipWithResponse&#40;&quot;myDigitalTwinId&quot;, &quot;myRelationshipName&quot;,
     *         BasicRelationship.class&#41;
     *     .subscribe&#40;retrievedRelationshipWithResponse -&gt; System.out.println&#40;
     *         &quot;Retrieved relationship with Id: &quot; + retrievedRelationshipWithResponse.getValue&#40;&#41;.getId&#40;&#41; + &quot; from: &quot;
     *             + retrievedRelationshipWithResponse.getValue&#40;&#41;.getSourceId&#40;&#41; + &quot; to: &quot;
     *             + retrievedRelationshipWithResponse.getValue&#40;&#41;.getTargetId&#40;&#41; + &quot;HTTP status code: &quot;
     *             + retrievedRelationshipWithResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationshipWithResponse#String-String-Class-Options#BasicRelationship -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationshipWithResponse#String-String-Class-Options#String -->
     * <pre>
     * digitalTwinsAsyncClient.getRelationshipWithResponse&#40;&quot;myDigitalTwinId&quot;, &quot;myRelationshipName&quot;, String.class&#41;
     *     .subscribe&#40;retrievedRelationshipStringWithResponse -&gt; System.out.println&#40;
     *         &quot;Retrieved relationship: &quot; + retrievedRelationshipStringWithResponse + &quot; HTTP status code: &quot;
     *             + retrievedRelationshipStringWithResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getRelationshipWithResponse#String-String-Class-Options#String -->
     *
     * @param digitalTwinId The ID of the source digital twin.
     * @param relationshipId The ID of the relationship to retrieve.
     * @param clazz The model class to deserialize the relationship into.
     * @param <T> The generic type to deserialize the relationship into.
     * @return A {@link DigitalTwinsResponse} containing the deserialized relationship.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getRelationshipWithResponse(String digitalTwinId, String relationshipId,
        Class<T> clazz) {
        return withContext(context -> getRelationshipWithResponse(digitalTwinId, relationshipId, clazz, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getRelationshipWithResponse(String digitalTwinId, String relationshipId,
        Class<T> clazz, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .getRelationshipByIdNoCustomHeadersWithResponseAsync(digitalTwinId, relationshipId, null, context)
            .flatMap(response -> deserializeHelper(response, clazz));
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateRelationship#String-String-JsonPatchDocument -->
     * <pre>
     * JsonPatchDocument jsonPatchDocument = new JsonPatchDocument&#40;&#41;;
     * jsonPatchDocument.appendReplace&#40;&quot;&#47;relationshipProperty1&quot;, &quot;new property value&quot;&#41;;
     *
     * digitalTwinsAsyncClient.updateRelationship&#40;&quot;myDigitalTwinId&quot;, &quot;myRelationshipId&quot;, jsonPatchDocument&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateRelationship#String-String-JsonPatchDocument -->
     *
     * @param digitalTwinId The ID of the source digital twin.
     * @param relationshipId The ID of the relationship to be updated.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     * This argument can be created using {@link JsonPatchDocument}.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateRelationship(String digitalTwinId, String relationshipId, JsonPatchDocument jsonPatch) {
        return updateRelationshipWithResponse(digitalTwinId, relationshipId, jsonPatch, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Updates the properties of a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateRelationshipWithResponse#String-String-JsonPatchDocument-UpdateRelationshipOptions -->
     * <pre>
     * JsonPatchDocument jsonPatchDocument = new JsonPatchDocument&#40;&#41;;
     * jsonPatchDocument.appendReplace&#40;&quot;&#47;relationshipProperty1&quot;, &quot;new property value&quot;&#41;;
     *
     * digitalTwinsAsyncClient.updateRelationshipWithResponse&#40;&quot;myDigitalTwinId&quot;, &quot;myRelationshipId&quot;, jsonPatchDocument,
     *         new UpdateRelationshipOptions&#40;&#41;&#41;
     *     .subscribe&#40;updateResponse -&gt; System.out.println&#40;
     *         &quot;Relationship updated with status code: &quot; + updateResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateRelationshipWithResponse#String-String-JsonPatchDocument-UpdateRelationshipOptions -->
     *
     * @param digitalTwinId The ID of the source digital twin.
     * @param relationshipId The ID of the relationship to be updated.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     * This argument can be created using {@link JsonPatchDocument}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing no parsed payload object. This response object includes an
     * HTTP header that gives you the updated ETag for this resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateRelationshipWithResponse(String digitalTwinId, String relationshipId,
        JsonPatchDocument jsonPatch, UpdateRelationshipOptions options) {
        return withContext(
            context -> updateRelationshipWithResponse(digitalTwinId, relationshipId, jsonPatch, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateRelationshipWithResponse(String digitalTwinId, String relationshipId,
        JsonPatchDocument jsonPatch, UpdateRelationshipOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .updateRelationshipNoCustomHeadersWithResponseAsync(digitalTwinId, relationshipId, jsonPatch,
                OptionsConverter.toProtocolLayerOptions(options), context)
            .map(response -> new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), response.getValue(), createDTResponseHeadersFromResponse(response)));
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteRelationship#String-String -->
     * <pre>
     * digitalTwinsAsyncClient.deleteRelationship&#40;&quot;myDigitalTwinId&quot;, &quot;myRelationshipId&quot;&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteRelationship#String-String -->
     *
     * @param digitalTwinId The ID of the source digital twin.
     * @param relationshipId The ID of the relationship to delete.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRelationship(String digitalTwinId, String relationshipId) {
        return deleteRelationshipWithResponse(digitalTwinId, relationshipId, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a relationship on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteRelationshipWithResponse#String-String-DeleteRelationshipOptions -->
     * <pre>
     * digitalTwinsAsyncClient.deleteRelationshipWithResponse&#40;&quot;myDigitalTwinId&quot;, &quot;myRelationshipId&quot;,
     *         new DeleteRelationshipOptions&#40;&#41;&#41;
     *     .subscribe&#40;deleteResponse -&gt; System.out.println&#40;
     *         &quot;Deleted relationship with HTTP status code: &quot; + deleteResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteRelationshipWithResponse#String-String-DeleteRelationshipOptions -->
     *
     * @param digitalTwinId The ID of the source digital twin.
     * @param relationshipId The ID of the relationship to delete.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing no parsed payload object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId,
        DeleteRelationshipOptions options) {
        return withContext(context -> deleteRelationshipWithResponse(digitalTwinId, relationshipId, options, context));
    }

    Mono<Response<Void>> deleteRelationshipWithResponse(String digitalTwinId, String relationshipId,
        DeleteRelationshipOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .deleteRelationshipWithResponseAsync(digitalTwinId, relationshipId,
                OptionsConverter.toProtocolLayerOptions(options), context);
    }

    /**
     * Gets all the relationships on a digital twin by iterating through a collection.
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter
     * to deserialize the response into.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-Class-Options#BasicRelationship#IterateByItem -->
     * <pre>
     * digitalTwinsAsyncClient.listRelationships&#40;&quot;myDigitalTwinId&quot;, BasicRelationship.class&#41;
     *     .doOnNext&#40;basicRel -&gt; System.out.println&#40;&quot;Retrieved relationship with Id: &quot; + basicRel.getId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-Class-Options#BasicRelationship#IterateByItem -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-Class-Options#String#IterateByItem -->
     * <pre>
     * digitalTwinsAsyncClient.listRelationships&#40;&quot;myDigitalTwinId&quot;, String.class&#41;
     *     .doOnNext&#40;rel -&gt; System.out.println&#40;&quot;Retrieved relationship: &quot; + rel&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-Class-Options#String#IterateByItem -->
     *
     * @param digitalTwinId The ID of the source digital twin.
     * @param clazz The model class to convert the relationship to. Since a digital twin might have relationships
     * conforming to different models, it is advisable to convert them to a generic model like
     * {@link BasicRelationship}.
     * @param <T> The generic type to convert the relationship to.
     * @return A {@link PagedFlux} of relationships belonging to the specified digital twin and the http response.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> listRelationships(String digitalTwinId, Class<T> clazz) {
        return listRelationships(digitalTwinId, null, clazz);
    }

    /**
     * Gets all the relationships on a digital twin filtered by the relationship name, by iterating through a
     * collection.
     *
     * <p>A strongly typed digital twin object such as {@link BasicRelationship} can be provided as the input parameter
     * to deserialize the response into.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-String-Class-Options#BasicRelationship#IterateByItem -->
     * <pre>
     * digitalTwinsAsyncClient.listRelationships&#40;&quot;myDigitalTwinId&quot;, &quot;myRelationshipName&quot;, BasicRelationship.class&#41;
     *     .doOnNext&#40;rel -&gt; System.out.println&#40;&quot;Retrieved relationship with Id: &quot; + rel.getId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-String-Class-Options#BasicRelationship#IterateByItem -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-String-Class-Options#String#IterateByItem -->
     * <pre>
     * digitalTwinsAsyncClient.listRelationships&#40;&quot;myDigitalTwinId&quot;, &quot;myRelationshipId&quot;, String.class&#41;
     *     .doOnNext&#40;rel -&gt; System.out.println&#40;&quot;Retrieved relationship: &quot; + rel&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listRelationships#String-String-Class-Options#String#IterateByItem -->
     *
     * @param digitalTwinId The ID of the source digital twin.
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
        return new PagedFlux<>(() -> listRelationshipsFirstPage(digitalTwinId, relationshipName, clazz,
            context != null ? context : Context.NONE),
            nextLink -> listRelationshipsNextPage(nextLink, clazz, context != null ? context : Context.NONE));
    }

    <T> Mono<PagedResponse<T>> listRelationshipsFirstPage(String digitalTwinId, String relationshipName, Class<T> clazz,
        Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .listRelationshipsSinglePageAsync(digitalTwinId, relationshipName, null, context)
            .map(objectPagedResponse -> {
                List<T> list = objectPagedResponse.getValue().stream().map(object -> mapObject(object, clazz))
                    .filter(Objects::nonNull).collect(Collectors.toList());
                return new PagedResponseBase<>(objectPagedResponse.getRequest(), objectPagedResponse.getStatusCode(),
                    objectPagedResponse.getHeaders(), list, objectPagedResponse.getContinuationToken(),
                    ((PagedResponseBase<?, ?>) objectPagedResponse).getDeserializedHeaders());
            });
    }

    <T> Mono<PagedResponse<T>> listRelationshipsNextPage(String nextLink, Class<T> clazz, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .listRelationshipsNextSinglePageAsync(nextLink, null, context)
            .map(objectPagedResponse -> {
                List<T> stringList = objectPagedResponse.getValue().stream().map(object -> mapObject(object, clazz))
                    .filter(Objects::nonNull).collect(Collectors.toList());
                return new PagedResponseBase<>(objectPagedResponse.getRequest(), objectPagedResponse.getStatusCode(),
                    objectPagedResponse.getHeaders(), stringList, objectPagedResponse.getContinuationToken(),
                    ((PagedResponseBase<?, ?>) objectPagedResponse).getDeserializedHeaders());
            });
    }

    /**
     * Gets all the relationships referencing a digital twin as a target by iterating through a collection.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listIncomingRelationships#String -->
     * <pre>
     * digitalTwinsAsyncClient.listIncomingRelationships&#40;&quot;myDigitalTwinId&quot;&#41;
     *     .doOnNext&#40;incomingRel -&gt; System.out.println&#40;
     *         &quot;Retrieved relationship with Id: &quot; + incomingRel.getRelationshipId&#40;&#41; + &quot; from: &quot;
     *             + incomingRel.getSourceId&#40;&#41; + &quot; to: myDigitalTwinId&quot;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listIncomingRelationships#String -->
     *
     * @param digitalTwinId The ID of the target digital twin.
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
            nextLink -> listIncomingRelationshipsNextSinglePageAsync(nextLink,
                context != null ? context : Context.NONE));
    }

    Mono<PagedResponse<IncomingRelationship>> listIncomingRelationshipsFirstPageAsync(String digitalTwinId,
        Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .listIncomingRelationshipsSinglePageAsync(digitalTwinId, null, context)
            .map(DigitalTwinsAsyncClient::mapIncomingRelationship);
    }

    Mono<PagedResponse<IncomingRelationship>> listIncomingRelationshipsNextSinglePageAsync(String nextLink,
        Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .listIncomingRelationshipsNextSinglePageAsync(nextLink, null, context)
            .map(DigitalTwinsAsyncClient::mapIncomingRelationship);
    }

    private static PagedResponse<IncomingRelationship> mapIncomingRelationship(
        PagedResponse<com.azure.digitaltwins.core.implementation.models.IncomingRelationship> incomingRelationship) {
        List<IncomingRelationship> convertedList = incomingRelationship.getValue()
            .stream()
            .map(IncomingRelationshipConverter::map)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return new PagedResponseBase<>(incomingRelationship.getRequest(), incomingRelationship.getStatusCode(),
            incomingRelationship.getHeaders(), convertedList, incomingRelationship.getContinuationToken(), null);
    }

    //endregion Relationship APIs

    //region Model APIs

    /**
     * Creates one or many models.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createModels#Iterable -->
     * <pre>
     * digitalTwinsAsyncClient.createModels&#40;Arrays.asList&#40;model1, model2, model3&#41;&#41;
     *     .subscribe&#40;createdModels -&gt; createdModels.forEach&#40;
     *         model -&gt; System.out.println&#40;&quot;Retrieved model with Id: &quot; + model.getModelId&#40;&#41;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createModels#Iterable -->
     *
     * @param dtdlModels The list of models to create. Each string corresponds to exactly one model.
     * @return A List of created models. Each {@link DigitalTwinsModelData} instance in this list
     * will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Iterable<DigitalTwinsModelData>> createModels(Iterable<String> dtdlModels) {
        return createModelsWithResponse(dtdlModels).flatMap(FluxUtil::toMono);
    }

    /**
     * Creates one or many models.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createModelsWithResponse#Iterable-Options -->
     * <pre>
     * digitalTwinsAsyncClient.createModelsWithResponse&#40;Arrays.asList&#40;model1, model2, model3&#41;&#41;
     *     .subscribe&#40;createdModels -&gt; &#123;
     *         System.out.println&#40;&quot;Received a response with HTTP status code: &quot; + createdModels.getStatusCode&#40;&#41;&#41;;
     *         createdModels.getValue&#40;&#41;
     *             .forEach&#40;model -&gt; System.out.println&#40;&quot;Retrieved model with Id: &quot; + model.getModelId&#40;&#41;&#41;&#41;;
     *     &#125;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createModelsWithResponse#Iterable-Options -->
     *
     * @param dtdlModels The list of models to create. Each string corresponds to exactly one model.
     * @return A {@link Response} containing the list of created models. Each {@link DigitalTwinsModelData} instance in
     * this list will contain metadata about the created model, but will not contain the model itself.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Response<Iterable<DigitalTwinsModelData>>> createModelsWithResponse(Iterable<String> dtdlModels) {
        return withContext(context -> createModelsWithResponse(dtdlModels, context));
    }

    Mono<Response<Iterable<DigitalTwinsModelData>>> createModelsWithResponse(Iterable<String> dtdlModels,
        Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        List<Object> modelsPayload = new ArrayList<>();
        for (String model : dtdlModels) {
            try {
                modelsPayload.add(protocolLayer.getSerializerAdapter().deserialize(model, Object.class,
                    SerializerEncoding.JSON));
            } catch (IOException e) {
                LOGGER.atError()
                    .addKeyValue("model", model)
                    .log(() -> "Could not parse the model payload", e);
                return Mono.error(e);
            }
        }

        return protocolLayer.getDigitalTwinModels()
            .addWithResponseAsync(modelsPayload, null, context)
            .map(listResponse -> {
                Iterable<DigitalTwinsModelData> convertedList = listResponse.getValue()
                    .stream()
                    .map(DigitalTwinsModelDataConverter::map)
                    .collect(Collectors.toList());

                return new SimpleResponse<>(listResponse.getRequest(), listResponse.getStatusCode(),
                    listResponse.getHeaders(), convertedList);
            });
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getModel#String -->
     * <pre>
     * digitalTwinsAsyncClient.getModel&#40;&quot;dtmi:com:samples:Building;1&quot;&#41;
     *     .subscribe&#40;model -&gt; System.out.println&#40;&quot;Retrieved model with Id: &quot; + model.getModelId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getModel#String -->
     *
     * @param modelId The ID of the model.
     * @return A {@link DigitalTwinsModelData} instance that contains the model and its metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsModelData> getModel(String modelId) {
        return getModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Gets a model, including the model metadata and the model definition.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getModelWithResponse#String-Options -->
     * <pre>
     * digitalTwinsAsyncClient.getModelWithResponse&#40;&quot;dtmi:com:samples:Building;1&quot;&#41;.subscribe&#40;modelWithResponse -&gt; &#123;
     *     System.out.println&#40;&quot;Received HTTP response with status code: &quot; + modelWithResponse.getStatusCode&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Retrieved model with Id: &quot; + modelWithResponse.getValue&#40;&#41;.getModelId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getModelWithResponse#String-Options -->
     *
     * @param modelId The ID of the model.
     * @return A {@link Response} containing a {@link DigitalTwinsModelData} instance that contains the model and its
     * metadata.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DigitalTwinsModelData>> getModelWithResponse(String modelId) {
        return withContext(context -> getModelWithResponse(modelId, context));
    }

    Mono<Response<DigitalTwinsModelData>> getModelWithResponse(String modelId, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwinModels()
            .getByIdWithResponseAsync(modelId, INCLUDE_MODEL_DEFINITION_ON_GET, null, context)
            .map(response -> new SimpleResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), DigitalTwinsModelDataConverter.map(response.getValue())));
    }

    /**
     * List all the models in this digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listModels -->
     * <pre>
     * digitalTwinsAsyncClient.listModels&#40;&#41;
     *     .doOnNext&#40;model -&gt; System.out.println&#40;&quot;Retrieved model with Id: &quot; + model.getModelId&#40;&#41;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listModels -->
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
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listModels#ListModelsOptions -->
     * <pre>
     * digitalTwinsAsyncClient.listModels&#40;
     *         new ListModelsOptions&#40;&#41;.setMaxItemsPerPage&#40;5&#41;.setIncludeModelDefinition&#40;true&#41;&#41;
     *     .doOnNext&#40;model -&gt; System.out.println&#40;&quot;Retrieved model with Id: &quot; + model.getModelId&#40;&#41;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listModels#ListModelsOptions -->
     *
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link PagedFlux} containing the retrieved {@link DigitalTwinsModelData} instances.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DigitalTwinsModelData> listModels(ListModelsOptions options) {
        return new PagedFlux<>(() -> withContext(context -> listModelsSinglePageAsync(options, context)),
            nextLink -> withContext(context -> listModelsNextSinglePageAsync(nextLink, options, context)));
    }

    PagedFlux<DigitalTwinsModelData> listModels(ListModelsOptions options, Context context) {
        return new PagedFlux<>(() -> listModelsSinglePageAsync(options, context != null ? context : Context.NONE),
            nextLink -> listModelsNextSinglePageAsync(nextLink, options, context != null ? context : Context.NONE));
    }

    Mono<PagedResponse<DigitalTwinsModelData>> listModelsSinglePageAsync(ListModelsOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        // default values for these options
        List<String> getDependenciesFor = null;
        boolean includeModelDefinition
            = true; //service default is false, but we expect customers to want the model definitions by default
        com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions protocolLayerOptions
            = OptionsConverter.toProtocolLayerOptions(options);

        if (options != null) {
            getDependenciesFor = options.getDependenciesFor();
            includeModelDefinition = options.getIncludeModelDefinition();
        }

        return protocolLayer.getDigitalTwinModels()
            .listSinglePageAsync(getDependenciesFor, includeModelDefinition, protocolLayerOptions, context)
            .map(objectPagedResponse -> {
                List<DigitalTwinsModelData> convertedList = objectPagedResponse.getValue()
                    .stream()
                    .map(DigitalTwinsModelDataConverter::map)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                return new PagedResponseBase<>(objectPagedResponse.getRequest(), objectPagedResponse.getStatusCode(),
                    objectPagedResponse.getHeaders(), convertedList, objectPagedResponse.getContinuationToken(),
                    ((PagedResponseBase<?, ?>) objectPagedResponse).getDeserializedHeaders());
            });
    }

    Mono<PagedResponse<DigitalTwinsModelData>> listModelsNextSinglePageAsync(String nextLink, ListModelsOptions options,
        Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions protocolLayerOptions = null;
        if (options != null) {
            protocolLayerOptions
                = new com.azure.digitaltwins.core.implementation.models.DigitalTwinModelsListOptions()
                .setMaxItemsPerPage(options.getMaxItemsPerPage());
        }

        return protocolLayer.getDigitalTwinModels()
            .listNextSinglePageAsync(nextLink, protocolLayerOptions, context)
            .map(objectPagedResponse -> {
                List<DigitalTwinsModelData> convertedList = objectPagedResponse.getValue()
                    .stream()
                    .map(DigitalTwinsModelDataConverter::map)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
                return new PagedResponseBase<>(objectPagedResponse.getRequest(), objectPagedResponse.getStatusCode(),
                    objectPagedResponse.getHeaders(), convertedList, objectPagedResponse.getContinuationToken(),
                    ((PagedResponseBase<?, ?>) objectPagedResponse).getDeserializedHeaders());
            });
    }

    /**
     * Deletes a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteModel#String -->
     * <pre>
     * digitalTwinsAsyncClient.deleteModel&#40;&quot;dtmi:com:samples:Building;1&quot;&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteModel#String -->
     *
     * @param modelId The ID for the model. The ID is globally unique and case-sensitive.
     * @return An empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteModel(String modelId) {
        return deleteModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Deletes a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteModelWithResponse#String-Options -->
     * <pre>
     * digitalTwinsAsyncClient.deleteModelWithResponse&#40;&quot;dtmi:com:samples:Building;1&quot;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;
     *         &quot;Received delete model operation response with HTTP status code:&quot; + response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteModelWithResponse#String-Options -->
     *
     * @param modelId The ID for the model. The ID is globally unique and case-sensitive.
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

        return protocolLayer.getDigitalTwinModels().deleteWithResponseAsync(modelId, null, context);
    }

    /**
     * Decommissions a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.decommissionModel#String -->
     * <pre>
     * digitalTwinsAsyncClient.decommissionModel&#40;&quot;dtmi:com:samples:Building;1&quot;&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.decommissionModel#String -->
     *
     * @param modelId The ID of the model to decommission.
     * @return an empty Mono
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> decommissionModel(String modelId) {
        return decommissionModelWithResponse(modelId).flatMap(FluxUtil::toMono);
    }

    /**
     * Decommissions a model.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.decommissionModelWithResponse#String-Options -->
     * <pre>
     * digitalTwinsAsyncClient.decommissionModelWithResponse&#40;&quot;dtmi:com:samples:Building;1&quot;&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;
     *         &quot;Received decommission model HTTP response with status:&quot; + response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.decommissionModelWithResponse#String-Options -->
     *
     * @param modelId The ID of the model to decommission.
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

        JsonPatchDocument updateOperation = new JsonPatchDocument().appendReplace("/decommissioned", true);

        return protocolLayer.getDigitalTwinModels().updateWithResponseAsync(modelId, updateOperation, null, context);
    }

    //endregion Model APIs

    //region Component APIs

    /**
     * Get a component of a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getComponent#String-String-Class -->
     * <pre>
     * digitalTwinsAsyncClient.getComponent&#40;&quot;myDigitalTwinId&quot;, &quot;myComponentName&quot;, String.class&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getComponent#String-String-Class -->
     *
     * @param digitalTwinId The ID of the digital twin to get the component from.
     * @param componentName The name of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize application/json the component into.
     * @return The deserialized application/json object representing the component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> getComponent(String digitalTwinId, String componentName, Class<T> clazz) {
        return getComponentWithResponse(digitalTwinId, componentName, clazz).flatMap(FluxUtil::toMono);
    }

    /**
     * Get a component of a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getComponentWithResponse#String-String-Class-Options -->
     * <pre>
     * digitalTwinsAsyncClient.getComponentWithResponse&#40;&quot;myDigitalTwinId&quot;, &quot;myComponentName&quot;, String.class&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;
     *         &quot;Received component get operation response with HTTP status code: &quot; + response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getComponentWithResponse#String-String-Class-Options -->
     *
     * @param digitalTwinId The ID of the digital twin to get the component from.
     * @param componentName The name of the component on the digital twin to retrieve.
     * @param clazz The class to deserialize the application/json component into.
     * @param <T> The generic type to deserialize the application/json component into.
     * @return A {@link DigitalTwinsResponse} containing the deserialized application/json object representing the
     * component of the digital twin.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<DigitalTwinsResponse<T>> getComponentWithResponse(String digitalTwinId, String componentName,
        Class<T> clazz) {
        return withContext(context -> getComponentWithResponse(digitalTwinId, componentName, clazz, context));
    }

    <T> Mono<DigitalTwinsResponse<T>> getComponentWithResponse(String digitalTwinId, String componentName,
        Class<T> clazz, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .getComponentNoCustomHeadersWithResponseAsync(digitalTwinId, componentName, null, context)
            .flatMap(response -> deserializeHelper(response, clazz));
    }

    /**
     * Patch a component on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateComponent#String-String-JsonPatchDocument -->
     * <pre>
     * JsonPatchDocument jsonPatchDocument = new JsonPatchDocument&#40;&#41;;
     * jsonPatchDocument.appendReplace&#40;&quot;&#47;ComponentProp1&quot;, &quot;Some new value&quot;&#41;;
     *
     * digitalTwinsAsyncClient.updateComponent&#40;&quot;myDigitalTwinId&quot;, &quot;myComponentName&quot;, jsonPatchDocument&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateComponent#String-String-JsonPatchDocument -->
     *
     * @param digitalTwinId The ID of the digital twin that has the component to patch.
     * @param componentName The name of the component on the digital twin.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     * This argument can be created using {@link JsonPatchDocument}.
     * @return An empty Mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateComponent(String digitalTwinId, String componentName, JsonPatchDocument jsonPatch) {
        return updateComponentWithResponse(digitalTwinId, componentName, jsonPatch, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Patch a component on a digital twin.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateComponentWithResponse#String-String-JsonPatchDocument-UpdateComponentOptions -->
     * <pre>
     * JsonPatchDocument jsonPatchDocument = new JsonPatchDocument&#40;&#41;;
     * jsonPatchDocument.appendReplace&#40;&quot;&#47;ComponentProp1&quot;, &quot;Some new value&quot;&#41;;
     *
     * digitalTwinsAsyncClient.updateComponentWithResponse&#40;&quot;myDigitalTwinId&quot;, &quot;myComponentName&quot;, jsonPatchDocument,
     *         new UpdateComponentOptions&#40;&#41;.setIfMatch&#40;&quot;*&quot;&#41;&#41;
     *     .subscribe&#40;updateResponse -&gt; System.out.println&#40;
     *         &quot;Received update operation response with HTTP status code: &quot; + updateResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.updateComponentWithResponse#String-String-JsonPatchDocument-UpdateComponentOptions -->
     *
     * @param digitalTwinId The ID of the digital twin that has the component to patch.
     * @param componentName The name of the component on the digital twin.
     * @param jsonPatch The JSON patch to apply to the specified digital twin's relationship.
     * This argument can be created using {@link JsonPatchDocument}.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link DigitalTwinsResponse} containing an empty Mono. This response object includes an HTTP header
     * that gives you the updated ETag for this resource.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsResponse<Void>> updateComponentWithResponse(String digitalTwinId, String componentName,
        JsonPatchDocument jsonPatch, UpdateComponentOptions options) {
        return withContext(
            context -> updateComponentWithResponse(digitalTwinId, componentName, jsonPatch, options, context));
    }

    Mono<DigitalTwinsResponse<Void>> updateComponentWithResponse(String digitalTwinId, String componentName,
        JsonPatchDocument jsonPatch, UpdateComponentOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getDigitalTwins()
            .updateComponentNoCustomHeadersWithResponseAsync(digitalTwinId, componentName, jsonPatch,
                OptionsConverter.toProtocolLayerOptions(options), context)
            .flatMap(response -> Mono.just(
                new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
                    null, createDTResponseHeadersFromResponse(response))));
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
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String#BasicDigitalTwin -->
     * <pre>
     * digitalTwinsAsyncClient.query&#40;&quot;SELECT * FROM digitaltwins&quot;, BasicDigitalTwin.class&#41;
     *     .doOnNext&#40;
     *         basicTwin -&gt; System.out.println&#40;&quot;Retrieved digitalTwin query result with Id: &quot; + basicTwin.getId&#40;&#41;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String#BasicDigitalTwin -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String#String -->
     * <pre>
     * digitalTwinsAsyncClient.query&#40;&quot;SELECT * FROM digitaltwins&quot;, String.class&#41;
     *     .doOnNext&#40;twinString -&gt; System.out.println&#40;&quot;Retrieved digitalTwin query result with Id: &quot; + twinString&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String#String -->
     *
     * Note that there may be a delay between before changes in your instance are reflected in queries.
     * For more details on query limitations, see
     * <a href="https://docs.microsoft.com/azure/digital-twins/how-to-query-graph#query-limitations">Query limitations</a>
     *
     * @param query The query string, in SQL-like syntax.
     * @param clazz The model class to deserialize each queried digital twin into. Since the queried twins may not all
     * have the same model class, it is recommended to use a common denominator class such as {@link BasicDigitalTwin}.
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
     * <p>A strongly typed digital twin object such as {@link BasicDigitalTwin} can be provided as the input parameter
     * to deserialize the response into.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String-Options#BasicDigitalTwin -->
     * <pre>
     * digitalTwinsAsyncClient.query&#40;&quot;SELECT * FROM digitaltwins&quot;, BasicDigitalTwin.class,
     *         new QueryOptions&#40;&#41;.setMaxItemsPerPage&#40;5&#41;&#41;
     *     .doOnNext&#40;
     *         basicTwin -&gt; System.out.println&#40;&quot;Retrieved digitalTwin query result with Id: &quot; + basicTwin.getId&#40;&#41;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String-Options#BasicDigitalTwin -->
     *
     * <p>Or alternatively String can be used as input and output deserialization type:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String-Options#String -->
     * <pre>
     * digitalTwinsAsyncClient.query&#40;&quot;SELECT * FROM digitaltwins&quot;, String.class,
     *         new QueryOptions&#40;&#41;.setMaxItemsPerPage&#40;5&#41;&#41;
     *     .doOnNext&#40;twinString -&gt; System.out.println&#40;&quot;Retrieved digitalTwin query result with Id: &quot; + twinString&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.query#String-Options#String -->
     *
     * Note that there may be a delay between before changes in your instance are reflected in queries.
     * For more details on query limitations, see
     * <a href="https://docs.microsoft.com/azure/digital-twins/how-to-query-graph#query-limitations">Query limitations</a>
     *
     * @param query The query string, in SQL-like syntax.
     * @param clazz The model class to deserialize each queried digital twin into. Since the queried twins may not all
     * have the same model class, it is recommended to use a common denominator class such as {@link BasicDigitalTwin}.
     * @param <T> The generic type to deserialize each queried digital twin into.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link PagedFlux} of deserialized digital twins.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public <T> PagedFlux<T> query(String query, Class<T> clazz, QueryOptions options) {
        return new PagedFlux<T>(() -> withContext(context -> queryFirstPage(query, clazz, options, context)),
            nextLink -> withContext(context -> queryNextPage(nextLink, clazz, options, context)));
    }

    <T> PagedFlux<T> query(String query, Class<T> clazz, QueryOptions options, Context context) {
        return new PagedFlux<T>(() -> queryFirstPage(query, clazz, options, context != null ? context : Context.NONE),
            nextLink -> queryNextPage(nextLink, clazz, options, context != null ? context : Context.NONE));
    }

    <T> Mono<PagedResponse<T>> queryFirstPage(String query, Class<T> clazz, QueryOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        QuerySpecification querySpecification = new QuerySpecification().setQuery(query);

        return protocolLayer.getQueries()
            .queryTwinsWithResponseAsync(querySpecification, OptionsConverter.toProtocolLayerOptions(options), context)
            .map(objectPagedResponse -> new PagedResponseBase<>(objectPagedResponse.getRequest(),
                objectPagedResponse.getStatusCode(), objectPagedResponse.getHeaders(),
                objectPagedResponse.getValue().getValue().stream().map(object -> mapObject(object, clazz))
                    .filter(Objects::nonNull).collect(Collectors.toList()),
                SerializationHelpers.serializeContinuationToken(objectPagedResponse.getValue().getContinuationToken()),
                objectPagedResponse.getDeserializedHeaders()));
    }

    <T> Mono<PagedResponse<T>> queryNextPage(String nextLink, Class<T> clazz, QueryOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        QuerySpecification querySpecification = new QuerySpecification().setContinuationToken(nextLink);

        return protocolLayer.getQueries()
            .queryTwinsWithResponseAsync(querySpecification, OptionsConverter.toProtocolLayerOptions(options), context)
            .map(objectPagedResponse -> new PagedResponseBase<>(objectPagedResponse.getRequest(),
                objectPagedResponse.getStatusCode(), objectPagedResponse.getHeaders(),
                objectPagedResponse.getValue().getValue().stream().map(object -> mapObject(object, clazz))
                    .filter(Objects::nonNull).collect(Collectors.toList()),
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
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceEventRoute#String-DigitalTwinsEventRoute -->
     * <pre>
     * String filter
     *     = &quot;$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'&quot;;
     *
     * DigitalTwinsEventRoute eventRoute = new DigitalTwinsEventRoute&#40;&quot;myEndpointName&quot;&#41;.setFilter&#40;filter&#41;;
     * digitalTwinsAsyncClient.createOrReplaceEventRoute&#40;&quot;myEventRouteId&quot;, eventRoute&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceEventRoute#String-DigitalTwinsEventRoute -->
     *
     * @param eventRouteId The ID of the event route to create.
     * @param eventRoute The event route to create.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> createOrReplaceEventRoute(String eventRouteId, DigitalTwinsEventRoute eventRoute) {
        return createOrReplaceEventRouteWithResponse(eventRouteId, eventRoute).flatMap(FluxUtil::toMono);
    }

    /**
     * Create an event route. If the provided eventRouteId is already in use, then this will attempt to replace the
     * existing event route with the provided event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceEventRouteWithResponse#String-DigitalTwinsEventRoute -->
     * <pre>
     * String filter
     *     = &quot;$eventType = 'DigitalTwinTelemetryMessages' or $eventType = 'DigitalTwinLifecycleNotification'&quot;;
     *
     * DigitalTwinsEventRoute eventRoute = new DigitalTwinsEventRoute&#40;&quot;myEndpointName&quot;&#41;.setFilter&#40;filter&#41;;
     * digitalTwinsAsyncClient.createOrReplaceEventRouteWithResponse&#40;&quot;myEventRouteId&quot;, eventRoute&#41;
     *     .subscribe&#40;response -&gt; System.out.println&#40;
     *         &quot;Created an event rout with HTTP status code: &quot; + response.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.createOrReplaceEventRouteWithResponse#String-DigitalTwinsEventRoute -->
     *
     * @param eventRouteId The ID of the event route to create.
     * @param eventRoute The event route to create.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> createOrReplaceEventRouteWithResponse(String eventRouteId,
        DigitalTwinsEventRoute eventRoute) {
        return withContext(context -> createOrReplaceEventRouteWithResponse(eventRouteId, eventRoute, context));
    }

    Mono<Response<Void>> createOrReplaceEventRouteWithResponse(String eventRouteId, DigitalTwinsEventRoute eventRoute,
        Context context) {
        return this.protocolLayer.getEventRoutes()
            .addWithResponseAsync(eventRouteId, EventRouteConverter.map(eventRoute), null,
                context != null ? context : Context.NONE);
    }

    /**
     * Get an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getEventRoute#String -->
     * <pre>
     * digitalTwinsAsyncClient.getEventRoute&#40;&quot;myEventRouteId&quot;&#41;
     *     .subscribe&#40;
     *         eventRoute -&gt; System.out.println&#40;&quot;Retrieved event route with Id: &quot; + eventRoute.getEventRouteId&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getEventRoute#String -->
     *
     * @param eventRouteId The ID of the event route to get.
     * @return The retrieved event route.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsEventRoute> getEventRoute(String eventRouteId) {
        return getEventRouteWithResponse(eventRouteId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getEventRouteWithResponse#String-Options -->
     * <pre>
     * digitalTwinsAsyncClient.getEventRouteWithResponse&#40;&quot;myEventRouteId&quot;&#41;.subscribe&#40;eventRouteWithResponse -&gt; &#123;
     *     System.out.println&#40;&quot;Received get event route operation response with HTTP status code: &quot;
     *         + eventRouteWithResponse.getStatusCode&#40;&#41;&#41;;
     *     System.out.println&#40;&quot;Retrieved event route with Id: &quot; + eventRouteWithResponse.getValue&#40;&#41;.getEventRouteId&#40;&#41;&#41;;
     * &#125;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.getEventRouteWithResponse#String-Options -->
     *
     * @param eventRouteId The ID of the event route to get.
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

        return this.protocolLayer.getEventRoutes()
            .getByIdWithResponseAsync(eventRouteId, null, context)
            .map(eventRouteResponse -> new SimpleResponse<>(eventRouteResponse.getRequest(),
                eventRouteResponse.getStatusCode(), eventRouteResponse.getHeaders(),
                EventRouteConverter.map(eventRouteResponse.getValue())));
    }

    /**
     * Delete an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteEventRoute#String -->
     * <pre>
     * digitalTwinsAsyncClient.deleteEventRoute&#40;&quot;myEventRouteId&quot;&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteEventRoute#String -->
     *
     * @param eventRouteId The ID of the event route to delete.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteEventRoute(String eventRouteId) {
        return deleteEventRouteWithResponse(eventRouteId).flatMap(FluxUtil::toMono);
    }

    /**
     * Delete an event route.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteEventRouteWithResponse#String-Options -->
     * <pre>
     * digitalTwinsAsyncClient.deleteEventRouteWithResponse&#40;&quot;myEventRouteId&quot;&#41;
     *     .subscribe&#40;deleteResponse -&gt; System.out.println&#40;
     *         &quot;Received delete event route operation response with HTTP status code: &quot;
     *             + deleteResponse.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.deleteEventRouteWithResponse#String-Options -->
     *
     * @param eventRouteId The ID of the event route to delete.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteEventRouteWithResponse(String eventRouteId) {
        return withContext(context -> deleteEventRouteWithResponse(eventRouteId, context));
    }

    Mono<Response<Void>> deleteEventRouteWithResponse(String eventRouteId, Context context) {
        return this.protocolLayer.getEventRoutes()
            .deleteWithResponseAsync(eventRouteId, null, context != null ? context : Context.NONE);
    }

    /**
     * List all the event routes that exist in your digital twins instance.
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listEventRoutes -->
     * <pre>
     * digitalTwinsAsyncClient.listEventRoutes&#40;&#41;
     *     .doOnNext&#40;
     *         eventRoute -&gt; System.out.println&#40;&quot;Retrieved event route with Id: &quot; + eventRoute.getEventRouteId&#40;&#41;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listEventRoutes -->
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
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listEventRoutes#ListDigitalTwinsEventRoutesOptions -->
     * <pre>
     * digitalTwinsAsyncClient.listEventRoutes&#40;new ListDigitalTwinsEventRoutesOptions&#40;&#41;.setMaxItemsPerPage&#40;5&#41;&#41;
     *     .doOnNext&#40;
     *         eventRoute -&gt; System.out.println&#40;&quot;Retrieved event route with Id: &quot; + eventRoute.getEventRouteId&#40;&#41;&#41;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.listEventRoutes#ListDigitalTwinsEventRoutesOptions -->
     *
     * @param options The optional parameters to use when listing event routes. See
     * {@link ListDigitalTwinsEventRoutesOptions} for more details on what optional parameters can be set.
     * @return A {@link PagedFlux} that contains all the event routes that exist in your digital twins instance.
     * This PagedFlux may take multiple service requests to iterate over all event routes.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<DigitalTwinsEventRoute> listEventRoutes(ListDigitalTwinsEventRoutesOptions options) {
        return new PagedFlux<>(() -> withContext(context -> listEventRoutesFirstPage(options, context)),
            nextLink -> withContext(context -> listEventRoutesNextPage(nextLink, options, context)));
    }

    PagedFlux<DigitalTwinsEventRoute> listEventRoutes(ListDigitalTwinsEventRoutesOptions options, Context context) {
        return new PagedFlux<>(() -> listEventRoutesFirstPage(options, context != null ? context : Context.NONE),
            nextLink -> listEventRoutesNextPage(nextLink, options, context != null ? context : Context.NONE));
    }

    Mono<PagedResponse<DigitalTwinsEventRoute>> listEventRoutesFirstPage(ListDigitalTwinsEventRoutesOptions options,
        Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getEventRoutes()
            .listSinglePageAsync(OptionsConverter.toProtocolLayerOptions(options), context)
            .map(DigitalTwinsAsyncClient::mapEventRoute);
    }

    Mono<PagedResponse<DigitalTwinsEventRoute>> listEventRoutesNextPage(String nextLink,
        ListDigitalTwinsEventRoutesOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        return protocolLayer.getEventRoutes()
            .listNextSinglePageAsync(nextLink, OptionsConverter.toProtocolLayerOptions(options), context)
            .map(DigitalTwinsAsyncClient::mapEventRoute);
    }

    private static PagedResponse<DigitalTwinsEventRoute> mapEventRoute(
        PagedResponse<com.azure.digitaltwins.core.implementation.models.EventRoute> eventRoute) {
        List<DigitalTwinsEventRoute> convertedList = eventRoute.getValue()
            .stream()
            .map(EventRouteConverter::map)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        return new PagedResponseBase<>(eventRoute.getRequest(), eventRoute.getStatusCode(), eventRoute.getHeaders(),
            convertedList, eventRoute.getContinuationToken(), null);
    }

    //endregion Event Route APIs

    //region Telemetry APIs

    /**
     * Publishes telemetry from a digital twin
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the
     * telemetry payload.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetry#String-String-Object#Object -->
     * <pre>
     * Dictionary&lt;String, Integer&gt; telemetryPayload = new Hashtable&lt;&gt;&#40;&#41;;
     * telemetryPayload.put&#40;&quot;Telemetry1&quot;, 5&#41;;
     *
     * digitalTwinsAsyncClient.publishTelemetry&#40;&quot;myDigitalTwinId&quot;, UUID.randomUUID&#40;&#41;.toString&#40;&#41;, telemetryPayload&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetry#String-String-Object#Object -->
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetry#String-String-Object#String -->
     * <pre>
     * digitalTwinsAsyncClient.publishTelemetry&#40;&quot;myDigitalTwinId&quot;, UUID.randomUUID&#40;&#41;.toString&#40;&#41;, &quot;&#123;&#92;&quot;Telemetry1&#92;&quot;: 5&#125;&quot;&#41;
     *     .subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetry#String-String-Object#String -->
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under
     * {@link DigitalTwinsEventRoute}. These event routes need to be set before publishing a telemetry message, in order
     * for the telemetry message to be consumed.
     *
     * @param digitalTwinId The ID of the digital twin.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for
     * de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly
     * typed object like a Dictionary.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> publishTelemetry(String digitalTwinId, String messageId, Object payload) {
        return publishTelemetryWithResponse(digitalTwinId, messageId, payload, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Publishes telemetry from a digital twin
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the
     * telemetry payload.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetryWithResponse#String-String-Object-Options#Object -->
     * <pre>
     * Dictionary&lt;String, Integer&gt; telemetryPayload = new Hashtable&lt;&gt;&#40;&#41;;
     * telemetryPayload.put&#40;&quot;Telemetry1&quot;, 5&#41;;
     *
     * digitalTwinsAsyncClient.publishTelemetryWithResponse&#40;&quot;myDigitalTwinId&quot;, UUID.randomUUID&#40;&#41;.toString&#40;&#41;,
     *         telemetryPayload, new PublishTelemetryOptions&#40;&#41;.setTimestamp&#40;OffsetDateTime.now&#40;ZoneId.systemDefault&#40;&#41;&#41;&#41;&#41;
     *     .subscribe&#40;responseObject -&gt; System.out.println&#40;
     *         &quot;Received publish telemetry operation response with HTTP status code: &quot;
     *             + responseObject.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetryWithResponse#String-String-Object-Options#Object -->
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetryWithResponse#String-String-Object-Options#String -->
     * <pre>
     * digitalTwinsAsyncClient.publishTelemetryWithResponse&#40;&quot;myDigitalTwinId&quot;, UUID.randomUUID&#40;&#41;.toString&#40;&#41;,
     *         &quot;&#123;&#92;&quot;Telemetry1&#92;&quot;: 5&#125;&quot;,
     *         new PublishTelemetryOptions&#40;&#41;.setTimestamp&#40;OffsetDateTime.now&#40;ZoneId.systemDefault&#40;&#41;&#41;&#41;&#41;
     *     .subscribe&#40;responseString -&gt; System.out.println&#40;
     *         &quot;Received publish telemetry operation response with HTTP status code: &quot;
     *             + responseString.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishTelemetryWithResponse#String-String-Object-Options#String -->
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under
     * {@link DigitalTwinsEventRoute}. These event routes need to be set before publishing a telemetry message, in order
     * for the telemetry message to be consumed.
     *
     * @param digitalTwinId The ID of the digital twin.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for
     * de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly
     * typed object like a Dictionary.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishTelemetryWithResponse(String digitalTwinId, String messageId, Object payload,
        PublishTelemetryOptions options) {
        return withContext(
            context -> publishTelemetryWithResponse(digitalTwinId, messageId, payload, options, context));
    }

    Mono<Response<Void>> publishTelemetryWithResponse(String digitalTwinId, String messageId, Object payload,
        PublishTelemetryOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        if (messageId == null || messageId.isEmpty()) {
            messageId = CoreUtils.randomUuid().toString();
        }

        if (options == null) {
            options = new PublishTelemetryOptions();
        }

        return protocolLayer.getDigitalTwins()
            .sendTelemetryWithResponseAsync(digitalTwinId, messageId, payload, options.getTimestamp().toString(), null,
                context);
    }

    /**
     * Publishes telemetry from a digital twin's component
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the
     * telemetry payload.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetry#String-String-String-Object#Object -->
     * <pre>
     * Dictionary&lt;String, Integer&gt; telemetryPayload = new Hashtable&lt;&gt;&#40;&#41;;
     * telemetryPayload.put&#40;&quot;Telemetry1&quot;, 5&#41;;
     *
     * digitalTwinsAsyncClient.publishComponentTelemetry&#40;&quot;myDigitalTwinId&quot;, &quot;myComponentName&quot;,
     *     UUID.randomUUID&#40;&#41;.toString&#40;&#41;, telemetryPayload&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetry#String-String-String-Object#Object -->
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetry#String-String-String-Object#String -->
     * <pre>
     * digitalTwinsAsyncClient.publishComponentTelemetry&#40;&quot;myDigitalTwinId&quot;, &quot;myComponentName&quot;,
     *     UUID.randomUUID&#40;&#41;.toString&#40;&#41;, &quot;&#123;&#92;&quot;Telemetry1&#92;&quot;: 5&#125;&quot;&#41;.subscribe&#40;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetry#String-String-String-Object#String -->
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under
     * {@link DigitalTwinsEventRoute}. These event routes need to be set before publishing a telemetry message, in order
     * for the telemetry message to be consumed.
     *
     * @param digitalTwinId The ID of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for
     * de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly
     * typed object like a Dictionary.
     * @return An empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> publishComponentTelemetry(String digitalTwinId, String componentName, String messageId,
        Object payload) {
        return publishComponentTelemetryWithResponse(digitalTwinId, componentName, messageId, payload, null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Publishes telemetry from a digital twin's component
     *
     * <p><strong>Code Samples</strong></p>
     *
     * <p>A strongly typed object such as {@link java.util.Hashtable} can be provided as the input parameter for the
     * telemetry payload.</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#Object -->
     * <pre>
     * Dictionary&lt;String, Integer&gt; telemetryPayload = new Hashtable&lt;&gt;&#40;&#41;;
     * telemetryPayload.put&#40;&quot;Telemetry1&quot;, 5&#41;;
     *
     * digitalTwinsAsyncClient.publishComponentTelemetryWithResponse&#40;&quot;myDigitalTwinId&quot;, &quot;myComponentName&quot;,
     *         UUID.randomUUID&#40;&#41;.toString&#40;&#41;, telemetryPayload,
     *         new PublishComponentTelemetryOptions&#40;&#41;.setTimestamp&#40;OffsetDateTime.now&#40;ZoneId.systemDefault&#40;&#41;&#41;&#41;&#41;
     *     .subscribe&#40;responseObject -&gt; System.out.println&#40;
     *         &quot;Received publish component telemetry operation response with HTTP status code: &quot;
     *             + responseObject.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#Object -->
     *
     * <p>Or alternatively String can be used as input type to construct the json string telemetry payload:</p>
     *
     * <!-- src_embed com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#String -->
     * <pre>
     * digitalTwinsAsyncClient.publishComponentTelemetryWithResponse&#40;&quot;myDigitalTwinId&quot;, &quot;myComponentName&quot;,
     *         UUID.randomUUID&#40;&#41;.toString&#40;&#41;, &quot;&#123;&#92;&quot;Telemetry1&#92;&quot;: 5&#125;&quot;,
     *         new PublishComponentTelemetryOptions&#40;&#41;.setTimestamp&#40;OffsetDateTime.now&#40;ZoneId.systemDefault&#40;&#41;&#41;&#41;&#41;
     *     .subscribe&#40;responseString -&gt; System.out.println&#40;
     *         &quot;Received publish component telemetry operation response with HTTP status code: &quot;
     *             + responseString.getStatusCode&#40;&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.digitaltwins.core.DigitalTwinsAsyncClient.publishComponentTelemetryWithResponse#String-String-String-Object-Options#String -->
     *
     * The result is then consumed by one or many destination endpoints (subscribers) defined under
     * {@link DigitalTwinsEventRoute}. These event routes need to be set before publishing a telemetry message, in order
     * for the telemetry message to be consumed.
     *
     * @param digitalTwinId The ID of the digital twin.
     * @param componentName The name of the DTDL component.
     * @param messageId A unique message identifier (within the scope of the digital twin id) that is commonly used for
     * de-duplicating messages. Defaults to a random UUID if argument is null.
     * @param payload The application/json telemetry payload to be sent. payload can be a raw json string or a strongly
     * typed object like a Dictionary.
     * @param options The optional parameters for this request. If null, the default option values will be used.
     * @return A {@link Response} containing an empty mono.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> publishComponentTelemetryWithResponse(String digitalTwinId, String componentName,
        String messageId, Object payload, PublishComponentTelemetryOptions options) {
        return withContext(
            context -> publishComponentTelemetryWithResponse(digitalTwinId, componentName, messageId, payload, options,
                context));
    }

    Mono<Response<Void>> publishComponentTelemetryWithResponse(String digitalTwinId, String componentName,
        String messageId, Object payload, PublishComponentTelemetryOptions options, Context context) {
        if (context == null) {
            context = Context.NONE;
        }

        if (messageId == null || messageId.isEmpty()) {
            messageId = CoreUtils.randomUuid().toString();
        }

        if (options == null) {
            options = new PublishComponentTelemetryOptions();
        }

        return protocolLayer.getDigitalTwins()
            .sendComponentTelemetryWithResponseAsync(digitalTwinId, componentName, messageId, payload,
                options.getTimestamp().toString(), null, context);
    }

    //endregion Telemetry APIs

    private static DigitalTwinsResponseHeaders createDTResponseHeadersFromResponse(Response<?> response) {
        return (response == null)
            ? null
            : new DigitalTwinsResponseHeaders().setETag(response.getHeaders().getValue(HttpHeaderName.ETAG));
    }

    private <T> Mono<DigitalTwinsResponse<T>> deserializeHelper(Response<Object> response, Class<T> clazz) {
        try {
            T genericResponse = DeserializationHelpers.deserializeObject(protocolLayer.getSerializerAdapter(),
                response.getValue(), clazz, this.serializer);
            return Mono.just(new DigitalTwinsResponse<>(response.getRequest(), response.getStatusCode(),
                response.getHeaders(), genericResponse, createDTResponseHeadersFromResponse(response)));
        } catch (IOException e) {
            return Mono.error(LOGGER.atError().log(e));
        }
    }

    private <T> T mapObject(Object object, Class<T> clazz) {
        try {
            return DeserializationHelpers.deserializeObject(protocolLayer.getSerializerAdapter(), object, clazz,
                serializer);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }
}
