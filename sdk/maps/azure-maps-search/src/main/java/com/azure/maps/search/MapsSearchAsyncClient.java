// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.polling.DefaultPollingStrategy;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.search.implementation.SearchesImpl;
import com.azure.maps.search.implementation.helpers.BatchResponseSerializer;
import com.azure.maps.search.implementation.helpers.Utility;
import com.azure.maps.search.implementation.models.BatchRequest;
import com.azure.maps.search.implementation.models.BatchRequestItem;
import com.azure.maps.search.implementation.models.ErrorResponseException;
import com.azure.maps.search.implementation.models.GeoJsonLineString;
import com.azure.maps.search.implementation.models.GeoJsonObject;
import com.azure.maps.search.implementation.models.JsonFormat;
import com.azure.maps.search.implementation.models.ResponseFormat;
import com.azure.maps.search.implementation.models.SearchAlongRouteRequest;
import com.azure.maps.search.implementation.models.SearchInsideGeometryRequest;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.FuzzySearchOptions;
import com.azure.maps.search.models.MapsPolygon;
import com.azure.maps.search.models.PointOfInterestCategoryTreeResult;
import com.azure.maps.search.models.ReverseSearchAddressOptions;
import com.azure.maps.search.models.ReverseSearchAddressResult;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressOptions;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressResult;
import com.azure.maps.search.models.SearchAddressOptions;
import com.azure.maps.search.models.SearchAddressResult;
import com.azure.maps.search.models.SearchAlongRouteOptions;
import com.azure.maps.search.models.SearchInsideGeometryOptions;
import com.azure.maps.search.models.SearchNearbyPointsOfInterestOptions;
import com.azure.maps.search.models.SearchPointOfInterestCategoryOptions;
import com.azure.maps.search.models.SearchPointOfInterestOptions;
import com.azure.maps.search.models.SearchStructuredAddressOptions;
import com.azure.maps.search.models.StructuredAddress;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/** Initializes a new instance of the asynchronous SearchClient type.
* Creating an async client using a {@link com.azure.core.credential.AzureKeyCredential}:
* <!-- src_embed com.azure.maps.search.async.builder.key.instantiation -->
* <pre>
* &#47;&#47; Authenticates using subscription key
* AzureKeyCredential keyCredential = new AzureKeyCredential&#40;System.getenv&#40;&quot;SUBSCRIPTION_KEY&quot;&#41;&#41;;
*
* &#47;&#47; Creates a builder
* MapsSearchClientBuilder builder = new MapsSearchClientBuilder&#40;&#41;;
* builder.credential&#40;keyCredential&#41;;
* builder.httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;;
*
* &#47;&#47; Builds the client
* MapsSearchAsyncClient client = builder.buildAsyncClient&#40;&#41;;
* </pre>
* <!-- end com.azure.maps.search.async.builder.key.instantiation -->
* Creating an async client using a {@link com.azure.core.credential.TokenCredential}:
* <!-- src_embed com.azure.maps.search.async.builder.ad.instantiation -->
* <pre>
* &#47;&#47; Authenticates using Azure AD building a default credential
* &#47;&#47; This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
* DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder&#40;&#41;.build&#40;&#41;;
*
* &#47;&#47; Creates a builder
* MapsSearchClientBuilder builder = new MapsSearchClientBuilder&#40;&#41;;
* builder.credential&#40;tokenCredential&#41;;
* builder.mapsClientId&#40;System.getenv&#40;&quot;MAPS_CLIENT_ID&quot;&#41;&#41;;
* builder.httpLogOptions&#40;new HttpLogOptions&#40;&#41;.setLogLevel&#40;HttpLogDetailLevel.BODY_AND_HEADERS&#41;&#41;;
*
* &#47;&#47; Builds a client
* MapsSearchAsyncClient client = builder.buildAsyncClient&#40;&#41;;
* </pre>
* <!-- end com.azure.maps.search.async.builder.ad.instantiation -->
*/

@ServiceClient(builder = MapsSearchClientBuilder.class, isAsync = true)
public final class MapsSearchAsyncClient {
    // constants
    private static final int BATCH_SIZE = 100;
    private static final int POLLING_FREQUENCY = 1;
    private static final String POLLING_BATCH_HEADER_KEY = "BatchId";

    // reference static classes
    static class BatchSearchTypeReference extends TypeReference<BatchSearchResult> { };
    static class ReverseBatchSearchTypeReference extends TypeReference<BatchReverseSearchResult> { };

    // instance fields
    private final SearchesImpl serviceClient;
    private final HttpPipeline httpPipeline;
    private final BatchResponseSerializer serializer;
    private final DefaultPollingStrategy<BatchSearchResult, BatchSearchResult> forwardStrategy;
    private final DefaultPollingStrategy<BatchReverseSearchResult, BatchReverseSearchResult> reverseStrategy;

    /**
     * Initializes an instance of Searches client.
     *
     * @param serviceClient the service client implementation.
     */
    MapsSearchAsyncClient(SearchesImpl serviceClient, HttpPipeline pipeline) {
        this.serviceClient = serviceClient;
        this.httpPipeline = pipeline;
        this.serializer = new BatchResponseSerializer();
        this.forwardStrategy = new DefaultPollingStrategy<>(httpPipeline, serializer);
        this.reverseStrategy = new DefaultPollingStrategy<>(httpPipeline, serializer);
    }

    /**
     * List Polygons
     * <!-- src_embed com.azure.maps.search.async.get_polygon -->
     * <pre>
     * Response&lt;SearchAddressResult&gt; fuzzySearchResponse = asyncClient.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     * String fuzzySearchId = fuzzySearchResponse.getValue&#40;&#41;.getResults&#40;&#41;.get&#40;0&#41;.getDataSource&#40;&#41;.getGeometry&#40;&#41;;
     * List&lt;String&gt; getPolygonIds = results.getResults&#40;&#41;.stream&#40;&#41;
     *     .filter&#40;item -&gt; item.getDataSource&#40;&#41; != null &amp;&amp; item.getDataSource&#40;&#41;.getGeometry&#40;&#41; != null&#41;
     *     .map&#40;item -&gt; item.getDataSource&#40;&#41;.getGeometry&#40;&#41;&#41;
     *     .collect&#40;Collectors.toList&#40;&#41;&#41;;
     * getPolygonIds.add&#40;fuzzySearchId&#41;;
     *
     * if &#40;ids != null &amp;&amp; !getPolygonIds.isEmpty&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;Get Polygon: &quot; + ids&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.maps.search.async.get_polygon -->
     *
     * @param geometryIds Comma separated list of geometry UUIDs, previously retrieved from an Online Search request.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Polygon call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<MapsPolygon>> getPolygons(List<String> geometryIds) {
        return getPolygonsWithResponse(geometryIds).flatMap(FluxUtil::toMono);
    }

    /**
     * List Polygons
     * <!-- src_embed com.azure.maps.search.async.get_polygon -->
     * <pre>
     * Response&lt;SearchAddressResult&gt; fuzzySearchResponse = asyncClient.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     * String fuzzySearchId = fuzzySearchResponse.getValue&#40;&#41;.getResults&#40;&#41;.get&#40;0&#41;.getDataSource&#40;&#41;.getGeometry&#40;&#41;;
     * List&lt;String&gt; getPolygonIds = results.getResults&#40;&#41;.stream&#40;&#41;
     *     .filter&#40;item -&gt; item.getDataSource&#40;&#41; != null &amp;&amp; item.getDataSource&#40;&#41;.getGeometry&#40;&#41; != null&#41;
     *     .map&#40;item -&gt; item.getDataSource&#40;&#41;.getGeometry&#40;&#41;&#41;
     *     .collect&#40;Collectors.toList&#40;&#41;&#41;;
     * getPolygonIds.add&#40;fuzzySearchId&#41;;
     *
     * if &#40;ids != null &amp;&amp; !getPolygonIds.isEmpty&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;Get Polygon: &quot; + ids&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.maps.search.async.get_polygon -->
     *
     * @param geometryIds Comma separated list of geometry UUIDs, previously retrieved from an Online Search request.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Polygon call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<List<MapsPolygon>>> getPolygonsWithResponse(List<String> geometryIds) {
        return withContext(context -> getPolygonsWithResponse(geometryIds, context));
    }

    /**
     * List Polygons
     * <!-- src_embed com.azure.maps.search.async.get_polygon -->
     * <pre>
     * Response&lt;SearchAddressResult&gt; fuzzySearchResponse = asyncClient.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     * String fuzzySearchId = fuzzySearchResponse.getValue&#40;&#41;.getResults&#40;&#41;.get&#40;0&#41;.getDataSource&#40;&#41;.getGeometry&#40;&#41;;
     * List&lt;String&gt; getPolygonIds = results.getResults&#40;&#41;.stream&#40;&#41;
     *     .filter&#40;item -&gt; item.getDataSource&#40;&#41; != null &amp;&amp; item.getDataSource&#40;&#41;.getGeometry&#40;&#41; != null&#41;
     *     .map&#40;item -&gt; item.getDataSource&#40;&#41;.getGeometry&#40;&#41;&#41;
     *     .collect&#40;Collectors.toList&#40;&#41;&#41;;
     * getPolygonIds.add&#40;fuzzySearchId&#41;;
     *
     * if &#40;ids != null &amp;&amp; !getPolygonIds.isEmpty&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;Get Polygon: &quot; + ids&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.maps.search.async.get_polygon -->
     *
     * @param geometryIds Comma separated list of geometry UUIDs, previously retrieved from an Online Search request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Polygon call.
     */
    Mono<Response<List<MapsPolygon>>> getPolygonsWithResponse(List<String> geometryIds, Context context) {
        return this.serviceClient.listPolygonsWithResponseAsync(JsonFormat.JSON, geometryIds, context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable)
            .map(response -> new SimpleResponse<>(response, response.getValue().getPolygons()));
    }

