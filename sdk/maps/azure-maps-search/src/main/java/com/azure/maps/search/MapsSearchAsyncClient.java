// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.maps.search.implementation.SearchesImpl;
import com.azure.maps.search.implementation.models.Boundary;
import com.azure.maps.search.implementation.models.BoundaryResultTypeEnum;
import com.azure.maps.search.implementation.models.ErrorResponseException;
import com.azure.maps.search.implementation.models.GeocodingBatchRequestBody;
import com.azure.maps.search.implementation.models.GeocodingResponse;
import com.azure.maps.search.implementation.models.ResolutionEnum;
import com.azure.maps.search.implementation.models.ReverseGeocodingBatchRequestBody;
import com.azure.maps.search.implementation.models.ReverseGeocodingResultTypeEnum;
import com.azure.maps.search.implementation.models.SearchesGetGeocodingHeaders;
import com.azure.maps.search.implementation.models.GeocodingBatchResponse;
import com.azure.maps.search.models.BaseSearchOptions;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

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

    // instance fields
    private final SearchesImpl serviceClient;

    /**
     * Initializes an instance of Searches client.
     *
     * @param serviceClient the service client implementation.
     */
    MapsSearchAsyncClient(SearchesImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Use to get polygon data of a geographical area shape such as a city or a country region.
     *
     *
     * The `Get Polygon` API is an HTTP `GET` request that supplies polygon data of a geographical area outline such as
     * a city or a country region.
     *
     * @param coordinates A point on the earth specified as a longitude and latitude. Example: &amp;coordinates=lon,lat.
     * @param view A string that represents an [ISO 3166-1 Alpha-2 region/country
     * code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2). This will alter Geopolitical disputed borders and labels
     * to align with the specified user region. By default, the View parameter is set to “Auto” even if you haven’t
     * defined it in the request.
     *
     * Please refer to [Supported Views](https://aka.ms/AzureMapsLocalizationViews) for details and to see the available
     * Views.
     * @param resultType The geopolitical concept to return a boundary for. If not specified, the default is
     * `countryRegion` result type.
     * @param resolution Resolution determines the amount of points to send back. If not specified, the default is
     * medium resolution.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return `GeoJSON GeocodingFeature` object that describe the boundaries of a geographical area.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Boundary> getPolygons(GeoPosition coordinates, String view, BoundaryResultTypeEnum resultType, ResolutionEnum resolution) {
        List<Double> coordinatesList = null;
        if (coordinates != null) {
            coordinatesList = new ArrayList<>();
            coordinatesList.add(coordinates.getLongitude());
            coordinatesList.add(coordinates.getLatitude());
        }
        return serviceClient.getPolygonAsync(coordinatesList, view, resultType, resolution);
    }

    /**
     * Use to get polygon data of a geographical area shape such as a city or a country region.
     *
     *
     *
     * The `Get Polygon` API is an HTTP `GET` request that supplies polygon data of a geographical area outline such as
     * a city or a country region.
     *
     * @param coordinates A point on the earth specified as a longitude and latitude. Example: &amp;coordinates=lon,lat.
     * @param view A string that represents an [ISO 3166-1 Alpha-2 region/country
     * code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2). This will alter Geopolitical disputed borders and labels
     * to align with the specified user region. By default, the View parameter is set to “Auto” even if you haven’t
     * defined it in the request.
     *
     * Please refer to [Supported Views](https://aka.ms/AzureMapsLocalizationViews) for details and to see the available
     * Views.
     * @param resultType The geopolitical concept to return a boundary for. If not specified, the default is
     * `countryRegion` result type.
     * @param resolution Resolution determines the amount of points to send back. If not specified, the default is
     * medium resolution.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return {@link Boundary} object that describe the boundaries of a geographical area along with
     * {@link Response} on successful completion of {@link Mono}.
     */
    public Mono<Response<Boundary>> getPolygonsWithResponse(GeoPosition coordinates, String view, BoundaryResultTypeEnum resultType, ResolutionEnum resolution, Context context) {
        List<Double> coordinatesList = null;
        if (coordinates != null) {
            coordinatesList = new ArrayList<>();
            coordinatesList.add(coordinates.getLongitude());
            coordinatesList.add(coordinates.getLatitude());
        }
        return this.serviceClient.getPolygonWithResponseAsync(coordinatesList, view, resultType, resolution, context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable)
            .map(response -> new SimpleResponse<>(response, response.getValue()));
    }

    /**
     * Use to get longitude and latitude coordinates of a street address or name of a place.
     *
     *
     *
     * The `Get Geocoding` API is an HTTP `GET` request that returns the longitude and latitude coordinates of the
     * location being searched.
     *
     * In many cases, the complete search service might be too much, for instance if you are only interested in
     * traditional geocoding. Search can also be accessed for address look up exclusively. The geocoding is performed by
     * hitting the geocoding endpoint with just the address or partial address in question. The geocoding search index
     * will be queried for everything above the street level data. No Point of Interest (POIs) will be returned. Note
     * that the geocoder is very tolerant of typos and incomplete addresses. It will also handle everything from exact
     * street addresses or street or intersections as well as higher level geographies such as city centers, counties
     * and states. The response also returns detailed address properties such as street, postal code, municipality, and
     * country/region information.
     *
     * @param options base search options.
     *
     * **If query is given, should not use this parameter.**.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding call on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GeocodingResponse> getGeocoding(BaseSearchOptions options) {
        return getGeocoding(options, null);
    }

    /**
     * Use to get longitude and latitude coordinates of a street address or name of a place.
     *
     *
     *
     * The `Get Geocoding` API is an HTTP `GET` request that returns the longitude and latitude coordinates of the
     * location being searched.
     *
     * In many cases, the complete search service might be too much, for instance if you are only interested in
     * traditional geocoding. Search can also be accessed for address look up exclusively. The geocoding is performed by
     * hitting the geocoding endpoint with just the address or partial address in question. The geocoding search index
     * will be queried for everything above the street level data. No Point of Interest (POIs) will be returned. Note
     * that the geocoder is very tolerant of typos and incomplete addresses. It will also handle everything from exact
     * street addresses or street or intersections as well as higher level geographies such as city centers, counties
     * and states. The response also returns detailed address properties such as street, postal code, municipality, and
     * country/region information.
     *
     * @param options Base Search options.
     *
     * **If query is given, should not use this parameter.**.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding call along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<GeocodingResponse>> getGeocodingNoCustomHeaderWithResponse(BaseSearchOptions options) {
        return withContext(context -> getGeocodingNoCustomHeaderWithResponse(options, context));
    }

    /**
     * Use to get longitude and latitude coordinates of a street address or name of a place.
     *
     *
     *
     * The `Get Geocoding` API is an HTTP `GET` request that returns the longitude and latitude coordinates of the
     * location being searched.
     *
     * In many cases, the complete search service might be too much, for instance if you are only interested in
     * traditional geocoding. Search can also be accessed for address look up exclusively. The geocoding is performed by
     * hitting the geocoding endpoint with just the address or partial address in question. The geocoding search index
     * will be queried for everything above the street level data. No Point of Interest (POIs) will be returned. Note
     * that the geocoder is very tolerant of typos and incomplete addresses. It will also handle everything from exact
     * street addresses or street or intersections as well as higher level geographies such as city centers, counties
     * and states. The response also returns detailed address properties such as street, postal code, municipality, and
     * country/region information.
     *
     * @param options base search options.
     *
     * **If query is given, should not use this parameter.**.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding call along with {@link ResponseBase} on successful
     * completion of {@link Mono}.
     */

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ResponseBase<SearchesGetGeocodingHeaders, GeocodingResponse>> getGeocodingWithResponse(BaseSearchOptions options) {
        return withContext(context -> getGeocodingWithResponse(options, context));
    }

    /**
     * Use to get longitude and latitude coordinates of a street address or name of a place.
     *
     *
     *
     * The `Get Geocoding` API is an HTTP `GET` request that returns the longitude and latitude coordinates of the
     * location being searched.
     *
     * In many cases, the complete search service might be too much, for instance if you are only interested in
     * traditional geocoding. Search can also be accessed for address look up exclusively. The geocoding is performed by
     * hitting the geocoding endpoint with just the address or partial address in question. The geocoding search index
     * will be queried for everything above the street level data. No Point of Interest (POIs) will be returned. Note
     * that the geocoder is very tolerant of typos and incomplete addresses. It will also handle everything from exact
     * street addresses or street or intersections as well as higher level geographies such as city centers, counties
     * and states. The response also returns detailed address properties such as street, postal code, municipality, and
     * country/region information.
     *
     * @param options base search options.
     *
     * **If query is given, should not use this parameter.**.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding call along with {@link ResponseBase} on successful
     * completion of {@link Mono}.
     */
    Mono<Response<GeocodingResponse>> getGeocodingNoCustomHeaderWithResponse(BaseSearchOptions options, Context context) {
        List<Double> boundingBox = null;
        if (options.getBoundingBox().isPresent()) {
            boundingBox = new ArrayList<>();
            GeoBoundingBox boundingBoxObj = (GeoBoundingBox)options.getBoundingBox().get();
            boundingBox.add(boundingBoxObj.getNorth());
            boundingBox.add(boundingBoxObj.getWest());
            boundingBox.add(boundingBoxObj.getSouth());
            boundingBox.add(boundingBoxObj.getEast());
        }
        List<Double> coordinates = null;
        if (options.getCoordinates() != null) {
            coordinates = new ArrayList<>();
            coordinates.add(options.getCoordinates().getLongitude());
            coordinates.add(options.getCoordinates().getLatitude());
        }

        return serviceClient.getGeocodingNoCustomHeadersWithResponseAsync(options.getTop(), options.getQuery(),
                options.getAddressLine(), options.getCountryRegion(), boundingBox, options.getView(), coordinates,
                options.getAdminDistrict(), options.getAdminDistrict2(), options.getAdminDistrict3(),
                options.getLocality(), options.getPostalCode(), context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Use to get longitude and latitude coordinates of a street address or name of a place.
     *
     *
     *
     * The `Get Geocoding` API is an HTTP `GET` request that returns the longitude and latitude coordinates of the
     * location being searched.
     *
     * In many cases, the complete search service might be too much, for instance if you are only interested in
     * traditional geocoding. Search can also be accessed for address look up exclusively. The geocoding is performed by
     * hitting the geocoding endpoint with just the address or partial address in question. The geocoding search index
     * will be queried for everything above the street level data. No Point of Interest (POIs) will be returned. Note
     * that the geocoder is very tolerant of typos and incomplete addresses. It will also handle everything from exact
     * street addresses or street or intersections as well as higher level geographies such as city centers, counties
     * and states. The response also returns detailed address properties such as street, postal code, municipality, and
     * country/region information.
     *
     * @param options base search options.
     *
     * **If query is given, should not use this parameter.**.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding call along with {@link ResponseBase} on successful
     * completion of {@link Mono}.
     */

    Mono<ResponseBase<SearchesGetGeocodingHeaders, GeocodingResponse>> getGeocodingWithResponse(BaseSearchOptions options, Context context) {
        List<Double> boundingBox = null;
        if (options.getBoundingBox().isPresent()) {
            boundingBox = new ArrayList<>();
            GeoBoundingBox boundingBoxObj = (GeoBoundingBox)options.getBoundingBox().get();
            boundingBox.add(boundingBoxObj.getNorth());
            boundingBox.add(boundingBoxObj.getWest());
            boundingBox.add(boundingBoxObj.getSouth());
            boundingBox.add(boundingBoxObj.getEast());
        }
        List<Double> coordinates = null;
        if (options.getCoordinates() != null) {
            coordinates = new ArrayList<>();
            coordinates.add(options.getCoordinates().getLongitude());
            coordinates.add(options.getCoordinates().getLatitude());
        }

        return serviceClient.getGeocodingWithResponseAsync(options.getTop(), options.getQuery(),
                options.getAddressLine(), options.getCountryRegion(), boundingBox, options.getView(), coordinates,
                options.getAdminDistrict(), options.getAdminDistrict2(), options.getAdminDistrict3(),
                options.getLocality(), options.getPostalCode(), context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Use to get longitude and latitude coordinates of a street address or name of a place.
     *
     *
     *
     * The `Get Geocoding` API is an HTTP `GET` request that returns the longitude and latitude coordinates of the
     * location being searched.
     *
     * In many cases, the complete search service might be too much, for instance if you are only interested in
     * traditional geocoding. Search can also be accessed for address look up exclusively. The geocoding is performed by
     * hitting the geocoding endpoint with just the address or partial address in question. The geocoding search index
     * will be queried for everything above the street level data. No Point of Interest (POIs) will be returned. Note
     * that the geocoder is very tolerant of typos and incomplete addresses. It will also handle everything from exact
     * street addresses or street or intersections as well as higher level geographies such as city centers, counties
     * and states. The response also returns detailed address properties such as street, postal code, municipality, and
     * country/region information.
     *
     * @param options base search options.
     *
     * **If query is given, should not use this parameter.**.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding call on successful completion of {@link Mono}.
     */

    Mono<GeocodingResponse> getGeocoding(BaseSearchOptions options, Context context) {
        List<Double> boundingBox = null;
        if (options.getBoundingBox().isPresent()) {
            boundingBox = new ArrayList<>();
            GeoBoundingBox boundingBoxObj = (GeoBoundingBox)options.getBoundingBox().get();
            boundingBox.add(boundingBoxObj.getNorth());
            boundingBox.add(boundingBoxObj.getWest());
            boundingBox.add(boundingBoxObj.getSouth());
            boundingBox.add(boundingBoxObj.getEast());
        }
        List<Double> coordinates = null;
        if (options.getCoordinates() != null) {
            coordinates = new ArrayList<>();
            coordinates.add(options.getCoordinates().getLongitude());
            coordinates.add(options.getCoordinates().getLatitude());
        }

        return serviceClient.getGeocodingAsync(options.getTop(), options.getQuery(),
                options.getAddressLine(), options.getCountryRegion(), boundingBox, options.getView(), coordinates,
                options.getAdminDistrict(), options.getAdminDistrict2(), options.getAdminDistrict3(),
                options.getLocality(), options.getPostalCode(), context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }


    /**
     * Use to send a batch of queries to the [Geocoding](/rest/api/maps/search/get-geocoding) API in a single request.
     *
     *
     *
     * The `Get Geocoding Batch` API is an HTTP `POST` request that sends batches of up to **100** queries to the
     * [Geocoding](/rest/api/maps/search/get-geocoding) API in a single request.
     *
     * ### Submit Synchronous Batch Request
     * The Synchronous API is recommended for lightweight batch requests. When the service receives a request, it will
     * respond as soon as the batch items are calculated and there will be no possibility to retrieve the results later.
     * The Synchronous API will return a timeout error (a 408 response) if the request takes longer than 60 seconds. The
     * number of batch items is limited to **100** for this API.
     * ```
     * POST https://atlas.microsoft.com/geocode:batch?api-version=2023-06-01
     * ```
     * ### POST Body for Batch Request
     * To send the _geocoding_ queries you will use a `POST` request where the request body will contain the
     * `batchItems` array in `json` format and the `Content-Type` header will be set to `application/json`. Here's a
     * sample request body containing 2 _geocoding_ queries:
     *
     *
     * ```
     * {
     * "batchItems": [
     * {
     * "addressLine": "One, Microsoft Way, Redmond, WA 98052",
     * "top": 2
     * },
     * {
     * "addressLine": "Pike Pl",
     * "adminDistrict": "WA",
     * "locality": "Seattle",
     * "top": 3
     * }
     * ]
     * }
     * ```
     *
     * A _geocoding_ batchItem object can accept any of the supported _geocoding_ [URI
     * parameters](/rest/api/maps/search/get-geocoding#uri-parameters).
     *
     *
     * The batch should contain at least **1** query.
     *
     *
     * ### Batch Response Model
     * The batch response contains a `summary` component that indicates the `totalRequests` that were part of the
     * original batch request and `successfulRequests` i.e. queries which were executed successfully. The batch response
     * also includes a `batchItems` array which contains a response for each and every query in the batch request. The
     * `batchItems` will contain the results in the exact same order the original queries were sent in the batch
     * request. Each item is of one of the following types:
     *
     * - [`GeocodingResponse`](/rest/api/maps/search/get-geocoding#geocodingresponse) - If the query completed
     * successfully.
     *
     * - `Error` - If the query failed. The response will contain a `code` and a `message` in this case.
     *
     * @param body The list of address geocoding queries/requests to process. The list can contain
     * a max of 100 queries and must contain at least 1 query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding Batch service call on successful completion of
     * {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GeocodingBatchResponse> getGeocodingBatch(GeocodingBatchRequestBody body) {
        return serviceClient.getGeocodingBatchAsync(body);
    }

    /**
     * Use to send a batch of queries to the [Geocoding](/rest/api/maps/search/get-geocoding) API in a single request.
     *
     *
     *
     * The `Get Geocoding Batch` API is an HTTP `POST` request that sends batches of up to **100** queries to the
     * [Geocoding](/rest/api/maps/search/get-geocoding) API in a single request.
     *
     * ### Submit Synchronous Batch Request
     * The Synchronous API is recommended for lightweight batch requests. When the service receives a request, it will
     * respond as soon as the batch items are calculated and there will be no possibility to retrieve the results later.
     * The Synchronous API will return a timeout error (a 408 response) if the request takes longer than 60 seconds. The
     * number of batch items is limited to **100** for this API.
     * ```
     * POST https://atlas.microsoft.com/geocode:batch?api-version=2023-06-01
     * ```
     * ### POST Body for Batch Request
     * To send the _geocoding_ queries you will use a `POST` request where the request body will contain the
     * `batchItems` array in `json` format and the `Content-Type` header will be set to `application/json`. Here's a
     * sample request body containing 2 _geocoding_ queries:
     *
     *
     * ```
     * {
     * "batchItems": [
     * {
     * "addressLine": "One, Microsoft Way, Redmond, WA 98052",
     * "top": 2
     * },
     * {
     * "addressLine": "Pike Pl",
     * "adminDistrict": "WA",
     * "locality": "Seattle",
     * "top": 3
     * }
     * ]
     * }
     * ```
     *
     * A _geocoding_ batchItem object can accept any of the supported _geocoding_ [URI
     * parameters](/rest/api/maps/search/get-geocoding#uri-parameters).
     *
     *
     * The batch should contain at least **1** query.
     *
     *
     * ### Batch Response Model
     * The batch response contains a `summary` component that indicates the `totalRequests` that were part of the
     * original batch request and `successfulRequests` i.e. queries which were executed successfully. The batch response
     * also includes a `batchItems` array which contains a response for each and every query in the batch request. The
     * `batchItems` will contain the results in the exact same order the original queries were sent in the batch
     * request. Each item is of one of the following types:
     *
     * - [`GeocodingResponse`](/rest/api/maps/search/get-geocoding#geocodingresponse) - If the query completed
     * successfully.
     *
     * - `Error` - If the query failed. The response will contain a `code` and a `message` in this case.
     *
     * @param body The list of address geocoding queries/requests to process. The list can contain
     * a max of 100 queries and must contain at least 1 query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding Batch service call along with {@link Response} on
     * successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<GeocodingBatchResponse>> getGeocodingBatchWithResponse(GeocodingBatchRequestBody body) {
        return withContext(context -> getGeocodingBatchWithResponse(body, context));
    }

    /**
     * Use to send a batch of queries to the [Geocoding](/rest/api/maps/search/get-geocoding) API in a single request.
     *
     *
     *
     * The `Get Geocoding Batch` API is an HTTP `POST` request that sends batches of up to **100** queries to the
     * [Geocoding](/rest/api/maps/search/get-geocoding) API in a single request.
     *
     * ### Submit Synchronous Batch Request
     * The Synchronous API is recommended for lightweight batch requests. When the service receives a request, it will
     * respond as soon as the batch items are calculated and there will be no possibility to retrieve the results later.
     * The Synchronous API will return a timeout error (a 408 response) if the request takes longer than 60 seconds. The
     * number of batch items is limited to **100** for this API.
     * ```
     * POST https://atlas.microsoft.com/geocode:batch?api-version=2023-06-01
     * ```
     * ### POST Body for Batch Request
     * To send the _geocoding_ queries you will use a `POST` request where the request body will contain the
     * `batchItems` array in `json` format and the `Content-Type` header will be set to `application/json`. Here's a
     * sample request body containing 2 _geocoding_ queries:
     *
     *
     * ```
     * {
     * "batchItems": [
     * {
     * "addressLine": "One, Microsoft Way, Redmond, WA 98052",
     * "top": 2
     * },
     * {
     * "addressLine": "Pike Pl",
     * "adminDistrict": "WA",
     * "locality": "Seattle",
     * "top": 3
     * }
     * ]
     * }
     * ```
     *
     * A _geocoding_ batchItem object can accept any of the supported _geocoding_ [URI
     * parameters](/rest/api/maps/search/get-geocoding#uri-parameters).
     *
     *
     * The batch should contain at least **1** query.
     *
     *
     * ### Batch Response Model
     * The batch response contains a `summary` component that indicates the `totalRequests` that were part of the
     * original batch request and `successfulRequests` i.e. queries which were executed successfully. The batch response
     * also includes a `batchItems` array which contains a response for each and every query in the batch request. The
     * `batchItems` will contain the results in the exact same order the original queries were sent in the batch
     * request. Each item is of one of the following types:
     *
     * - [`GeocodingResponse`](/rest/api/maps/search/get-geocoding#geocodingresponse) - If the query completed
     * successfully.
     *
     * - `Error` - If the query failed. The response will contain a `code` and a `message` in this case.
     *
     * @param geocodingBatchRequestBody The list of address geocoding queries/requests to process. The list can contain
     * a max of 100 queries and must contain at least 1 query.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding Batch service call along with {@link Response} on
     * successful completion of {@link Mono}.
     */
    Mono<Response<GeocodingBatchResponse>> getGeocodingBatchWithResponse(GeocodingBatchRequestBody geocodingBatchRequestBody,
                                                                         Context context) {
        return serviceClient.getGeocodingBatchWithResponseAsync(geocodingBatchRequestBody, context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Use to get a street address and location info from longitude and latitude coordinates.
     *
     *
     *
     * The `Get Reverse Geocoding` API is an HTTP `GET` request used to translate a coordinate (example: 37.786505,
     * -122.3862) into a human understandable street address. Useful in tracking applications where you receive a GPS
     * feed from the device or asset and wish to know the address associated with the coordinates. This endpoint will
     * return address information for a given coordinate.
     *
     * @param coordinates The coordinates of the location that you want to reverse geocode. Example:
     * &amp;coordinates=lon,lat.
     * @param resultTypes Specify entity types that you want in the response. Only the types you specify will be
     * returned. If the point cannot be mapped to the entity types you specify, no location information is returned in
     * the response.
     * Default value is all possible entities.
     * A comma separated list of entity types selected from the following options.
     *
     * - Address
     * - Neighborhood
     * - PopulatedPlace
     * - Postcode1
     * - AdminDivision1
     * - AdminDivision2
     * - CountryRegion
     *
     * These entity types are ordered from the most specific entity to the least specific entity. When entities of more
     * than one entity type are found, only the most specific entity is returned. For example, if you specify Address
     * and AdminDistrict1 as entity types and entities were found for both types, only the Address entity information is
     * returned in the response.
     * @param view A string that represents an [ISO 3166-1 Alpha-2 region/country
     * code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2). This will alter Geopolitical disputed borders and labels
     * to align with the specified user region. By default, the View parameter is set to “Auto” even if you haven’t
     * defined it in the request.
     *
     * Please refer to [Supported Views](https://aka.ms/AzureMapsLocalizationViews) for details and to see the available
     * Views.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding call on successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GeocodingResponse> getReverseGeocoding(GeoPosition coordinates, List<ReverseGeocodingResultTypeEnum> resultTypes, String view) {
        List<Double> coordinatesList = null;
        if (coordinates != null) {
            coordinatesList = new ArrayList<>();
            coordinatesList.add(coordinates.getLongitude());
            coordinatesList.add(coordinates.getLatitude());
        }
        return serviceClient.getReverseGeocodingAsync(coordinatesList, resultTypes, view, null);
    }

    /**
     * Use to get a street address and location info from longitude and latitude coordinates.
     *
     *
     *
     * The `Get Reverse Geocoding` API is an HTTP `GET` request used to translate a coordinate (example: 37.786505,
     * -122.3862) into a human understandable street address. Useful in tracking applications where you receive a GPS
     * feed from the device or asset and wish to know the address associated with the coordinates. This endpoint will
     * return address information for a given coordinate.
     *
     * @param coordinates The coordinates of the location that you want to reverse geocode. Example:
     * &amp;coordinates=lon,lat.
     * @param resultTypes Specify entity types that you want in the response. Only the types you specify will be
     * returned. If the point cannot be mapped to the entity types you specify, no location information is returned in
     * the response.
     * Default value is all possible entities.
     * A comma separated list of entity types selected from the following options.
     *
     * - Address
     * - Neighborhood
     * - PopulatedPlace
     * - Postcode1
     * - AdminDivision1
     * - AdminDivision2
     * - CountryRegion
     *
     * These entity types are ordered from the most specific entity to the least specific entity. When entities of more
     * than one entity type are found, only the most specific entity is returned. For example, if you specify Address
     * and AdminDistrict1 as entity types and entities were found for both types, only the Address entity information is
     * returned in the response.
     * @param view A string that represents an [ISO 3166-1 Alpha-2 region/country
     * code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2). This will alter Geopolitical disputed borders and labels
     * to align with the specified user region. By default, the View parameter is set to “Auto” even if you haven’t
     * defined it in the request.
     *
     * Please refer to [Supported Views](https://aka.ms/AzureMapsLocalizationViews) for details and to see the available
     * Views.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding call along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<GeocodingResponse>> getReverseGeocodingWithResponse(
        GeoPosition coordinates, List<ReverseGeocodingResultTypeEnum> resultTypes, String view) {
        return withContext(context -> getReverseGeocodingWithResponse(coordinates, resultTypes, view, context));
    }

    /**
     * Use to get a street address and location info from longitude and latitude coordinates.
     *
     *
     *
     * The `Get Reverse Geocoding` API is an HTTP `GET` request used to translate a coordinate (example: 37.786505,
     * -122.3862) into a human understandable street address. Useful in tracking applications where you receive a GPS
     * feed from the device or asset and wish to know the address associated with the coordinates. This endpoint will
     * return address information for a given coordinate.
     *
     * @param coordinates The coordinates of the location that you want to reverse geocode. Example:
     * &amp;coordinates=lon,lat.
     * @param resultTypes Specify entity types that you want in the response. Only the types you specify will be
     * returned. If the point cannot be mapped to the entity types you specify, no location information is returned in
     * the response.
     * Default value is all possible entities.
     * A comma separated list of entity types selected from the following options.
     *
     * - Address
     * - Neighborhood
     * - PopulatedPlace
     * - Postcode1
     * - AdminDivision1
     * - AdminDivision2
     * - CountryRegion
     *
     * These entity types are ordered from the most specific entity to the least specific entity. When entities of more
     * than one entity type are found, only the most specific entity is returned. For example, if you specify Address
     * and AdminDistrict1 as entity types and entities were found for both types, only the Address entity information is
     * returned in the response.
     * @param view A string that represents an [ISO 3166-1 Alpha-2 region/country
     * code](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2). This will alter Geopolitical disputed borders and labels
     * to align with the specified user region. By default, the View parameter is set to “Auto” even if you haven’t
     * defined it in the request.
     *
     * Please refer to [Supported Views](https://aka.ms/AzureMapsLocalizationViews) for details and to see the available
     * Views.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding call along with {@link Response} on successful
     * completion of {@link Mono}.
     */
    Mono<Response<GeocodingResponse>> getReverseGeocodingWithResponse(
        GeoPosition coordinates, List<ReverseGeocodingResultTypeEnum> resultTypes, String view, Context context) {

        List<Double> coordinatesList = null;
        if (coordinates != null) {
            coordinatesList = new ArrayList<>();
            coordinatesList.add(coordinates.getLongitude());
            coordinatesList.add(coordinates.getLatitude());
        }
        return serviceClient.getReverseGeocodingWithResponseAsync(coordinatesList, resultTypes, view, context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }

    /**
     * Use to send a batch of queries to the [Reverse Geocoding](/rest/api/maps/search/get-reverse-geocoding) API in a
     * single request.
     *
     *
     *
     * The `Get Reverse Geocoding Batch` API is an HTTP `POST` request that sends batches of up to **100** queries to
     * [Reverse Geocoding](/rest/api/maps/search/get-reverse-geocoding) API using a single request.
     *
     * ### Submit Synchronous Batch Request
     * The Synchronous API is recommended for lightweight batch requests. When the service receives a request, it will
     * respond as soon as the batch items are calculated and there will be no possibility to retrieve the results later.
     * The Synchronous API will return a timeout error (a 408 response) if the request takes longer than 60 seconds. The
     * number of batch items is limited to **100** for this API.
     * ```
     * POST https://atlas.microsoft.com/reverseGeocode:batch?api-version=2023-06-01
     * ```
     * ### POST Body for Batch Request
     * To send the _reverse geocoding_ queries you will use a `POST` request where the request body will contain the
     * `batchItems` array in `json` format and the `Content-Type` header will be set to `application/json`. Here's a
     * sample request body containing 2 _reverse geocoding_ queries:
     *
     *
     * ```
     * {
     * "batchItems": [
     * {
     * "coordinates": [-122.128275, 47.639429],
     * "resultTypes": ["Address", "PopulatedPlace"]
     * },
     * {
     * "coordinates": [-122.341979399674, 47.6095253501216]
     * }
     * ]
     * }
     * ```
     *
     * A _reverse geocoding_ batchItem object can accept any of the supported _reverse geocoding_ [URI
     * parameters](/rest/api/maps/search/get-reverse-geocoding#uri-parameters).
     *
     *
     * The batch should contain at least **1** query.
     *
     *
     * ### Batch Response Model
     * The batch response contains a `summary` component that indicates the `totalRequests` that were part of the
     * original batch request and `successfulRequests` i.e. queries which were executed successfully. The batch response
     * also includes a `batchItems` array which contains a response for each and every query in the batch request. The
     * `batchItems` will contain the results in the exact same order the original queries were sent in the batch
     * request. Each item is of one of the following types:
     *
     * - [`GeocodingResponse`](/rest/api/maps/search/get-reverse-geocoding#geocodingresponse) - If the query completed
     * successfully.
     *
     * - `Error` - If the query failed. The response will contain a `code` and a `message` in this case.
     *
     * @param reverseGeocodingBatchRequestBody The list of reverse geocoding queries/requests to process. The list can
     * contain a max of 100 queries and must contain at least 1 query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding Batch service call on successful completion of
     * {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<GeocodingBatchResponse> getReverseGeocodingBatch(ReverseGeocodingBatchRequestBody reverseGeocodingBatchRequestBody) {
        return serviceClient.getReverseGeocodingBatchAsync(reverseGeocodingBatchRequestBody);
    }

    /**
     * Use to send a batch of queries to the [Reverse Geocoding](/rest/api/maps/search/get-reverse-geocoding) API in a
     * single request.
     *
     *
     *
     * The `Get Reverse Geocoding Batch` API is an HTTP `POST` request that sends batches of up to **100** queries to
     * [Reverse Geocoding](/rest/api/maps/search/get-reverse-geocoding) API using a single request.
     *
     * ### Submit Synchronous Batch Request
     * The Synchronous API is recommended for lightweight batch requests. When the service receives a request, it will
     * respond as soon as the batch items are calculated and there will be no possibility to retrieve the results later.
     * The Synchronous API will return a timeout error (a 408 response) if the request takes longer than 60 seconds. The
     * number of batch items is limited to **100** for this API.
     * ```
     * POST https://atlas.microsoft.com/reverseGeocode:batch?api-version=2023-06-01
     * ```
     * ### POST Body for Batch Request
     * To send the _reverse geocoding_ queries you will use a `POST` request where the request body will contain the
     * `batchItems` array in `json` format and the `Content-Type` header will be set to `application/json`. Here's a
     * sample request body containing 2 _reverse geocoding_ queries:
     *
     *
     * ```
     * {
     * "batchItems": [
     * {
     * "coordinates": [-122.128275, 47.639429],
     * "resultTypes": ["Address", "PopulatedPlace"]
     * },
     * {
     * "coordinates": [-122.341979399674, 47.6095253501216]
     * }
     * ]
     * }
     * ```
     *
     * A _reverse geocoding_ batchItem object can accept any of the supported _reverse geocoding_ [URI
     * parameters](/rest/api/maps/search/get-reverse-geocoding#uri-parameters).
     *
     *
     * The batch should contain at least **1** query.
     *
     *
     * ### Batch Response Model
     * The batch response contains a `summary` component that indicates the `totalRequests` that were part of the
     * original batch request and `successfulRequests` i.e. queries which were executed successfully. The batch response
     * also includes a `batchItems` array which contains a response for each and every query in the batch request. The
     * `batchItems` will contain the results in the exact same order the original queries were sent in the batch
     * request. Each item is of one of the following types:
     *
     * - [`GeocodingResponse`](/rest/api/maps/search/get-reverse-geocoding#geocodingresponse) - If the query completed
     * successfully.
     *
     * - `Error` - If the query failed. The response will contain a `code` and a `message` in this case.
     *
     * @param reverseGeocodingBatchRequestBody The list of reverse geocoding queries/requests to process. The list can
     * contain a max of 100 queries and must contain at least 1 query.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding Batch service call along with {@link Response} on
     * successful completion of {@link Mono}.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<GeocodingBatchResponse>> getReverseGeocodingBatchWithResponse(ReverseGeocodingBatchRequestBody reverseGeocodingBatchRequestBody) {
        return withContext(context -> getReverseGeocodingBatchWithResponse(reverseGeocodingBatchRequestBody, context));
    }

    /**
     * Use to send a batch of queries to the [Reverse Geocoding](/rest/api/maps/search/get-reverse-geocoding) API in a
     * single request.
     *
     *
     *
     * The `Get Reverse Geocoding Batch` API is an HTTP `POST` request that sends batches of up to **100** queries to
     * [Reverse Geocoding](/rest/api/maps/search/get-reverse-geocoding) API using a single request.
     *
     * ### Submit Synchronous Batch Request
     * The Synchronous API is recommended for lightweight batch requests. When the service receives a request, it will
     * respond as soon as the batch items are calculated and there will be no possibility to retrieve the results later.
     * The Synchronous API will return a timeout error (a 408 response) if the request takes longer than 60 seconds. The
     * number of batch items is limited to **100** for this API.
     * ```
     * POST https://atlas.microsoft.com/reverseGeocode:batch?api-version=2023-06-01
     * ```
     * ### POST Body for Batch Request
     * To send the _reverse geocoding_ queries you will use a `POST` request where the request body will contain the
     * `batchItems` array in `json` format and the `Content-Type` header will be set to `application/json`. Here's a
     * sample request body containing 2 _reverse geocoding_ queries:
     *
     *
     * ```
     * {
     * "batchItems": [
     * {
     * "coordinates": [-122.128275, 47.639429],
     * "resultTypes": ["Address", "PopulatedPlace"]
     * },
     * {
     * "coordinates": [-122.341979399674, 47.6095253501216]
     * }
     * ]
     * }
     * ```
     *
     * A _reverse geocoding_ batchItem object can accept any of the supported _reverse geocoding_ [URI
     * parameters](/rest/api/maps/search/get-reverse-geocoding#uri-parameters).
     *
     *
     * The batch should contain at least **1** query.
     *
     *
     * ### Batch Response Model
     * The batch response contains a `summary` component that indicates the `totalRequests` that were part of the
     * original batch request and `successfulRequests` i.e. queries which were executed successfully. The batch response
     * also includes a `batchItems` array which contains a response for each and every query in the batch request. The
     * `batchItems` will contain the results in the exact same order the original queries were sent in the batch
     * request. Each item is of one of the following types:
     *
     * - [`GeocodingResponse`](/rest/api/maps/search/get-reverse-geocoding#geocodingresponse) - If the query completed
     * successfully.
     *
     * - `Error` - If the query failed. The response will contain a `code` and a `message` in this case.
     *
     * @param reverseGeocodingBatchRequestBody The list of reverse geocoding queries/requests to process. The list can
     * contain a max of 100 queries and must contain at least 1 query.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return this object is returned from a successful Geocoding Batch service call along with {@link Response} on
     * successful completion of {@link Mono}.
     */
    Mono<Response<GeocodingBatchResponse>> getReverseGeocodingBatchWithResponse(ReverseGeocodingBatchRequestBody reverseGeocodingBatchRequestBody, Context context) {
        return serviceClient.getReverseGeocodingBatchWithResponseAsync(reverseGeocodingBatchRequestBody, context)
            .onErrorMap(MapsSearchAsyncClient::mapThrowable);
    }


    private static Throwable mapThrowable(Throwable throwable) {
        if (!(throwable instanceof ErrorResponseException)) {
            return throwable;
        }
        ErrorResponseException exception = (ErrorResponseException) throwable;
        return new HttpResponseException(exception.getMessage(), exception.getResponse());
    }
}
