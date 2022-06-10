// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.core.util.polling.DefaultPollingStrategy;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.route.implementation.RoutesImpl;
import com.azure.maps.route.implementation.helpers.Utility;
import com.azure.maps.route.implementation.models.BatchRequest;
import com.azure.maps.route.implementation.models.BatchRequestItem;
import com.azure.maps.route.implementation.models.JsonFormat;
import com.azure.maps.route.implementation.models.ResponseFormat;
import com.azure.maps.route.implementation.models.RouteMatrixQueryPrivate;
import com.azure.maps.route.models.ErrorResponseException;
import com.azure.maps.route.models.RouteDirections;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteDirectionsOptions;
import com.azure.maps.route.models.RouteDirectionsParameters;
import com.azure.maps.route.models.RouteMatrixOptions;
import com.azure.maps.route.models.RouteMatrixQuery;
import com.azure.maps.route.models.RouteMatrixResult;
import com.azure.maps.route.models.RouteRangeOptions;
import com.azure.maps.route.models.RouteRangeResult;

import reactor.core.publisher.Mono;

/**
 * Initializes a new instance of the asynchronous RouteAsyncClient type.
 * Creating an async client using a {@link AzureKeyCredential}:
 * <!-- src_embed com.azure.maps.route.async.builder.key.instantiation -->
 * <pre>
 * &#47;&#47; Authenticates using subscription key
 * AzureKeyCredential keyCredential = new AzureKeyCredential&#40;System.getenv&#40;&quot;SUBSCRIPTION_KEY&quot;&#41;&#41;;
 *
 * &#47;&#47; Creates a builder
 * RouteClientBuilder builder = new RouteClientBuilder&#40;&#41;;
 * builder.credential&#40;keyCredential&#41;;
 * builder.httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;;
 *
 * &#47;&#47; Builds the client
 * RouteAsyncClient client = builder.buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.maps.route.async.builder.key.instantiation -->
 * Creating an async client using a {@link TokenCredential}:
 * <!-- src_embed com.azure.maps.route.async.builder.ad.instantiation -->
 * <pre>
 * &#47;&#47; Authenticates using Azure AD building a default credential
 * &#47;&#47; This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
 * DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
 *
 * &#47;&#47; Creates a builder
 * RouteClientBuilder builder = new RouteClientBuilder&#40;&#41;;
 * builder.credential&#40;tokenCredential&#41;;
 * builder.mapsClientId&#40;System.getenv&#40;&quot;MAPS_CLIENT_ID&quot;&#41;&#41;;
 * builder.httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;;
 *
 * &#47;&#47; Builds a client
 * RouteAsyncClient client = builder.buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.maps.route.async.builder.ad.instantiation -->
*/
@ServiceClient(builder = RouteClientBuilder.class, isAsync = true)
public final class RouteAsyncClient {
    // route batch size constants
    private static final int ROUTE_MATRIX_SMALL_SIZE = 100;
    private static final int ROUTE_DIRECTIONS_SMALL_SIZE = 100;

    // polling strategy constants
    private static final int POLLING_FREQUENCY = 1;
    private static final String POLLING_BATCH_HEADER_KEY = "BatchId";

    // instance fields
    private final DefaultPollingStrategy<RouteDirectionsBatchResult,
        RouteDirectionsBatchResult> forwardStrategy;
    private final DefaultPollingStrategy<RouteMatrixResult, RouteMatrixResult>
        routeMatrixStrategy;
    @Generated private final RoutesImpl serviceClient;

    // static class
    static class RouteDirectionsBatchReference extends TypeReference<RouteDirectionsBatchResult> { };
    static class RouteMatrixReference extends TypeReference<RouteMatrixResult> { };

    /**
     * Initializes an instance of Routes client.
     *
     * @param serviceClient the service client implementation.
     */
    @Generated
    RouteAsyncClient(RoutesImpl serviceClient, HttpPipeline httpPipeline) {
        this.serviceClient = serviceClient;
        this.forwardStrategy = new DefaultPollingStrategy<>(httpPipeline);
        this.routeMatrixStrategy = new DefaultPollingStrategy<>(httpPipeline);
    }