    /**
     * Fuzzy Search
     * <!-- src_embed com.azure.maps.search.async.fuzzy_search -->
     * <pre>
     * System.out.println&#40;&quot;Search Fuzzy:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.fuzzySearch&#40;new FuzzySearchOptions&#40;&quot;starbucks&quot;&#41;&#41;;
     *
     * &#47;&#47; with options
     * SearchAddressResult fuzzySearchResults = asyncClient.fuzzySearch&#40;
     *     new FuzzySearchOptions&#40;&quot;1 Microsoft Way&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; with response
     * Response&lt;SearchAddressResult&gt; fuzzySearchResponse = asyncClient.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.fuzzy_search -->
     *
     * @param options {@link FuzzySearchOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Fuzzy Search call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> fuzzySearch(FuzzySearchOptions options) {
        return fuzzySearchWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Fuzzy Search
     * <!-- src_embed com.azure.maps.search.async.fuzzy_search -->
     * <pre>
     * System.out.println&#40;&quot;Search Fuzzy:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.fuzzySearch&#40;new FuzzySearchOptions&#40;&quot;starbucks&quot;&#41;&#41;;
     *
     * &#47;&#47; with options
     * SearchAddressResult fuzzySearchResults = asyncClient.fuzzySearch&#40;
     *     new FuzzySearchOptions&#40;&quot;1 Microsoft Way&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; with response
     * Response&lt;SearchAddressResult&gt; fuzzySearchResponse = asyncClient.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.fuzzy_search -->
     *
     * @param query the query string used in the search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Fuzzy Search call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> fuzzySearch(String query) {
        return fuzzySearchWithResponse(new FuzzySearchOptions(query), null).flatMap(FluxUtil::toMono);
    }

    /**
     * Fuzzy Search
     * <!-- src_embed com.azure.maps.search.async.fuzzy_search -->
     * <pre>
     * System.out.println&#40;&quot;Search Fuzzy:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.fuzzySearch&#40;new FuzzySearchOptions&#40;&quot;starbucks&quot;&#41;&#41;;
     *
     * &#47;&#47; with options
     * SearchAddressResult fuzzySearchResults = asyncClient.fuzzySearch&#40;
     *     new FuzzySearchOptions&#40;&quot;1 Microsoft Way&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; with response
     * Response&lt;SearchAddressResult&gt; fuzzySearchResponse = asyncClient.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.fuzzy_search -->
     *
     * @param options {@link FuzzySearchOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Fuzzy Search call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> fuzzySearchWithResponse(FuzzySearchOptions options) {
        return withContext(context -> fuzzySearchWithResponse(options, context));
    }

    /**
     * Fuzzy Search
     * <!-- src_embed com.azure.maps.search.async.fuzzy_search -->
     * <pre>
     * System.out.println&#40;&quot;Search Fuzzy:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.fuzzySearch&#40;new FuzzySearchOptions&#40;&quot;starbucks&quot;&#41;&#41;;
     *
     * &#47;&#47; with options
     * SearchAddressResult fuzzySearchResults = asyncClient.fuzzySearch&#40;
     *     new FuzzySearchOptions&#40;&quot;1 Microsoft Way&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; with response
     * Response&lt;SearchAddressResult&gt; fuzzySearchResponse = asyncClient.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.fuzzy_search -->
     *
     * @param options {@link FuzzySearchOptions} the options to be used in this search.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Fuzzy Search call.
     */
    Mono<Response<SearchAddressResult>> fuzzySearchWithResponse(FuzzySearchOptions options, Context context) {
        return serviceClient.fuzzySearchWithResponseAsync(ResponseFormat.JSON, options.getQuery(),
                options.isTypeAhead(), options.getTop(), options.getSkip(), options.getCategoryFilter(),
                options.getCountryFilter(), options.getCoordinates().map(GeoPosition::getLatitude).orElse(null),
                options.getCoordinates().map(GeoPosition::getLongitude).orElse(null), options.getRadiusInMeters(),
                options.getBoundingBox().map(item -> new GeoPosition(item.getWest(), item.getNorth())).map(Utility::positionToString).orElse(null),
                options.getBoundingBox().map(item -> new GeoPosition(item.getEast(), item.getSouth())).map(Utility::positionToString).orElse(null),
                options.getLanguage(), options.getExtendedPostalCodesFor(), options.getMinFuzzyLevel(),
                options.getMaxFuzzyLevel(), options.getIndexFilter(), options.getBrandFilter(),
                options.getElectricVehicleConnectorFilter(), options.getEntityType(), options.getLocalizedMapView(),
                options.getOperatingHours(), context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Search Point of Interest
     * <!-- src_embed com.azure.maps.search.async.get_search_poi -->
     * <pre>
     * System.out.println&#40;&quot;Search Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; coordinates
     * asyncClient.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;;
     *
     * &#47;&#47; with response
     * asyncClient.searchPointOfInterestWithResponse&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.get_search_poi -->
     *
     * @param options {@link SearchPointOfInterestOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchPointOfInterest(SearchPointOfInterestOptions options) {
        return searchPointOfInterestWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Search Point of Interest
     * <!-- src_embed com.azure.maps.search.async.get_search_poi -->
     * <pre>
     * System.out.println&#40;&quot;Search Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; coordinates
     * asyncClient.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;;
     *
     * &#47;&#47; with response
     * asyncClient.searchPointOfInterestWithResponse&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.get_search_poi -->
     *
     * @param query The query to be used to search for points of interest.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchPointOfInterest(String query) {
        return searchPointOfInterestWithResponse(new SearchPointOfInterestOptions(query), null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Search Point of Interest
     * <!-- src_embed com.azure.maps.search.async.get_search_poi -->
     * <pre>
     * System.out.println&#40;&quot;Search Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; coordinates
     * asyncClient.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;;
     *
     * &#47;&#47; with response
     * asyncClient.searchPointOfInterestWithResponse&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.get_search_poi -->
     *
     * @param options {@link SearchPointOfInterestOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchPointOfInterestWithResponse(SearchPointOfInterestOptions options) {
        return withContext(context -> searchPointOfInterestWithResponse(options, context));
    }

    /**
     * Search Point of Interest
     * <!-- src_embed com.azure.maps.search.async.get_search_poi -->
     * <pre>
     * System.out.println&#40;&quot;Search Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; coordinates
     * asyncClient.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;;
     *
     * &#47;&#47; with response
     * asyncClient.searchPointOfInterestWithResponse&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.get_search_poi -->
     *
     * @param options {@link SearchPointOfInterestOptions} the options to be used in this search.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest call.
     */
    Mono<Response<SearchAddressResult>> searchPointOfInterestWithResponse(SearchPointOfInterestOptions options,
        Context context) {
        return serviceClient.searchPointOfInterestWithResponseAsync(ResponseFormat.JSON, options.getQuery(),
                options.isTypeAhead(), options.getTop(), options.getSkip(), options.getCategoryFilter(),
                options.getCountryFilter(), options.getCoordinates().map(GeoPosition::getLatitude).orElse(null),
                options.getCoordinates().map(GeoPosition::getLongitude).orElse(null), options.getRadiusInMeters(),
                options.getBoundingBox().map(item -> new GeoPosition(item.getWest(), item.getNorth())).map(Utility::positionToString).orElse(null),
                options.getBoundingBox().map(item -> new GeoPosition(item.getEast(), item.getSouth())).map(Utility::positionToString).orElse(null),
                options.getLanguage(), options.getExtendedPostalCodesFor(), options.getBrandFilter(),
                options.getElectricVehicleConnectorFilter(), options.getLocalizedMapView(), options.getOperatingHours(),
                context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Search Nearby Points of Interest
     * <!-- src_embed com.azure.maps.search.async.search_nearby -->
     * <pre>
     * System.out.println&#40;&quot;Search Nearby Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchNearbyPointsOfInterest&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;;
     *
     * &#47;&#47; response
     * asyncClient.searchNearbyPointsOfInterestWithResponse&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_nearby -->
     *
     * @param options {@link SearchNearbyPointsOfInterestOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Nearby Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchNearbyPointsOfInterest(SearchNearbyPointsOfInterestOptions options) {
        return searchNearbyPointsOfInterestWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Search Nearby Points of Interest
     * <!-- src_embed com.azure.maps.search.async.search_nearby -->
     * <pre>
     * System.out.println&#40;&quot;Search Nearby Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchNearbyPointsOfInterest&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;;
     *
     * &#47;&#47; response
     * asyncClient.searchNearbyPointsOfInterestWithResponse&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_nearby -->
     *
     * @param query A pair of coordinates for query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Nearby Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchNearbyPointsOfInterest(GeoPosition query) {
        return searchNearbyPointsOfInterestWithResponse(new SearchNearbyPointsOfInterestOptions(query), null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Search Nearby Points of Interest
     * <!-- src_embed com.azure.maps.search.async.search_nearby -->
     * <pre>
     * System.out.println&#40;&quot;Search Nearby Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchNearbyPointsOfInterest&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;;
     *
     * &#47;&#47; response
     * asyncClient.searchNearbyPointsOfInterestWithResponse&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_nearby -->
     *
     * @param options {@link SearchNearbyPointsOfInterestOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Nearby Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchNearbyPointsOfInterestWithResponse(
        SearchNearbyPointsOfInterestOptions options) {
        return withContext(context -> searchNearbyPointsOfInterestWithResponse(options, context));
    }

    /**
     * Search Nearby Points of Interest
     * <!-- src_embed com.azure.maps.search.async.search_nearby -->
     * <pre>
     * System.out.println&#40;&quot;Search Nearby Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchNearbyPointsOfInterest&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;;
     *
     * &#47;&#47; response
     * asyncClient.searchNearbyPointsOfInterestWithResponse&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_nearby -->
     *
     * @param options {@link SearchNearbyPointsOfInterestOptions} the options to be used in this search.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Nearby Point of Interest call.
     */
    Mono<Response<SearchAddressResult>> searchNearbyPointsOfInterestWithResponse(
        SearchNearbyPointsOfInterestOptions options, Context context) {
        // this should throw an exception if the coordinates are null, as for
        // this method they are mandatory
        final GeoPosition coordinates = options.getCoordinates().get();
        return serviceClient.searchNearbyPointOfInterestWithResponseAsync(ResponseFormat.JSON,
                coordinates.getLatitude(), coordinates.getLongitude(), options.getTop(), options.getSkip(),
                options.getCategoryFilter(), options.getCountryFilter(), options.getRadiusInMeters(),
                options.getLanguage(), options.getExtendedPostalCodesFor(), options.getBrandFilter(),
                options.getElectricVehicleConnectorFilter(), options.getLocalizedMapView(), context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Search Point of Interest per Category
     * <!-- src_embed com.azure.maps.search.async.search_poi_category -->
     * <pre>
     * System.out.println&#40;&quot;Get Point of Interest Category:&quot;&#41;;
     *
     * &#47;&#47; complete - search for italian restaurant in NYC
     * asyncClient.searchPointOfInterestCategory&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; with response
     * asyncClient.searchPointOfInterestCategoryWithResponse&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_poi_category -->
     *
     * @param options a {@link SearchPointOfInterestCategoryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest per Category calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchPointOfInterestCategory(SearchPointOfInterestCategoryOptions options) {
        return searchPointOfInterestCategoryWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Search Point of Interest per Category
     * <!-- src_embed com.azure.maps.search.async.search_poi_category -->
     * <pre>
     * System.out.println&#40;&quot;Get Point of Interest Category:&quot;&#41;;
     *
     * &#47;&#47; complete - search for italian restaurant in NYC
     * asyncClient.searchPointOfInterestCategory&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; with response
     * asyncClient.searchPointOfInterestCategoryWithResponse&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_poi_category -->
     *
     * @param query The query to be used to search for points of interest.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest per Category calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchPointOfInterestCategory(String query) {
        return searchPointOfInterestCategoryWithResponse(new SearchPointOfInterestCategoryOptions(query), null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Search Point of Interest per Category
     * <!-- src_embed com.azure.maps.search.async.search_poi_category -->
     * <pre>
     * System.out.println&#40;&quot;Get Point of Interest Category:&quot;&#41;;
     *
     * &#47;&#47; complete - search for italian restaurant in NYC
     * asyncClient.searchPointOfInterestCategory&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; with response
     * asyncClient.searchPointOfInterestCategoryWithResponse&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_poi_category -->
     *
     * @param options a {@link SearchPointOfInterestCategoryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest per Category calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchPointOfInterestCategoryWithResponse(
        SearchPointOfInterestCategoryOptions options) {
        return withContext(context -> searchPointOfInterestCategoryWithResponse(options, context));
    }

    /**
     * Search Point of Interest per Category
     * <!-- src_embed com.azure.maps.search.async.search_poi_category -->
     * <pre>
     * System.out.println&#40;&quot;Get Point of Interest Category:&quot;&#41;;
     *
     * &#47;&#47; complete - search for italian restaurant in NYC
     * asyncClient.searchPointOfInterestCategory&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; with response
     * asyncClient.searchPointOfInterestCategoryWithResponse&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_poi_category -->
     *
     * @param options a {@link SearchPointOfInterestCategoryOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest per Category calls.
     */
    Mono<Response<SearchAddressResult>> searchPointOfInterestCategoryWithResponse(
        SearchPointOfInterestCategoryOptions options, Context context) {
        return serviceClient.searchPointOfInterestCategoryWithResponseAsync(ResponseFormat.JSON, options.getQuery(),
                options.isTypeAhead(), options.getTop(), options.getSkip(), options.getCategoryFilter(),
                options.getCountryFilter(), options.getCoordinates().map(GeoPosition::getLatitude).orElse(null),
                options.getCoordinates().map(GeoPosition::getLongitude).orElse(null), options.getRadiusInMeters(),
                options.getBoundingBox().map(item -> new GeoPosition(item.getWest(), item.getNorth())).map(Utility::positionToString).orElse(null),
                options.getBoundingBox().map(item -> new GeoPosition(item.getEast(), item.getSouth())).map(Utility::positionToString).orElse(null),
                options.getLanguage(), options.getExtendedPostalCodesFor(), options.getBrandFilter(),
                options.getElectricVehicleConnectorFilter(), options.getLocalizedMapView(), options.getOperatingHours(),
                context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Get Point of Interest Category Tree
     * <!-- src_embed com.azure.maps.search.sync.search_poi_category_tree -->
     * <pre>
     * System.out.println&#40;&quot;Get Search POI Category Tree:&quot;&#41;;
     * client.getPointOfInterestCategoryTree&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_poi_category_tree -->
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful POI Category Tree call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PointOfInterestCategoryTreeResult> getPointOfInterestCategoryTree() {
        return serviceClient.getPointOfInterestCategoryTreeAsync(JsonFormat.JSON, null)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Get Point of Interest Category Tree
     * <!-- src_embed com.azure.maps.search.sync.search_poi_category_tree -->
     * <pre>
     * System.out.println&#40;&quot;Get Search POI Category Tree:&quot;&#41;;
     * client.getPointOfInterestCategoryTree&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_poi_category_tree -->
     *
     * @param language Language in which search results should be returned. Should be one of supported IETF language
     *     tags, except NGT and NGT-Latn. Language tag is case-insensitive. When data in specified language is not
     *     available for a specific field, default language is used (English).
     *     <p>Please refer to [Supported Languages](https://docs.microsoft.com/azure/azure-maps/supported-languages) for
     *     details.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful POI Category Tree call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PointOfInterestCategoryTreeResult>> getPointOfInterestCategoryTreeWithResponse(
        String language) {
        return withContext(context -> getPointOfInterestCategoryTreeWithResponse(language, context));
    }

    /**
     * Get Point of Interest Category Tree
     * <!-- src_embed com.azure.maps.search.sync.search_poi_category_tree -->
     * <pre>
     * System.out.println&#40;&quot;Get Search POI Category Tree:&quot;&#41;;
     * client.getPointOfInterestCategoryTree&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_poi_category_tree -->
     *
     * @param language Language in which search results should be returned. Should be one of supported IETF language
     *     tags, except NGT and NGT-Latn. Language tag is case-insensitive. When data in specified language is not
     *     available for a specific field, default language is used (English).
     *     <p>Please refer to [Supported Languages](https://docs.microsoft.com/azure/azure-maps/supported-languages) for
     *     details.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful POI Category Tree call.
     */
    Mono<Response<PointOfInterestCategoryTreeResult>> getPointOfInterestCategoryTreeWithResponse(String language,
        Context context) {
        return serviceClient.getPointOfInterestCategoryTreeWithResponseAsync(JsonFormat.JSON, language, context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Search Address
     * <!-- src_embed com.azure.maps.search.async.search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;15127 NE 24th Street, Redmond, WA 98052&quot;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchAddressWithResponse&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_address -->
     *
     * @param options a {@link SearchAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchAddress(SearchAddressOptions options) {
        return searchAddressWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Search Address
     * <!-- src_embed com.azure.maps.search.async.search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;15127 NE 24th Street, Redmond, WA 98052&quot;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchAddressWithResponse&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_address -->
     *
     * @param query the query string used in the fuzzy search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchAddress(String query) {
        return searchAddressWithResponse(new SearchAddressOptions(query), null).flatMap(FluxUtil::toMono);
    }

    /**
     * Search Address
     * <!-- src_embed com.azure.maps.search.async.search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;15127 NE 24th Street, Redmond, WA 98052&quot;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchAddressWithResponse&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_address -->
     *
     * @param options a {@link SearchAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchAddressWithResponse(SearchAddressOptions options) {
        return withContext(context -> searchAddressWithResponse(options, context));
    }

    /**
     * Search Address
     * <!-- src_embed com.azure.maps.search.async.search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;15127 NE 24th Street, Redmond, WA 98052&quot;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchAddressWithResponse&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_address -->
     *
     * @param options a {@link SearchAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address call.
     */
    Mono<Response<SearchAddressResult>> searchAddressWithResponse(SearchAddressOptions options, Context context) {
        return serviceClient.searchAddressWithResponseAsync(ResponseFormat.JSON, options.getQuery(),
                options.isTypeAhead(), options.getTop(), options.getSkip(), options.getCountryFilter(),
                options.getCoordinates().map(GeoPosition::getLatitude).orElse(null),
                options.getCoordinates().map(GeoPosition::getLongitude).orElse(null), options.getRadiusInMeters(),
                options.getBoundingBox().map(item -> new GeoPosition(item.getWest(), item.getNorth())).map(Utility::positionToString).orElse(null),
                options.getBoundingBox().map(item -> new GeoPosition(item.getEast(), item.getSouth())).map(Utility::positionToString).orElse(null),
                options.getLanguage(), options.getExtendedPostalCodesFor(), options.getEntityType(),
                options.getLocalizedMapView(), context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Reverse Address Search
     * <!-- src_embed com.azure.maps.search.async.reverse_search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Reverse:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41; &#47;&#47; returns only city
     * &#41;;
     *
     * &#47;&#47; complete
     * asyncClient.reverseSearchAddressWithResponse&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.reverse_search_address -->
     *
     * @param options a {@link ReverseSearchAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ReverseSearchAddressResult> reverseSearchAddress(ReverseSearchAddressOptions options) {
        return reverseSearchAddressWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Reverse Address Search
     * <!-- src_embed com.azure.maps.search.async.reverse_search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Reverse:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41; &#47;&#47; returns only city
     * &#41;;
     *
     * &#47;&#47; complete
     * asyncClient.reverseSearchAddressWithResponse&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.reverse_search_address -->
     *
     * @param query The applicable query as a pair of coordinates.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ReverseSearchAddressResult> reverseSearchAddress(GeoPosition query) {
        return reverseSearchAddressWithResponse(new ReverseSearchAddressOptions(query), null).flatMap(FluxUtil::toMono);
    }

    /**
     * Reverse Address Search
     * <!-- src_embed com.azure.maps.search.async.reverse_search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Reverse:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41; &#47;&#47; returns only city
     * &#41;;
     *
     * &#47;&#47; complete
     * asyncClient.reverseSearchAddressWithResponse&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.reverse_search_address -->
     *
     * @param options a {@link ReverseSearchAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ReverseSearchAddressResult>> reverseSearchAddressWithResponse(
        ReverseSearchAddressOptions options) {
        return withContext(context -> reverseSearchAddressWithResponse(options, context));
    }

    /**
     * Reverse Address Search
     * <!-- src_embed com.azure.maps.search.async.reverse_search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Reverse:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41; &#47;&#47; returns only city
     * &#41;;
     *
     * &#47;&#47; complete
     * asyncClient.reverseSearchAddressWithResponse&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.reverse_search_address -->
     *
     * @param options a {@link ReverseSearchAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    Mono<Response<ReverseSearchAddressResult>> reverseSearchAddressWithResponse(ReverseSearchAddressOptions options,
        Context context) {
        return serviceClient.reverseSearchAddressWithResponseAsync(ResponseFormat.JSON,
                Arrays.asList(options.getCoordinates().getLatitude(), options.getCoordinates().getLongitude()),
                options.getLanguage(), options.includeSpeedLimit(), options.getHeading(), options.getRadiusInMeters(),
                options.getNumber(), options.includeRoadUse(), options.getRoadUse(), options.allowFreeformNewline(),
                options.includeMatchType(), options.getEntityType(), options.getLocalizedMapView(), context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Reverse Address Search to a Cross Street
     * <!-- src_embed com.azure.maps.search.async.search_reverse_cross_street_address -->
     * <pre>
     * System.out.println&#40;&quot;Revere Search Cross Street Address:&quot;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.reverseSearchCrossStreetAddressWithResponse&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_reverse_cross_street_address -->
     *
     * @param options a {@link ReverseSearchCrossStreetAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ReverseSearchCrossStreetAddressResult> reverseSearchCrossStreetAddress(
        ReverseSearchCrossStreetAddressOptions options) {
        return reverseSearchCrossStreetAddressWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Reverse Address Search to a Cross Street
     * <!-- src_embed com.azure.maps.search.async.search_reverse_cross_street_address -->
     * <pre>
     * System.out.println&#40;&quot;Revere Search Cross Street Address:&quot;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.reverseSearchCrossStreetAddressWithResponse&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_reverse_cross_street_address -->
     *
     * @param query with a pair of coordinates.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ReverseSearchCrossStreetAddressResult> reverseSearchCrossStreetAddress(GeoPosition query) {
        return reverseSearchCrossStreetAddressWithResponse(new ReverseSearchCrossStreetAddressOptions(query), null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Reverse Address Search to a Cross Street
     * <!-- src_embed com.azure.maps.search.async.search_reverse_cross_street_address -->
     * <pre>
     * System.out.println&#40;&quot;Revere Search Cross Street Address:&quot;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.reverseSearchCrossStreetAddressWithResponse&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_reverse_cross_street_address -->
     *
     * @param options a {@link ReverseSearchCrossStreetAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ReverseSearchCrossStreetAddressResult>> reverseSearchCrossStreetAddressWithResponse(
        ReverseSearchCrossStreetAddressOptions options) {
        return withContext(context -> reverseSearchCrossStreetAddressWithResponse(options, context));
    }

    /**
     * Reverse Address Search to a Cross Street
     * <!-- src_embed com.azure.maps.search.async.search_reverse_cross_street_address -->
     * <pre>
     * System.out.println&#40;&quot;Revere Search Cross Street Address:&quot;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.reverseSearchCrossStreetAddressWithResponse&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_reverse_cross_street_address -->
     *
     * @param options a {@link ReverseSearchCrossStreetAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    Mono<Response<ReverseSearchCrossStreetAddressResult>> reverseSearchCrossStreetAddressWithResponse(
        ReverseSearchCrossStreetAddressOptions options, Context context) {
        return serviceClient.reverseSearchCrossStreetAddressWithResponseAsync(ResponseFormat.JSON,
                Arrays.asList(options.getCoordinates().getLatitude(), options.getCoordinates().getLongitude()),
                options.getTop(), options.getHeading(), options.getRadiusInMeters(), options.getLanguage(),
                options.getLocalizedMapView(), context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Structured Address Search
     * <!-- src_embed com.azure.maps.search.async.search_structured_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Structured:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchStructuredAddress&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;, null&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchStructuredAddressWithResponse&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;,
     *     new SearchStructuredAddressOptions&#40;&#41;
     *             .setTop&#40;2&#41;
     *             .setRadiusInMeters&#40;1000&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_structured_address -->
     *
     * @param address a {@link StructuredAddress} to be searched by the API.
     * @param options a {@link SearchStructuredAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchStructuredAddress(StructuredAddress address,
        SearchStructuredAddressOptions options) {
        return searchStructuredAddressWithResponse(address, options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Structured Address Search
     * <!-- src_embed com.azure.maps.search.async.search_structured_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Structured:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchStructuredAddress&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;, null&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchStructuredAddressWithResponse&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;,
     *     new SearchStructuredAddressOptions&#40;&#41;
     *             .setTop&#40;2&#41;
     *             .setRadiusInMeters&#40;1000&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_structured_address -->
     *
     * @param countryCode the country code for query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchStructuredAddress(String countryCode) {
        return searchStructuredAddressWithResponse(new StructuredAddress(countryCode), null).flatMap(FluxUtil::toMono);
    }

    /**
     * Structured Address Search
     * <!-- src_embed com.azure.maps.search.async.search_structured_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Structured:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchStructuredAddress&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;, null&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchStructuredAddressWithResponse&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;,
     *     new SearchStructuredAddressOptions&#40;&#41;
     *             .setTop&#40;2&#41;
     *             .setRadiusInMeters&#40;1000&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_structured_address -->
     *
     * @param address a {@link StructuredAddress} to be searched by the API.
     * @param options a {@link SearchStructuredAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchStructuredAddressWithResponse(StructuredAddress address,
        SearchStructuredAddressOptions options) {
        return withContext(context -> searchStructuredAddressWithResponse(address, options, context));
    }

    /**
     * Structured Address Search
     * <!-- src_embed com.azure.maps.search.async.search_structured_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Structured:&quot;&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchStructuredAddress&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;, null&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchStructuredAddressWithResponse&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;,
     *     new SearchStructuredAddressOptions&#40;&#41;
     *             .setTop&#40;2&#41;
     *             .setRadiusInMeters&#40;1000&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_structured_address -->
     *
     * @param address a {@link StructuredAddress} to be searched by the API.
     * @param options a {@link SearchStructuredAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Structured Address Search call.
     */
    Mono<Response<SearchAddressResult>> searchStructuredAddressWithResponse(StructuredAddress address,
        SearchStructuredAddressOptions options, Context context) {
        final SearchStructuredAddressOptions param = Optional.ofNullable(options)
            .orElse(new SearchStructuredAddressOptions());
        return serviceClient.searchStructuredAddressWithResponseAsync(ResponseFormat.JSON, address.getCountryCode(),
                param.getLanguage(), param.getTop(), param.getSkip(), address.getStreetNumber(),
                address.getStreetName(), address.getCrossStreet(), address.getMunicipality(),
                address.getMunicipalitySubdivision(), address.getCountryTertiarySubdivision(),
                address.getCountrySecondarySubdivision(), address.getCountrySubdivision(), address.getPostalCode(),
                param.getExtendedPostalCodesFor(), param.getEntityType(), param.getLocalizedMapView(), context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Search Inside Geometry
     * <!-- src_embed com.azure.maps.search.async.search_inside_geometry -->
     * <pre>
     * System.out.println&#40;&quot;Search Inside Geometry&quot;&#41;;
     *
     * &#47;&#47; create GeoPolygon
     * List&lt;GeoPosition&gt; searchInsideGeometryCoordinates = new ArrayList&lt;&gt;&#40;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43301391601562, 37.70660472542312&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.36434936523438, 37.712059855877314&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * GeoLinearRing searchInsideGeometryRing = new GeoLinearRing&#40;searchInsideGeometryCoordinates&#41;;
     * GeoPolygon searchInsideGeometryPolygon = new GeoPolygon&#40;searchInsideGeometryRing&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchInsideGeometryWithResponse&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_inside_geometry -->
     *
     * @param options a {@link SearchInsideGeometryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Inside Geometry call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchInsideGeometry(SearchInsideGeometryOptions options) {
        return searchInsideGeometryWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Search Inside Geometry
     * <!-- src_embed com.azure.maps.search.async.search_inside_geometry -->
     * <pre>
     * System.out.println&#40;&quot;Search Inside Geometry&quot;&#41;;
     *
     * &#47;&#47; create GeoPolygon
     * List&lt;GeoPosition&gt; searchInsideGeometryCoordinates = new ArrayList&lt;&gt;&#40;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43301391601562, 37.70660472542312&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.36434936523438, 37.712059855877314&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * GeoLinearRing searchInsideGeometryRing = new GeoLinearRing&#40;searchInsideGeometryCoordinates&#41;;
     * GeoPolygon searchInsideGeometryPolygon = new GeoPolygon&#40;searchInsideGeometryRing&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchInsideGeometryWithResponse&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_inside_geometry -->
     *
     * @param query query string
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Inside Geometry call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchInsideGeometry(String query) {
        return searchInsideGeometryWithResponse(new SearchInsideGeometryOptions(query), null).flatMap(FluxUtil::toMono);
    }

    /**
     * Search Inside Geometry
     * <!-- src_embed com.azure.maps.search.async.search_inside_geometry -->
     * <pre>
     * System.out.println&#40;&quot;Search Inside Geometry&quot;&#41;;
     *
     * &#47;&#47; create GeoPolygon
     * List&lt;GeoPosition&gt; searchInsideGeometryCoordinates = new ArrayList&lt;&gt;&#40;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43301391601562, 37.70660472542312&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.36434936523438, 37.712059855877314&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * GeoLinearRing searchInsideGeometryRing = new GeoLinearRing&#40;searchInsideGeometryCoordinates&#41;;
     * GeoPolygon searchInsideGeometryPolygon = new GeoPolygon&#40;searchInsideGeometryRing&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchInsideGeometryWithResponse&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_inside_geometry -->
     *
     * @param options a {@link SearchInsideGeometryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Inside Geometry call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchInsideGeometryWithResponse(SearchInsideGeometryOptions options) {
        return withContext(context -> searchInsideGeometryWithResponse(options, context));
    }

    /**
     * Search Inside Geometry
     * <!-- src_embed com.azure.maps.search.async.search_inside_geometry -->
     * <pre>
     * System.out.println&#40;&quot;Search Inside Geometry&quot;&#41;;
     *
     * &#47;&#47; create GeoPolygon
     * List&lt;GeoPosition&gt; searchInsideGeometryCoordinates = new ArrayList&lt;&gt;&#40;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43301391601562, 37.70660472542312&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.36434936523438, 37.712059855877314&#41;&#41;;
     * searchInsideGeometryCoordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * GeoLinearRing searchInsideGeometryRing = new GeoLinearRing&#40;searchInsideGeometryCoordinates&#41;;
     * GeoPolygon searchInsideGeometryPolygon = new GeoPolygon&#40;searchInsideGeometryRing&#41;;
     *
     * &#47;&#47; simple
     * asyncClient.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchInsideGeometryWithResponse&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, searchInsideGeometryPolygon&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_inside_geometry -->
     *
     * @param options a {@link SearchInsideGeometryOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Inside Geometry call.
     */
    Mono<Response<SearchAddressResult>> searchInsideGeometryWithResponse(SearchInsideGeometryOptions options,
        Context context) {
        GeoJsonObject geoJsonObject = Utility.toGeoJsonObject(options.getGeometry());
        return serviceClient.searchInsideGeometryWithResponseAsync(ResponseFormat.JSON, options.getQuery(),
                new SearchInsideGeometryRequest().setGeometry(geoJsonObject), options.getTop(), options.getLanguage(),
                options.getCategoryFilter(), options.getExtendedPostalCodesFor(), options.getIndexFilter(),
                options.getLocalizedMapView(), options.getOperatingHours(), context)
                .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Search Along Route
     * <!-- src_embed com.azure.maps.search.async.search_along_route -->
     * <pre>
     * System.out.println&#40;&quot;Search Along Route&quot;&#41;;
     *
     * &#47;&#47; create route points
     * List&lt;GeoPosition&gt; getPolygonPoints = new ArrayList&lt;&gt;&#40;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.143035, 47.653536&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.187164, 47.617556&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.114981, 47.570599&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.132756, 47.654009&#41;&#41;;
     * GeoLineString getPolygonRoute = new GeoLineString&#40;getPolygonPoints&#41;;
     *
     * &#47;&#47; simple
     * SearchAddressResult result = asyncClient.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchAlongRouteWithResponse&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;
     *     .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;.setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;
     *     .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_along_route -->
     *
     * @param options a {@link SearchAlongRouteOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Along Route call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchAlongRoute(SearchAlongRouteOptions options) {
        return searchAlongRouteWithResponse(options, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Search Along Route
     * <!-- src_embed com.azure.maps.search.async.search_along_route -->
     * <pre>
     * System.out.println&#40;&quot;Search Along Route&quot;&#41;;
     *
     * &#47;&#47; create route points
     * List&lt;GeoPosition&gt; getPolygonPoints = new ArrayList&lt;&gt;&#40;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.143035, 47.653536&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.187164, 47.617556&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.114981, 47.570599&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.132756, 47.654009&#41;&#41;;
     * GeoLineString getPolygonRoute = new GeoLineString&#40;getPolygonPoints&#41;;
     *
     * &#47;&#47; simple
     * SearchAddressResult result = asyncClient.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchAlongRouteWithResponse&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;
     *     .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;.setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;
     *     .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_along_route -->
     *
     * @param query the search query
     * @param maxDetourTime the maximum detour time allowed
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Along Route call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchAlongRoute(String query, int maxDetourTime) {
        return searchAlongRouteWithResponse(new SearchAlongRouteOptions(query, maxDetourTime), null)
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Search Along Route
     * <!-- src_embed com.azure.maps.search.async.search_along_route -->
     * <pre>
     * System.out.println&#40;&quot;Search Along Route&quot;&#41;;
     *
     * &#47;&#47; create route points
     * List&lt;GeoPosition&gt; getPolygonPoints = new ArrayList&lt;&gt;&#40;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.143035, 47.653536&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.187164, 47.617556&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.114981, 47.570599&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.132756, 47.654009&#41;&#41;;
     * GeoLineString getPolygonRoute = new GeoLineString&#40;getPolygonPoints&#41;;
     *
     * &#47;&#47; simple
     * SearchAddressResult result = asyncClient.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchAlongRouteWithResponse&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;
     *     .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;.setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;
     *     .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_along_route -->
     *
     * @param options a {@link SearchAlongRouteOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Along Route call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchAlongRouteWithResponse(SearchAlongRouteOptions options) {
        return withContext(context -> searchAlongRouteWithResponse(options, context));
    }

    /**
     * Search Along Route
     * <!-- src_embed com.azure.maps.search.async.search_along_route -->
     * <pre>
     * System.out.println&#40;&quot;Search Along Route&quot;&#41;;
     *
     * &#47;&#47; create route points
     * List&lt;GeoPosition&gt; getPolygonPoints = new ArrayList&lt;&gt;&#40;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.143035, 47.653536&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.187164, 47.617556&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.114981, 47.570599&#41;&#41;;
     * getPolygonPoints.add&#40;new GeoPosition&#40;-122.132756, 47.654009&#41;&#41;;
     * GeoLineString getPolygonRoute = new GeoLineString&#40;getPolygonPoints&#41;;
     *
     * &#47;&#47; simple
     * SearchAddressResult result = asyncClient.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; options
     * asyncClient.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;5&#41;&#41;.block&#40;&#41;;
     *
     * &#47;&#47; complete
     * asyncClient.searchAlongRouteWithResponse&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, getPolygonRoute&#41;
     *     .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;.setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;
     *     .setTop&#40;5&#41;&#41;.block&#40;&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_along_route -->
     *
     * @param options a {@link SearchAlongRouteOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Along Route call.
     */
    Mono<Response<SearchAddressResult>> searchAlongRouteWithResponse(SearchAlongRouteOptions options, Context context) {
        GeoJsonLineString geoJsonLineString = (GeoJsonLineString) Utility.toGeoJsonObject(options.getRoute());
        return serviceClient.searchAlongRouteWithResponseAsync(ResponseFormat.JSON, options.getQuery(),
                options.getMaxDetourTime(), new SearchAlongRouteRequest().setRoute(geoJsonLineString), options.getTop(),
                options.getBrandFilter(), options.getCategoryFilter(), options.getElectricVehicleConnectorFilter(),
                options.getLocalizedMapView(), options.getOperatingHours(), context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Batch Fuzzy Search
     * <!-- src_embed com.azure.maps.search.async.fuzzy_search_batch -->
     * <pre>
     * List&lt;FuzzySearchOptions&gt; fuzzySearchBatchOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;atm&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;.setTop&#40;5&#41;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Statue of Liberty&quot;&#41;.setTop&#40;2&#41;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Starbucks&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;&#41;;
     *
     * System.out.println&#40;&quot;Post Search Fuzzy Batch Async&quot;&#41;;
     * asyncClient.beginFuzzySearchBatch&#40;fuzzyOptionsList&#41;.getSyncPoller&#40;&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.fuzzy_search_batch -->
     *
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Batch Fuzzy Search service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<BatchSearchResult, BatchSearchResult> beginFuzzySearchBatch(
        List<FuzzySearchOptions> optionsList) {
        return this.beginFuzzySearchBatch(optionsList, null);
    }

    /**
     * Batch Fuzzy Search
     * <!-- src_embed com.azure.maps.search.async.fuzzy_search_batch -->
     * <pre>
     * List&lt;FuzzySearchOptions&gt; fuzzySearchBatchOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;atm&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;.setTop&#40;5&#41;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Statue of Liberty&quot;&#41;.setTop&#40;2&#41;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Starbucks&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;&#41;;
     *
     * System.out.println&#40;&quot;Post Search Fuzzy Batch Async&quot;&#41;;
     * asyncClient.beginFuzzySearchBatch&#40;fuzzyOptionsList&#41;.getSyncPoller&#40;&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.fuzzy_search_batch -->
     *
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    PollerFlux<BatchSearchResult, BatchSearchResult> beginFuzzySearchBatch(
            List<FuzzySearchOptions> optionsList, Context context) {
        Objects.requireNonNull(optionsList, "'optionsList' is a required parameter.");

        // convert list to batch request
        List<BatchRequestItem> items = optionsList.stream().map(Utility::toFuzzySearchBatchRequestItem)
            .collect(Collectors.toList());
        BatchRequest batchRequest = new BatchRequest().setBatchItems(items);

        if (batchRequest.getBatchItems().size() <= BATCH_SIZE) {
            return createPollerFlux(
                () -> this.serviceClient.fuzzySearchBatchSyncWithResponseAsync(JsonFormat.JSON, batchRequest, context)
                    .flatMap(response -> Mono.just(Utility.createBatchSearchResponse(response))
                        .onErrorMap(MapsSearchAsyncClient::mapThrowable)), this.forwardStrategy);
        } else {
            return createPollerFlux(
                () -> this.serviceClient.fuzzySearchBatchNoCustomHeadersWithResponseAsync(JsonFormat.JSON, batchRequest,
                    context)
                    .flatMap(response -> Mono.just(Utility.createBatchSearchResponse(response))
                        .onErrorMap(MapsSearchAsyncClient::mapThrowable)), this.forwardStrategy);
        }
    }

    /**
     * Get Fuzzy Batch Search by Id
     * <!-- src_embed com.azure.maps.search.async.fuzzy_search_batch -->
     * <pre>
     * List&lt;FuzzySearchOptions&gt; fuzzySearchBatchOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;atm&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;.setTop&#40;5&#41;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Statue of Liberty&quot;&#41;.setTop&#40;2&#41;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Starbucks&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;&#41;;
     *
     * System.out.println&#40;&quot;Post Search Fuzzy Batch Async&quot;&#41;;
     * asyncClient.beginFuzzySearchBatch&#40;fuzzyOptionsList&#41;.getSyncPoller&#40;&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.fuzzy_search_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<BatchSearchResult, BatchSearchResult> beginGetFuzzySearchBatch(String batchId) {
        return this.beginGetFuzzySearchBatch(batchId, null);
    }

    /**
     * Get Fuzzy Batch Search by Id
     * <!-- src_embed com.azure.maps.search.async.fuzzy_search_batch -->
     * <pre>
     * List&lt;FuzzySearchOptions&gt; fuzzySearchBatchOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;atm&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;.setTop&#40;5&#41;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Statue of Liberty&quot;&#41;.setTop&#40;2&#41;&#41;;
     * fuzzySearchBatchOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Starbucks&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;&#41;;
     *
     * System.out.println&#40;&quot;Post Search Fuzzy Batch Async&quot;&#41;;
     * asyncClient.beginFuzzySearchBatch&#40;fuzzyOptionsList&#41;.getSyncPoller&#40;&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.fuzzy_search_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    PollerFlux<BatchSearchResult, BatchSearchResult> beginGetFuzzySearchBatch(String batchId, Context context) {
        return createPollerFlux(
            () -> this.serviceClient.getFuzzySearchBatchNoCustomHeadersWithResponseAsync(batchId, context)
                .flatMap(response -> Mono.just(Utility.createBatchSearchResponse(response))
                    .onErrorMap(MapsSearchAsyncClient::mapThrowable)), this.forwardStrategy);
    }

    /**
     * Batch Address Search
     * <!-- src_embed com.azure.maps.search.async.search_address_batch -->
     * <pre>
     * List&lt;SearchAddressOptions&gt; list = new ArrayList&lt;&gt;&#40;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;400 Broad St, Seattle, WA 98109&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;One, Microsoft Way, Redmond, WA 98052&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;350 5th Ave, New York, NY 10118&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *     .setCountryFilter&#40;Arrays.asList&#40;&quot;GB&quot;, &quot;US&quot;, &quot;AU&quot;&#41;&#41;.setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; Search address batch async -
     * &#47;&#47; https:&#47;&#47;docs.microsoft.com&#47;en-us&#47;rest&#47;api&#47;maps&#47;search&#47;post-search-address-batch
     * &#47;&#47; This call posts addresses for search using the Asynchronous Batch API.
     * &#47;&#47; SyncPoller will do the polling automatically and you can retrieve the result
     * &#47;&#47; with getFinalResult&#40;&#41;
     * System.out.println&#40;&quot;Search Address Batch Async&quot;&#41;;
     * asyncClient.beginSearchAddressBatch&#40;list&#41;.blockFirst&#40;&#41;.getFinalResult&#40;&#41;;
     * SyncPoller&lt;BatchSearchResult, BatchSearchResult&gt; bp2 = asyncClient.beginSearchAddressBatch&#40;list&#41;.getSyncPoller&#40;&#41;;
     * BatchSearchResult batchResult2 = bp2.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_address_batch -->
     *
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<BatchSearchResult, BatchSearchResult> beginSearchAddressBatch(
        List<SearchAddressOptions> optionsList) {
        return this.beginSearchAddressBatch(optionsList, null);
    }

    /**
     * Batch Address Search
     * <!-- src_embed com.azure.maps.search.async.search_address_batch -->
     * <pre>
     * List&lt;SearchAddressOptions&gt; list = new ArrayList&lt;&gt;&#40;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;400 Broad St, Seattle, WA 98109&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;One, Microsoft Way, Redmond, WA 98052&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;350 5th Ave, New York, NY 10118&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *     .setCountryFilter&#40;Arrays.asList&#40;&quot;GB&quot;, &quot;US&quot;, &quot;AU&quot;&#41;&#41;.setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; Search address batch async -
     * &#47;&#47; https:&#47;&#47;docs.microsoft.com&#47;en-us&#47;rest&#47;api&#47;maps&#47;search&#47;post-search-address-batch
     * &#47;&#47; This call posts addresses for search using the Asynchronous Batch API.
     * &#47;&#47; SyncPoller will do the polling automatically and you can retrieve the result
     * &#47;&#47; with getFinalResult&#40;&#41;
     * System.out.println&#40;&quot;Search Address Batch Async&quot;&#41;;
     * asyncClient.beginSearchAddressBatch&#40;list&#41;.blockFirst&#40;&#41;.getFinalResult&#40;&#41;;
     * SyncPoller&lt;BatchSearchResult, BatchSearchResult&gt; bp2 = asyncClient.beginSearchAddressBatch&#40;list&#41;.getSyncPoller&#40;&#41;;
     * BatchSearchResult batchResult2 = bp2.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_address_batch -->
     *
     * @param optionsList a list of {@link SearchAddressOptions} to be searched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    PollerFlux<BatchSearchResult, BatchSearchResult> beginSearchAddressBatch(
            List<SearchAddressOptions> optionsList, Context context) {
        Objects.requireNonNull(optionsList, "'optionsList' is a required parameter.");

        // convert list to batch request
        List<BatchRequestItem> items = optionsList.stream().map(Utility::toSearchBatchRequestItem)
            .collect(Collectors.toList());
        BatchRequest batchRequest = new BatchRequest().setBatchItems(items);

        // run
        if (batchRequest.getBatchItems().size() <= BATCH_SIZE) {
            return createPollerFlux(() -> this.serviceClient.searchAddressBatchSyncWithResponseAsync(JsonFormat.JSON,
                    batchRequest, context)
                .flatMap(response -> Mono.just(Utility.createBatchSearchResponse(response))
                    .onErrorMap(MapsSearchAsyncClient::mapThrowable)), this.forwardStrategy);
        } else {
            return createPollerFlux(
                () -> this.serviceClient.searchAddressBatchNoCustomHeadersWithResponseAsync(JsonFormat.JSON,
                    batchRequest, context).flatMap(response -> Mono.just(Utility.createBatchSearchResponse(response))
                    .onErrorMap(MapsSearchAsyncClient::mapThrowable)), this.forwardStrategy);
        }
    }



    /**
     * Get Batch Search Id
     * <!-- src_embed com.azure.maps.search.async.search_address_batch -->
     * <pre>
     * List&lt;SearchAddressOptions&gt; list = new ArrayList&lt;&gt;&#40;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;400 Broad St, Seattle, WA 98109&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;One, Microsoft Way, Redmond, WA 98052&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;350 5th Ave, New York, NY 10118&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *     .setCountryFilter&#40;Arrays.asList&#40;&quot;GB&quot;, &quot;US&quot;, &quot;AU&quot;&#41;&#41;.setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; Search address batch async -
     * &#47;&#47; https:&#47;&#47;docs.microsoft.com&#47;en-us&#47;rest&#47;api&#47;maps&#47;search&#47;post-search-address-batch
     * &#47;&#47; This call posts addresses for search using the Asynchronous Batch API.
     * &#47;&#47; SyncPoller will do the polling automatically and you can retrieve the result
     * &#47;&#47; with getFinalResult&#40;&#41;
     * System.out.println&#40;&quot;Search Address Batch Async&quot;&#41;;
     * asyncClient.beginSearchAddressBatch&#40;list&#41;.blockFirst&#40;&#41;.getFinalResult&#40;&#41;;
     * SyncPoller&lt;BatchSearchResult, BatchSearchResult&gt; bp2 = asyncClient.beginSearchAddressBatch&#40;list&#41;.getSyncPoller&#40;&#41;;
     * BatchSearchResult batchResult2 = bp2.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_address_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<BatchSearchResult, BatchSearchResult> beginGetSearchAddressBatch(String batchId) {
        return this.beginGetSearchAddressBatch(batchId, null);
    }

    /**
     * Get Batch Search Id
     * <!-- src_embed com.azure.maps.search.async.search_address_batch -->
     * <pre>
     * List&lt;SearchAddressOptions&gt; list = new ArrayList&lt;&gt;&#40;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;400 Broad St, Seattle, WA 98109&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;One, Microsoft Way, Redmond, WA 98052&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;350 5th Ave, New York, NY 10118&quot;&#41;.setTop&#40;3&#41;&#41;;
     * list.add&#40;new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *     .setCountryFilter&#40;Arrays.asList&#40;&quot;GB&quot;, &quot;US&quot;, &quot;AU&quot;&#41;&#41;.setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; Search address batch async -
     * &#47;&#47; https:&#47;&#47;docs.microsoft.com&#47;en-us&#47;rest&#47;api&#47;maps&#47;search&#47;post-search-address-batch
     * &#47;&#47; This call posts addresses for search using the Asynchronous Batch API.
     * &#47;&#47; SyncPoller will do the polling automatically and you can retrieve the result
     * &#47;&#47; with getFinalResult&#40;&#41;
     * System.out.println&#40;&quot;Search Address Batch Async&quot;&#41;;
     * asyncClient.beginSearchAddressBatch&#40;list&#41;.blockFirst&#40;&#41;.getFinalResult&#40;&#41;;
     * SyncPoller&lt;BatchSearchResult, BatchSearchResult&gt; bp2 = asyncClient.beginSearchAddressBatch&#40;list&#41;.getSyncPoller&#40;&#41;;
     * BatchSearchResult batchResult2 = bp2.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.search_address_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    PollerFlux<BatchSearchResult, BatchSearchResult> beginGetSearchAddressBatch(
            String batchId, Context context) {
        return createPollerFlux(
            () -> this.serviceClient.getSearchAddressBatchNoCustomHeadersWithResponseAsync(batchId, context)
                .flatMap(response -> Mono.just(Utility.createBatchSearchResponse(response))
                    .onErrorMap(MapsSearchAsyncClient::mapThrowable)), this.forwardStrategy);
    }

    /**
     * Searches a batch of addresses given their coordinates.
     * <!-- src_embed com.azure.maps.search.async.reverse_search_address_batch  -->
     * <pre>
     * List&lt;ReverseSearchAddressOptions&gt; list2 = new ArrayList&lt;&gt;&#40;&#41;;
     * list2.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;2.294911, 48.858561&#41;&#41;&#41;;
     * list2.add&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.127896, 47.639765&#41;&#41;
     *         .setRadiusInMeters&#40;5000&#41;
     * &#41;;
     * list2.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.348170, 47.621028&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Reverse Search Address Batch Async&quot;&#41;;
     * BatchReverseSearchResult batchReverseSearchResult =
     *     asyncClient.beginReverseSearchAddressBatch&#40;list2&#41;.getSyncPoller&#40;&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.reverse_search_address_batch -->
     *
     * @param optionsList a list of {@link ReverseSearchAddressOptions} to be searched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult> beginReverseSearchAddressBatch(
        List<ReverseSearchAddressOptions> optionsList) {
        return this.beginReverseSearchAddressBatch(optionsList, null);
    }

    /**
     * Searches a batch of addresses given their coordinates.
     * <!-- src_embed com.azure.maps.search.async.reverse_search_address_batch  -->
     * <pre>
     * List&lt;ReverseSearchAddressOptions&gt; list2 = new ArrayList&lt;&gt;&#40;&#41;;
     * list2.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;2.294911, 48.858561&#41;&#41;&#41;;
     * list2.add&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.127896, 47.639765&#41;&#41;
     *         .setRadiusInMeters&#40;5000&#41;
     * &#41;;
     * list2.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.348170, 47.621028&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Reverse Search Address Batch Async&quot;&#41;;
     * BatchReverseSearchResult batchReverseSearchResult =
     *     asyncClient.beginReverseSearchAddressBatch&#40;list2&#41;.getSyncPoller&#40;&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.reverse_search_address_batch -->
     *
     * @param optionsList a list of {@link ReverseSearchAddressOptions} to be searched.
     * @param context a {@link Context} object for distributed tracing.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult> beginReverseSearchAddressBatch(
        List<ReverseSearchAddressOptions> optionsList, Context context) {
        Objects.requireNonNull(optionsList, "'optionsList' is a required parameter.");

        // convert list to batch request
        List<BatchRequestItem> items = optionsList.stream().map(Utility::toReverseSearchBatchRequestItem)
            .collect(Collectors.toList());
        BatchRequest batchRequest = new BatchRequest().setBatchItems(items);

        if (batchRequest.getBatchItems().size() <= BATCH_SIZE) {
            return createReversePollerFlux(
                () -> this.serviceClient.reverseSearchAddressBatchSyncWithResponseAsync(JsonFormat.JSON, batchRequest,
                        context)
                    .flatMap(response -> Mono.just(Utility.createBatchReverseSearchResponse(response))
                        .onErrorMap(MapsSearchAsyncClient::mapThrowable)), this.reverseStrategy);
        } else {
            return createReversePollerFlux(
                () -> this.serviceClient.reverseSearchAddressBatchNoCustomHeadersWithResponseAsync(JsonFormat.JSON,
                    batchRequest, context).flatMap(response ->
                    Mono.just(Utility.createBatchReverseSearchResponse(response))
                        .onErrorMap(MapsSearchAsyncClient::mapThrowable)),
                this.reverseStrategy);
        }
    }

    /**
     * Returns a batch of previously searched addressed given a batch id.
     * <!-- src_embed com.azure.maps.search.async.reverse_search_address_batch  -->
     * <pre>
     * List&lt;ReverseSearchAddressOptions&gt; list2 = new ArrayList&lt;&gt;&#40;&#41;;
     * list2.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;2.294911, 48.858561&#41;&#41;&#41;;
     * list2.add&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.127896, 47.639765&#41;&#41;
     *         .setRadiusInMeters&#40;5000&#41;
     * &#41;;
     * list2.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.348170, 47.621028&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Reverse Search Address Batch Async&quot;&#41;;
     * BatchReverseSearchResult batchReverseSearchResult =
     *     asyncClient.beginReverseSearchAddressBatch&#40;list2&#41;.getSyncPoller&#40;&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.reverse_search_address_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult> beginGetReverseSearchAddressBatch(
        String batchId) {
        return this.beginGetReverseSearchAddressBatch(batchId, null);
    }

    /**
     * Returns a batch of previously searched addressed given a batch id.
     * <!-- src_embed com.azure.maps.search.async.reverse_search_address_batch  -->
     * <pre>
     * List&lt;ReverseSearchAddressOptions&gt; list2 = new ArrayList&lt;&gt;&#40;&#41;;
     * list2.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;2.294911, 48.858561&#41;&#41;&#41;;
     * list2.add&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.127896, 47.639765&#41;&#41;
     *         .setRadiusInMeters&#40;5000&#41;
     * &#41;;
     * list2.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.348170, 47.621028&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Reverse Search Address Batch Async&quot;&#41;;
     * BatchReverseSearchResult batchReverseSearchResult =
     *     asyncClient.beginReverseSearchAddressBatch&#40;list2&#41;.getSyncPoller&#40;&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.async.reverse_search_address_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @param context a {@link Context} object for distributed tracing.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult> beginGetReverseSearchAddressBatch(String batchId,
        Context context) {
        return createReversePollerFlux(
            () -> this.serviceClient.getReverseSearchAddressBatchNoCustomHeadersWithResponseAsync(batchId, context)
                .flatMap(response -> Mono.just(Utility.createBatchReverseSearchResponse(response))
                    .onErrorMap(MapsSearchAsyncClient::mapThrowable)), this.reverseStrategy);
    }

    // create a poller for a forward search operation
    private PollerFlux<BatchSearchResult, BatchSearchResult> createPollerFlux(
        Supplier<Mono<? extends Response<?>>> initialOperation,
        DefaultPollingStrategy<BatchSearchResult, BatchSearchResult> strategy) {

        // batch search type reference
        BatchSearchTypeReference reference = new BatchSearchTypeReference();

        // Create poller instance
        return PollerFlux.create(Duration.ofSeconds(POLLING_FREQUENCY), context -> initialOperation.get()
                .flatMap(response -> strategy.canPoll(response).flatMap(canPoll -> {
                    if (!canPoll) {
                        return Mono.error(new IllegalStateException(
                            "Cannot poll with strategy " + strategy.getClass().getSimpleName()));
                    }
                    context.setData(POLLING_BATCH_HEADER_KEY, Utility.getBatchId(response.getHeaders()));
                    return strategy.onInitialResponse(response, context, reference);
                })), context -> strategy.poll(context, reference), strategy::cancel,
            context -> strategy.getResult(context, reference)
                .flatMap(result -> {
                    final String batchId = context.getData(POLLING_BATCH_HEADER_KEY);
                    result.setBatchId(batchId);
                    return Mono.just(result);
                }));
    }

    // create a poller for the reverse search operation
    private PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult> createReversePollerFlux(
        Supplier<Mono<? extends Response<?>>> initialOperation,
        DefaultPollingStrategy<BatchReverseSearchResult, BatchReverseSearchResult> strategy) {

        // batch search type reference
        ReverseBatchSearchTypeReference reference = new ReverseBatchSearchTypeReference();

        // Create poller instance
        return PollerFlux.create(Duration.ofSeconds(POLLING_FREQUENCY), context -> initialOperation.get()
                .flatMap(response -> strategy.canPoll(response).flatMap(canPoll -> {
                    if (!canPoll) {
                        return Mono.error(new IllegalStateException(
                            "Cannot poll with strategy " + strategy.getClass().getSimpleName()));
                    }
                    context.setData(POLLING_BATCH_HEADER_KEY, Utility.getBatchId(response.getHeaders()));
                    return strategy.onInitialResponse(response, context, reference);
                })), context -> strategy.poll(context, reference), strategy::cancel,
            context -> strategy.getResult(context, reference)
                .flatMap(result -> {
                    final String batchId = context.getData(POLLING_BATCH_HEADER_KEY);
                    result.setBatchId(batchId);
                    return Mono.just(result);
                }));
    }

    private static Throwable mapThrowable(Throwable throwable) {
        if (!(throwable instanceof ErrorResponseException)) {
            return throwable;
        }
        ErrorResponseException exception = (ErrorResponseException) throwable;
        return new HttpResponseException(exception.getMessage(), exception.getResponse());
    }
}
