// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route.implementation.helpers;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.GeoCollection;
import com.azure.core.models.GeoPolygonCollection;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.UrlBuilder;
import com.azure.core.util.serializer.JsonSerializer;
import com.azure.core.util.serializer.JsonSerializerProviders;
import com.azure.core.util.serializer.TypeReference;
import com.azure.maps.route.implementation.models.BatchRequestItem;
import com.azure.maps.route.implementation.models.GeoJsonGeometryCollection;
import com.azure.maps.route.implementation.models.GeoJsonMultiPoint;
import com.azure.maps.route.implementation.models.GeoJsonMultiPolygon;
import com.azure.maps.route.implementation.models.RouteDirectionParametersPrivate;
import com.azure.maps.route.implementation.models.RouteDirectionsBatchResultPrivate;
import com.azure.maps.route.implementation.models.RouteMatrixQueryPrivate;
import com.azure.maps.route.implementation.models.RouteMatrixResultPrivate;
import com.azure.maps.route.models.RouteDirectionsBatchResult;
import com.azure.maps.route.models.RouteDirectionsOptions;
import com.azure.maps.route.models.RouteDirectionsParameters;
import com.azure.maps.route.models.RouteMatrixQuery;
import com.azure.maps.route.models.RouteMatrixResult;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utility {
    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9A-Fa-f\\-]{36}");
    private static final JsonSerializer SERIALIZER = JsonSerializerProviders.createInstance(true);

    /**
     * Gets batch Id from headers to be used in the returned batch result object.
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
     * converts the internal representation of {@link RouteMatrixResult} into the public one
     * from inside the HTTP response.
     *
     * @param response
     * @return
     */
    public static SimpleResponse<RouteMatrixResult> createRouteMatrixResponse(
        Response<RouteMatrixResultPrivate> response) {
        RouteMatrixResult result = response.getValue() != null
            ? Utility.toRouteMatrixResult(response.getValue())
            : null;

        return new SimpleResponse<>(response, result);
    }

    /**
     * Converts a private {@link RouteMatrixResultPrivate} to a public @{link RouteMatrixResult}
     *
     * @param privateResult
     * @return a public instance of {@link RouteMatrixResult}
     */
    private static RouteMatrixResult toRouteMatrixResult(RouteMatrixResultPrivate privateResult) {
        RouteMatrixResult result = new RouteMatrixResult();
        RouteMatrixResultPropertiesHelper.setFromRouteMatrixResultPrivate(result, privateResult);
        return result;
    }

    /**
     * Converts a private {@link RouteMatrixQueryPrivate} into a public {@link RouteMatrixQuery}
     *
     * @param query
     * @return
     */
    public static RouteMatrixQueryPrivate toRouteMatrixQueryPrivate(RouteMatrixQuery query) {
        RouteMatrixQueryPrivate privateQuery = new RouteMatrixQueryPrivate();
        GeoJsonMultiPoint origins = new GeoJsonMultiPoint();
        GeoJsonMultiPoint destinations = new GeoJsonMultiPoint();

        // origins
        List<List<Double>> originCoordinates = query.getOrigins().getPoints().stream().map(point -> {
            GeoPosition position = point.getCoordinates();
            return Arrays.asList(position.getLongitude(), position.getLatitude());
        }).collect(Collectors.toList());

        // destinations
        List<List<Double>> destCoordinates = query.getDestinations().getPoints().stream().map(point -> {
            GeoPosition position = point.getCoordinates();
            return Arrays.asList(position.getLongitude(), position.getLatitude());
        }).collect(Collectors.toList());

        origins.setCoordinates(originCoordinates);
        destinations.setCoordinates(destCoordinates);
        privateQuery.setOrigins(origins);
        privateQuery.setDestinations(destinations);
        return privateQuery;
    }

    /**
     * Returns the route points (coordinates) as a colon separated string.
     *
     * @param routePoints
     * @return a String containing all route points
     */
    public static String toRouteQueryString(List<GeoPosition> routePoints) {
        return routePoints.stream()
            .map(item -> item.getLatitude() + "," + item.getLongitude())
            .collect(Collectors.joining(":"));
    }

    /**
     * Returns the private version of a @{link RouteDirectionsParameters}.
     *
     * return private parameters
     */
    public static RouteDirectionParametersPrivate toRouteDirectionParametersPrivate(
        RouteDirectionsParameters parameters) {
        RouteDirectionParametersPrivate privateParams = new RouteDirectionParametersPrivate();

        // set allowVignette
        privateParams.setAllowVignette(parameters.getAllowVignette());

        // convert GeoCollection into GeoJsonGeometryCollection
        GeoCollection collection = parameters.getSupportingPoints();
        GeoJsonGeometryCollection internalCollection = toGeoJsonGeometryCollection(collection);
        privateParams.setSupportingPoints(internalCollection);

        // convert GeoPolygonCollection into a multi polygon
        GeoPolygonCollection polygonCollection = parameters.getAvoidAreas();
        GeoJsonMultiPolygon avoidAreas = toGeoJsonMultiPolygon(polygonCollection);
        privateParams.setAvoidAreas(avoidAreas);

        return privateParams;
    }

    /**
     * Converts a {@link GeoCollection} into a private {@link GeoJsonGeometryCollection}.
     *
     * @param collection
     * @return
     */
    public static GeoJsonGeometryCollection toGeoJsonGeometryCollection(GeoCollection collection) {
        // serialize to GeoJson
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SERIALIZER.serialize(baos, collection);

        // deserialize into GeoJsonObject
        final TypeReference<GeoJsonGeometryCollection> typeReference = new TypeReference<GeoJsonGeometryCollection>() {
        };
        return SERIALIZER.deserializeFromBytes(baos.toByteArray(), typeReference);
    }

    /**
     * Converts a {@link GeoPolygonCollection} into a private {@link GeoJsonMultiPolygon}.
     *
     * @param collection
     * @return
     */
    public static GeoJsonMultiPolygon toGeoJsonMultiPolygon(GeoPolygonCollection collection) {
        // serialize to GeoJson
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SERIALIZER.serialize(baos, collection);

        // deserialize into GeoJsonObject
        final TypeReference<GeoJsonMultiPolygon> typeReference = new TypeReference<GeoJsonMultiPolygon>() {
        };
        return SERIALIZER.deserializeFromBytes(baos.toByteArray(), typeReference);
    }

    /**
     * converts the internal representation of {@link RouteDirectionsBatchResult} into the public one
     * from inside the HTTP response.
     *
     * @param response
     * @return
     */
    public static SimpleResponse<RouteDirectionsBatchResult> createRouteDirectionsResponse(
        Response<RouteDirectionsBatchResultPrivate> response) {
        RouteDirectionsBatchResult result = response.getValue() != null ? Utility.toRouteDirectionsBatchResult(
            response.getValue()) : null;
        SimpleResponse<RouteDirectionsBatchResult> simpleResponse = new SimpleResponse<>(response.getRequest(),
            response.getStatusCode(), response.getHeaders(), result);

        return simpleResponse;
    }

    /**
     * Converts a private {@link RouteDirectionsBatchResultPrivate} to a public @{link RouteDirectionsBatchResult}
     *
     * @param privateResult
     * @return a public instance of {@link RouteDirectionsBatchResult}
     */
    private static RouteDirectionsBatchResult toRouteDirectionsBatchResult(
        RouteDirectionsBatchResultPrivate privateResult) {
        RouteDirectionsBatchResult routeDirections = new RouteDirectionsBatchResult();
        RouteDirectionsBatchResultPropertiesHelper.setFromRouteDirectionsBatchResultPrivate(routeDirections,
            privateResult);
        return routeDirections;
    }

    /**
     * Converts a high level RouteDirectionsOptions object into a suitable query string for
     * a batch request.
     *
     * @param options
     * @return
     */
    public static BatchRequestItem toRouteDirectionsBatchItem(RouteDirectionsOptions options) {
        UrlBuilder urlBuilder = new UrlBuilder();

        // single value parameters
        processQueryParameter(urlBuilder, "query", Utility.toRouteQueryString(options.getRoutePoints()));
        processQueryParameter(urlBuilder, "maxAlternatives", options.getMaxAlternatives());
        processQueryParameter(urlBuilder, "alternativeType", options.getAlternativeType());
        processQueryParameter(urlBuilder, "minDeviationDistance", options.getMinDeviationDistance());
        processQueryParameter(urlBuilder, "arriveAt", options.getArriveAt());
        processQueryParameter(urlBuilder, "departAt", options.getDepartAt());
        processQueryParameter(urlBuilder, "minDeviationTime", options.getMinDeviationTime());
        processQueryParameter(urlBuilder, "instructionsType", options.getInstructionsType());
        processQueryParameter(urlBuilder, "language", options.getLanguage());
        processQueryParameter(urlBuilder, "computeBestOrder", options.getComputeBestWaypointOrder());
        processQueryParameter(urlBuilder, "routeRepresentationForBestOrder", options.getRouteRepresentationForBestOrder());
        processQueryParameter(urlBuilder, "computeTravelTimeFor", options.getComputeTravelTime());
        processQueryParameter(urlBuilder, "vehicleHeading", options.getVehicleHeading());
        processQueryParameter(urlBuilder, "report", options.getReport());
        processQueryParameter(urlBuilder, "sectionType", options.getFilterSectionType());
        processQueryParameter(urlBuilder, "vehicleAxleWeight", options.getVehicleAxleWeight());
        processQueryParameter(urlBuilder, "vehicleWidth", options.getVehicleWidth());
        processQueryParameter(urlBuilder, "vehicleHeight", options.getVehicleHeight());
        processQueryParameter(urlBuilder, "vehicleLength", options.getVehicleLength());
        processQueryParameter(urlBuilder, "vehicleMaxSpeed", options.getVehicleMaxSpeed());
        processQueryParameter(urlBuilder, "vehicleWeight", options.getVehicleWeight());
        processQueryParameter(urlBuilder, "vehicleCommercial", options.isCommercialVehicle());
        processQueryParameter(urlBuilder, "windingness", options.getWindingness());
        processQueryParameter(urlBuilder, "hilliness", options.getInclineLevel());
        processQueryParameter(urlBuilder, "travelMode", options.getTravelMode());
        processQueryParameter(urlBuilder, "avoid", options.getAvoidRouteTypes());
        processQueryParameter(urlBuilder, "traffic", options.isGetUseTrafficData());
        processQueryParameter(urlBuilder, "routeType", options.getRouteType());
        processQueryParameter(urlBuilder, "vehicleLoadType", options.getVehicleLoadType());
        processQueryParameter(urlBuilder, "vehicleEngineType", options.getVehicleEngineType());
        processQueryParameter(urlBuilder, "constantSpeedConsumptionInLitersPerHundredkm",
            options.getConstantSpeedConsumptionInLitersPerHundredKm());
        processQueryParameter(urlBuilder, "currentFuelInLiters", options.getCurrentFuelInLiters());
        processQueryParameter(urlBuilder, "auxiliaryPowerInLitersPerHour", options.getAuxiliaryPowerInLitersPerHour());
        processQueryParameter(urlBuilder, "fuelEnergyDensityInMJoulesPerLiter",
            options.getFuelEnergyDensityInMegajoulesPerLiter());
        processQueryParameter(urlBuilder, "accelerationEfficiency", options.getAccelerationEfficiency());
        processQueryParameter(urlBuilder, "decelerationEfficiency", options.getDecelerationEfficiency());
        processQueryParameter(urlBuilder, "uphillEfficiency", options.getUphillEfficiency());
        processQueryParameter(urlBuilder, "downhillEfficiency", options.getDownhillEfficiency());
        processQueryParameter(urlBuilder, "constantSpeedConsumptionInkWhPerHundredkm",
            options.getConstantSpeedConsumptionInKwHPerHundredKm());
        processQueryParameter(urlBuilder, "currentChargeInkWh", options.getCurrentChargeInKwH());
        processQueryParameter(urlBuilder, "maxChargeInkWh", options.getMaxChargeInKwH());
        processQueryParameter(urlBuilder, "auxiliaryPowerInkW", options.getAuxiliaryPowerInKw());

        // convert to batchrequestitem
        return new BatchRequestItem().setQuery(urlBuilder.getQueryString());
    }

    /**
     * Adds the given query parameter key-value pair to the UrlBuilder.
     *
     * @param urlBuilder the UrlBuilder to add the query parameter to
     * @param name the name of the query parameter
     * @param value the value of the query parameter
     */
    private static void processQueryParameter(UrlBuilder urlBuilder, String name, Object value) {
        if (value == null) {
            return;
        }

        try {
            urlBuilder.addQueryParameter(name, URLEncoder.encode(String.valueOf(value), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