    /**
     * Request Route Matrix
     *
     * @param options the {@code RouteMatrixOptions} applicable to this query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<RouteMatrixResult, RouteMatrixResult> beginRequestRouteMatrix(
            RouteMatrixOptions options) {
        return this.beginRequestRouteMatrix(options, null);
    }

    /**
     * Request Route Matrix
     *
     * @param options the {@code RouteMatrixOptions} applicable to this query.
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    PollerFlux<RouteMatrixResult, RouteMatrixResult> beginRequestRouteMatrix(
            RouteMatrixOptions options, Context context) {
        // if it's a small batch, then let's wait for results and avoid polling
        RouteMatrixQuery query = options.getRouteMatrixQuery();
        RouteMatrixQueryPrivate privateQuery = Utility.toRouteMatrixQueryPrivate(query);
        final int originSize = privateQuery.getOrigins().getCoordinates().size();
        final int destSize = privateQuery.getDestinations().getCoordinates().size();
        boolean waitForResults = (originSize * destSize <= ROUTE_MATRIX_SMALL_SIZE);

        return createPollerFlux(
            () ->
                this.serviceClient.requestRouteMatrixWithResponseAsync(
                    JsonFormat.JSON,
                    privateQuery,
                    waitForResults,
                    options.getComputeTravelTime(),
                    options.getFilterSectionType(),
                    options.getArriveAt(),
                    options.getDepartAt(),
                    options.getVehicleAxleWeight(),
                    options.getVehicleLength(),
                    options.getVehicleHeight(),
                    options.getVehicleWidth(),
                    options.getVehicleMaxSpeed(),
                    options.getVehicleWeight(),
                    options.getWindingness(),
                    options.getInclineLevel(),
                    options.getTravelMode(),
                    options.getAvoid(),
                    options.getUseTrafficData(),
                    options.getRouteType(),
                    options.getVehicleLoadType(),
                    context).flatMap(response -> {
                        return Mono.just(Utility.createRouteMatrixResponse(response));
                    }),
            this.routeMatrixStrategy);
    }

    /**
     * Get Route Matrix Batch
     *
     * @param matrixId Matrix id received after the Matrix Route request was accepted successfully.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<RouteMatrixResult, RouteMatrixResult> beginGetRouteMatrix(String matrixId) {
        return this.beginGetRouteMatrix(matrixId, null);
    }

    /**
     * Get Route Matrix Batch
     *
     * @param matrixId Matrix id received after the Matrix Route request was accepted successfully.
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    PollerFlux<RouteMatrixResult, RouteMatrixResult> beginGetRouteMatrix(String matrixId, Context context) {
        return createPollerFlux(
            () -> this.serviceClient.getRouteMatrixWithResponseAsync(matrixId, context)
                    .flatMap(response -> {
                        return Mono.just(Utility.createRouteMatrixResponse(response));
                    }),
            this.routeMatrixStrategy);
    }

    /**
     * Get Route Directions
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouteDirections> getRouteDirections(
            RouteDirectionsOptions options) {
        Mono<Response<RouteDirections>> result = this.getRouteDirectionsWithResponse(options);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Get Route Directions
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @Generated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouteDirections>> getRouteDirectionsWithResponse(
            RouteDirectionsOptions options) {
        return this.getRouteDirectionsWithResponse(options, null);
    }

    /**
     * Get Route Directions
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    Mono<Response<RouteDirections>> getRouteDirectionsWithResponse(
            RouteDirectionsOptions options, Context context) {
        return this.serviceClient.getRouteDirectionsWithResponseAsync(
                ResponseFormat.JSON,
                Utility.toRouteQueryString(options.getRoutePoints()),
                options.getMaxAlternatives(),
                options.getAlternativeType(),
                options.getMinDeviationDistance(),
                options.getArriveAt(),
                options.getDepartAt(),
                options.getMinDeviationTime(),
                options.getInstructionsType(),
                options.getLanguage(),
                options.getComputeBestWaypointOrder(),
                options.getRouteRepresentationForBestOrder(),
                options.getComputeTravelTime(),
                options.getVehicleHeading(),
                options.getReport(),
                options.getFilterSectionType(),
                options.getVehicleAxleWeight(),
                options.getVehicleWidth(),
                options.getVehicleHeight(),
                options.getVehicleLength(),
                options.getVehicleMaxSpeed(),
                options.getVehicleWeight(),
                options.isCommercialVehicle(),
                options.getWindingness(),
                options.getInclineLevel(),
                options.getTravelMode(),
                options.getAvoid(),
                options.getUseTrafficData(),
                options.getRouteType(),
                options.getVehicleLoadType(),
                options.getVehicleEngineType(),
                options.getConstantSpeedConsumptionInLitersPerHundredKm(),
                options.getCurrentFuelInLiters(),
                options.getAuxiliaryPowerInLitersPerHour(),
                options.getFuelEnergyDensityInMegajoulesPerLiter(),
                options.getAccelerationEfficiency(),
                options.getDecelerationEfficiency(),
                options.getUphillEfficiency(),
                options.getDownhillEfficiency(),
                options.getConstantSpeedConsumptionInKwHPerHundredKm(),
                options.getCurrentChargeInKwH(),
                options.getMaxChargeInKwH(),
                options.getAuxiliaryPowerInKw(),
                context);
    }

    /**
     * Get Route Directions
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param parameters the {@code RouteDirectionsParameters} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @Generated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouteDirections> getRouteDirectionsWithAdditionalParameters(
            RouteDirectionsOptions options,
            RouteDirectionsParameters parameters) {
        Mono<Response<RouteDirections>> result =
            this.getRouteDirectionsWithAdditionalParametersWithResponse(options, parameters);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Get Route Directions
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param parameters the {@code RouteDirectionsParameters} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouteDirections>> getRouteDirectionsWithAdditionalParametersWithResponse(
            RouteDirectionsOptions options,
            RouteDirectionsParameters parameters) {
        return this.getRouteDirectionsWithAdditionalParametersWithResponse(options, parameters, null);
    }

    /**
     * Get Route Directions
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param parameters the {@code RouteDirectionsParameters} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    Mono<Response<RouteDirections>> getRouteDirectionsWithAdditionalParametersWithResponse(
            RouteDirectionsOptions options,
            RouteDirectionsParameters parameters,
            Context context) {
        return this.serviceClient.getRouteDirectionsWithAdditionalParametersWithResponseAsync(
                ResponseFormat.JSON,
                Utility.toRouteQueryString(options.getRoutePoints()),
                Utility.toRouteDirectionParametersPrivate(parameters),
                options.getMaxAlternatives(),
                options.getAlternativeType(),
                options.getMinDeviationDistance(),
                options.getMinDeviationTime(),
                options.getInstructionsType(),
                options.getLanguage(),
                options.getComputeBestWaypointOrder(),
                options.getRouteRepresentationForBestOrder(),
                options.getComputeTravelTime(),
                options.getVehicleHeading(),
                options.getReport(),
                options.getFilterSectionType(),
                options.getArriveAt(),
                options.getDepartAt(),
                options.getVehicleAxleWeight(),
                options.getVehicleLength(),
                options.getVehicleHeight(),
                options.getVehicleWidth(),
                options.getVehicleMaxSpeed(),
                options.getVehicleWeight(),
                options.isCommercialVehicle(),
                options.getWindingness(),
                options.getInclineLevel(),
                options.getTravelMode(),
                options.getAvoid(),
                options.getUseTrafficData(),
                options.getRouteType(),
                options.getVehicleLoadType(),
                options.getVehicleEngineType(),
                options.getConstantSpeedConsumptionInLitersPerHundredKm(),
                options.getCurrentFuelInLiters(),
                options.getAuxiliaryPowerInLitersPerHour(),
                options.getFuelEnergyDensityInMegajoulesPerLiter(),
                options.getAccelerationEfficiency(),
                options.getDecelerationEfficiency(),
                options.getUphillEfficiency(),
                options.getDownhillEfficiency(),
                options.getConstantSpeedConsumptionInKwHPerHundredKm(),
                options.getCurrentChargeInKwH(),
                options.getMaxChargeInKwH(),
                options.getAuxiliaryPowerInKw(),
                context);
    }

    /**
     * Get Route Range
     *
     * @param options the {@code RouteRangeOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Range call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouteRangeResult> getRouteRange(RouteRangeOptions options) {
        Mono<Response<RouteRangeResult>> result = this.getRouteRangeWithResponse(options);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Get Route Range
     *
     * @param options the {@code RouteRangeOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Range call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<RouteRangeResult>> getRouteRangeWithResponse(RouteRangeOptions options) {
        return this.getRouteRangeWithResponse(options, null);
    }

    /**
     * Get Route Range
     *
     * @param options the {@code RouteRangeOptions} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Range call.
     */
    Mono<Response<RouteRangeResult>> getRouteRangeWithResponse(
            RouteRangeOptions options, Context context) {
        GeoPosition startingPoint = options.getStartingPoint();
        List<Double> startingPointCoordinates = Arrays.asList(
            startingPoint.getLongitude(), startingPoint.getLatitude());

        return this.serviceClient.getRouteRangeWithResponseAsync(
                ResponseFormat.JSON,
                startingPointCoordinates,
                options.getFuelBudgetInLiters(),
                options.getEnergyBudgetInKwH(),
                options.getTimeBudgetInSec(),
                options.getDistanceBudgetInMeters(),
                options.getDepartAt(),
                options.getRouteType(),
                options.getUseTrafficData(),
                options.getAvoid(),
                options.getTravelMode(),
                options.getInclineLevel(),
                options.getWindingness(),
                options.getVehicleAxleWeight(),
                options.getVehicleWidth(),
                options.getVehicleHeight(),
                options.getVehicleLength(),
                options.getVehicleMaxSpeed(),
                options.getVehicleWeight(),
                options.isCommercialVehicle(),
                options.getVehicleLoadType(),
                options.getVehicleEngineType(),
                options.getConstantSpeedConsumptionInKwHPerHundredKm(),
                options.getCurrentFuelInLiters(),
                options.getAuxiliaryPowerInLitersPerHour(),
                options.getFuelEnergyDensityInMegajoulesPerLiter(),
                options.getAccelerationEfficiency(),
                options.getDecelerationEfficiency(),
                options.getUphillEfficiency(),
                options.getDownhillEfficiency(),
                options.getConstantSpeedConsumptionInKwHPerHundredKm(),
                options.getCurrentChargeInKwH(),
                options.getMaxChargeInKwH(),
                options.getAuxiliaryPowerInKw(),
                context);
    }

