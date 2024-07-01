// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.DefaultPollingStrategy;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.route.implementation.RoutesImpl;
import com.azure.maps.route.implementation.helpers.Utility;
import com.azure.maps.route.implementation.models.BatchRequest;
import com.azure.maps.route.implementation.models.BatchRequestItem;
import com.azure.maps.route.implementation.models.ErrorResponseException;
import com.azure.maps.route.implementation.models.JsonFormat;
import com.azure.maps.route.implementation.models.ResponseFormat;
import com.azure.maps.route.implementation.models.RouteMatrixQueryPrivate;
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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;
import static java.util.Collections.singletonList;

/**
 * Initializes a new instance of the asynchronous RouteAsyncClient type.
 * Creating an async client using a {@link AzureKeyCredential}:
 * <!-- src_embed com.azure.maps.route.async.builder.key.instantiation -->
 * <pre>
 * &#47;&#47; Authenticates using subscription key
 * AzureKeyCredential keyCredential = new AzureKeyCredential&#40;System.getenv&#40;&quot;SUBSCRIPTION_KEY&quot;&#41;&#41;;
 *
 * &#47;&#47; Creates a builder
 * MapsRouteClientBuilder builder = new MapsRouteClientBuilder&#40;&#41;;
 * builder.credential&#40;keyCredential&#41;;
 * builder.httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;;
 *
 * &#47;&#47; Builds the client
 * MapsRouteAsyncClient client = builder.buildAsyncClient&#40;&#41;;
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
 * MapsRouteClientBuilder builder = new MapsRouteClientBuilder&#40;&#41;;
 * builder.credential&#40;tokenCredential&#41;;
 * builder.mapsClientId&#40;System.getenv&#40;&quot;MAPS_CLIENT_ID&quot;&#41;&#41;;
 * builder.httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;;
 *
 * &#47;&#47; Builds a client
 * MapsRouteAsyncClient client = builder.buildAsyncClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.maps.route.async.builder.ad.instantiation -->
*/
@ServiceClient(builder = MapsRouteClientBuilder.class, isAsync = true)
public final class MapsRouteAsyncClient {
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
    private final RoutesImpl serviceClient;

    // static class
    static class RouteDirectionsBatchReference extends TypeReference<RouteDirectionsBatchResult> { };
    static class RouteMatrixReference extends TypeReference<RouteMatrixResult> { };

    /**
     * Initializes an instance of Routes client.
     *
     * @param serviceClient the service client implementation.
     */
    MapsRouteAsyncClient(RoutesImpl serviceClient, HttpPipeline httpPipeline) {
        this.serviceClient = serviceClient;
        this.forwardStrategy = new DefaultPollingStrategy<>(httpPipeline);
        this.routeMatrixStrategy = new DefaultPollingStrategy<>(httpPipeline);
    }

