// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.maps.search.implementation.models.ErrorResponseException;
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

import java.util.List;

/**
 * {@link MapsSearchClient} instances are created via the {@link MapsSearchClientBuilder}, as shown below.
 * Creating a sync client using a {@link AzureKeyCredential}:
 * <!-- src_embed com.azure.maps.search.sync.builder.key.instantiation -->
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
 * MapsSearchClient client = builder.buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.maps.search.sync.builder.ad.instantiation -->
 */
@ServiceClient(builder = MapsSearchClientBuilder.class)
public final class MapsSearchClient {
    private final MapsSearchAsyncClient asyncClient;

    /**
     * Initializes an instance of Searches client.
     *
     * @param asyncClient the service client implementation.
     */
    MapsSearchClient(MapsSearchAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * List Polygons
     * <!-- src_embed com.azure.maps.search.sync.get_polygon -->
     * <pre>
     * SearchAddressResult results = client.fuzzySearch&#40;
     *     new FuzzySearchOptions&#40;&quot;1 Microsoft Way&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setTop&#40;5&#41;&#41;;
     * Response&lt;SearchAddressResult&gt; response = client.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;, null&#41;;
     * String id = response.getValue&#40;&#41;.getResults&#40;&#41;.get&#40;0&#41;.getDataSource&#40;&#41;.getGeometry&#40;&#41;;
     * List&lt;String&gt; ids = results.getResults&#40;&#41;.stream&#40;&#41;
     *     .filter&#40;item -&gt; item.getDataSource&#40;&#41; != null &amp;&amp; item.getDataSource&#40;&#41;.getGeometry&#40;&#41; != null&#41;
     *     .map&#40;item -&gt; item.getDataSource&#40;&#41;.getGeometry&#40;&#41;&#41;
     *     .collect&#40;Collectors.toList&#40;&#41;&#41;;
     * ids.add&#40;id&#41;;
     *
     * if &#40;ids != null &amp;&amp; !ids.isEmpty&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;Get Polygon: &quot; + ids&#41;;
     *     client.getPolygons&#40;ids&#41;;
     *     client.getPolygonsWithResponse&#40;ids, null&#41;.getValue&#40;&#41;.getClass&#40;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.maps.search.sync.get_polygon -->
     *
     * @param geometryIds Comma separated list of geometry UUIDs, previously retrieved from an Online Search request.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Polygon call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<MapsPolygon> getPolygons(List<String> geometryIds) {
        return this.asyncClient.getPolygons(geometryIds).block();
    }

    /**
     * List Polygons
     * <!-- src_embed com.azure.maps.search.sync.get_polygon -->
     * <pre>
     * SearchAddressResult results = client.fuzzySearch&#40;
     *     new FuzzySearchOptions&#40;&quot;1 Microsoft Way&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setTop&#40;5&#41;&#41;;
     * Response&lt;SearchAddressResult&gt; response = client.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;, null&#41;;
     * String id = response.getValue&#40;&#41;.getResults&#40;&#41;.get&#40;0&#41;.getDataSource&#40;&#41;.getGeometry&#40;&#41;;
     * List&lt;String&gt; ids = results.getResults&#40;&#41;.stream&#40;&#41;
     *     .filter&#40;item -&gt; item.getDataSource&#40;&#41; != null &amp;&amp; item.getDataSource&#40;&#41;.getGeometry&#40;&#41; != null&#41;
     *     .map&#40;item -&gt; item.getDataSource&#40;&#41;.getGeometry&#40;&#41;&#41;
     *     .collect&#40;Collectors.toList&#40;&#41;&#41;;
     * ids.add&#40;id&#41;;
     *
     * if &#40;ids != null &amp;&amp; !ids.isEmpty&#40;&#41;&#41; &#123;
     *     System.out.println&#40;&quot;Get Polygon: &quot; + ids&#41;;
     *     client.getPolygons&#40;ids&#41;;
     *     client.getPolygonsWithResponse&#40;ids, null&#41;.getValue&#40;&#41;.getClass&#40;&#41;;
     * &#125;
     * </pre>
     * <!-- end com.azure.maps.search.sync.get_polygon -->
     *
     * @param geometryIds Comma separated list of geometry UUIDs, previously retrieved from an Online Search request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Polygon call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<MapsPolygon>> getPolygonsWithResponse(List<String> geometryIds, Context context) {
        return this.asyncClient.getPolygonsWithResponse(geometryIds, context).block();
    }

    /**
     * Fuzzy Search
     * <!-- src_embed com.azure.maps.search.sync.fuzzy_search -->
     * <pre>
     * System.out.println&#40;&quot;Search Fuzzy:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.fuzzySearch&#40;new FuzzySearchOptions&#40;&quot;starbucks&quot;&#41;&#41;;
     *
     * &#47;&#47; with options
     * SearchAddressResult results = client.fuzzySearch&#40;
     *     new FuzzySearchOptions&#40;&quot;1 Microsoft Way&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; with response
     * Response&lt;SearchAddressResult&gt; response = client.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;, null&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.fuzzy_search -->
     *
     * @param options {@link FuzzySearchOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Fuzzy Search call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult fuzzySearch(FuzzySearchOptions options) {
        return this.asyncClient.fuzzySearch(options).block();
    }

    /**
     * Fuzzy Search
     * <!-- src_embed com.azure.maps.search.sync.fuzzy_search -->
     * <pre>
     * System.out.println&#40;&quot;Search Fuzzy:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.fuzzySearch&#40;new FuzzySearchOptions&#40;&quot;starbucks&quot;&#41;&#41;;
     *
     * &#47;&#47; with options
     * SearchAddressResult results = client.fuzzySearch&#40;
     *     new FuzzySearchOptions&#40;&quot;1 Microsoft Way&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; with response
     * Response&lt;SearchAddressResult&gt; response = client.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;, null&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.fuzzy_search -->
     *
     * @param query the query string used in the search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Fuzzy Search call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult fuzzySearch(String query) {
        return this.asyncClient.fuzzySearch(new FuzzySearchOptions(query)).block();
    }

    /**
     * Fuzzy Search
     * <!-- src_embed com.azure.maps.search.sync.fuzzy_search -->
     * <pre>
     * System.out.println&#40;&quot;Search Fuzzy:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.fuzzySearch&#40;new FuzzySearchOptions&#40;&quot;starbucks&quot;&#41;&#41;;
     *
     * &#47;&#47; with options
     * SearchAddressResult results = client.fuzzySearch&#40;
     *     new FuzzySearchOptions&#40;&quot;1 Microsoft Way&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; with response
     * Response&lt;SearchAddressResult&gt; response = client.fuzzySearchWithResponse&#40;
     *     new FuzzySearchOptions&#40;&quot;Monaco&quot;&#41;.setEntityType&#40;GeographicEntityType.COUNTRY&#41;
     *         .setTop&#40;5&#41;, null&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.fuzzy_search -->
     *
     * @param options {@link FuzzySearchOptions} the options to be used in this search.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Fuzzy Search call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> fuzzySearchWithResponse(FuzzySearchOptions options, Context context) {
        return this.asyncClient.fuzzySearchWithResponse(options, context).block();
    }

    /**
     * Search Point of Interest
     * <!-- src_embed com.azure.maps.search.sync.get_search_poi -->
     * <pre>
     * System.out.println&#40;&quot;Search Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; coordinates
     * client.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;;
     *
     * &#47;&#47; with response
     * client.searchPointOfInterestWithResponse&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.get_search_poi -->
     *
     * @param options {@link SearchPointOfInterestOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchPointOfInterest(SearchPointOfInterestOptions options) {
        return this.asyncClient.searchPointOfInterest(options).block();
    }

     /**
     * Search Point of Interest
     * <!-- src_embed com.azure.maps.search.sync.get_search_poi -->
     * <pre>
     * System.out.println&#40;&quot;Search Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; coordinates
     * client.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;;
     *
     * &#47;&#47; with response
     * client.searchPointOfInterestWithResponse&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.get_search_poi -->
     *
     * @param query The query to be used to search for points of interest.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchPointOfInterest(String query) {
        return this.asyncClient.searchPointOfInterest(new SearchPointOfInterestOptions(query)).block();
    }

    /**
     * Search Point of Interest
     * <!-- src_embed com.azure.maps.search.sync.get_search_poi -->
     * <pre>
     * System.out.println&#40;&quot;Search Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; coordinates
     * client.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchPointOfInterest&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;&#41;;
     *
     * &#47;&#47; with response
     * client.searchPointOfInterestWithResponse&#40;
     *     new SearchPointOfInterestOptions&#40;&quot;pizza&quot;, new GeoPosition&#40;-121.97483, 36.98844&#41;&#41;
     *         .setTop&#40;10&#41;
     *         .setOperatingHours&#40;OperatingHoursRange.NEXT_SEVEN_DAYS&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.get_search_poi -->
     *
     * @param options {@link SearchPointOfInterestOptions} the options to be used in this search.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchPointOfInterestWithResponse(SearchPointOfInterestOptions options,
        Context context) {
        return this.asyncClient.searchPointOfInterestWithResponse(options, context).block();
    }

    /**
     * Search Nearby Points of Interest
     * <!-- src_embed com.azure.maps.search.sync.search_nearby -->
     * <pre>
     * System.out.println&#40;&quot;Search Nearby Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; options
     * client.searchNearbyPointsOfInterest&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;;
     *
     * &#47;&#47; response
     * client.searchNearbyPointsOfInterestWithResponse&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_nearby -->
     *
     * @param options {@link SearchNearbyPointsOfInterestOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Nearby Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchNearbyPointsOfInterest(SearchNearbyPointsOfInterestOptions options) {
        return this.asyncClient.searchNearbyPointsOfInterest(options).block();
    }

    /**
     * Search Nearby Points of Interest
     * <!-- src_embed com.azure.maps.search.sync.search_nearby -->
     * <pre>
     * System.out.println&#40;&quot;Search Nearby Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; options
     * client.searchNearbyPointsOfInterest&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;;
     *
     * &#47;&#47; response
     * client.searchNearbyPointsOfInterestWithResponse&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_nearby -->
     *
     * @param query A pair of coordinates for query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Nearby Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchNearbyPointsOfInterest(GeoPosition query) {
        return this.asyncClient.searchNearbyPointsOfInterest(new SearchNearbyPointsOfInterestOptions(query)).block();
    }

    /**
     * Search Nearby Points of Interest
     * <!-- src_embed com.azure.maps.search.sync.search_nearby -->
     * <pre>
     * System.out.println&#40;&quot;Search Nearby Points of Interest:&quot;&#41;;
     *
     * &#47;&#47; options
     * client.searchNearbyPointsOfInterest&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;&#41;;
     *
     * &#47;&#47; response
     * client.searchNearbyPointsOfInterestWithResponse&#40;
     *     new SearchNearbyPointsOfInterestOptions&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCountryFilter&#40;Arrays.asList&#40;&quot;US&quot;&#41;&#41;
     *         .setTop&#40;10&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_nearby -->
     *
     * @param options {@link SearchNearbyPointsOfInterestOptions} the options to be used in this search.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Nearby Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchNearbyPointsOfInterestWithResponse(
        SearchNearbyPointsOfInterestOptions options, Context context) {
        return this.asyncClient.searchNearbyPointsOfInterestWithResponse(options, context).block();
    }

    /**
     * Search Point of Interest per Category
     * <!-- src_embed com.azure.maps.search.sync.search_poi_category -->
     * <pre>
     * System.out.println&#40;&quot;Get Point of Interest Category:&quot;&#41;;
     *
     * &#47;&#47; complete - search for italian restaurant in NYC
     * client.searchPointOfInterestCategory&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; with response
     * client.searchPointOfInterestCategoryWithResponse&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_poi_category -->
     *
     * @param options a {@link SearchPointOfInterestCategoryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest per Category calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchPointOfInterestCategory(SearchPointOfInterestCategoryOptions options) {
        return this.asyncClient.searchPointOfInterestCategory(options).block();
    }

    /**
     * Search Point of Interest per Category
     * <!-- src_embed com.azure.maps.search.sync.search_poi_category -->
     * <pre>
     * System.out.println&#40;&quot;Get Point of Interest Category:&quot;&#41;;
     *
     * &#47;&#47; complete - search for italian restaurant in NYC
     * client.searchPointOfInterestCategory&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; with response
     * client.searchPointOfInterestCategoryWithResponse&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_poi_category -->
     *
     * @param query The query to be used to search for points of interest.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest per Category calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchPointOfInterestCategory(String query) {
        return this.asyncClient.searchPointOfInterestCategory(new SearchPointOfInterestCategoryOptions(query)).block();
    }

    /**
     * Search Point of Interest per Category
     * <!-- src_embed com.azure.maps.search.sync.search_poi_category -->
     * <pre>
     * System.out.println&#40;&quot;Get Point of Interest Category:&quot;&#41;;
     *
     * &#47;&#47; complete - search for italian restaurant in NYC
     * client.searchPointOfInterestCategory&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; with response
     * client.searchPointOfInterestCategoryWithResponse&#40;
     *     new SearchPointOfInterestCategoryOptions&#40;&quot;pasta&quot;, new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;3&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_poi_category -->
     *
     * @param options a {@link SearchPointOfInterestCategoryOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest per Category calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchPointOfInterestCategoryWithResponse(
        SearchPointOfInterestCategoryOptions options, Context context) {
        return this.asyncClient.searchPointOfInterestCategoryWithResponse(options, context).block();
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
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful POI Category Tree call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PointOfInterestCategoryTreeResult getPointOfInterestCategoryTree() {
        return this.asyncClient.getPointOfInterestCategoryTree().block();
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
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful POI Category Tree call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PointOfInterestCategoryTreeResult> getPointOfInterestCategoryTreeWithResponse(String language,
        Context context) {
        return this.asyncClient.getPointOfInterestCategoryTreeWithResponse(language, context).block();
    }

    /**
     * Search Address
     *
     * <!-- src_embed com.azure.maps.search.sync.search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;15127 NE 24th Street, Redmond, WA 98052&quot;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.searchAddressWithResponse&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;, null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_address -->
     *
     * @param options a {@link SearchAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchAddress(SearchAddressOptions options) {
        return this.asyncClient.searchAddress(options).block();
    }

    /**
     * Search Address
     * <!-- src_embed com.azure.maps.search.sync.search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;15127 NE 24th Street, Redmond, WA 98052&quot;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.searchAddressWithResponse&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;, null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_address -->
     *
     * @param query the query string used in the fuzzy search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchAddress(String query) {
        return this.asyncClient.searchAddress(new SearchAddressOptions(query)).block();
    }

    /**
     * Search Address
     * <!-- src_embed com.azure.maps.search.sync.search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;15127 NE 24th Street, Redmond, WA 98052&quot;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchAddress&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.searchAddressWithResponse&#40;
     *     new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *         .setCoordinates&#40;new GeoPosition&#40;-74.011454, 40.706270&#41;&#41;
     *         .setRadiusInMeters&#40;40000&#41;
     *         .setTop&#40;5&#41;, null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_address -->
     *
     * @param options a {@link SearchAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchAddressWithResponse(SearchAddressOptions options, Context context) {
        return this.asyncClient.searchAddressWithResponse(options, context).block();
    }

    /**
     * Reverse Address Search
     * <!-- src_embed com.azure.maps.search.sync.reverse_search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Reverse:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * client.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41; &#47;&#47; returns only city
     * &#41;;
     *
     * &#47;&#47; complete
     * client.reverseSearchAddressWithResponse&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41;,
     *         null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.reverse_search_address -->
     *
     * @param options a {@link ReverseSearchAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReverseSearchAddressResult reverseSearchAddress(ReverseSearchAddressOptions options) {
        return this.asyncClient.reverseSearchAddress(options).block();
    }

    /**
     * Reverse Address Search
     * <!-- src_embed com.azure.maps.search.sync.reverse_search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Reverse:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * client.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41; &#47;&#47; returns only city
     * &#41;;
     *
     * &#47;&#47; complete
     * client.reverseSearchAddressWithResponse&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41;,
     *         null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.reverse_search_address -->
     *
     * @param query The applicable query as a pair of coordinates.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReverseSearchAddressResult reverseSearchAddress(GeoPosition query) {
        return this.asyncClient.reverseSearchAddress(new ReverseSearchAddressOptions(query)).block();
    }

    /**
     * Reverse Address Search
     * <!-- src_embed com.azure.maps.search.sync.reverse_search_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Reverse:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * client.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.reverseSearchAddress&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41; &#47;&#47; returns only city
     * &#41;;
     *
     * &#47;&#47; complete
     * client.reverseSearchAddressWithResponse&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setIncludeSpeedLimit&#40;true&#41;
     *         .setEntityType&#40;GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION&#41;,
     *         null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.reverse_search_address -->
     *
     * @param options a {@link ReverseSearchAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ReverseSearchAddressResult> reverseSearchAddressWithResponse(ReverseSearchAddressOptions options,
        Context context) {
        return this.asyncClient.reverseSearchAddressWithResponse(options, context).block();
    }

    /**
     * Reverse Address Search to a Cross Street
     * <!-- src_embed com.azure.maps.search.sync.search_reverse_cross_street_address -->
     * <pre>
     * System.out.println&#40;&quot;Revere Search Cross Street Address:&quot;&#41;;
     *
     * &#47;&#47; options
     * client.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.reverseSearchCrossStreetAddressWithResponse&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_reverse_cross_street_address -->
     *
     * @param options a {@link ReverseSearchCrossStreetAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReverseSearchCrossStreetAddressResult reverseSearchCrossStreetAddress(
        ReverseSearchCrossStreetAddressOptions options) {
        return this.asyncClient.reverseSearchCrossStreetAddress(options).block();
    }

    /**
     * Reverse Address Search to a Cross Street
     * <!-- src_embed com.azure.maps.search.sync.search_reverse_cross_street_address -->
     * <pre>
     * System.out.println&#40;&quot;Revere Search Cross Street Address:&quot;&#41;;
     *
     * &#47;&#47; options
     * client.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.reverseSearchCrossStreetAddressWithResponse&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_reverse_cross_street_address -->
     *
     * @param query with a pair of coordinates..
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReverseSearchCrossStreetAddressResult reverseSearchCrossStreetAddress(GeoPosition query) {
        return this.asyncClient.reverseSearchCrossStreetAddress(new ReverseSearchCrossStreetAddressOptions(query))
            .block();
    }

    /**
     * Reverse Address Search to a Cross Street
     * <!-- src_embed com.azure.maps.search.sync.search_reverse_cross_street_address -->
     * <pre>
     * System.out.println&#40;&quot;Revere Search Cross Street Address:&quot;&#41;;
     *
     * &#47;&#47; options
     * client.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;&#41;;
     *
     * &#47;&#47; options
     * client.reverseSearchCrossStreetAddress&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.reverseSearchCrossStreetAddressWithResponse&#40;
     *     new ReverseSearchCrossStreetAddressOptions&#40;new GeoPosition&#40;-121.89, 37.337&#41;&#41;
     *         .setTop&#40;2&#41;
     *         .setHeading&#40;5&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_reverse_cross_street_address -->
     *
     * @param options a {@link ReverseSearchCrossStreetAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ReverseSearchCrossStreetAddressResult> reverseSearchCrossStreetAddressWithResponse(
        ReverseSearchCrossStreetAddressOptions options, Context context) {
        return this.asyncClient.reverseSearchCrossStreetAddressWithResponse(options, context).block();
    }

    /**
     * Structured Address Search
     * <!-- src_embed com.azure.maps.search.sync.search_structured_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Structured:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.searchStructuredAddress&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;, null&#41;;
     *
     * &#47;&#47; complete
     * client.searchStructuredAddressWithResponse&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;,
     *     new SearchStructuredAddressOptions&#40;&#41;
     *             .setTop&#40;2&#41;
     *             .setRadiusInMeters&#40;1000&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_structured_address -->
     *
     * @param address a {@link StructuredAddress} to be searched by the API.
     * @param options a {@link SearchStructuredAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchStructuredAddress(StructuredAddress address,
        SearchStructuredAddressOptions options) {
        return this.asyncClient.searchStructuredAddress(address, options).block();
    }

    /**
     * Structured Address Search
     * <!-- src_embed com.azure.maps.search.sync.search_structured_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Structured:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.searchStructuredAddress&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;, null&#41;;
     *
     * &#47;&#47; complete
     * client.searchStructuredAddressWithResponse&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;,
     *     new SearchStructuredAddressOptions&#40;&#41;
     *             .setTop&#40;2&#41;
     *             .setRadiusInMeters&#40;1000&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_structured_address -->
     *
     * @param countryCode the country code for query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchStructuredAddress(String countryCode) {
        return this.asyncClient.searchStructuredAddress(new StructuredAddress(countryCode), null).block();
    }

    /**
     * Structured Address Search
     * <!-- src_embed com.azure.maps.search.sync.search_structured_address -->
     * <pre>
     * System.out.println&#40;&quot;Search Address Structured:&quot;&#41;;
     *
     * &#47;&#47; simple
     * client.searchStructuredAddress&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;, null&#41;;
     *
     * &#47;&#47; complete
     * client.searchStructuredAddressWithResponse&#40;new StructuredAddress&#40;&quot;US&quot;&#41;
     *     .setPostalCode&#40;&quot;98121&quot;&#41;
     *     .setStreetNumber&#40;&quot;15127&quot;&#41;
     *     .setStreetName&#40;&quot;NE 24th Street&quot;&#41;
     *     .setMunicipality&#40;&quot;Redmond&quot;&#41;
     *     .setCountrySubdivision&#40;&quot;WA&quot;&#41;,
     *     new SearchStructuredAddressOptions&#40;&#41;
     *             .setTop&#40;2&#41;
     *             .setRadiusInMeters&#40;1000&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_structured_address -->
     *
     * @param address a {@link StructuredAddress} to be searched by the API.
     * @param options a {@link SearchStructuredAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Structured Address Search call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchStructuredAddressWithResponse(StructuredAddress address,
        SearchStructuredAddressOptions options, Context context) {
        return this.asyncClient.searchStructuredAddressWithResponse(address, options, context).block();
    }

    /**
     * Search Inside Geometry
     * <!-- src_embed com.azure.maps.search.sync.search_inside_geometry -->
     * <pre>
     * System.out.println&#40;&quot;Search Inside Geometry&quot;&#41;;
     *
     * &#47;&#47; create GeoPolygon
     * List&lt;GeoPosition&gt; coordinates = new ArrayList&lt;&gt;&#40;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.43301391601562, 37.70660472542312&#41;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.36434936523438, 37.712059855877314&#41;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * GeoLinearRing ring = new GeoLinearRing&#40;coordinates&#41;;
     * GeoPolygon polygon = new GeoPolygon&#40;ring&#41;;
     *
     * &#47;&#47; simple
     * client.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, polygon&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, polygon&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.searchInsideGeometryWithResponse&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, polygon&#41;
     *         .setTop&#40;5&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_inside_geometry -->
     *
     * @param options a {@link SearchInsideGeometryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Inside Geometry call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchInsideGeometry(SearchInsideGeometryOptions options) {
        return this.asyncClient.searchInsideGeometry(options).block();
    }

    /**
     * Search Inside Geometry
     * <!-- src_embed com.azure.maps.search.sync.search_inside_geometry -->
     * <pre>
     * System.out.println&#40;&quot;Search Inside Geometry&quot;&#41;;
     *
     * &#47;&#47; create GeoPolygon
     * List&lt;GeoPosition&gt; coordinates = new ArrayList&lt;&gt;&#40;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.43301391601562, 37.70660472542312&#41;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.36434936523438, 37.712059855877314&#41;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * GeoLinearRing ring = new GeoLinearRing&#40;coordinates&#41;;
     * GeoPolygon polygon = new GeoPolygon&#40;ring&#41;;
     *
     * &#47;&#47; simple
     * client.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, polygon&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, polygon&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.searchInsideGeometryWithResponse&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, polygon&#41;
     *         .setTop&#40;5&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_inside_geometry -->
     *
     * @param query query string
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Inside Geometry call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchInsideGeometry(String query) {
        return this.asyncClient.searchInsideGeometry(new SearchInsideGeometryOptions(query)).block();
    }

    /**
     * Search Inside Geometry
     * <!-- src_embed com.azure.maps.search.sync.search_inside_geometry -->
     * <pre>
     * System.out.println&#40;&quot;Search Inside Geometry&quot;&#41;;
     *
     * &#47;&#47; create GeoPolygon
     * List&lt;GeoPosition&gt; coordinates = new ArrayList&lt;&gt;&#40;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.43301391601562, 37.70660472542312&#41;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.36434936523438, 37.712059855877314&#41;&#41;;
     * coordinates.add&#40;new GeoPosition&#40;-122.43576049804686, 37.7524152343544&#41;&#41;;
     * GeoLinearRing ring = new GeoLinearRing&#40;coordinates&#41;;
     * GeoPolygon polygon = new GeoPolygon&#40;ring&#41;;
     *
     * &#47;&#47; simple
     * client.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, polygon&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchInsideGeometry&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, polygon&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.searchInsideGeometryWithResponse&#40;
     *     new SearchInsideGeometryOptions&#40;&quot;Leland Avenue&quot;, polygon&#41;
     *         .setTop&#40;5&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_inside_geometry -->
     *
     * @param options a {@link SearchInsideGeometryOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Inside Geometry call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchInsideGeometryWithResponse(SearchInsideGeometryOptions options,
        Context context) {
        return this.asyncClient.searchInsideGeometryWithResponse(options, context).block();
    }

    /**
     * Search Along Route
     * <!-- src_embed com.azure.maps.search.sync.search_along_route -->
     * <pre>
     * System.out.println&#40;&quot;Search Along Route&quot;&#41;;
     *
     * &#47;&#47; create route points
     * List&lt;GeoPosition&gt; points = new ArrayList&lt;&gt;&#40;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.143035, 47.653536&#41;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.187164, 47.617556&#41;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.114981, 47.570599&#41;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.132756, 47.654009&#41;&#41;;
     * GeoLineString route = new GeoLineString&#40;points&#41;;
     *
     * &#47;&#47; simple
     * client.searchAlongRoute&#40;new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, route&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, route&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.searchAlongRouteWithResponse&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, route&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;5&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_along_route -->
     *
     * @param options a {@link SearchAlongRouteOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Along Route call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchAlongRoute(SearchAlongRouteOptions options) {
        return this.asyncClient.searchAlongRoute(options).block();
    }

    /**
     * Search Along Route
     * <!-- src_embed com.azure.maps.search.sync.search_along_route -->
     * <pre>
     * System.out.println&#40;&quot;Search Along Route&quot;&#41;;
     *
     * &#47;&#47; create route points
     * List&lt;GeoPosition&gt; points = new ArrayList&lt;&gt;&#40;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.143035, 47.653536&#41;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.187164, 47.617556&#41;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.114981, 47.570599&#41;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.132756, 47.654009&#41;&#41;;
     * GeoLineString route = new GeoLineString&#40;points&#41;;
     *
     * &#47;&#47; simple
     * client.searchAlongRoute&#40;new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, route&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, route&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.searchAlongRouteWithResponse&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, route&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;5&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_along_route -->
     *
     * @param query the search query
     * @param maxDetourTime the maximum detour time allowed
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Along Route call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchAlongRoute(String query, int maxDetourTime) {
        return this.asyncClient.searchAlongRoute(new SearchAlongRouteOptions(query, maxDetourTime)).block();
    }

    /**
     * Search Along Route
     * <!-- src_embed com.azure.maps.search.sync.search_along_route -->
     * <pre>
     * System.out.println&#40;&quot;Search Along Route&quot;&#41;;
     *
     * &#47;&#47; create route points
     * List&lt;GeoPosition&gt; points = new ArrayList&lt;&gt;&#40;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.143035, 47.653536&#41;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.187164, 47.617556&#41;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.114981, 47.570599&#41;&#41;;
     * points.add&#40;new GeoPosition&#40;-122.132756, 47.654009&#41;&#41;;
     * GeoLineString route = new GeoLineString&#40;points&#41;;
     *
     * &#47;&#47; simple
     * client.searchAlongRoute&#40;new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, route&#41;&#41;;
     *
     * &#47;&#47; options
     * client.searchAlongRoute&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, route&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;5&#41;&#41;;
     *
     * &#47;&#47; complete
     * client.searchAlongRouteWithResponse&#40;
     *     new SearchAlongRouteOptions&#40;&quot;burger&quot;, 1000, route&#41;
     *         .setCategoryFilter&#40;Arrays.asList&#40;7315&#41;&#41;
     *         .setTop&#40;5&#41;,
     *     null&#41;.getStatusCode&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_along_route -->
     *
     * @param options a {@link SearchAlongRouteOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Along Route call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchAlongRouteWithResponse(SearchAlongRouteOptions options,
        Context context) {
        return this.asyncClient.searchAlongRouteWithResponse(options, context).block();
    }

    /**
     * Batch Fuzzy Search
     * <!-- src_embed com.azure.maps.search.sync.fuzzy_search_batch -->
     * <pre>
     * List&lt;FuzzySearchOptions&gt; fuzzyOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;atm&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;.setTop&#40;5&#41;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Statue of Liberty&quot;&#41;.setTop&#40;2&#41;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Starbucks&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;&#41;;
     *
     * System.out.println&#40;&quot;Post Search Fuzzy Batch Async&quot;&#41;;
     * client.beginFuzzySearchBatch&#40;fuzzyOptionsList&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.fuzzy_search_batch -->
     *
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Batch Fuzzy Search service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginFuzzySearchBatch(
        List<FuzzySearchOptions> optionsList) {
        return this.asyncClient.beginFuzzySearchBatch(optionsList).getSyncPoller();
    }

    /**
     * Batch Fuzzy Search
     * <!-- src_embed com.azure.maps.search.sync.fuzzy_search_batch -->
     * <pre>
     * List&lt;FuzzySearchOptions&gt; fuzzyOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;atm&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;.setTop&#40;5&#41;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Statue of Liberty&quot;&#41;.setTop&#40;2&#41;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Starbucks&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;&#41;;
     *
     * System.out.println&#40;&quot;Post Search Fuzzy Batch Async&quot;&#41;;
     * client.beginFuzzySearchBatch&#40;fuzzyOptionsList&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.fuzzy_search_batch -->
     *
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginFuzzySearchBatch(List<FuzzySearchOptions> optionsList,
        Context context) {
        return this.asyncClient.beginFuzzySearchBatch(optionsList, context).getSyncPoller();
    }

    /**
     * Get Fuzzy Batch Search by Id
     * <!-- src_embed com.azure.maps.search.sync.fuzzy_search_batch -->
     * <pre>
     * List&lt;FuzzySearchOptions&gt; fuzzyOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;atm&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;.setTop&#40;5&#41;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Statue of Liberty&quot;&#41;.setTop&#40;2&#41;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Starbucks&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;&#41;;
     *
     * System.out.println&#40;&quot;Post Search Fuzzy Batch Async&quot;&#41;;
     * client.beginFuzzySearchBatch&#40;fuzzyOptionsList&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.fuzzy_search_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginGetFuzzySearchBatch(String batchId) {
        return this.asyncClient.beginGetFuzzySearchBatch(batchId).getSyncPoller();
    }

    /**
     * Get Fuzzy Batch Search by Id
     * <!-- src_embed com.azure.maps.search.sync.fuzzy_search_batch -->
     * <pre>
     * List&lt;FuzzySearchOptions&gt; fuzzyOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;atm&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;.setTop&#40;5&#41;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Statue of Liberty&quot;&#41;.setTop&#40;2&#41;&#41;;
     * fuzzyOptionsList.add&#40;new FuzzySearchOptions&#40;&quot;Starbucks&quot;, new GeoPosition&#40;-122.128362, 47.639769&#41;&#41;
     *     .setRadiusInMeters&#40;5000&#41;&#41;;
     *
     * System.out.println&#40;&quot;Post Search Fuzzy Batch Async&quot;&#41;;
     * client.beginFuzzySearchBatch&#40;fuzzyOptionsList&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.fuzzy_search_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginGetFuzzySearchBatch(String batchId, Context context) {
        return this.asyncClient.beginGetFuzzySearchBatch(batchId, context).getSyncPoller();
    }

    /**
     * Batch Address Search
     * <!-- src_embed com.azure.maps.search.sync.search_address_batch -->
     * <pre>
     * List&lt;SearchAddressOptions&gt; optionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;400 Broad St, Seattle, WA 98109&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;One, Microsoft Way, Redmond, WA 98052&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;350 5th Ave, New York, NY 10118&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *     .setCountryFilter&#40;Arrays.asList&#40;&quot;GB&quot;, &quot;US&quot;, &quot;AU&quot;&#41;&#41;.setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; Search address batch async -
     * &#47;&#47; https:&#47;&#47;docs.microsoft.com&#47;en-us&#47;rest&#47;api&#47;maps&#47;search&#47;post-search-address-batch
     * &#47;&#47; This call posts addresses for search using the Asynchronous Batch API.
     * &#47;&#47; SyncPoller will do the polling automatically and you can retrieve the result
     * &#47;&#47; with getFinalResult&#40;&#41;
     * System.out.println&#40;&quot;Search Address Batch Async&quot;&#41;;
     * client.beginSearchAddressBatch&#40;optionsList&#41;.getFinalResult&#40;&#41;;
     * SyncPoller&lt;BatchSearchResult, BatchSearchResult&gt; poller = client.beginSearchAddressBatch&#40;optionsList&#41;;
     * BatchSearchResult result = poller.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_address_batch -->
     *
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginSearchAddressBatch(
        List<SearchAddressOptions> optionsList) {
        return this.asyncClient.beginSearchAddressBatch(optionsList).getSyncPoller();
    }

    /**
     * Batch Address Search
     * <!-- src_embed com.azure.maps.search.sync.search_address_batch -->
     * <pre>
     * List&lt;SearchAddressOptions&gt; optionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;400 Broad St, Seattle, WA 98109&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;One, Microsoft Way, Redmond, WA 98052&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;350 5th Ave, New York, NY 10118&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *     .setCountryFilter&#40;Arrays.asList&#40;&quot;GB&quot;, &quot;US&quot;, &quot;AU&quot;&#41;&#41;.setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; Search address batch async -
     * &#47;&#47; https:&#47;&#47;docs.microsoft.com&#47;en-us&#47;rest&#47;api&#47;maps&#47;search&#47;post-search-address-batch
     * &#47;&#47; This call posts addresses for search using the Asynchronous Batch API.
     * &#47;&#47; SyncPoller will do the polling automatically and you can retrieve the result
     * &#47;&#47; with getFinalResult&#40;&#41;
     * System.out.println&#40;&quot;Search Address Batch Async&quot;&#41;;
     * client.beginSearchAddressBatch&#40;optionsList&#41;.getFinalResult&#40;&#41;;
     * SyncPoller&lt;BatchSearchResult, BatchSearchResult&gt; poller = client.beginSearchAddressBatch&#40;optionsList&#41;;
     * BatchSearchResult result = poller.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_address_batch -->
     *
     * @param optionsList a list of {@link SearchAddressOptions} to be searched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginSearchAddressBatch(
        List<SearchAddressOptions> optionsList, Context context) {
        return this.asyncClient.beginSearchAddressBatch(optionsList, context).getSyncPoller();
    }

    /**
     * Get Batch Search Id
     * <!-- src_embed com.azure.maps.search.sync.search_address_batch -->
     * <pre>
     * List&lt;SearchAddressOptions&gt; optionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;400 Broad St, Seattle, WA 98109&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;One, Microsoft Way, Redmond, WA 98052&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;350 5th Ave, New York, NY 10118&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *     .setCountryFilter&#40;Arrays.asList&#40;&quot;GB&quot;, &quot;US&quot;, &quot;AU&quot;&#41;&#41;.setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; Search address batch async -
     * &#47;&#47; https:&#47;&#47;docs.microsoft.com&#47;en-us&#47;rest&#47;api&#47;maps&#47;search&#47;post-search-address-batch
     * &#47;&#47; This call posts addresses for search using the Asynchronous Batch API.
     * &#47;&#47; SyncPoller will do the polling automatically and you can retrieve the result
     * &#47;&#47; with getFinalResult&#40;&#41;
     * System.out.println&#40;&quot;Search Address Batch Async&quot;&#41;;
     * client.beginSearchAddressBatch&#40;optionsList&#41;.getFinalResult&#40;&#41;;
     * SyncPoller&lt;BatchSearchResult, BatchSearchResult&gt; poller = client.beginSearchAddressBatch&#40;optionsList&#41;;
     * BatchSearchResult result = poller.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_address_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginGetSearchAddressBatch(String batchId) {
        return this.asyncClient.beginGetSearchAddressBatch(batchId).getSyncPoller();
    }

    /**
     * Get Batch Search Id
     * <!-- src_embed com.azure.maps.search.sync.search_address_batch -->
     * <pre>
     * List&lt;SearchAddressOptions&gt; optionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;400 Broad St, Seattle, WA 98109&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;One, Microsoft Way, Redmond, WA 98052&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;350 5th Ave, New York, NY 10118&quot;&#41;.setTop&#40;3&#41;&#41;;
     * optionsList.add&#40;new SearchAddressOptions&#40;&quot;1 Main Street&quot;&#41;
     *     .setCountryFilter&#40;Arrays.asList&#40;&quot;GB&quot;, &quot;US&quot;, &quot;AU&quot;&#41;&#41;.setTop&#40;3&#41;&#41;;
     *
     * &#47;&#47; Search address batch async -
     * &#47;&#47; https:&#47;&#47;docs.microsoft.com&#47;en-us&#47;rest&#47;api&#47;maps&#47;search&#47;post-search-address-batch
     * &#47;&#47; This call posts addresses for search using the Asynchronous Batch API.
     * &#47;&#47; SyncPoller will do the polling automatically and you can retrieve the result
     * &#47;&#47; with getFinalResult&#40;&#41;
     * System.out.println&#40;&quot;Search Address Batch Async&quot;&#41;;
     * client.beginSearchAddressBatch&#40;optionsList&#41;.getFinalResult&#40;&#41;;
     * SyncPoller&lt;BatchSearchResult, BatchSearchResult&gt; poller = client.beginSearchAddressBatch&#40;optionsList&#41;;
     * BatchSearchResult result = poller.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.search_address_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginGetSearchAddressBatch(String batchId,
        Context context) {
        return this.asyncClient.beginGetSearchAddressBatch(batchId, context).getSyncPoller();
    }

    /**
     * Searches a batch of addresses given their coordinates.
     * <!-- src_embed com.azure.maps.search.sync.reverse_search_address_batch -->
     * <pre>
     * List&lt;ReverseSearchAddressOptions&gt; reverseOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * reverseOptionsList.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;2.294911, 48.858561&#41;&#41;&#41;;
     * reverseOptionsList.add&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.127896, 47.639765&#41;&#41;
     *         .setRadiusInMeters&#40;5000&#41;
     * &#41;;
     * reverseOptionsList.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.348170, 47.621028&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Reverse Search Address Batch Async&quot;&#41;;
     * BatchReverseSearchResult br1 =
     *     client.beginReverseSearchAddressBatch&#40;reverseOptionsList&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.reverse_search_address_batch -->
     *
     * @param optionsList a list of {@link ReverseSearchAddressOptions} to be searched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchReverseSearchResult, BatchReverseSearchResult> beginReverseSearchAddressBatch(
        List<ReverseSearchAddressOptions> optionsList) {
        return this.asyncClient.beginReverseSearchAddressBatch(optionsList).getSyncPoller();
    }

    /**
     * Searches a batch of addresses given their coordinates.
     * <!-- src_embed com.azure.maps.search.sync.reverse_search_address_batch -->
     * <pre>
     * List&lt;ReverseSearchAddressOptions&gt; reverseOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * reverseOptionsList.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;2.294911, 48.858561&#41;&#41;&#41;;
     * reverseOptionsList.add&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.127896, 47.639765&#41;&#41;
     *         .setRadiusInMeters&#40;5000&#41;
     * &#41;;
     * reverseOptionsList.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.348170, 47.621028&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Reverse Search Address Batch Async&quot;&#41;;
     * BatchReverseSearchResult br1 =
     *     client.beginReverseSearchAddressBatch&#40;reverseOptionsList&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.reverse_search_address_batch -->
     *
     * @param optionsList a list of {@link ReverseSearchAddressOptions} to be searched.
     * @param context a {@link Context} object for distributed tracing.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchReverseSearchResult, BatchReverseSearchResult> beginReverseSearchAddressBatch(
        List<ReverseSearchAddressOptions> optionsList, Context context) {
        return this.asyncClient.beginReverseSearchAddressBatch(optionsList, context).getSyncPoller();
    }

    /**
     * Returns a batch of previously searched addressed given a batch id.
     * <!-- src_embed com.azure.maps.search.sync.reverse_search_address_batch -->
     * <pre>
     * List&lt;ReverseSearchAddressOptions&gt; reverseOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * reverseOptionsList.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;2.294911, 48.858561&#41;&#41;&#41;;
     * reverseOptionsList.add&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.127896, 47.639765&#41;&#41;
     *         .setRadiusInMeters&#40;5000&#41;
     * &#41;;
     * reverseOptionsList.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.348170, 47.621028&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Reverse Search Address Batch Async&quot;&#41;;
     * BatchReverseSearchResult br1 =
     *     client.beginReverseSearchAddressBatch&#40;reverseOptionsList&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.reverse_search_address_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchReverseSearchResult, BatchReverseSearchResult> beginGetReverseSearchAddressBatch(
        String batchId) {
        return this.asyncClient.beginGetReverseSearchAddressBatch(batchId).getSyncPoller();
    }

    /**
     * Returns a batch of previously searched addressed given a batch id.
     * <!-- src_embed com.azure.maps.search.sync.reverse_search_address_batch -->
     * <pre>
     * List&lt;ReverseSearchAddressOptions&gt; reverseOptionsList = new ArrayList&lt;&gt;&#40;&#41;;
     * reverseOptionsList.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;2.294911, 48.858561&#41;&#41;&#41;;
     * reverseOptionsList.add&#40;
     *     new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.127896, 47.639765&#41;&#41;
     *         .setRadiusInMeters&#40;5000&#41;
     * &#41;;
     * reverseOptionsList.add&#40;new ReverseSearchAddressOptions&#40;new GeoPosition&#40;-122.348170, 47.621028&#41;&#41;&#41;;
     *
     * System.out.println&#40;&quot;Reverse Search Address Batch Async&quot;&#41;;
     * BatchReverseSearchResult br1 =
     *     client.beginReverseSearchAddressBatch&#40;reverseOptionsList&#41;.getFinalResult&#40;&#41;;
     * </pre>
     * <!-- end com.azure.maps.search.sync.reverse_search_address_batch -->
     *
     * @param batchId Batch id for querying the operation.
     * @param context a {@link Context} object for distributed tracing.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchReverseSearchResult, BatchReverseSearchResult> beginGetReverseSearchAddressBatch(
        String batchId, Context context) {
        return this.asyncClient.beginGetReverseSearchAddressBatch(batchId, context).getSyncPoller();
    }
}