    /**
     * Get Route Directions Batch
     *
     * @param optionsList the list of {@code RouteDirectionsOptions} used in this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<RouteDirectionsBatchResult, RouteDirectionsBatchResult>
            beginRequestRouteDirectionsBatch(List<RouteDirectionsOptions> optionsList) {
        return this.beginRequestRouteDirectionsBatch(optionsList, null);
    }

    /**
     * Get Route Directions Batch
     *
     * @param optionsList the list of {@code RouteDirectionsOptions} used in this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    PollerFlux<RouteDirectionsBatchResult, RouteDirectionsBatchResult>
            beginRequestRouteDirectionsBatch(List<RouteDirectionsOptions> optionsList, Context context) {
        Objects.requireNonNull(optionsList, "'optionsList' is a required parameter.");

        // convert list to batch request
        List<BatchRequestItem> items = optionsList.stream()
            .map(item -> Utility.toRouteDirectionsBatchItem(item))
            .collect(Collectors.toList());
        BatchRequest batchRequest = new BatchRequest().setBatchItems(items);

        if (batchRequest.getBatchItems().size() <= ROUTE_DIRECTIONS_SMALL_SIZE) {
            return createDirectionsPollerFlux(
                () -> this.serviceClient
                        .requestRouteDirectionsBatchSyncWithResponseAsync(JsonFormat.JSON, batchRequest, context)
                    .flatMap(response -> {
                        return Mono.just(Utility.createRouteDirectionsResponse(response));
                    }),
                this.forwardStrategy);
        } else {
            return createDirectionsPollerFlux(
                () -> this.serviceClient
                        .requestRouteDirectionsBatchWithResponseAsync(JsonFormat.JSON, batchRequest, context)
                    .flatMap(response -> {
                        return Mono.just(Utility.createRouteDirectionsResponse(response));
                    }),
                this.forwardStrategy);
        }
    }

    /**
     * Get Route Directions Batch Id
     *
     * @param batchId the batch id received from a previous call to route directions batch.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<RouteDirectionsBatchResult, RouteDirectionsBatchResult> beginGetRouteDirectionsBatch(
            String batchId) {
        return this.beginGetRouteDirectionsBatch(batchId, null);
    }

    /**
     * Get Route Directions Batch Id
     *
     * @param batchId the batch id received from a previous call to route directions batch.
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    PollerFlux<RouteDirectionsBatchResult, RouteDirectionsBatchResult> beginGetRouteDirectionsBatch(
            String batchId, Context context) {
        return createDirectionsPollerFlux(
            () -> this.serviceClient.getRouteDirectionsBatchWithResponseAsync(batchId, context)
                    .flatMap(response -> {
                        return Mono.just(Utility.createRouteDirectionsResponse(response));
                    }),
            this.forwardStrategy);
    }

    // private utility methods
    private PollerFlux<RouteMatrixResult, RouteMatrixResult> createPollerFlux(
            Supplier<Mono<? extends Response<?>>> initialOperation,
            DefaultPollingStrategy<RouteMatrixResult, RouteMatrixResult> strategy) {

        // type reference
        RouteMatrixReference typeReference = new RouteMatrixReference();

        // Create poller instance
        return PollerFlux.create(
            Duration.ofSeconds(POLLING_FREQUENCY),
            context -> initialOperation.get()
                .flatMap(response -> strategy.canPoll(response).flatMap(canPoll -> {
                    if (!canPoll) {
                        return Mono.error(new IllegalStateException(
                            "Cannot poll with strategy " + strategy.getClass().getSimpleName()));
                    }
                    context.setData(POLLING_BATCH_HEADER_KEY, Utility.getBatchId(response.getHeaders()));
                    return strategy.onInitialResponse(response, context, typeReference);
                })),
            context -> strategy.poll(context, typeReference),
            strategy::cancel,
            context -> {
                return strategy
                    .getResult(context, typeReference)
                        .flatMap(result -> {
                            final String matrixId = context.getData(POLLING_BATCH_HEADER_KEY);
                            result.setMatrixId(matrixId);
                            return Mono.just(result);
                        });
            });
    }

    private PollerFlux<RouteDirectionsBatchResult, RouteDirectionsBatchResult> createDirectionsPollerFlux(
            Supplier<Mono<? extends Response<?>>> initialOperation,
            DefaultPollingStrategy<RouteDirectionsBatchResult, RouteDirectionsBatchResult> strategy) {

        // batch directions type reference
        RouteDirectionsBatchReference typeReference = new RouteDirectionsBatchReference();

        // Create poller instance
        return PollerFlux.create(
            Duration.ofSeconds(POLLING_FREQUENCY),
            context -> initialOperation.get()
                .flatMap(response -> strategy.canPoll(response).flatMap(canPoll -> {
                    if (!canPoll) {
                        return Mono.error(new IllegalStateException(
                            "Cannot poll with strategy " + strategy.getClass().getSimpleName()));
                    }
                    context.setData(POLLING_BATCH_HEADER_KEY, Utility.getBatchId(response.getHeaders()));
                    return strategy.onInitialResponse(response, context, typeReference);
                })),
            context -> strategy.poll(context, typeReference),
            strategy::cancel,
            context -> {
                return strategy
                    .getResult(context, typeReference)
                        .flatMap(result -> {
                            final String batchId = context.getData(POLLING_BATCH_HEADER_KEY);
                            result.setBatchId(batchId);
                            return Mono.just(result);
                        });
            });
    }

}
