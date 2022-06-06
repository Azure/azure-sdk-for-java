// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.core.util.polling.DefaultPollingStrategy;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.search.implementation.SearchesImpl;
import com.azure.maps.search.implementation.helpers.BatchResponseSerializer;
import com.azure.maps.search.implementation.helpers.Utility;
import com.azure.maps.search.implementation.models.BatchRequest;
import com.azure.maps.search.implementation.models.BatchRequestItem;
import com.azure.maps.search.implementation.models.GeoJsonLineString;
import com.azure.maps.search.implementation.models.GeoJsonObject;
import com.azure.maps.search.implementation.models.JsonFormat;
import com.azure.maps.search.implementation.models.PolygonResult;
import com.azure.maps.search.implementation.models.ResponseFormat;
import com.azure.maps.search.implementation.models.SearchAlongRouteRequest;
import com.azure.maps.search.implementation.models.SearchInsideGeometryRequest;
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

import reactor.core.publisher.Mono;

/** Initializes a new instance of the asynchronous SearchClient type. */
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
     *
     * @param geometryIds Comma separated list of geometry UUIDs, previously retrieved from an Online Search request.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Polygon call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<List<Polygon>> getPolygons(List<String> geometryIds) {
        Mono<Response<List<Polygon>>> result = this.getPolygonsWithResponse(geometryIds);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * List Polygons
     *
     * @param geometryIds Comma separated list of geometry UUIDs, previously retrieved from an Online Search request.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Polygon call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<List<Polygon>>> getPolygonsWithResponse(List<String> geometryIds) {
        return this.getPolygonsWithResponse(geometryIds, null);
    }

    /**
     * List Polygons
     *
     * @param geometryIds Comma separated list of geometry UUIDs, previously retrieved from an Online Search request.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Polygon call.
     */
    Mono<Response<List<Polygon>>> getPolygonsWithResponse(List<String> geometryIds,
            Context context) {
        Mono<Response<PolygonResult>> result = this.serviceClient.listPolygonsWithResponseAsync(JsonFormat.JSON,
            geometryIds, context);
        return result.flatMap(response -> {
            List<Polygon> polygons = response.getValue().getPolygons();
            Response<List<Polygon>> simpleResponse = new SimpleResponse<List<Polygon>>(response,
                polygons);
            return Mono.just(simpleResponse);
        });
    }

    /**
     * Fuzzy Search
     *
     * @param options {@link FuzzySearchOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Fuzzy Search call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> fuzzySearch(FuzzySearchOptions options) {
        Mono<Response<SearchAddressResult>> result = this.fuzzySearchWithResponse(options, null);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Fuzzy Search
     *
     * @param options {@link FuzzySearchOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Fuzzy Search call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> fuzzySearchWithResponse(FuzzySearchOptions options) {
        return this.fuzzySearchWithResponse(options, null);
    }

    /**
     * Fuzzy Search
     *
     * @param options {@link FuzzySearchOptions} the options to be used in this search.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Fuzzy Search call.
     */
    Mono<Response<SearchAddressResult>> fuzzySearchWithResponse(FuzzySearchOptions options, Context context) {
        return
            this.serviceClient.fuzzySearchWithResponseAsync(
                ResponseFormat.JSON,
                options.getQuery(),
                options.isTypeAhead(),
                options.getTop(),
                options.getSkip(),
                options.getCategoryFilter(),
                options.getCountryFilter(),
                options.getCoordinates().map(GeoPosition::getLatitude).orElse(null),
                options.getCoordinates().map(GeoPosition::getLongitude).orElse(null),
                options.getRadiusInMeters(),
                options.getBoundingBox().map(item -> new GeoPosition(item.getWest(), item.getNorth())).map(Utility::positionToString).orElse(null),
                options.getBoundingBox().map(item -> new GeoPosition(item.getEast(), item.getSouth())).map(Utility::positionToString).orElse(null),
                options.getLanguage(),
                options.getExtendedPostalCodesFor(),
                options.getMinFuzzyLevel(),
                options.getMaxFuzzyLevel(),
                options.getIndexFilter(),
                options.getBrandFilter(),
                options.getElectricVehicleConnectorFilter(),
                options.getEntityType(),
                options.getLocalizedMapView(),
                options.getOperatingHours(),
                context);
    }

    /**
     * Search Point of Interest
     *
     * @param options {@link SearchPointOfInterestOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchPointOfInterest(SearchPointOfInterestOptions options) {
        Mono<Response<SearchAddressResult>> result = this.searchPointOfInterestWithResponse(options, null);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Search Point of Interest
     *
     * @param options {@link SearchPointOfInterestOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchPointOfInterestWithResponse(SearchPointOfInterestOptions options) {
        return this.searchPointOfInterestWithResponse(options, null);
    }

    /**
     * Search Point of Interest
     *
     * @param options {@link SearchPointOfInterestOptions} the options to be used in this search.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest call.
     */
    Mono<Response<SearchAddressResult>> searchPointOfInterestWithResponse(SearchPointOfInterestOptions options,
            Context context) {
        return
            this.serviceClient.searchPointOfInterestWithResponseAsync(
                ResponseFormat.JSON,
                options.getQuery(),
                options.isTypeAhead(),
                options.getTop(),
                options.getSkip(),
                options.getCategoryFilter(),
                options.getCountryFilter(),
                options.getCoordinates().map(GeoPosition::getLatitude).orElse(null),
                options.getCoordinates().map(GeoPosition::getLongitude).orElse(null),
                options.getRadiusInMeters(),
                options.getBoundingBox().map(item -> new GeoPosition(item.getWest(), item.getNorth())).map(Utility::positionToString).orElse(null),
                options.getBoundingBox().map(item -> new GeoPosition(item.getEast(), item.getSouth())).map(Utility::positionToString).orElse(null),
                options.getLanguage(),
                options.getExtendedPostalCodesFor(),
                options.getBrandFilter(),
                options.getElectricVehicleConnectorFilter(),
                options.getLocalizedMapView(),
                options.getOperatingHours(),
                context);
    }

    /**
     * Search Nearby Point of Interest
     *
     * @param options {@link SearchNearbyPointsOfInterestOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Nearby Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchNearbyPointOfInterest(
            SearchNearbyPointsOfInterestOptions options) {
        Mono<Response<SearchAddressResult>> result = this.searchNearbyPointOfInterestWithResponse(options, null);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Search Nearby Point of Interest
     *
     * @param options {@link SearchNearbyPointsOfInterestOptions} the options to be used in this search.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Nearby Point of Interest call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchNearbyPointOfInterestWithResponse(
            SearchNearbyPointsOfInterestOptions options) {
        return this.searchNearbyPointOfInterestWithResponse(options, null);
    }

    /**
     * Search Nearby Point of Interest
     *
     * @param options {@link SearchNearbyPointsOfInterestOptions} the options to be used in this search.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Nearby Point of Interest call.
     */
    Mono<Response<SearchAddressResult>> searchNearbyPointOfInterestWithResponse(
            SearchNearbyPointsOfInterestOptions options, Context context) {
        // this should throw an exception if the coordinates are null, as for
        // this method they are mandatory
        final GeoPosition coordinates = options.getCoordinates().get();
        return
            this.serviceClient.searchNearbyPointOfInterestWithResponseAsync(
                ResponseFormat.JSON,
                coordinates.getLatitude(),
                coordinates.getLongitude(),
                options.getTop(),
                options.getSkip(),
                options.getCategoryFilter(),
                options.getCountryFilter(),
                options.getRadiusInMeters(),
                options.getLanguage(),
                options.getExtendedPostalCodesFor(),
                options.getBrandFilter(),
                options.getElectricVehicleConnectorFilter(),
                options.getLocalizedMapView(),
                context);
    }

    /**
     * Search Point of Interest per Category
     *
     * @param options a {@link SearchPointOfInterestCategoryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest per Category calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchPointOfInterestCategory(SearchPointOfInterestCategoryOptions options) {
        Mono<Response<SearchAddressResult>> result = this.searchPointOfInterestCategoryWithResponse(options, null);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Search Point of Interest per Category
     *
     * @param options a {@link SearchPointOfInterestCategoryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest per Category calls.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchPointOfInterestCategoryWithResponse(
                SearchPointOfInterestCategoryOptions options) {
        return this.searchPointOfInterestCategoryWithResponse(options, null);
    }

    /**
     * Search Point of Interest per Category
     *
     * @param options a {@link SearchPointOfInterestCategoryOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Point of Interest per Category calls.
     */
    Mono<Response<SearchAddressResult>> searchPointOfInterestCategoryWithResponse(
                SearchPointOfInterestCategoryOptions options, Context context) {
        return
            this.serviceClient.searchPointOfInterestCategoryWithResponseAsync(
                ResponseFormat.JSON,
                options.getQuery(),
                options.isTypeAhead(),
                options.getTop(),
                options.getSkip(),
                options.getCategoryFilter(),
                options.getCountryFilter(),
                options.getCoordinates().map(GeoPosition::getLatitude).orElse(null),
                options.getCoordinates().map(GeoPosition::getLongitude).orElse(null),
                options.getRadiusInMeters(),
                options.getBoundingBox().map(item -> new GeoPosition(item.getWest(), item.getNorth())).map(Utility::positionToString).orElse(null),
                options.getBoundingBox().map(item -> new GeoPosition(item.getEast(), item.getSouth())).map(Utility::positionToString).orElse(null),
                options.getLanguage(),
                options.getExtendedPostalCodesFor(),
                options.getBrandFilter(),
                options.getElectricVehicleConnectorFilter(),
                options.getLocalizedMapView(),
                options.getOperatingHours(),
                context);
    }

    /**
     * Get Point of Interest Category Tree
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
    public Mono<PointOfInterestCategoryTreeResult> getPointOfInterestCategoryTree(String language) {
        return this.serviceClient.getPointOfInterestCategoryTreeAsync(JsonFormat.JSON, language);
    }

    /**
     * Get Point of Interest Category Tree
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
    public Mono<Response<PointOfInterestCategoryTreeResult>> getPointOfInterestCategoryTreeWithResponse(
            String language) {
        return this.getPointOfInterestCategoryTreeWithResponse(language, null);
    }

    /**
     * Get Point of Interest Category Tree
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
    Mono<Response<PointOfInterestCategoryTreeResult>> getPointOfInterestCategoryTreeWithResponse(
            String language, Context context) {
        return this.serviceClient.getPointOfInterestCategoryTreeWithResponseAsync(JsonFormat.JSON, language,
            context);
    }

    /**
     * Search Address
     *
     * @param options a {@link SearchAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchAddress(SearchAddressOptions options) {
        Mono<Response<SearchAddressResult>> result = this.searchAddressWithResponse(options, null);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Search Address
     *
     * @param options a {@link SearchAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchAddressWithResponse(SearchAddressOptions options) {
        return this.searchAddressWithResponse(options, null);
    }

    /**
     * Search Address
     *
     * @param options a {@link SearchAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address call.
     */
    Mono<Response<SearchAddressResult>> searchAddressWithResponse(SearchAddressOptions options, Context context) {
        return
            this.serviceClient.searchAddressWithResponseAsync(
                ResponseFormat.JSON,
                options.getQuery(),
                options.isTypeAhead(),
                options.getTop(),
                options.getSkip(),
                options.getCountryFilter(),
                options.getCoordinates().map(GeoPosition::getLatitude).orElse(null),
                options.getCoordinates().map(GeoPosition::getLongitude).orElse(null),
                options.getRadiusInMeters(),
                options.getBoundingBox().map(item -> new GeoPosition(item.getWest(), item.getNorth())).map(Utility::positionToString).orElse(null),
                options.getBoundingBox().map(item -> new GeoPosition(item.getEast(), item.getSouth())).map(Utility::positionToString).orElse(null),
                options.getLanguage(),
                options.getExtendedPostalCodesFor(),
                options.getEntityType(),
                options.getLocalizedMapView(),
                context);
    }

    /**
     * Reverse Address Search
     *
     * @param options a {@link ReverseSearchAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ReverseSearchAddressResult> reverseSearchAddress(ReverseSearchAddressOptions options) {
        Mono<Response<ReverseSearchAddressResult>> result = this.reverseSearchAddressWithResponse(options, null);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Reverse Address Search
     *
     * @param options a {@link ReverseSearchAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ReverseSearchAddressResult>> reverseSearchAddressWithResponse(
                ReverseSearchAddressOptions options) {
        return this.reverseSearchAddressWithResponse(options, null);
    }

    /**
     * Reverse Address Search
     *
     * @param options a {@link ReverseSearchAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    Mono<Response<ReverseSearchAddressResult>> reverseSearchAddressWithResponse(
                ReverseSearchAddressOptions options, Context context) {
        return
            this.serviceClient.reverseSearchAddressWithResponseAsync(
                ResponseFormat.JSON,
                Arrays.asList(options.getCoordinates().getLatitude(), options.getCoordinates().getLongitude()),
                options.getLanguage(),
                options.includeSpeedLimit(),
                options.getHeading(),
                options.getRadiusInMeters(),
                options.getNumber(),
                options.includeRoadUse(),
                options.getRoadUse(),
                options.allowFreeformNewline(),
                options.includeMatchType(),
                options.getEntityType(),
                options.getLocalizedMapView(),
                context);
    }

    /**
     * Reverse Address Search to a Cross Street
     *
     * @param options a {@link ReverseSearchCrossStreetAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ReverseSearchCrossStreetAddressResult> reverseSearchCrossStreetAddress(
            ReverseSearchCrossStreetAddressOptions options) {
        Mono<Response<ReverseSearchCrossStreetAddressResult>> result =
            this.reverseSearchCrossStreetAddressWithResponse(options, null);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Reverse Address Search to a Cross Street
     *
     * @param options a {@link ReverseSearchCrossStreetAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ReverseSearchCrossStreetAddressResult>> reverseSearchCrossStreetAddressWithResponse(
            ReverseSearchCrossStreetAddressOptions options) {
        return this.reverseSearchCrossStreetAddressWithResponse(options, null);
    }

    /**
     * Reverse Address Search to a Cross Street
     *
     * @param options a {@link ReverseSearchCrossStreetAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    Mono<Response<ReverseSearchCrossStreetAddressResult>> reverseSearchCrossStreetAddressWithResponse(
            ReverseSearchCrossStreetAddressOptions options, Context context) {
        return
            this.serviceClient.reverseSearchCrossStreetAddressWithResponseAsync(
                ResponseFormat.JSON,
                Arrays.asList(options.getCoordinates().getLatitude(), options.getCoordinates().getLongitude()),
                options.getTop(),
                options.getHeading(),
                options.getRadiusInMeters(),
                options.getLanguage(),
                options.getLocalizedMapView(),
                context);
    }

    /**
     * Structured Address Search
     *
     * @param address a {@link StructuredAddress} to be searched by the API.
     * @param options a {@link SearchStructuredAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchStructuredAddress(StructuredAddress address,
            SearchStructuredAddressOptions options) {
        Mono<Response<SearchAddressResult>> result = this.searchStructuredAddressWithResponse(address,
            options, null);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Structured Address Search
     *
     * @param address a {@link StructuredAddress} to be searched by the API.
     * @param options a {@link SearchStructuredAddressOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Reverse Search Address call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchStructuredAddressWithResponse(
            StructuredAddress address, SearchStructuredAddressOptions options) {
        return this.searchStructuredAddressWithResponse(address, options, null);
    }

    /**
     * Structured Address Search
     *
     * @param address a {@link StructuredAddress} to be searched by the API.
     * @param options a {@link SearchStructuredAddressOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Structured Address Search call.
     */
    Mono<Response<SearchAddressResult>> searchStructuredAddressWithResponse(
            StructuredAddress address, SearchStructuredAddressOptions options, Context context) {
        final SearchStructuredAddressOptions param = Optional.ofNullable(options)
            .orElse(new SearchStructuredAddressOptions());
        return
            this.serviceClient.searchStructuredAddressWithResponseAsync(
                ResponseFormat.JSON,
                address.getCountryCode(),
                param.getLanguage(),
                param.getTop(),
                param.getSkip(),
                address.getStreetNumber(),
                address.getStreetName(),
                address.getCrossStreet(),
                address.getMunicipality(),
                address.getMunicipalitySubdivision(),
                address.getCountryTertiarySubdivision(),
                address.getCountrySecondarySubdivision(),
                address.getCountrySubdivision(),
                address.getPostalCode(),
                param.getExtendedPostalCodesFor(),
                param.getEntityType(),
                param.getLocalizedMapView(),
                context);
    }

    /**
     * Search Inside Geometry
     *
     * @param options a {@link SearchInsideGeometryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Inside Geometry call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchInsideGeometry(SearchInsideGeometryOptions options) {
        Mono<Response<SearchAddressResult>> result = this.searchInsideGeometryWithResponse(options, null);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Search Inside Geometry
     *
     * @param options a {@link SearchInsideGeometryOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Inside Geometry call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchInsideGeometryWithResponse(
            SearchInsideGeometryOptions options) {
        return this.searchInsideGeometryWithResponse(options, null);
    }

    /**
     * Search Inside Geometry
     *
     * @param options a {@link SearchInsideGeometryOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Inside Geometry call.
     */
    Mono<Response<SearchAddressResult>> searchInsideGeometryWithResponse(SearchInsideGeometryOptions options,
            Context context) {
        GeoJsonObject geoJsonObject = Utility.toGeoJsonObject(options.getGeometry());
        return
            this.serviceClient.searchInsideGeometryWithResponseAsync(
                ResponseFormat.JSON,
                options.getQuery(),
                new SearchInsideGeometryRequest().setGeometry(geoJsonObject),
                options.getTop(),
                options.getLanguage(),
                options.getCategoryFilter(),
                options.getExtendedPostalCodesFor(),
                options.getIndexFilter(),
                options.getLocalizedMapView(),
                options.getOperatingHours(),
                context);
    }

    /**
     * Search Along Route
     *
     * @param options a {@link SearchAlongRouteOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Along Route call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<SearchAddressResult> searchAlongRoute(SearchAlongRouteOptions options) {
        Mono<Response<SearchAddressResult>> result = this.searchAlongRouteWithResponse(options, null);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Search Along Route
     *
     * @param options a {@link SearchAlongRouteOptions} representing the search parameters.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Along Route call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<SearchAddressResult>> searchAlongRouteWithResponse(SearchAlongRouteOptions options) {
        return this.searchAlongRouteWithResponse(options, null);
    }

    /**
     * Search Along Route
     *
     * @param options a {@link SearchAlongRouteOptions} representing the search parameters.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Along Route call.
     */
    Mono<Response<SearchAddressResult>> searchAlongRouteWithResponse(SearchAlongRouteOptions options,
            Context context) {
        GeoJsonLineString geoJsonLineString = (GeoJsonLineString) Utility.toGeoJsonObject(options.getRoute());
        return
            this.serviceClient.searchAlongRouteWithResponseAsync(
                ResponseFormat.JSON,
                options.getQuery(),
                options.getMaxDetourTime(),
                new SearchAlongRouteRequest().setRoute(geoJsonLineString),
                options.getTop(),
                options.getBrandFilter(),
                options.getCategoryFilter(),
                options.getElectricVehicleConnectorFilter(),
                options.getLocalizedMapView(),
                options.getOperatingHours(),
                context);
    }

    /**
     * Batch Fuzzy Search
     *
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
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
     *
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    PollerFlux<BatchSearchResult, BatchSearchResult> beginFuzzySearchBatch(
            List<FuzzySearchOptions> optionsList, Context context) {
        Objects.requireNonNull(optionsList, "'optionsList' is a required parameter.");

        // convert list to batch request
        List<BatchRequestItem> items = optionsList.stream()
            .map(item -> Utility.toFuzzySearchBatchRequestItem(item)).collect(Collectors.toList());
        BatchRequest batchRequest = new BatchRequest().setBatchItems(items);

        if (batchRequest.getBatchItems().size() <= BATCH_SIZE) {
            return createPollerFlux(
                () -> this.serviceClient.fuzzySearchBatchSyncWithResponseAsync(JsonFormat.JSON,
                    batchRequest, context)
                    .flatMap(response -> {
                        return Mono.just(Utility.createBatchSearchResponse(response));
                    }),
                this.forwardStrategy);
        } else {
            return createPollerFlux(
                () -> this.serviceClient.fuzzySearchBatchWithResponseAsync(JsonFormat.JSON,
                    batchRequest, context).flatMap(response -> {
                        return Mono.just(Utility.createBatchSearchResponse(response));
                    }),
                this.forwardStrategy);
        }
    }

    /**
     * Get Fuzzy Batch Search by Id
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<BatchSearchResult, BatchSearchResult> beginGetFuzzySearchBatch(
            String batchId) {
        return this.beginGetFuzzySearchBatch(batchId, null);
    }

    /**
     * Get Fuzzy Batch Search by Id
     *
     * @param batchId Batch id for querying the operation.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    PollerFlux<BatchSearchResult, BatchSearchResult> beginGetFuzzySearchBatch(
            String batchId, Context context) {
        return createPollerFlux(
            () -> this.serviceClient.getFuzzySearchBatchWithResponseAsync(batchId, context)
                    .flatMap(response -> {
                        return Mono.just(Utility.createBatchSearchResponse(response));
                    }),
            this.forwardStrategy);
    }

    /**
     * Batch Address Search
     *
     * @param optionsList a list of {@link FuzzySearchOptions} to be searched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
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
     *
     * @param optionsList a list of {@link SearchAddressOptions} to be searched.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    PollerFlux<BatchSearchResult, BatchSearchResult> beginSearchAddressBatch(
            List<SearchAddressOptions> optionsList, Context context) {
        Objects.requireNonNull(optionsList, "'optionsList' is a required parameter.");

        // convert list to batch request
        List<BatchRequestItem> items = optionsList.stream()
            .map(item -> Utility.toSearchBatchRequestItem(item)).collect(Collectors.toList());
        BatchRequest batchRequest = new BatchRequest().setBatchItems(items);

        // run
        if (batchRequest.getBatchItems().size() <= BATCH_SIZE) {
            return createPollerFlux(
                () -> this.serviceClient.searchAddressBatchSyncWithResponseAsync(JsonFormat.JSON,
                    batchRequest, context)
                    .flatMap(response -> {
                        return Mono.just(Utility.createBatchSearchResponse(response));
                    }),
                this.forwardStrategy);
        } else {
            return createPollerFlux(
                () -> this.serviceClient.searchAddressBatchWithResponseAsync(JsonFormat.JSON,
                    batchRequest, context).flatMap(response -> {
                        return Mono.just(Utility.createBatchSearchResponse(response));
                    }),
                this.forwardStrategy);
        }
    }



    /**
     * Get Batch Search Id
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<BatchSearchResult, BatchSearchResult> beginGetSearchAddressBatch(
            String batchId) {
        return this.beginGetSearchAddressBatch(batchId, null);
    }

    /**
     * Get Batch Search Id
     *
     * @param batchId Batch id for querying the operation.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Search Address Batch service call.
     */
    PollerFlux<BatchSearchResult, BatchSearchResult> beginGetSearchAddressBatch(
            String batchId, Context context) {
        return createPollerFlux(
            () -> this.serviceClient.getSearchAddressBatchWithResponseAsync(batchId, context)
                .flatMap(response -> {
                    return Mono.just(Utility.createBatchSearchResponse(response));
                }),
            this.forwardStrategy);
    }

    /**
     * Searches a batch of addresses given their coordinates.
     *
     * @param optionsList a list of {@link ReverseSearchAddressOptions} to be searched.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult>
            beginReverseSearchAddressBatch(List<ReverseSearchAddressOptions> optionsList) {
        return this.beginReverseSearchAddressBatch(optionsList, null);
    }

    /**
     * Searches a batch of addresses given their coordinates.
     *
     * @param optionsList a list of {@link ReverseSearchAddressOptions} to be searched.
     * @param context a {@link Context} object for distributed tracing.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult>
            beginReverseSearchAddressBatch(List<ReverseSearchAddressOptions> optionsList, Context context) {
        Objects.requireNonNull(optionsList, "'optionsList' is a required parameter.");

        // convert list to batch request
        List<BatchRequestItem> items = optionsList.stream()
            .map(item -> Utility.toReverseSearchBatchRequestItem(item)).collect(Collectors.toList());
        BatchRequest batchRequest = new BatchRequest().setBatchItems(items);

        if (batchRequest.getBatchItems().size() <= BATCH_SIZE) {
            return createReversePollerFlux(
                () -> this.serviceClient.reverseSearchAddressBatchSyncWithResponseAsync(JsonFormat.JSON,
                    batchRequest, context)
                    .flatMap(response -> {
                        return Mono.just(Utility.createBatchReverseSearchResponse(response));
                    }),
                this.reverseStrategy);
        } else {
            return createReversePollerFlux(
                () -> this.serviceClient.reverseSearchAddressBatchWithResponseAsync(JsonFormat.JSON,
                    batchRequest, context).flatMap(response -> {
                        return Mono.just(Utility.createBatchReverseSearchResponse(response));
                    }),
                this.reverseStrategy);
        }
    }

    /**
     * Returns a batch of previously searched addressed given a batch id.
     *
     * @param batchId Batch id for querying the operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    @ServiceMethod(returns = ReturnType.LONG_RUNNING_OPERATION)
    public PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult>
            beginGetReverseSearchAddressBatch(String batchId) {
        return this.beginGetReverseSearchAddressBatch(batchId, null);
    }

    /**
     * Returns a batch of previously searched addressed given a batch id.
     *
     * @param batchId Batch id for querying the operation.
     * @param context a {@link Context} object for distributed tracing.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return a {@code SyncPoller} wrapping the service call.
     */
    PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult>
            beginGetReverseSearchAddressBatch(String batchId, Context context) {
        return createReversePollerFlux(
            () -> this.serviceClient.getReverseSearchAddressBatchWithResponseAsync(batchId, context)
                .flatMap(response -> {
                    return Mono.just(Utility.createBatchReverseSearchResponse(response));
                }),
            this.reverseStrategy);
    }

    // create a poller for a forward search operation
    private PollerFlux<BatchSearchResult, BatchSearchResult> createPollerFlux(
            Supplier<Mono<? extends Response<?>>> initialOperation,
            DefaultPollingStrategy<BatchSearchResult, BatchSearchResult> strategy) {

        // batch search type reference
        BatchSearchTypeReference reference = new BatchSearchTypeReference();

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
                    return strategy.onInitialResponse(response, context, reference);
                })),
            context -> strategy.poll(context, reference),
            strategy::cancel,
            context -> {
                return strategy
                    .getResult(context, reference)
                        .flatMap(result -> {
                            final String batchId = context.getData(POLLING_BATCH_HEADER_KEY);
                            result.setBatchId(batchId);
                            return Mono.just(result);
                        });
            });
    }

    // create a poller for the reverse search operation
    private PollerFlux<BatchReverseSearchResult, BatchReverseSearchResult> createReversePollerFlux(
            Supplier<Mono<? extends Response<?>>> initialOperation,
            DefaultPollingStrategy<BatchReverseSearchResult, BatchReverseSearchResult> strategy) {

        // batch search type reference
        ReverseBatchSearchTypeReference reference = new ReverseBatchSearchTypeReference();

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
                    return strategy.onInitialResponse(response, context, reference);
                })),
            context -> strategy.poll(context, reference),
            strategy::cancel,
            context -> {
                return strategy
                    .getResult(context, reference)
                        .flatMap(result -> {
                            final String batchId = context.getData(POLLING_BATCH_HEADER_KEY);
                            result.setBatchId(batchId);
                            return Mono.just(result);
                        });
            });
    }
}
