// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImpl;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImplBuilder;
import com.azure.digitaltwins.core.models.DigitalTwinsAddHeaders;
import com.azure.digitaltwins.core.models.DigitalTwinsAddResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;


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
public class DigitalTwinsAsyncClient {
    private final ClientLogger logger = new ClientLogger(DigitalTwinsAsyncClient.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final DigitalTwinsServiceVersion serviceVersion;
    private final AzureDigitalTwinsAPIImpl protocolLayer;

    DigitalTwinsAsyncClient(HttpPipeline pipeline, DigitalTwinsServiceVersion serviceVersion, String host) {
        this.protocolLayer = new AzureDigitalTwinsAPIImplBuilder().host(host).pipeline(pipeline).buildClient();
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

    // TODO: Input and output as String.
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<String> createDigitalTwinString(String digitalTwinId, String digitalTwin) throws JsonProcessingException {
        Object payload = mapper.readValue(digitalTwin, Object.class);
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, payload)
            .flatMap(
                response -> {
                    try {
                        return Mono.just(mapper.writeValueAsString(response.getValue()));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    // TODO: Input and output are Object.
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Object> createDigitalTwinObject(String digitalTwinId, Object digitalTwin) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin)
            .flatMap(
                response -> Mono.just(response.getValue()));
    }

    // TODO: Input and output are T (Generics).
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<T> createDigitalTwinGeneric(String digitalTwinId, Object digitalTwin, Class<T> klazz) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin)
            .flatMap(
                response -> Mono.just(mapper.convertValue(response.getValue(), klazz)));
    }

    // TODO: Input is String and output is Response<String> -> ResponseBase<DigitalTwinsAddHeaders, String>.
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ResponseBase<DigitalTwinsAddHeaders, String>> createDigitalTwinWithResponseResponseBaseString(String digitalTwinId, String digitalTwin) throws JsonProcessingException {
        Object payload = mapper.readValue(digitalTwin, Object.class);
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, payload)
            .flatMap(
                response -> {
                    try {
                        String jsonResponse = mapper.writeValueAsString(response.getValue());
                        return Mono.just(new ResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), jsonResponse, response.getDeserializedHeaders()));
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    // TODO: Input is String and output is Response<String> -> DigitalTwinsAddResponse (json string).
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsAddResponse> createDigitalTwinWithResponseDigitalTwinAddResponseString(String digitalTwinId, String digitalTwin) throws JsonProcessingException {
        Object payload = mapper.readValue(digitalTwin, Object.class);
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, payload)
            .flatMap(
                response -> {
                    try {
                        String jsonResponse = mapper.writeValueAsString(response.getValue());
                        DigitalTwinsAddResponse addResponse = new DigitalTwinsAddResponse(response.getRequest(), response.getStatusCode(), response.getHeaders(), jsonResponse, response.getDeserializedHeaders());
                        return Mono.just(addResponse);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    // TODO: Input is Object and output is Response<Object> -> DigitalTwinsAddResponse.
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsAddResponse> createDigitalTwinWithResponseDigitalTwinsAddResponseObject(String digitalTwinId, Object digitalTwin) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin);
    }

    // TODO: Input is Object and output is Response<T> -> ResponseBase<DigitalTwinsAddHeaders, T>.
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<ResponseBase<DigitalTwinsAddHeaders, T>> createDigitalTwinWithResponseGeneric(String digitalTwinId, Object digitalTwin, Class<T> klazz) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin)
            .flatMap(
                response -> {
                    T genericResponse = mapper.convertValue(response.getValue(), klazz);
                    return Mono.just(new ResponseBase<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse, response.getDeserializedHeaders()));
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
        try {
            return protocolLayer
                .getDigitalTwins()
                .addRelationshipWithResponseAsync(digitalTwinId, relationshipId, relationship)
                // Mono.flatMap: Transform the item emitted by this Mono asynchronously, returning the value emitted by another Mono (possibly changing the value type).
                // The PL gives us a Mono<DigitalTwinsAddRelationshipResponse>, so we use Mono.flatMap to transform the items emitted
                // from Mono<DigitalTwinsAddRelationshipResponse> to Mono<String>, asynchronously.
                .flatMap(
                    // Mono.just(item) creates a new Mono that emits the specified item.
                    // response.getValue gives us the deserialized Http response body (Object).
                    response -> Mono.just(response.getValue().toString()));
        } catch (RuntimeException ex) {
            // TODO: Ensure that exceptions are handled in a reactive way
            return FluxUtil.monoError(logger, ex);
        }
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
        try {
            return protocolLayer
                .getDigitalTwins()
                .addRelationshipWithResponseAsync(digitalTwinId, relationshipId, relationship)
                // Mono.flatMap: Transform the item emitted by this Mono asynchronously, returning the value emitted by another Mono (possibly changing the value type).
                // The PL gives us a Mono<DigitalTwinsAddRelationshipResponse>, so we use Mono.flatMap to transform the items emitted
                // from Mono<DigitalTwinsAddRelationshipResponse> to Mono<Response<<String>>, asynchronously.
                .flatMap(
                    // Mono.just(item) creates a new Mono that emits the specified item.
                    // SimpleResponse is an implementation of the interface Response<T>.
                    // response.getValue gives us the deserialized Http response body (Object).
                    response -> Mono.just(new SimpleResponse<>(response, response.getValue().toString())));
        } catch (RuntimeException ex) {
            // TODO: Ensure that exceptions are handled in a reactive way
            return FluxUtil.monoError(logger, ex);
        }
    }

    /**
     * Gets all the relationships on a digital twin filtered by the relationship name, by iterating through a collection.
     *
     * @param digitalTwinId The Id of the source digital twin.
     * @param relationshipName The name of a relationship to filter to.
     * @return A {@link PagedFlux} of application/json relationships belonging to the specified digital twin and the http response.
     * TODO: Impl here returns an Object and not a String.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<Object> listRelationships(String digitalTwinId, String relationshipName) {
        return new PagedFlux<>(
            () -> protocolLayer.getDigitalTwins().listRelationshipsSinglePageAsync(digitalTwinId, relationshipName),
            nextLink -> protocolLayer.getDigitalTwins().listRelationshipsNextSinglePageAsync(nextLink));
    }
}
