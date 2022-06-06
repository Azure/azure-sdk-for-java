// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.implementation.helpers;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoObject;
import com.azure.core.models.GeoPosition;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerProvider;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.search.implementation.models.BatchRequestItem;
import com.azure.maps.search.implementation.models.BoundingBoxCompassNotation;
import com.azure.maps.search.implementation.models.GeoJsonFeatureCollection;
import com.azure.maps.search.implementation.models.GeoJsonObject;
import com.azure.maps.search.implementation.models.ReverseSearchAddressBatchItemPrivate;
import com.azure.maps.search.implementation.models.ReverseSearchAddressBatchResult;
import com.azure.maps.search.implementation.models.SearchAddressBatchItemPrivate;
import com.azure.maps.search.implementation.models.SearchAddressBatchResult;
import com.azure.maps.search.models.BaseSearchOptions;
import com.azure.maps.search.models.BatchResultSummary;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.FuzzySearchOptions;
import com.azure.maps.search.models.ReverseSearchAddressBatchItem;
import com.azure.maps.search.models.ReverseSearchAddressOptions;
import com.azure.maps.search.models.ReverseSearchAddressResult;
import com.azure.maps.search.models.SearchAddressBatchItem;
import com.azure.maps.search.models.SearchAddressOptions;
import com.azure.maps.search.models.SearchAddressResult;

/**
 * Utility method class.
 */