    /**
     * Request Route Matrix
     * <!-- src_embed com.azure.maps.search.async.begin_request_route_matrix -->
     * <pre>
     * System.out.println&#40;&quot;Request route matrix&quot;&#41;;
     * RouteMatrixQuery matrixQuery3 = new RouteMatrixQuery&#40;&#41;;
     *
     * &#47;&#47; origins
     * GeoPointCollection origins3 = new GeoPointCollection&#40;Arrays.asList&#40;
     *     new GeoPoint&#40;4.85106, 52.36006&#41;,
     *     new GeoPoint&#40;4.85056, 52.36187&#41;
     * &#41;&#41;;
     *
     * &#47;&#47; destinations
     * GeoPointCollection destinations3 = new GeoPointCollection&#40;Arrays.asList&#40;
     *     new GeoPoint&#40;4.85003, 52.36241&#41;,
     *     new GeoPoint&#40;13.42937, 52.50931&#41;
     * &#41;&#41;;
     *
     * matrixQuery3.setDestinations&#40;destinations3&#41;;
     * matrixQuery3.setOrigins&#40;origins3&#41;;
     *
     * RouteMatrixOptions matrixOptions2 = new RouteMatrixOptions&#40;matrixQuery3&#41;;
     * asyncClient.beginGetRouteMatrix&#40;matrixOptions2&#41;.blockFirst&#40;&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.begin_request_route_matrix -->
     *
     * @param options the {@code RouteMatrixOptions} applicable to this query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<RouteMatrixResult, RouteMatrixResult> beginGetRouteMatrix(RouteMatrixOptions options) {
        return this.beginGetRouteMatrix(options, null);
    }

    /**
     * Request Route Matrix
     * <!-- src_embed com.azure.maps.search.async.begin_request_route_matrix -->
     * <pre>
     * System.out.println&#40;&quot;Request route matrix&quot;&#41;;
     * RouteMatrixQuery matrixQuery3 = new RouteMatrixQuery&#40;&#41;;
     *
     * &#47;&#47; origins
     * GeoPointCollection origins3 = new GeoPointCollection&#40;Arrays.asList&#40;
     *     new GeoPoint&#40;4.85106, 52.36006&#41;,
     *     new GeoPoint&#40;4.85056, 52.36187&#41;
     * &#41;&#41;;
     *
     * &#47;&#47; destinations
     * GeoPointCollection destinations3 = new GeoPointCollection&#40;Arrays.asList&#40;
     *     new GeoPoint&#40;4.85003, 52.36241&#41;,
     *     new GeoPoint&#40;13.42937, 52.50931&#41;
     * &#41;&#41;;
     *
     * matrixQuery3.setDestinations&#40;destinations3&#41;;
     * matrixQuery3.setOrigins&#40;origins3&#41;;
     *
     * RouteMatrixOptions matrixOptions2 = new RouteMatrixOptions&#40;matrixQuery3&#41;;
     * asyncClient.beginGetRouteMatrix&#40;matrixOptions2&#41;.blockFirst&#40;&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.begin_request_route_matrix -->
     *
     * @param options the {@code RouteMatrixOptions} applicable to this query.
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    PollerFlux<RouteMatrixResult, RouteMatrixResult> beginGetRouteMatrix(
            RouteMatrixOptions options, Context context) {
        // if it's a small batch, then let's wait for results and avoid polling
        RouteMatrixQuery query = options.getRouteMatrixQuery();
        RouteMatrixQueryPrivate privateQuery = Utility.toRouteMatrixQueryPrivate(query);
        final int originSize = privateQuery.getOrigins().getCoordinates().size();
        final int destSize = privateQuery.getDestinations().getCoordinates().size();
        boolean waitForResults = (originSize * destSize <= ROUTE_MATRIX_SMALL_SIZE);

        return createPollerFlux(() -> this.serviceClient.requestRouteMatrixNoCustomHeadersWithResponseAsync(
            JsonFormat.JSON, privateQuery, waitForResults, options.getComputeTravelTime(),
                (options.getFilterSectionType() == null) ? null : singletonList(options.getFilterSectionType()),
                options.getArriveAt(), options.getDepartAt(), options.getVehicleAxleWeight(),
                options.getVehicleLength(), options.getVehicleHeight(), options.getVehicleWidth(),
                options.getVehicleMaxSpeed(), options.getVehicleWeight(), options.getWindingness(),
                options.getInclineLevel(), options.getTravelMode(), options.getAvoid(), options.getUseTrafficData(),
                options.getRouteType(), options.getVehicleLoadType(), context)
                .flatMap(response -> Mono.just(Utility.createRouteMatrixResponse(response)).onErrorMap(throwable -> {
                    if (!(throwable instanceof ErrorResponseException)) {
                        return throwable;
                    }
                    ErrorResponseException exception = (ErrorResponseException) throwable;
                    return new HttpResponseException(exception.getMessage(), exception.getResponse());
                })), this.routeMatrixStrategy);
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
        // TO DO Add null check and print if origin and destination multiply less than the max limit
        return createPollerFlux(() -> this.serviceClient
                .getRouteMatrixNoCustomHeadersWithResponseAsync(matrixId, context)
                .flatMap(response -> Mono.just(Utility.createRouteMatrixResponse(response)).onErrorMap(throwable -> {
                    if (!(throwable instanceof ErrorResponseException)) {
                        return throwable;
                    }
                    ErrorResponseException exception = (ErrorResponseException) throwable;
                    return new HttpResponseException(exception.getMessage(), exception.getResponse());
                })), this.routeMatrixStrategy);
    }

    /**
     * Get Route Directions
     * <!-- src_embed com.azure.maps.route.async.get_route_directions -->
     * <pre>
     * System.out.println&#40;&quot;Get route directions&quot;&#41;;
     * List&lt;GeoPosition&gt; routePoints2 = Arrays.asList&#40;
     *     new GeoPosition&#40;13.42936, 52.50931&#41;,
     *     new GeoPosition&#40;13.43872, 52.50274&#41;&#41;;
     * RouteDirectionsOptions routeOptions2 = new RouteDirectionsOptions&#40;routePoints2&#41;;
     * RouteDirections directions4 = asyncClient.getRouteDirections&#40;routeOptions2&#41;.block&#40;&#41;;
     * RouteReport report2 = directions4.getReport&#40;&#41;; &#47;&#47; get the report and use it
     * </pre>
     * <!-- end com.azure.maps.route.async.get_route_directions -->
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouteDirections> getRouteDirections(RouteDirectionsOptions options) {
        return getRouteDirectionsWithResponse(options).flatMap(FluxUtil::toMono);
    }

    /**
     * Get Route Directions
     * <!-- src_embed com.azure.maps.route.async.get_route_directions -->
     * <pre>
     * System.out.println&#40;&quot;Get route directions&quot;&#41;;
     * List&lt;GeoPosition&gt; routePoints2 = Arrays.asList&#40;
     *     new GeoPosition&#40;13.42936, 52.50931&#41;,
     *     new GeoPosition&#40;13.43872, 52.50274&#41;&#41;;
     * RouteDirectionsOptions routeOptions2 = new RouteDirectionsOptions&#40;routePoints2&#41;;
     * RouteDirections directions4 = asyncClient.getRouteDirections&#40;routeOptions2&#41;.block&#40;&#41;;
     * RouteReport report2 = directions4.getReport&#40;&#41;; &#47;&#47; get the report and use it
     * </pre>
     * <!-- end com.azure.maps.route.async.get_route_directions -->
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouteDirections>> getRouteDirectionsWithResponse(RouteDirectionsOptions options) {
        return withContext(context -> getRouteDirectionsWithContextWithResponse(options, context));
    }

    /**
     * Get Route Directions
     * <!-- src_embed com.azure.maps.route.async.get_route_directions -->
     * <pre>
     * System.out.println&#40;&quot;Get route directions&quot;&#41;;
     * List&lt;GeoPosition&gt; routePoints2 = Arrays.asList&#40;
     *     new GeoPosition&#40;13.42936, 52.50931&#41;,
     *     new GeoPosition&#40;13.43872, 52.50274&#41;&#41;;
     * RouteDirectionsOptions routeOptions2 = new RouteDirectionsOptions&#40;routePoints2&#41;;
     * RouteDirections directions4 = asyncClient.getRouteDirections&#40;routeOptions2&#41;.block&#40;&#41;;
     * RouteReport report2 = directions4.getReport&#40;&#41;; &#47;&#47; get the report and use it
     * </pre>
     * <!-- end com.azure.maps.route.async.get_route_directions -->
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    Mono<Response<RouteDirections>> getRouteDirectionsWithContextWithResponse(RouteDirectionsOptions options,
        Context context) {
        return this.serviceClient.getRouteDirectionsWithResponseAsync(ResponseFormat.JSON,
                Utility.toRouteQueryString(options.getRoutePoints()), options.getMaxAlternatives(),
                options.getAlternativeType(), options.getMinDeviationDistance(), options.getArriveAt(),
                options.getDepartAt(), options.getMinDeviationTime(), options.getInstructionsType(),
                options.getLanguage(), options.getComputeBestWaypointOrder(),
                options.getRouteRepresentationForBestOrder(), options.getComputeTravelTime(),
                options.getVehicleHeading(), options.getReport(),
                (options.getFilterSectionType() == null) ? null : singletonList(options.getFilterSectionType()),
                options.getVehicleAxleWeight(), options.getVehicleWidth(), options.getVehicleHeight(),
                options.getVehicleLength(), options.getVehicleMaxSpeed(), options.getVehicleWeight(),
                options.isCommercialVehicle(), options.getWindingness(), options.getInclineLevel(),
                options.getTravelMode(), options.getAvoidRouteTypes(), options.isGetUseTrafficData(),
                options.getRouteType(), options.getVehicleLoadType(), options.getVehicleEngineType(),
                options.getConstantSpeedConsumptionInLitersPerHundredKm(), options.getCurrentFuelInLiters(),
                options.getAuxiliaryPowerInLitersPerHour(), options.getFuelEnergyDensityInMegajoulesPerLiter(),
                options.getAccelerationEfficiency(), options.getDecelerationEfficiency(), options.getUphillEfficiency(),
                options.getDownhillEfficiency(), options.getConstantSpeedConsumptionInKwHPerHundredKm(),
                options.getCurrentChargeInKwH(), options.getMaxChargeInKwH(), options.getAuxiliaryPowerInKw(), context)
            .onErrorMap(throwable -> {
                if (!(throwable instanceof ErrorResponseException)) {
                    return throwable;
                }
                ErrorResponseException exception = (ErrorResponseException) throwable;
                return new HttpResponseException(exception.getMessage(), exception.getResponse());
            });
    }

    /**
     * Get Route Directions
     * <!-- src_embed com.azure.maps.route.async.get_route_directions_parameters -->
     * <pre>
     * System.out.println&#40;&quot;Get route parameters&quot;&#41;;
     * &#47;&#47; supporting points
     * GeoCollection supportingPoints2 = new GeoCollection&#40;
     *     Arrays.asList&#40;
     *         new GeoPoint&#40;13.42936, 52.5093&#41;,
     *         new GeoPoint&#40;13.42859, 52.50844&#41;
     *         &#41;&#41;;
     *
     * &#47;&#47; avoid areas
     * List&lt;GeoPolygon&gt; polygons2 = Arrays.asList&#40;
     *     new GeoPolygon&#40;
     *         new GeoLinearRing&#40;Arrays.asList&#40;
     *             new GeoPosition&#40;-122.39456176757811, 47.489368981370724&#41;,
     *             new GeoPosition&#40;-122.00454711914061, 47.489368981370724&#41;,
     *             new GeoPosition&#40;-122.00454711914061, 47.65151268066222&#41;,
     *             new GeoPosition&#40;-122.39456176757811, 47.65151268066222&#41;,
     *             new GeoPosition&#40;-122.39456176757811, 47.489368981370724&#41;
     *         &#41;&#41;
     *     &#41;,
     *     new GeoPolygon&#40;
     *         new GeoLinearRing&#40;Arrays.asList&#40;
     *             new GeoPosition&#40;100.0, 0.0&#41;,
     *             new GeoPosition&#40;101.0, 0.0&#41;,
     *             new GeoPosition&#40;101.0, 1.0&#41;,
     *             new GeoPosition&#40;100.0, 1.0&#41;,
     *             new GeoPosition&#40;100.0, 0.0&#41;
     *         &#41;&#41;
     *     &#41;
     * &#41;;
     * GeoPolygonCollection avoidAreas2 = new GeoPolygonCollection&#40;polygons2&#41;;
     * RouteDirectionsParameters parameters2 = new RouteDirectionsParameters&#40;&#41;
     *     .setSupportingPoints&#40;supportingPoints2&#41;
     *     .setAvoidVignette&#40;Arrays.asList&#40;&quot;AUS&quot;, &quot;CHE&quot;&#41;&#41;
     *     .setAvoidAreas&#40;avoidAreas2&#41;;
     * asyncClient.getRouteDirections&#40;routeOptions2,
     *     parameters2&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.route.async.get_route_directions_parameters -->
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param parameters the {@code RouteDirectionsParameters} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouteDirections> getRouteDirections(RouteDirectionsOptions options,
        RouteDirectionsParameters parameters) {
        return getRouteDirectionsWithResponse(options, parameters).flatMap(FluxUtil::toMono);
    }

    /**
     * Get Route Directions
     * <!-- src_embed com.azure.maps.route.async.get_route_directions_parameters -->
     * <pre>
     * System.out.println&#40;&quot;Get route parameters&quot;&#41;;
     * &#47;&#47; supporting points
     * GeoCollection supportingPoints2 = new GeoCollection&#40;
     *     Arrays.asList&#40;
     *         new GeoPoint&#40;13.42936, 52.5093&#41;,
     *         new GeoPoint&#40;13.42859, 52.50844&#41;
     *         &#41;&#41;;
     *
     * &#47;&#47; avoid areas
     * List&lt;GeoPolygon&gt; polygons2 = Arrays.asList&#40;
     *     new GeoPolygon&#40;
     *         new GeoLinearRing&#40;Arrays.asList&#40;
     *             new GeoPosition&#40;-122.39456176757811, 47.489368981370724&#41;,
     *             new GeoPosition&#40;-122.00454711914061, 47.489368981370724&#41;,
     *             new GeoPosition&#40;-122.00454711914061, 47.65151268066222&#41;,
     *             new GeoPosition&#40;-122.39456176757811, 47.65151268066222&#41;,
     *             new GeoPosition&#40;-122.39456176757811, 47.489368981370724&#41;
     *         &#41;&#41;
     *     &#41;,
     *     new GeoPolygon&#40;
     *         new GeoLinearRing&#40;Arrays.asList&#40;
     *             new GeoPosition&#40;100.0, 0.0&#41;,
     *             new GeoPosition&#40;101.0, 0.0&#41;,
     *             new GeoPosition&#40;101.0, 1.0&#41;,
     *             new GeoPosition&#40;100.0, 1.0&#41;,
     *             new GeoPosition&#40;100.0, 0.0&#41;
     *         &#41;&#41;
     *     &#41;
     * &#41;;
     * GeoPolygonCollection avoidAreas2 = new GeoPolygonCollection&#40;polygons2&#41;;
     * RouteDirectionsParameters parameters2 = new RouteDirectionsParameters&#40;&#41;
     *     .setSupportingPoints&#40;supportingPoints2&#41;
     *     .setAvoidVignette&#40;Arrays.asList&#40;&quot;AUS&quot;, &quot;CHE&quot;&#41;&#41;
     *     .setAvoidAreas&#40;avoidAreas2&#41;;
     * asyncClient.getRouteDirections&#40;routeOptions2,
     *     parameters2&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.route.async.get_route_directions_parameters -->
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param parameters the {@code RouteDirectionsParameters} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouteDirections>> getRouteDirectionsWithResponse(RouteDirectionsOptions options,
        RouteDirectionsParameters parameters) {
        return withContext(context -> getRouteDirectionsWithParametersWithResponse(options, parameters, context));
    }

    /**
     * Get Route Directions
     * <!-- src_embed com.azure.maps.route.async.get_route_directions_parameters -->
     * <pre>
     * System.out.println&#40;&quot;Get route parameters&quot;&#41;;
     * &#47;&#47; supporting points
     * GeoCollection supportingPoints2 = new GeoCollection&#40;
     *     Arrays.asList&#40;
     *         new GeoPoint&#40;13.42936, 52.5093&#41;,
     *         new GeoPoint&#40;13.42859, 52.50844&#41;
     *         &#41;&#41;;
     *
     * &#47;&#47; avoid areas
     * List&lt;GeoPolygon&gt; polygons2 = Arrays.asList&#40;
     *     new GeoPolygon&#40;
     *         new GeoLinearRing&#40;Arrays.asList&#40;
     *             new GeoPosition&#40;-122.39456176757811, 47.489368981370724&#41;,
     *             new GeoPosition&#40;-122.00454711914061, 47.489368981370724&#41;,
     *             new GeoPosition&#40;-122.00454711914061, 47.65151268066222&#41;,
     *             new GeoPosition&#40;-122.39456176757811, 47.65151268066222&#41;,
     *             new GeoPosition&#40;-122.39456176757811, 47.489368981370724&#41;
     *         &#41;&#41;
     *     &#41;,
     *     new GeoPolygon&#40;
     *         new GeoLinearRing&#40;Arrays.asList&#40;
     *             new GeoPosition&#40;100.0, 0.0&#41;,
     *             new GeoPosition&#40;101.0, 0.0&#41;,
     *             new GeoPosition&#40;101.0, 1.0&#41;,
     *             new GeoPosition&#40;100.0, 1.0&#41;,
     *             new GeoPosition&#40;100.0, 0.0&#41;
     *         &#41;&#41;
     *     &#41;
     * &#41;;
     * GeoPolygonCollection avoidAreas2 = new GeoPolygonCollection&#40;polygons2&#41;;
     * RouteDirectionsParameters parameters2 = new RouteDirectionsParameters&#40;&#41;
     *     .setSupportingPoints&#40;supportingPoints2&#41;
     *     .setAvoidVignette&#40;Arrays.asList&#40;&quot;AUS&quot;, &quot;CHE&quot;&#41;&#41;
     *     .setAvoidAreas&#40;avoidAreas2&#41;;
     * asyncClient.getRouteDirections&#40;routeOptions2,
     *     parameters2&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.route.async.get_route_directions_parameters -->
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param parameters the {@code RouteDirectionsParameters} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    Mono<Response<RouteDirections>> getRouteDirectionsWithParametersWithResponse(RouteDirectionsOptions options,
        RouteDirectionsParameters parameters, Context context) {
        return this.serviceClient.getRouteDirectionsWithAdditionalParametersWithResponseAsync(ResponseFormat.JSON,
                Utility.toRouteQueryString(options.getRoutePoints()),
                Utility.toRouteDirectionParametersPrivate(parameters), options.getMaxAlternatives(),
                options.getAlternativeType(), options.getMinDeviationDistance(), options.getMinDeviationTime(),
                options.getInstructionsType(), options.getLanguage(), options.getComputeBestWaypointOrder(),
                options.getRouteRepresentationForBestOrder(), options.getComputeTravelTime(),
                options.getVehicleHeading(), options.getReport(),
                (options.getFilterSectionType() == null) ? null : singletonList(options.getFilterSectionType()),
                options.getArriveAt(), options.getDepartAt(), options.getVehicleAxleWeight(),
                options.getVehicleLength(), options.getVehicleHeight(), options.getVehicleWidth(),
                options.getVehicleMaxSpeed(), options.getVehicleWeight(), options.isCommercialVehicle(),
                options.getWindingness(), options.getInclineLevel(), options.getTravelMode(),
                options.getAvoidRouteTypes(), options.isGetUseTrafficData(), options.getRouteType(),
                options.getVehicleLoadType(), options.getVehicleEngineType(),
                options.getConstantSpeedConsumptionInLitersPerHundredKm(), options.getCurrentFuelInLiters(),
                options.getAuxiliaryPowerInLitersPerHour(), options.getFuelEnergyDensityInMegajoulesPerLiter(),
                options.getAccelerationEfficiency(), options.getDecelerationEfficiency(), options.getUphillEfficiency(),
                options.getDownhillEfficiency(), options.getConstantSpeedConsumptionInKwHPerHundredKm(),
                options.getCurrentChargeInKwH(), options.getMaxChargeInKwH(), options.getAuxiliaryPowerInKw(), context)
            .onErrorMap(throwable -> {
                if (!(throwable instanceof ErrorResponseException)) {
                    return throwable;
                }
                ErrorResponseException exception = (ErrorResponseException) throwable;
                return new HttpResponseException(exception.getMessage(), exception.getResponse());
            });
    }

    /**
     * Get Route Range
     * <!-- src_embed com.azure.maps.search.async.route_range -->
     * <pre>
     * System.out.println&#40;&quot;Get route range&quot;&#41;;
     * RouteRangeOptions rangeOptions2 = new RouteRangeOptions&#40;new GeoPosition&#40;50.97452, 5.86605&#41;, Duration.ofSeconds&#40;6000&#41;&#41;;
     * asyncClient.getRouteRange&#40;rangeOptions2&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.route_range -->
     *
     * @param options the {@code RouteRangeOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Range call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RouteRangeResult> getRouteRange(RouteRangeOptions options) {
        return getRouteRangeWithResponse(options).flatMap(FluxUtil::toMono);
    }

    /**
     * Get Route Range
     * <!-- src_embed com.azure.maps.search.async.route_range -->
     * <pre>
     * System.out.println&#40;&quot;Get route range&quot;&#41;;
     * RouteRangeOptions rangeOptions2 = new RouteRangeOptions&#40;new GeoPosition&#40;50.97452, 5.86605&#41;, Duration.ofSeconds&#40;6000&#41;&#41;;
     * asyncClient.getRouteRange&#40;rangeOptions2&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.route_range -->
     *
     * @param options the {@code RouteRangeOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Range call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RouteRangeResult>> getRouteRangeWithResponse(RouteRangeOptions options) {
        return withContext(context -> getRouteRangeWithResponse(options, context));
    }

    /**
     * Get Route Range
     * <!-- src_embed com.azure.maps.search.async.route_range -->
     * <pre>
     * System.out.println&#40;&quot;Get route range&quot;&#41;;
     * RouteRangeOptions rangeOptions2 = new RouteRangeOptions&#40;new GeoPosition&#40;50.97452, 5.86605&#41;, Duration.ofSeconds&#40;6000&#41;&#41;;
     * asyncClient.getRouteRange&#40;rangeOptions2&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.route_range -->
     *
     * @param options the {@code RouteRangeOptions} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Range call.
     */
    Mono<Response<RouteRangeResult>> getRouteRangeWithResponse(RouteRangeOptions options, Context context) {
        GeoPosition startingPoint = options.getStartingPoint();
        List<Double> startingPointCoordinates = Arrays.asList(
            startingPoint.getLongitude(), startingPoint.getLatitude());

        return this.serviceClient.getRouteRangeWithResponseAsync(ResponseFormat.JSON, startingPointCoordinates,
            options.getFuelBudgetInLiters(), options.getEnergyBudgetInKwH(),
            (double) options.getTimeBudgetInSec().getSeconds(), options.getDistanceBudgetInMeters(),
            options.getDepartAt(), options.getRouteType(), options.isGetUseTrafficData(), options.getAvoidRouteTypes(),
            options.getTravelMode(), options.getInclineLevel(), options.getWindingness(),
            options.getVehicleAxleWeight(), options.getVehicleWidth(), options.getVehicleHeight(),
            options.getVehicleLength(), options.getVehicleMaxSpeed(), options.getVehicleWeight(),
            options.isCommercialVehicle(), options.getVehicleLoadType(), options.getVehicleEngineType(),
            options.getConstantSpeedConsumptionInKwHPerHundredKm(), options.getCurrentFuelInLiters(),
            options.getAuxiliaryPowerInLitersPerHour(), options.getFuelEnergyDensityInMegajoulesPerLiter(),
            options.getAccelerationEfficiency(), options.getDecelerationEfficiency(), options.getUphillEfficiency(),
            options.getDownhillEfficiency(), options.getConstantSpeedConsumptionInKwHPerHundredKm(),
            options.getCurrentChargeInKwH(), options.getMaxChargeInKwH(), options.getAuxiliaryPowerInKw(), context)
            .onErrorMap(throwable -> {
                if (!(throwable instanceof ErrorResponseException)) {
                    return throwable;
                }
                ErrorResponseException exception = (ErrorResponseException) throwable;
                return new HttpResponseException(exception.getMessage(), exception.getResponse());
            });
    }

