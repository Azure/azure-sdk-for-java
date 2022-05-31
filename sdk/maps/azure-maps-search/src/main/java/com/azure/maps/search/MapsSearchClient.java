// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import java.util.List;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.polling.SyncPoller;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.ErrorResponseException;
import com.azure.maps.search.models.FuzzySearchOptions;
import com.azure.maps.search.models.PointOfInterestCategoryTreeResult;
import com.azure.maps.search.models.Polygon;
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

/**
 * {@link MapsSearchClient} instances are created via the {@link MapsSearchClientBuilder}, as shown below.
 * <p/>
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
 * <!-- end com.azure.maps.search.sync.builder.key.instantiation -->
 * Creating a sync client using a {@link TokenCredential}:
 * <p/>
 * <!-- src_embed com.azure.maps.search.sync.builder.ad.instantiation -->
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
     * @param serviceClient the service client implementation.
     */
    MapsSearchClient(MapsSearchAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * List Polygons
     *
     * @param geometryIds
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<Polygon> getPolygons(List<String> geometryIds) {
        return this.asyncClient.getPolygons(geometryIds).block();
    }

    /**
     * **Get Polygon**
     *
     * @param geometryIds Comma separated list of geometry UUIDs, previously retrieved from an Online Search request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Polygon call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<Polygon>> getPolygonsWithResponse(List<String> geometryIds, Context context) {
        return this.asyncClient.getPolygonsWithResponse(geometryIds, context).block();
    }

    /**
     *
     * @param options
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult fuzzySearch(FuzzySearchOptions options){
        return this.asyncClient.fuzzySearch(options).block();
    }

    /**
     * **Free Form Search**
     *
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> fuzzySearchWithResponse(FuzzySearchOptions options, Context context) {
        return this.asyncClient.fuzzySearchWithResponse(options, context).block();
    }

    /**
     *
     * @param options
     * @return
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchPointOfInterest(SearchPointOfInterestOptions options) {
        return this.asyncClient.searchPointOfInterest(options).block();
    }

    /**
     * **Get POI by Name**
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchPointOfInterestWithResponse(SearchPointOfInterestOptions options,
            Context context) {
        return this.asyncClient.searchPointOfInterestWithResponse(options, context).block();
    }

    /**
     * **Nearby Search**
     *
     * <p>**Applies to**: S0 and S1 pricing tiers.
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchNearbyPointOfInterest(SearchNearbyPointsOfInterestOptions options) {
        return this.asyncClient.searchNearbyPointOfInterest(options).block();
    }

    /**
     * **Nearby Search**
     *
     * @param options a {@link SearchNearbyPointsOfInterestOptions} with the search options.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchNearbyPointOfInterestWithResponse(
            SearchNearbyPointsOfInterestOptions options, Context context) {
        return this.asyncClient.searchNearbyPointOfInterestWithResponse(options, context).block();
    }

    /**
     * **Get POI by Category**
     *
     * @param options a {@link SearchPointOfInterestCategoryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchPointOfInterestCategory(SearchPointOfInterestCategoryOptions options) {
        return this.asyncClient.searchPointOfInterestCategory(options).block();
    }

    /**
     * **Get POI by Category**
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchPointOfInterestCategoryWithResponse(
            SearchPointOfInterestCategoryOptions options, Context context) {
        return this.asyncClient.searchPointOfInterestCategoryWithResponse(options, context).block();
    }

    /**
     * **Get POI Category Tree**
     *
     * <p>**Applies to**: S0 and S1 pricing tiers.
     *
     * <p>POI Category API provides a full list of supported Points of Interest (POI) categories and subcategories
     * together with their translations and synonyms. The returned content can be used to provide more meaningful
     * results through other Search Service APIs, like [Get Search
     * POI](https://docs.microsoft.com/rest/api/maps/search/getsearchpoi).
     *
     * @param language Language in which search results should be returned. Should be one of supported IETF language
     *     tags, except NGT and NGT-Latn. Language tag is case insensitive. When data in specified language is not
     *     available for a specific field, default language is used (English).
     *     <p>Please refer to [Supported Languages](https://docs.microsoft.com/azure/azure-maps/supported-languages) for
     *     details.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful POI Category Tree call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PointOfInterestCategoryTreeResult getPointOfInterestCategoryTree(String language) {
        return this.asyncClient.getPointOfInterestCategoryTree(language).block();
    }

    /**
     * **Get POI Category Tree**
     *
     * <p>**Applies to**: S0 and S1 pricing tiers.
     *
     * <p>POI Category API provides a full list of supported Points of Interest (POI) categories and subcategories
     * together with their translations and synonyms. The returned content can be used to provide more meaningful
     * results through other Search Service APIs, like [Get Search
     * POI](https://docs.microsoft.com/rest/api/maps/search/getsearchpoi).
     *
     * @param language Language in which search results should be returned. Should be one of supported IETF language
     *     tags, except NGT and NGT-Latn. Language tag is case insensitive. When data in specified language is not
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
    public Response<PointOfInterestCategoryTreeResult> getPointOfInterestCategoryTreeWithResponse(
            String language, Context context) {
        return this.asyncClient.getPointOfInterestCategoryTreeWithResponse(language, context).block();
    }

    /**
     * **Address Geocoding**
     *
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchAddress(SearchAddressOptions options) {
        return this.asyncClient.searchAddress(options).block();
    }

    /**
     * **Address Geocoding**
     *
     * <p>**Applies to**: S0 and S1 pricing tiers.

     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchAddressWithResponse(SearchAddressOptions options,
            Context context) {
        return this.asyncClient.searchAddressWithResponse(options, context).block();
    }

    /**
     * **Reverse Geocode to an Address**

     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Reverse call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReverseSearchAddressResult reverseSearchAddress(ReverseSearchAddressOptions options) {
        return this.asyncClient.reverseSearchAddress(options).block();
    }

    /**
     * **Reverse Geocode to an Address**
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Reverse call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ReverseSearchAddressResult> reverseSearchAddressWithResponse(
            ReverseSearchAddressOptions options, Context context) {
        return this.asyncClient.reverseSearchAddressWithResponse(options, context).block();
    }

    /**
     * **Reverse Geocode to a Cross Street**
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Reverse CrossStreet call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ReverseSearchCrossStreetAddressResult reverseSearchCrossStreetAddress(
            ReverseSearchCrossStreetAddressOptions options) {
        return this.asyncClient.reverseSearchCrossStreetAddress(options).block();
    }

    /**
     * **Reverse Geocode to a Cross Street**
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Reverse CrossStreet call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ReverseSearchCrossStreetAddressResult> reverseSearchCrossStreetAddressWithResponse(
            ReverseSearchCrossStreetAddressOptions options, Context context) {
        return this.asyncClient.reverseSearchCrossStreetAddressWithResponse(options, context).block();
    }

    /**
     * **Structured Address Geocoding**
     *

     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchStructuredAddress(StructuredAddress address,
            SearchStructuredAddressOptions options) {
        return this.asyncClient.searchStructuredAddress(address, options).block();
    }

    /**
     * **Structured Address Geocoding**
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchStructuredAddressWithResponse(
            StructuredAddress address, SearchStructuredAddressOptions options, Context context) {
        return this.asyncClient.searchStructuredAddressWithResponse(address, options, context).block();
    }

    /**
     * **Applies to**: S0 and S1 pricing tiers.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchInsideGeometry(SearchInsideGeometryOptions options) {
        return this.asyncClient.searchInsideGeometry(options).block();
    }

    /**
     * **Applies to**: S0 and S1 pricing tiers.

     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchInsideGeometryWithResponse(SearchInsideGeometryOptions options,
            Context context) {
        return this.asyncClient.searchInsideGeometryWithResponse(options, null).block();
    }

    /**
     * **Applies to**: S0 and S1 pricing tiers.
     *

     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SearchAddressResult searchAlongRoute(SearchAlongRouteOptions options) {
        return this.asyncClient.searchAlongRoute(options).block();
    }

    /**
     * **Applies to**: S0 and S1 pricing tiers.
     *
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SearchAddressResult> searchAlongRouteWithResponse(SearchAlongRouteOptions options,
            Context context) {
        return this.asyncClient.searchAlongRouteWithResponse(options, context).block();
    }

    /**
     * **Search Fuzzy Batch API**
     *
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginFuzzySearchBatch(
            List<FuzzySearchOptions> optionsList) {
        return this.asyncClient.beginFuzzySearchBatch(optionsList).getSyncPoller();
    }

    /**
     * **Search Fuzzy Batch API**
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginFuzzySearchBatch(
            List<FuzzySearchOptions> optionsList, Context context) {
        return this.asyncClient.beginFuzzySearchBatch(optionsList, context).getSyncPoller();
    }

    /**
     * **Search Fuzzy Batch API**
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginGetFuzzySearchBatch(
            String batchId) {
        return this.asyncClient.beginGetFuzzySearchBatch(batchId).getSyncPoller();
    }

    /**
     * **Search Fuzzy Batch API**
     *
     * @param batchId Batch id for querying the operation.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginGetFuzzySearchBatch(
            String batchId, Context context) {
        return this.asyncClient.beginGetFuzzySearchBatch(batchId, context).getSyncPoller();
    }

    /**
     * **Search Address Batch API**
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
     * **Search Address Batch API**
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
     * **Search Address Batch API**
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginGetSearchAddressBatch(
            String batchId) {
        return this.asyncClient.beginGetSearchAddressBatch(batchId).getSyncPoller();
    }

    /**
     * **Search Address Batch API**
     * @param batchId Batch id for querying the operation.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchSearchResult, BatchSearchResult> beginGetSearchAddressBatch(
            String batchId, Context context) {
        return this.asyncClient.beginGetSearchAddressBatch(batchId, context).getSyncPoller();
    }

    /**
     * Searches a batch of addresses given their coordinates.
     *
     * @param optionsList a list of {@link ReverseSearchAddressOptions} to be searched.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchReverseSearchResult, BatchReverseSearchResult>
            beginReverseSearchAddressBatch(List<ReverseSearchAddressOptions> optionsList) {
        return this.asyncClient.beginReverseSearchAddressBatch(optionsList).getSyncPoller();
    }

    /**
     * Searches a batch of addresses given their coordinates.
     *
     * @param optionsList a list of {@link ReverseSearchAddressOptions} to be searched.
     * @param context a {@link Context} object for distributed tracing.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchReverseSearchResult, BatchReverseSearchResult>
            beginReverseSearchAddressBatch(List<ReverseSearchAddressOptions> optionsList, Context context) {
        return this.asyncClient.beginReverseSearchAddressBatch(optionsList, context).getSyncPoller();
    }

    /**
     * Returns a batch of previously searched addressed given a batch id.
     *
     * @param batchId Batch id for querying the operation.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchReverseSearchResult, BatchReverseSearchResult>
            beginGetReverseSearchAddressBatch(String batchId) {
        return this.asyncClient.beginGetReverseSearchAddressBatch(batchId).getSyncPoller();
    }

    /**
     * Returns a batch of previously searched addressed given a batch id.
     *
     * @param batchId Batch id for querying the operation.
     * @param context a {@link Context} object for distributed tracing.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public SyncPoller<BatchReverseSearchResult, BatchReverseSearchResult>
            beginGetReverseSearchAddressBatch(String batchId, Context context) {
        return this.asyncClient.beginGetReverseSearchAddressBatch(batchId, context).getSyncPoller();
    }
}
