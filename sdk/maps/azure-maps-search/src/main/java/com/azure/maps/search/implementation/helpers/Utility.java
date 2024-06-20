// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search.implementation.helpers;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoObject;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utility method class.
 */
public class Utility {
    private static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9A-Fa-f\\-]{36}");

    /**
     * converts the internal representation of SearchAddressResult into the public one
     * @param response
     * @return
     */
    public static SimpleResponse<BatchSearchResult> createBatchSearchResponse(
            Response<SearchAddressBatchResult> response) {
        BatchSearchResult result = (response.getValue() == null) ? null : toBatchSearchResult(response.getValue());

        return new SimpleResponse<>(response, result);
    }

    /**
     * converts the internal representation of SearchAddressResult into the public one
     * @param response
     * @return
     */
    public static SimpleResponse<BatchReverseSearchResult> createBatchReverseSearchResponse(
            Response<ReverseSearchAddressBatchResult> response) {
        BatchReverseSearchResult result = (response.getValue() == null) ? null : toBatchReverseSearchResult(response.getValue());

        return new SimpleResponse<>(response, result);
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
        final String location = headers.getValue(HttpHeaderName.LOCATION);

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

        return new GeoBoundingBox(bottomLeft.getLongitude(), bottomLeft.getLatitude(),
            topRight.getLongitude(), topRight.getLatitude());
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
            .map(Utility::toSearchAddressBatchItem)
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
            .map(Utility::toReverseSearchAddressBatchItem)
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
        UrlBuilder urlBuilder = new UrlBuilder();
        fillCommonSearchParameters(urlBuilder, options);

        // single value parameters
        addQueryParameterToUrl(urlBuilder, "query", options.getQuery());
        addQueryParameterToUrl(urlBuilder, "typeahead", options.isTypeAhead());
        addQueryParameterToUrl(urlBuilder, "entityType", options.getEntityType());

        // comma separated parameters
        addQueryParameterToUrl(urlBuilder, "countrySet", listToCommaSeparatedString(options.getCountryFilter()));
        addQueryParameterToUrl(urlBuilder, "extendedPostalCodesFor", listToCommaSeparatedString(options.getExtendedPostalCodesFor()));

        // double parameters
        addQueryParameterToUrl(urlBuilder, "lat", options.getCoordinates().map(GeoPosition::getLatitude).orElse(null));
        addQueryParameterToUrl(urlBuilder, "lon", options.getCoordinates().map(GeoPosition::getLongitude).orElse(null));

        // batch request item conversion
        return new BatchRequestItem().setQuery(urlBuilder.getQueryString());
    }

    public static BatchRequestItem toFuzzySearchBatchRequestItem(FuzzySearchOptions options) {
        UrlBuilder urlBuilder = new UrlBuilder();
        fillCommonSearchParameters(urlBuilder, options);

        // single value parameters
        addQueryParameterToUrl(urlBuilder, "query", options.getQuery());
        addQueryParameterToUrl(urlBuilder, "typeahead", options.isTypeAhead());
        addQueryParameterToUrl(urlBuilder, "entityType", options.getEntityType());
        addQueryParameterToUrl(urlBuilder, "openingHours", options.getOperatingHours());
        addQueryParameterToUrl(urlBuilder, "minFuzzyLevel", options.getMinFuzzyLevel());
        addQueryParameterToUrl(urlBuilder, "maxFuzzyLevel", options.getMaxFuzzyLevel());

        // comma separated parameters
        addQueryParameterToUrl(urlBuilder, "countrySet", listToCommaSeparatedString(options.getCountryFilter()));
        addQueryParameterToUrl(urlBuilder, "extendedPostalCodesFor", listToCommaSeparatedString(options.getExtendedPostalCodesFor()));
        addQueryParameterToUrl(urlBuilder, "idxSet", listToCommaSeparatedString(options.getIndexFilter()));
        addQueryParameterToUrl(urlBuilder, "connectorSet", listToCommaSeparatedString(options.getElectricVehicleConnectorFilter()));
        addQueryParameterToUrl(urlBuilder, "categorySet", listToCommaSeparatedString(options.getCategoryFilter()));
        addQueryParameterToUrl(urlBuilder, "brandSet", listToCommaSeparatedString(options.getBrandFilter()));

        // double parameters
        addQueryParameterToUrl(urlBuilder, "lat", options.getCoordinates().map(GeoPosition::getLatitude).orElse(null));
        addQueryParameterToUrl(urlBuilder, "lon", options.getCoordinates().map(GeoPosition::getLongitude).orElse(null));

        // convert to item
        return new BatchRequestItem().setQuery(urlBuilder.getQueryString());
    }

    public static BatchRequestItem toReverseSearchBatchRequestItem(ReverseSearchAddressOptions options) {
        UrlBuilder urlBuilder = new UrlBuilder();

        // single value parameters
        addQueryParameterToUrl(urlBuilder, "query", positionToString(options.getCoordinates()));
        addQueryParameterToUrl(urlBuilder, "allowFreeformNewline", options.allowFreeformNewline());
        addQueryParameterToUrl(urlBuilder, "entityType", options.getEntityType());
        addQueryParameterToUrl(urlBuilder, "heading", options.getHeading());
        addQueryParameterToUrl(urlBuilder, "number", options.getNumber());
        addQueryParameterToUrl(urlBuilder, "radius", options.getRadiusInMeters());
        addQueryParameterToUrl(urlBuilder, "returnMatchType", options.includeMatchType());
        addQueryParameterToUrl(urlBuilder, "returnSpeedLimit", options.includeSpeedLimit());
        addQueryParameterToUrl(urlBuilder, "returnRoadUse", options.includeRoadUse());
        addQueryParameterToUrl(urlBuilder, "roadUse", listToCommaSeparatedString(options.getRoadUse()));
        addQueryParameterToUrl(urlBuilder, "view", options.getLocalizedMapView());

        // convert to batchrequestitem
        return new BatchRequestItem().setQuery(urlBuilder.getQueryString());
    }

    private static void fillCommonSearchParameters(UrlBuilder urlBuilder, BaseSearchOptions<?> options) {

        // single value parameters
        addQueryParameterToUrl(urlBuilder, "limit", options.getTop());
        addQueryParameterToUrl(urlBuilder, "ofs", options.getSkip());
        addQueryParameterToUrl(urlBuilder, "language", options.getLanguage());
        addQueryParameterToUrl(urlBuilder, "radius", options.getRadiusInMeters());
        addQueryParameterToUrl(urlBuilder, "view", options.getLocalizedMapView());
        addQueryParameterToUrl(urlBuilder, "topLeft", options.getBoundingBox()
            .map(item -> new GeoPosition(item.getWest(), item.getNorth())).map(Utility::positionToString).orElse(null));
        addQueryParameterToUrl(urlBuilder, "btmRight", options.getBoundingBox()
            .map(item -> new GeoPosition(item.getEast(), item.getSouth())).map(Utility::positionToString).orElse(null));
    }

    private static <T> String listToCommaSeparatedString(List<T> list) {
        if (list != null) {
            return list.stream().map(Object::toString).collect(Collectors.joining(","));
        }
        return null;
    }

    private static void addQueryParameterToUrl(UrlBuilder urlBuilder, String key, Object value) {
        if (value == null) {
            return;
        }

        try {
            urlBuilder.addQueryParameter(key, URLEncoder.encode(value.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