public class Utility {
    private static final JacksonJsonSerializer SERIALIZER = new JacksonJsonSerializerProvider().createInstance();
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9A-Fa-f\\-]{36}");

    /**
     * converts the internal representation of SearchAddressResult into the public one
     * @param response
     * @return
     */
    public static SimpleResponse<BatchSearchResult> createBatchSearchResponse(
            Response<SearchAddressBatchResult> response) {
        BatchSearchResult result = (response.getValue() == null) ? null : toBatchSearchResult(response.getValue());
        SimpleResponse<BatchSearchResult> simpleResponse = new SimpleResponse<>(response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            result);

        return simpleResponse;
    }

    /**
     * converts the internal representation of SearchAddressResult into the public one
     * @param response
     * @return
     */
    public static SimpleResponse<BatchReverseSearchResult> createBatchReverseSearchResponse(
            Response<ReverseSearchAddressBatchResult> response) {
        BatchReverseSearchResult result = (response.getValue() == null) ? null : toBatchReverseSearchResult(response.getValue());
        SimpleResponse<BatchReverseSearchResult> simpleResponse = new SimpleResponse<>(response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            result);

        return simpleResponse;
    }

    /**
     *
     */
    public static String getBatchId(HttpHeaders headers) {
        // this can happen when deserialization is happening
        // to convert the private model to public model (see BatchResponseSerializer)
        if (headers == null) {
            return null;
        }

        // if not, let's go
        final String location = headers.getValue("Location");

        if (location != null) {
            Matcher matcher = UUID_PATTERN.matcher(location);
            matcher.find();
            return matcher.group();
        }

        return null;
    }

    /**
     * Returns a GeoPosition from a comma-separated position string.
     * @param position
     * @return
     */
    public static GeoPosition fromCommaSeparatedString(String position) {
        final String[] coords = position.split(",");

        if (coords.length == 2) {
            return new GeoPosition(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));
        }

        return null;
    }

    /**
     * Returns a string representation of a {@link GeoPosition}.
     *
     * @return string representation
     */
    public static String positionToString(GeoPosition position) {
        return position.getLatitude() + "," + position.getLongitude();
    }

    /**
     * Converts a {@link BoundingBoxCompassNotation} into a {@link GeoBoundingBox}
     *
     * @param internalBoundingBox
     * @return a GeoBoundingBox.
     */
    public static GeoBoundingBox toGeoBoundingBox(BoundingBoxCompassNotation internalBoundingBox) {
        Objects.requireNonNull(internalBoundingBox, "Internal bounding box model is needed.");
        Objects.requireNonNull(internalBoundingBox.getNorthEast(), "Top right of the bounding box is needed.");
        Objects.requireNonNull(internalBoundingBox.getSouthWest(), "Bottom left of the bounding box is needed.");

        final String northEastString = internalBoundingBox.getNorthEast();
        final String southWestString = internalBoundingBox.getSouthWest();
        final GeoPosition topRight = stringToCoordinate(northEastString);
        final GeoPosition bottomLeft = stringToCoordinate(southWestString);

        GeoBoundingBox box = new GeoBoundingBox(bottomLeft.getLongitude(), bottomLeft.getLatitude(),
            topRight.getLongitude(), topRight.getLatitude());

        return box;
    }

    /**
     * Converts a comma-separated string into a {@link GeoPosition}.
     *
     * @param s - string to convert
     * @return a GeoPosition
     */
    private static GeoPosition stringToCoordinate(String s) {
        final String[] coordinateString = s.split(",");

        if (coordinateString.length == 2) {
            return new GeoPosition(Double.parseDouble(coordinateString[1]),
                Double.parseDouble(coordinateString[0]));
        }

        return null;
    }

    public static SearchAddressBatchItem toSearchAddressBatchItem(SearchAddressBatchItemPrivate item) {
        SearchAddressBatchItem resultItem = new SearchAddressBatchItem();
        SearchAddressBatchItemPropertiesHelper.setErrorDetail(resultItem, item.getResponse().getError());
        SearchAddressBatchItemPropertiesHelper.setStatusCode(resultItem, item.getStatusCode());
        SearchAddressResult result = item.getResponse();
        SearchAddressBatchItemPropertiesHelper.setSearchAddressResult(resultItem, result);

        return resultItem;
    }

    public static BatchSearchResult toBatchSearchResult(SearchAddressBatchResult result) {
        BatchResultSummary summary = result.getBatchSummary();
        List<SearchAddressBatchItem> items = result.getBatchItems().stream()
            .map(item -> toSearchAddressBatchItem(item))
            .collect(Collectors.toList());

        return new BatchSearchResult(summary, items);
    }

    public static ReverseSearchAddressBatchItem toReverseSearchAddressBatchItem(
            ReverseSearchAddressBatchItemPrivate item) {
        ReverseSearchAddressBatchItem resultItem = new ReverseSearchAddressBatchItem();
        ReverseSearchAddressBatchItemPropertiesHelper.setErrorDetail(resultItem, item.getResponse().getError());
        ReverseSearchAddressBatchItemPropertiesHelper.setStatusCode(resultItem, item.getStatusCode());
        ReverseSearchAddressResult result = item.getResponse();
        ReverseSearchAddressBatchItemPropertiesHelper.setReverseSearchAddressResult(resultItem, result);

        return resultItem;
    }

    public static BatchReverseSearchResult toBatchReverseSearchResult(ReverseSearchAddressBatchResult result) {
        BatchResultSummary summary = result.getBatchSummary();
        List<ReverseSearchAddressBatchItem> items = result.getBatchItems().stream()
            .map(item -> toReverseSearchAddressBatchItem(item))
            .collect(Collectors.toList());

        return new BatchReverseSearchResult(summary, items);
    }

    public static GeoJsonObject toGeoJsonObject(GeoObject object) {
        // serialize to GeoJson
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SERIALIZER.serialize(baos, object);

        // deserialize into GeoJsonObject
        final TypeReference<GeoJsonObject> typeReference = new TypeReference<GeoJsonObject>() { };
        return SERIALIZER.deserializeFromBytes(baos.toByteArray(), typeReference);
    }

    public static GeoObject toGeoObject(GeoJsonObject object) {
        // serialize to GeoJson
        // TODO (khmic5) Review this for other types of returned FeatureCollection
        if (object instanceof GeoJsonFeatureCollection) {
            GeoJsonFeatureCollection fc = (GeoJsonFeatureCollection) object;
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            SERIALIZER.serialize(baos, fc.getFeatures().get(0).getGeometry());

            // deserialize into GeoObject
            final TypeReference<GeoObject> typeReference = new TypeReference<GeoObject>() { };
            return SERIALIZER.deserializeFromBytes(baos.toByteArray(), typeReference);
        }

        return null;
    }

    public static BatchRequestItem toSearchBatchRequestItem(SearchAddressOptions options) {
        Map<String, Object> params = fillCommonSearchParameters(options);

        // single value parameters
        params.compute("query", (k, v) -> options.getQuery());
        params.compute("typeahead", (k, v) -> options.isTypeAhead());
        params.compute("entityType", (k, v) -> options.getEntityType());

        // comma separated parameters
        params.compute("countrySet", (k, v) -> listToCommaSeparatedString(options.getCountryFilter()));
        params.compute("extendedPostalCodesFor", (k, v) -> listToCommaSeparatedString(options.getExtendedPostalCodesFor()));

        // double parameters
        params.compute("lat", (k, v) -> options.getCoordinates().map(GeoPosition::getLatitude).orElse(null));
        params.compute("lon", (k, v) -> options.getCoordinates().map(GeoPosition::getLongitude).orElse(null));

        // batch request item conversion
        BatchRequestItem item = convertParametersToRequestItem(params);
        return item;
    }

    public static BatchRequestItem toFuzzySearchBatchRequestItem(FuzzySearchOptions options) {
        Map<String, Object> params = fillCommonSearchParameters(options);

        // single value parameters
        params.compute("query", (k, v) -> options.getQuery());
        params.compute("typeahead", (k, v) -> options.isTypeAhead());
        params.compute("entityType", (k, v) -> options.getEntityType());
        params.compute("openingHours", (k, v) -> options.getOperatingHours());
        params.compute("minFuzzyLevel", (k, v) -> options.getMinFuzzyLevel());
        params.compute("maxFuzzyLevel", (k, v) -> options.getMaxFuzzyLevel());

        // comma separated parameters
        params.compute("countrySet", (k, v) -> listToCommaSeparatedString(options.getCountryFilter()));
        params.compute("extendedPostalCodesFor", (k, v) -> listToCommaSeparatedString(options.getExtendedPostalCodesFor()));
        params.compute("idxSet", (k, v) -> listToCommaSeparatedString(options.getIndexFilter()));
        params.compute("connectorSet", (k, v) -> listToCommaSeparatedString(options.getElectricVehicleConnectorFilter()));
        params.compute("categorySet", (k, v) -> listToCommaSeparatedString(options.getCategoryFilter()));
        params.compute("brandSet", (k, v) -> listToCommaSeparatedString(options.getBrandFilter()));

        // double parameters
        params.compute("lat", (k, v) -> options.getCoordinates().map(GeoPosition::getLatitude).orElse(null));
        params.compute("lon", (k, v) -> options.getCoordinates().map(GeoPosition::getLongitude).orElse(null));

        // convert to item
        BatchRequestItem fuzzyItem = convertParametersToRequestItem(params);
        return fuzzyItem;
    }

    public static BatchRequestItem toReverseSearchBatchRequestItem(ReverseSearchAddressOptions options) {
        Map<String, Object> params = new HashMap<>();

        // single value parameters
        params.compute("query", (k, v) -> positionToString(options.getCoordinates()));
        params.compute("allowFreeformNewline", (k, v) -> options.allowFreeformNewline());
        params.compute("entityType", (k, v) -> options.getEntityType());
        params.compute("heading", (k, v) -> options.getHeading());
        params.compute("number", (k, v) -> options.getNumber());
        params.compute("radius", (k, v) -> options.getRadiusInMeters());
        params.compute("returnMatchType", (k, v) -> options.includeMatchType());
        params.compute("returnSpeedLimit", (k, v) -> options.includeSpeedLimit());
        params.compute("returnRoadUse", (k, v) -> options.includeRoadUse());
        params.compute("roadUse", (k, v) -> listToCommaSeparatedString(options.getRoadUse()));
        params.compute("view", (k, v) -> options.getLocalizedMapView());

        // convert to batchrequestitem
        BatchRequestItem reverseItem = convertParametersToRequestItem(params);
        return reverseItem;
    }

    private static <T extends BaseSearchOptions<T>> Map<String, Object>
            fillCommonSearchParameters(BaseSearchOptions<T> options) {
        Map<String, Object> params = new HashMap<>();

        // single value parameters
        params.compute("limit", (k, v) -> options.getTop());
        params.compute("ofs", (k, v) -> options.getSkip());
        params.compute("language", (k, v) -> options.getLanguage());
        params.compute("radius", (k, v) -> options.getRadiusInMeters());
        params.compute("view", (k, v) -> options.getLocalizedMapView());
        params.compute("topLeft", (k, v) -> options.getBoundingBox()
            .map(item -> new GeoPosition(item.getWest(), item.getNorth())).map(Utility::positionToString).orElse(null));
        params.compute("btmRight", (k, v) -> options.getBoundingBox()
            .map(item -> new GeoPosition(item.getEast(), item.getSouth())).map(Utility::positionToString).orElse(null));

        return params;
    }

    private static <T> String listToCommaSeparatedString(List<T> list) {
        if (list != null) {
            return String.join(",", list.stream()
                .map(item -> item.toString())
                .collect(Collectors.toList()));
        }
        return null;
    }

    private static BatchRequestItem convertParametersToRequestItem(Map<String, Object> params) {
        // batch request item conversion
        BatchRequestItem item = new BatchRequestItem();
        UrlBuilder urlBuilder = new UrlBuilder();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            try {
                urlBuilder.addQueryParameter(entry.getKey(), URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        item.setQuery(urlBuilder.getQueryString());
        return item;
    }
}