    /**
     * Get Route Directions Batch
     * <!-- src_embed com.azure.maps.search.async.begin_request_route_directions_batch -->
     * <pre>
     * RouteDirectionsOptions options5 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-122.128384, 47.639987&#41;,
     *         new GeoPosition&#40;-122.184408, 47.621252&#41;,
     *         new GeoPosition&#40;-122.332000, 47.596437&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.FASTEST&#41;
     *     .setTravelMode&#40;TravelMode.CAR&#41;
     *     .setMaxAlternatives&#40;5&#41;;
     *
     * RouteDirectionsOptions options6 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-122.348934, 47.620659&#41;,
     *         new GeoPosition&#40;-122.342015, 47.610101&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.ECONOMY&#41;
     *     .setTravelMode&#40;TravelMode.BICYCLE&#41;
     *     .setUseTrafficData&#40;false&#41;;
     *
     * RouteDirectionsOptions options7 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-73.985108, 40.759856&#41;,
     *         new GeoPosition&#40;-73.973506, 40.771136&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.SHORTEST&#41;
     *     .setTravelMode&#40;TravelMode.PEDESTRIAN&#41;;
     *
     * System.out.println&#40;&quot;Get Route Directions Batch&quot;&#41;;
     *
     * List&lt;RouteDirectionsOptions&gt; optionsList2 = Arrays.asList&#40;options5, options6, options7&#41;;
     * SyncPoller&lt;RouteDirectionsBatchResult, RouteDirectionsBatchResult&gt; poller2 =
     *     asyncClient.beginRequestRouteDirectionsBatch&#40;optionsList2&#41;.getSyncPoller&#40;&#41;;
     * poller2.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.begin_request_route_directions_batch -->
     *
     * @param optionsList the list of {@code RouteDirectionsOptions} used in this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<RouteDirectionsBatchResult, RouteDirectionsBatchResult> beginRequestRouteDirectionsBatch(
        List<RouteDirectionsOptions> optionsList) {
        return this.beginRequestRouteDirectionsBatch(optionsList, null);
    }

    /**
     * Get Route Directions Batch
     * <!-- src_embed com.azure.maps.search.async.begin_request_route_directions_batch -->
     * <pre>
     * RouteDirectionsOptions options5 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-122.128384, 47.639987&#41;,
     *         new GeoPosition&#40;-122.184408, 47.621252&#41;,
     *         new GeoPosition&#40;-122.332000, 47.596437&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.FASTEST&#41;
     *     .setTravelMode&#40;TravelMode.CAR&#41;
     *     .setMaxAlternatives&#40;5&#41;;
     *
     * RouteDirectionsOptions options6 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-122.348934, 47.620659&#41;,
     *         new GeoPosition&#40;-122.342015, 47.610101&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.ECONOMY&#41;
     *     .setTravelMode&#40;TravelMode.BICYCLE&#41;
     *     .setUseTrafficData&#40;false&#41;;
     *
     * RouteDirectionsOptions options7 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-73.985108, 40.759856&#41;,
     *         new GeoPosition&#40;-73.973506, 40.771136&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.SHORTEST&#41;
     *     .setTravelMode&#40;TravelMode.PEDESTRIAN&#41;;
     *
     * System.out.println&#40;&quot;Get Route Directions Batch&quot;&#41;;
     *
     * List&lt;RouteDirectionsOptions&gt; optionsList2 = Arrays.asList&#40;options5, options6, options7&#41;;
     * SyncPoller&lt;RouteDirectionsBatchResult, RouteDirectionsBatchResult&gt; poller2 =
     *     asyncClient.beginRequestRouteDirectionsBatch&#40;optionsList2&#41;.getSyncPoller&#40;&#41;;
     * poller2.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.begin_request_route_directions_batch -->
     *
     * @param optionsList the list of {@code RouteDirectionsOptions} used in this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    PollerFlux<RouteDirectionsBatchResult, RouteDirectionsBatchResult> beginRequestRouteDirectionsBatch(
        List<RouteDirectionsOptions> optionsList, Context context) {
        Objects.requireNonNull(optionsList, "'optionsList' is a required parameter.");

        // convert list to batch request
        List<BatchRequestItem> items = optionsList.stream()
            .map(Utility::toRouteDirectionsBatchItem)
            .collect(Collectors.toList());
        BatchRequest batchRequest = new BatchRequest().setBatchItems(items);

        if (batchRequest.getBatchItems().size() <= ROUTE_DIRECTIONS_SMALL_SIZE) {
            return createDirectionsPollerFlux(() -> this.serviceClient.requestRouteDirectionsBatchSyncWithResponseAsync(
                JsonFormat.JSON, batchRequest, context).flatMap(response ->
                    Mono.just(Utility.createRouteDirectionsResponse(response)).onErrorMap(throwable -> {
                        if (!(throwable instanceof ErrorResponseException)) {
                            return throwable;
                        }
                        ErrorResponseException exception = (ErrorResponseException) throwable;
                        return new HttpResponseException(exception.getMessage(), exception.getResponse());
                    })), this.forwardStrategy);
        } else {
            return createDirectionsPollerFlux(() -> this.serviceClient
                    .requestRouteDirectionsBatchNoCustomHeadersWithResponseAsync(JsonFormat.JSON, batchRequest, context)
                    .flatMap(response -> Mono.just(Utility.createRouteDirectionsResponse(response))
                        .onErrorMap(throwable -> {
                            if (!(throwable instanceof ErrorResponseException)) {
                                return throwable;
                            }
                            ErrorResponseException exception = (ErrorResponseException) throwable;
                            return new HttpResponseException(exception.getMessage(), exception.getResponse());
                        })), this.forwardStrategy);
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
        return createDirectionsPollerFlux(() -> this.serviceClient
                .getRouteDirectionsBatchNoCustomHeadersWithResponseAsync(batchId, context).flatMap(response ->
                    Mono.just(Utility.createRouteDirectionsResponse(response)).onErrorMap(throwable -> {
                        if (!(throwable instanceof ErrorResponseException)) {
                            return throwable;
                        }
                        ErrorResponseException exception = (ErrorResponseException) throwable;
                        return new HttpResponseException(exception.getMessage(), exception.getResponse());
                    })), this.forwardStrategy);
    }

    // private utility methods
    private PollerFlux<RouteMatrixResult, RouteMatrixResult> createPollerFlux(
        Supplier<Mono<? extends Response<?>>> initialOperation,
        DefaultPollingStrategy<RouteMatrixResult, RouteMatrixResult> strategy) {

        // type reference
        RouteMatrixReference typeReference = new RouteMatrixReference();

        // Create poller instance
        return PollerFlux.create(Duration.ofSeconds(POLLING_FREQUENCY), context -> initialOperation.get()
                .flatMap(response -> strategy.canPoll(response).flatMap(canPoll -> {
                    if (!canPoll) {
                        return Mono.error(new IllegalStateException(
                            "Cannot poll with strategy " + strategy.getClass().getSimpleName()));
                    }
                    context.setData(POLLING_BATCH_HEADER_KEY, Utility.getBatchId(response.getHeaders()));
                    return strategy.onInitialResponse(response, context, typeReference);
                })), context -> strategy.poll(context, typeReference), strategy::cancel,
            context -> strategy.getResult(context, typeReference)
                .flatMap(result -> {
                    final String matrixId = context.getData(POLLING_BATCH_HEADER_KEY);
                    result.setMatrixId(matrixId);
                    return Mono.just(result);
                }));
    }

    private PollerFlux<RouteDirectionsBatchResult, RouteDirectionsBatchResult> createDirectionsPollerFlux(
        Supplier<Mono<? extends Response<?>>> initialOperation,
        DefaultPollingStrategy<RouteDirectionsBatchResult, RouteDirectionsBatchResult> strategy) {

        // batch directions type reference
        RouteDirectionsBatchReference typeReference = new RouteDirectionsBatchReference();

        // Create poller instance
        return PollerFlux.create(Duration.ofSeconds(POLLING_FREQUENCY), context -> initialOperation.get()
                .flatMap(response -> strategy.canPoll(response).flatMap(canPoll -> {
                    if (!canPoll) {
                        return Mono.error(new IllegalStateException(
                            "Cannot poll with strategy " + strategy.getClass().getSimpleName()));
                    }
                    context.setData(POLLING_BATCH_HEADER_KEY, Utility.getBatchId(response.getHeaders()));
                    return strategy.onInitialResponse(response, context, typeReference);
                })), context -> strategy.poll(context, typeReference), strategy::cancel,
            context -> strategy.getResult(context, typeReference)
                .flatMap(result -> {
                    final String batchId = context.getData(POLLING_BATCH_HEADER_KEY);
                    result.setBatchId(batchId);
                    return Mono.just(result);
                }));
    }

}
