// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.*;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImpl;
import com.azure.digitaltwins.core.implementation.AzureDigitalTwinsAPIImplBuilder;
import com.azure.digitaltwins.core.implementation.models.DigitalTwinsAddHeaders;
import com.azure.digitaltwins.core.implementation.models.DigitalTwinsAddResponse;
import com.azure.digitaltwins.core.implementation.serialization.BasicDigitalTwin;
import com.azure.digitaltwins.core.implementation.serializer.DigitalTwinsStringSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
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
public class DigitalTwinsAsyncClient {
    private static final ClientLogger logger = new ClientLogger(DigitalTwinsAsyncClient.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private final DigitalTwinsServiceVersion serviceVersion;
    private final AzureDigitalTwinsAPIImpl protocolLayer;

    DigitalTwinsAsyncClient(HttpPipeline pipeline, DigitalTwinsServiceVersion serviceVersion, String host) {
        final SimpleModule stringModule = new SimpleModule("String Serializer");
        stringModule.addSerializer(new DigitalTwinsStringSerializer(String.class, false, mapper));

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

    // Input is String and output is Response<String>.
    // String etag = result.getHeaders().get("etag").getValue();
    // String jsonData = result.getValue();
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<String>> createDigitalTwinWithResponseString(String digitalTwinId, String digitalTwin) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin)
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

    // Input is String and output is Response<String> -> ResponseBase<DigitalTwinsAddHeaders, String>.
    // String etag = result.getDeserializedHeaders().getETag();
    // String jsonData = result.getValue();
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ResponseBase<DigitalTwinsAddHeaders, String>> createDigitalTwinWithResponseBaseString(String digitalTwinId, String digitalTwin) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin)
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

    // Input is String and output is Response<String> -> DigitalTwinsAddResponse (json string).
    // String etag = result.getDeserializedHeaders().getETag();
    // String jsonData = result.getValue().toString();
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsAddResponse> createDigitalTwinWithDigitalTwinAddResponseString(String digitalTwinId, String digitalTwin) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin)
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

    // Input is Object and output is Response<Object> -> DigitalTwinsAddResponse.
    // String etag = result.getDeserializedHeaders().getETag();
    // Object jsonData = result.getValue(); [This Object can be cast to a LinkedHashMap].
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DigitalTwinsAddResponse> createDigitalTwinWithDigitalTwinsAddResponseObject(String digitalTwinId, Object digitalTwin) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin);
    }

    // Input is Object and output is Response<BasicDigitalTwin>.
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<BasicDigitalTwin>> createDigitalTwinWithResponseBasicDigitalTwin(String digitalTwinId, Object digitalTwin) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin)
            .flatMap(
                response -> {
                    BasicDigitalTwin genericResponse = mapper.convertValue(response.getValue(), BasicDigitalTwin.class);
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse));
                });
    }

    // Input is T and output is Response<T>.
    @ServiceMethod(returns = ReturnType.SINGLE)
    public <T> Mono<Response<T>> createDigitalTwinWithResponseGeneric(String digitalTwinId, T digitalTwin, Class<T> klazz) {
        return protocolLayer
            .getDigitalTwins()
            .addWithResponseAsync(digitalTwinId, digitalTwin)
            .flatMap(
                response -> {
                    T genericResponse = mapper.convertValue(response.getValue(), klazz);
                    return Mono.just(new SimpleResponse<>(response.getRequest(), response.getStatusCode(), response.getHeaders(), genericResponse));
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
        return protocolLayer
            .getDigitalTwins()
            .addRelationshipWithResponseAsync(digitalTwinId, relationshipId, relationship)
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

        Supplier<Mono<PagedResponse<String>>> firstPage = () -> protocolLayer.getDigitalTwins().listRelationshipsSinglePageAsync(digitalTwinId, relationshipName)
            .map(
                objectPagedResponse -> {
                    List<String> stringValue = objectPagedResponse.getValue().stream()
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
                        stringValue,
                        objectPagedResponse.getContinuationToken(),
                        ((PagedResponseBase) objectPagedResponse).getDeserializedHeaders());

                }
            );

        Function<String, Mono<PagedResponse<String>>> nextPage = s -> protocolLayer.getDigitalTwins().listRelationshipsNextSinglePageAsync(s)
            .map(objectPagedResponse -> {
                List<String> stringValue = objectPagedResponse.getValue().stream()
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
                    stringValue,
                    objectPagedResponse.getContinuationToken(),
                    ((PagedResponseBase)objectPagedResponse).getDeserializedHeaders());
            });

        return new PagedFlux<>(firstPage, nextPage);

    }

}
