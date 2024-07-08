// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.maps.route.implementation.models.ErrorResponseException;
import com.azure.maps.route.models.RouteDirections;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteDirectionsOptions;
import com.azure.maps.route.models.RouteDirectionsParameters;
import com.azure.maps.route.models.RouteMatrixOptions;
import com.azure.maps.route.models.RouteMatrixResult;
import com.azure.maps.route.models.RouteRangeOptions;
import com.azure.maps.route.models.RouteRangeResult;

import java.util.List;

/**
 * Initializes a new instance of the synchronous RouteClient type.
 * Creating a sync client using a {@link AzureKeyCredential}:
 * <!-- src_embed com.azure.maps.route.sync.builder.key.instantiation -->
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
 * MapsRouteClient client = builder.buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.maps.route.sync.builder.key.instantiation -->
 * Creating a sync client using a {@link TokenCredential}:
 * <!-- src_embed com.azure.maps.route.sync.builder.ad.instantiation -->
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
 * MapsRouteClient client = builder.buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.maps.route.sync.builder.ad.instantiation -->
*/
@ServiceClient(builder = MapsRouteClientBuilder.class)
public final class MapsRouteClient {
    @Generated private final MapsRouteAsyncClient asyncClient;

    /**
     * Initializes an instance of Routes client.
     *
     * @param asyncClient the service client implementation.
     */
    MapsRouteClient(MapsRouteAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Request Route Matrix
     * <!-- src_embed com.azure.maps.search.sync.begin_request_route_matrix -->
     * <pre>
     * System.out.println&#40;&quot;Request route matrix&quot;&#41;;
     * RouteMatrixQuery matrixQuery = new RouteMatrixQuery&#40;&#41;;
     *
     * &#47;&#47; origins
     * GeoPointCollection origins = new GeoPointCollection&#40;Arrays.asList&#40;
     *     new GeoPoint&#40;4.85106, 52.36006&#41;,
     *     new GeoPoint&#40;4.85056, 52.36187&#41;
     * &#41;&#41;;
     *
     * &#47;&#47; destinations
     * GeoPointCollection destinations = new GeoPointCollection&#40;Arrays.asList&#40;
     *     new GeoPoint&#40;4.85003, 52.36241&#41;,
     *     new GeoPoint&#40;13.42937, 52.50931&#41;
     * &#41;&#41;;
     *
     * matrixQuery.setDestinations&#40;destinations&#41;;
     * matrixQuery.setOrigins&#40;origins&#41;;
     *
     * RouteMatrixOptions matrixOptions = new RouteMatrixOptions&#40;matrixQuery&#41;;
     * client.beginGetRouteMatrix&#40;matrixOptions&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.begin_request_route_matrix -->
     *
     * @param options the {@code RouteMatrixOptions} applicable to this query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteMatrixResult, RouteMatrixResult> beginGetRouteMatrix(RouteMatrixOptions options) {
        return this.asyncClient.beginGetRouteMatrix(options).getSyncPoller();
    }

    /**
     * Request Route Matrix
     * <!-- src_embed com.azure.maps.search.sync.begin_request_route_matrix -->
     * <pre>
     * System.out.println&#40;&quot;Request route matrix&quot;&#41;;
     * RouteMatrixQuery matrixQuery = new RouteMatrixQuery&#40;&#41;;
     *
     * &#47;&#47; origins
     * GeoPointCollection origins = new GeoPointCollection&#40;Arrays.asList&#40;
     *     new GeoPoint&#40;4.85106, 52.36006&#41;,
     *     new GeoPoint&#40;4.85056, 52.36187&#41;
     * &#41;&#41;;
     *
     * &#47;&#47; destinations
     * GeoPointCollection destinations = new GeoPointCollection&#40;Arrays.asList&#40;
     *     new GeoPoint&#40;4.85003, 52.36241&#41;,
     *     new GeoPoint&#40;13.42937, 52.50931&#41;
     * &#41;&#41;;
     *
     * matrixQuery.setDestinations&#40;destinations&#41;;
     * matrixQuery.setOrigins&#40;origins&#41;;
     *
     * RouteMatrixOptions matrixOptions = new RouteMatrixOptions&#40;matrixQuery&#41;;
     * client.beginGetRouteMatrix&#40;matrixOptions&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.begin_request_route_matrix -->
     *
     * @param options the {@code RouteMatrixOptions} applicable to this query.
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Matrix call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteMatrixResult, RouteMatrixResult> beginGetRouteMatrix(RouteMatrixOptions options,
        Context context) {
        return this.asyncClient.beginGetRouteMatrix(options, context).getSyncPoller();
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
    public SyncPoller<RouteMatrixResult, RouteMatrixResult> beginGetRouteMatrix(String matrixId) {
        return this.asyncClient.beginGetRouteMatrix(matrixId).getSyncPoller();
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
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteMatrixResult, RouteMatrixResult> beginGetRouteMatrix(String matrixId, Context context) {
        return this.asyncClient.beginGetRouteMatrix(matrixId, context).getSyncPoller();
    }

    /**
     * Get Route Directions
     *
     * <!-- src_embed com.azure.maps.route.sync.get_route_directions -->
     * <pre>
     * System.out.println&#40;&quot;Get route directions&quot;&#41;;
     * List&lt;GeoPosition&gt; routePoints = Arrays.asList&#40;
     *     new GeoPosition&#40;13.42936, 52.50931&#41;,
     *     new GeoPosition&#40;13.43872, 52.50274&#41;&#41;;
     * RouteDirectionsOptions routeOptions = new RouteDirectionsOptions&#40;routePoints&#41;;
     * RouteDirections directions = client.getRouteDirections&#40;routeOptions&#41;;
     * RouteReport report = directions.getReport&#40;&#41;; &#47;&#47; get the report and use it
     * </pre>
     * <!-- end com.azure.maps.route.sync.get_route_directions -->
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouteDirections getRouteDirections(RouteDirectionsOptions options) {
        return this.asyncClient.getRouteDirections(options).block();
    }

    /**
     * Get Route Directions
     * <!-- src_embed com.azure.maps.route.sync.get_route_directions -->
     * <pre>
     * System.out.println&#40;&quot;Get route directions&quot;&#41;;
     * List&lt;GeoPosition&gt; routePoints = Arrays.asList&#40;
     *     new GeoPosition&#40;13.42936, 52.50931&#41;,
     *     new GeoPosition&#40;13.43872, 52.50274&#41;&#41;;
     * RouteDirectionsOptions routeOptions = new RouteDirectionsOptions&#40;routePoints&#41;;
     * RouteDirections directions = client.getRouteDirections&#40;routeOptions&#41;;
     * RouteReport report = directions.getReport&#40;&#41;; &#47;&#47; get the report and use it
     * </pre>
     * <!-- end com.azure.maps.route.sync.get_route_directions -->
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouteDirections> getRouteDirectionsWithResponse(RouteDirectionsOptions options, Context context) {
        return this.asyncClient.getRouteDirectionsWithContextWithResponse(options, context).block();
    }

    /**
     * Get Route Directions
     * <!-- src_embed com.azure.maps.route.sync.get_route_directions_parameters -->
     * <pre>
     * System.out.println&#40;&quot;Get route parameters&quot;&#41;;
     * &#47;&#47; supporting points
     * GeoCollection supportingPoints = new GeoCollection&#40;
     *     Arrays.asList&#40;
     *         new GeoPoint&#40;13.42936, 52.5093&#41;,
     *         new GeoPoint&#40;13.42859, 52.50844&#41;
     *         &#41;&#41;;
     *
     * &#47;&#47; avoid areas
     * List&lt;GeoPolygon&gt; polygons = Arrays.asList&#40;
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
     * GeoPolygonCollection avoidAreas = new GeoPolygonCollection&#40;polygons&#41;;
     * RouteDirectionsParameters parameters = new RouteDirectionsParameters&#40;&#41;
     *     .setSupportingPoints&#40;supportingPoints&#41;
     *     .setAvoidVignette&#40;Arrays.asList&#40;&quot;AUS&quot;, &quot;CHE&quot;&#41;&#41;
     *     .setAvoidAreas&#40;avoidAreas&#41;;
     * client.getRouteDirections&#40;routeOptions,
     *     parameters&#41;;
     * </pre>
     * <!-- end com.azure.maps.route.sync.get_route_directions_parameters -->
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param parameters the {@code RouteDirectionsParameters} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouteDirections getRouteDirections(RouteDirectionsOptions options, RouteDirectionsParameters parameters) {
        return this.asyncClient.getRouteDirections(options, parameters).block();
    }

    /**
     * Get Route Directions
     * <!-- src_embed com.azure.maps.route.sync.get_route_directions_parameters -->
     * <pre>
     * System.out.println&#40;&quot;Get route parameters&quot;&#41;;
     * &#47;&#47; supporting points
     * GeoCollection supportingPoints = new GeoCollection&#40;
     *     Arrays.asList&#40;
     *         new GeoPoint&#40;13.42936, 52.5093&#41;,
     *         new GeoPoint&#40;13.42859, 52.50844&#41;
     *         &#41;&#41;;
     *
     * &#47;&#47; avoid areas
     * List&lt;GeoPolygon&gt; polygons = Arrays.asList&#40;
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
     * GeoPolygonCollection avoidAreas = new GeoPolygonCollection&#40;polygons&#41;;
     * RouteDirectionsParameters parameters = new RouteDirectionsParameters&#40;&#41;
     *     .setSupportingPoints&#40;supportingPoints&#41;
     *     .setAvoidVignette&#40;Arrays.asList&#40;&quot;AUS&quot;, &quot;CHE&quot;&#41;&#41;
     *     .setAvoidAreas&#40;avoidAreas&#41;;
     * client.getRouteDirections&#40;routeOptions,
     *     parameters&#41;;
     * </pre>
     * <!-- end com.azure.maps.route.sync.get_route_directions_parameters -->
     *
     * @param options the {@code RouteDirectionsOptions} applicable to this query
     * @param parameters the {@code RouteDirectionsParameters} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouteDirections> getRouteDirectionsWithResponse(RouteDirectionsOptions options,
        RouteDirectionsParameters parameters, Context context) {
        return this.asyncClient.getRouteDirectionsWithParametersWithResponse(
            options, parameters, context).block();
    }

    /**
     * Get Route Range
     * <!-- src_embed com.azure.maps.search.sync.route_range -->
     * <pre>
     * System.out.println&#40;&quot;Get route range&quot;&#41;;
     * RouteRangeOptions rangeOptions = new RouteRangeOptions&#40;new GeoPosition&#40;50.97452, 5.86605&#41;, Duration.ofSeconds&#40;6000&#41;&#41;;
     * client.getRouteRange&#40;rangeOptions&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.route_range -->
     *
     * @param options the {@code RouteRangeOptions} applicable to this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Range call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RouteRangeResult getRouteRange(RouteRangeOptions options) {
        return this.asyncClient.getRouteRange(options).block();
    }

    /**
     * Get Route Range
     * <!-- src_embed com.azure.maps.search.sync.route_range -->
     * <pre>
     * System.out.println&#40;&quot;Get route range&quot;&#41;;
     * RouteRangeOptions rangeOptions = new RouteRangeOptions&#40;new GeoPosition&#40;50.97452, 5.86605&#41;, Duration.ofSeconds&#40;6000&#41;&#41;;
     * client.getRouteRange&#40;rangeOptions&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.route_range -->
     *
     * @param options the {@code RouteRangeOptions} applicable to this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Range call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RouteRangeResult> getRouteRangeWithResponse(RouteRangeOptions options, Context context) {
        return this.asyncClient.getRouteRangeWithResponse(options, context).block();
    }

    /**
     * Get Route Directions Batch
     * <!-- src_embed com.azure.maps.search.sync.begin_request_route_directions_batch -->
     * <pre>
     * RouteDirectionsOptions options1 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-122.128384, 47.639987&#41;,
     *         new GeoPosition&#40;-122.184408, 47.621252&#41;,
     *         new GeoPosition&#40;-122.332000, 47.596437&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.FASTEST&#41;
     *     .setTravelMode&#40;TravelMode.CAR&#41;
     *     .setMaxAlternatives&#40;5&#41;;
     *
     * RouteDirectionsOptions options2 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-122.348934, 47.620659&#41;,
     *         new GeoPosition&#40;-122.342015, 47.610101&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.ECONOMY&#41;
     *     .setTravelMode&#40;TravelMode.BICYCLE&#41;
     *     .setUseTrafficData&#40;false&#41;;
     *
     * RouteDirectionsOptions options3 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-73.985108, 40.759856&#41;,
     *         new GeoPosition&#40;-73.973506, 40.771136&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.SHORTEST&#41;
     *     .setTravelMode&#40;TravelMode.PEDESTRIAN&#41;;
     *
     * System.out.println&#40;&quot;Get Route Directions Batch&quot;&#41;;
     *
     * List&lt;RouteDirectionsOptions&gt; optionsList = Arrays.asList&#40;options1, options2, options3&#41;;
     * SyncPoller&lt;RouteDirectionsBatchResult, RouteDirectionsBatchResult&gt; poller =
     *     client.beginRequestRouteDirectionsBatch&#40;optionsList&#41;;
     * poller.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.begin_request_route_directions_batch -->
     *
     * @param optionsList the list of {@code RouteDirectionsOptions} used in this query
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> beginRequestRouteDirectionsBatch(
        List<RouteDirectionsOptions> optionsList) {
        return this.beginRequestRouteDirectionsBatch(optionsList, null);
    }

    /**
     * Get Route Directions Batch
     * <!-- src_embed com.azure.maps.search.sync.begin_request_route_directions_batch -->
     * <pre>
     * RouteDirectionsOptions options1 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-122.128384, 47.639987&#41;,
     *         new GeoPosition&#40;-122.184408, 47.621252&#41;,
     *         new GeoPosition&#40;-122.332000, 47.596437&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.FASTEST&#41;
     *     .setTravelMode&#40;TravelMode.CAR&#41;
     *     .setMaxAlternatives&#40;5&#41;;
     *
     * RouteDirectionsOptions options2 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-122.348934, 47.620659&#41;,
     *         new GeoPosition&#40;-122.342015, 47.610101&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.ECONOMY&#41;
     *     .setTravelMode&#40;TravelMode.BICYCLE&#41;
     *     .setUseTrafficData&#40;false&#41;;
     *
     * RouteDirectionsOptions options3 = new RouteDirectionsOptions&#40;
     *     Arrays.asList&#40;new GeoPosition&#40;-73.985108, 40.759856&#41;,
     *         new GeoPosition&#40;-73.973506, 40.771136&#41;&#41;&#41;
     *     .setRouteType&#40;RouteType.SHORTEST&#41;
     *     .setTravelMode&#40;TravelMode.PEDESTRIAN&#41;;
     *
     * System.out.println&#40;&quot;Get Route Directions Batch&quot;&#41;;
     *
     * List&lt;RouteDirectionsOptions&gt; optionsList = Arrays.asList&#40;options1, options2, options3&#41;;
     * SyncPoller&lt;RouteDirectionsBatchResult, RouteDirectionsBatchResult&gt; poller =
     *     client.beginRequestRouteDirectionsBatch&#40;optionsList&#41;;
     * poller.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.begin_request_route_directions_batch -->
     *
     * @param optionsList the list of {@code RouteDirectionsOptions} used in this query
     * @param context the context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Route Directions call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> beginRequestRouteDirectionsBatch(
        List<RouteDirectionsOptions> optionsList, Context context) {
        return this.asyncClient
            .beginRequestRouteDirectionsBatch(optionsList, context).getSyncPoller();
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
    public SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> beginGetRouteDirectionsBatch(
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
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<RouteDirectionsBatchResult, RouteDirectionsBatchResult> beginGetRouteDirectionsBatch(
        String batchId, Context context) {
        return this.asyncClient.beginGetRouteDirectionsBatch(batchId, context).getSyncPoller();
    }
}
