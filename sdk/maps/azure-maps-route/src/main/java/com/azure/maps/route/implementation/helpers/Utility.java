package com.azure.maps.route.implementation.helpers;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.models.GeoCollection;
import com.azure.core.models.GeoPolygonCollection;
import com.azure.core.models.GeoPosition;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerProvider;
import com.azure.core.util.UrlBuilder;
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

public class Utility {
    private static final Pattern uuidPattern = Pattern.compile("[0-9A-Fa-f\\-]{36}");
    private static final JacksonJsonSerializer serializer = new JacksonJsonSerializerProvider()
        .createInstance();

    /**
     * Gets batch Id from headers to be used in the returned batch result object.
     */
    public static String getBatchId(HttpHeaders headers) {
        // this can happen when deserialization is happening
        // to convert the private model to public model (see BatchResponseSerializer)
        if (headers == null) return null;

        // if not, let's go
        final String location = headers.getValue("Location");

        if (location != null) {
            Matcher matcher = uuidPattern.matcher(location);
            matcher.find();
            return matcher.group();
        }

        return null;
    }


    /**
     * converts the internal representation of {@link RouteMatrixResult} into the public one
     * from inside the HTTP response.
     * @param response
     * @return
     */
    public static SimpleResponse<RouteMatrixResult> createRouteMatrixResponse(
            Response<RouteMatrixResultPrivate> response) {
        RouteMatrixResult result = response.getValue() != null ?
            Utility.toRouteMatrixResult(response.getValue()): null;
        SimpleResponse<RouteMatrixResult> simpleResponse = new SimpleResponse<>(response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            result);

        return simpleResponse;
    }

    /**
     * Converts a private {@link RouteMatrixResultPrivate} to a public @{link RouteMatrixResult}
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
     * @param query
     * @return
     */
    public static RouteMatrixQueryPrivate toRouteMatrixQueryPrivate(RouteMatrixQuery query) {
        RouteMatrixQueryPrivate privateQuery = new RouteMatrixQueryPrivate();
        GeoJsonMultiPoint origins = new GeoJsonMultiPoint();
        GeoJsonMultiPoint destinations = new GeoJsonMultiPoint();

        // origins
        List<List<Double>> originCoordinates = query.getOrigins()
            .getPoints()
            .stream()
            .map(point -> {
                GeoPosition position = point.getCoordinates();
                return Arrays.asList(position.getLongitude(), position.getLatitude());
            })
            .collect(Collectors.toList());

        // destinations
        List<List<Double>> destCoordinates = query.getDestinations()
            .getPoints()
            .stream()
            .map(point -> {
                GeoPosition position = point.getCoordinates();
                return Arrays.asList(position.getLongitude(), position.getLatitude());
            })
            .collect(Collectors.toList());

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
        return routePoints
            .stream()
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
     * @param object
     * @return
     */
    public static GeoJsonGeometryCollection toGeoJsonGeometryCollection(GeoCollection collection) {
        // serialize to GeoJson
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(baos, collection);

        // deserialize into GeoJsonObject
        final TypeReference<GeoJsonGeometryCollection> typeReference = new TypeReference<GeoJsonGeometryCollection>(){};
        return serializer.deserializeFromBytes(baos.toByteArray(), typeReference);
    }

    /**
     * Converts a {@link GeoPolygonCollection} into a private {@link GeoJsonMultiPolygon}.
     *
     * @param object
     * @return
     */
    public static GeoJsonMultiPolygon toGeoJsonMultiPolygon(GeoPolygonCollection collection) {
        // serialize to GeoJson
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(baos, collection);

        // deserialize into GeoJsonObject
        final TypeReference<GeoJsonMultiPolygon> typeReference = new TypeReference<GeoJsonMultiPolygon>(){};
        return serializer.deserializeFromBytes(baos.toByteArray(), typeReference);
    }

    /**
     * converts the internal representation of {@link RouteDirectionsBatchResult} into the public one
     * from inside the HTTP response.
     * @param response
     * @return
     */
    public static SimpleResponse<RouteDirectionsBatchResult> createRouteDirectionsResponse(
            Response<RouteDirectionsBatchResultPrivate> response) {
        RouteDirectionsBatchResult result =
            response.getValue() != null ? Utility.toRouteDirectionsBatchResult(response.getValue()) : null;
        SimpleResponse<RouteDirectionsBatchResult> simpleResponse = new SimpleResponse<>(response.getRequest(),
            response.getStatusCode(),
            response.getHeaders(),
            result);

        return simpleResponse;
    }

    /**
     * Converts a private {@link RouteDirectionsBatchResultPrivate} to a public @{link RouteDirectionsBatchResult}
     * @param privateResult
     * @return a public instance of {@link RouteDirectionsBatchResult}
     */
    private static RouteDirectionsBatchResult toRouteDirectionsBatchResult(RouteDirectionsBatchResultPrivate privateResult) {
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
        Map<String, Object> params = new HashMap<>();

        // single value parameters
        params.compute("query", (k, v) -> Utility.toRouteQueryString(options.getRoutePoints()));
        params.compute("maxAlternatives", (k, v) -> options.getMaxAlternatives());
        params.compute("alternativeType", (k, v) -> options.getAlternativeType());
        params.compute("minDeviationDistance", (k, v) -> options.getMinDeviationDistance());
        params.compute("arriveAt", (k, v) -> options.getArriveAt());
        params.compute("departAt", (k, v) -> options.getDepartAt());
        params.compute("minDeviationTime", (k, v) -> options.getMinDeviationTime());
        params.compute("instructionsType", (k, v) -> options.getInstructionsType());
        params.compute("language", (k, v) -> options.getLanguage());
        params.compute("computeBestOrder", (k, v) -> options.getComputeBestWaypointOrder());
        params.compute("routeRepresentationForBestOrder", (k, v) -> options.getRouteRepresentationForBestOrder());
        params.compute("computeTravelTimeFor", (k, v) -> options.getComputeTravelTime());
        params.compute("vehicleHeading", (k, v) -> options.getVehicleHeading());
        params.compute("report", (k, v) -> options.getReport());
        params.compute("sectionType", (k, v) -> options.getFilterSectionType());
        params.compute("vehicleAxleWeight", (k, v) -> options.getVehicleAxleWeight());
        params.compute("vehicleWidth", (k, v) -> options.getVehicleWidth());
        params.compute("vehicleHeight", (k, v) -> options.getVehicleHeight());
        params.compute("vehicleLength", (k, v) -> options.getVehicleLength());
        params.compute("vehicleMaxSpeed", (k, v) -> options.getVehicleMaxSpeed());
        params.compute("vehicleWeight", (k, v) -> options.getVehicleWeight());
        params.compute("vehicleCommercial", (k, v) -> options.isCommercialVehicle());
        params.compute("windingness", (k, v) -> options.getWindingness());
        params.compute("hilliness", (k, v) -> options.getInclineLevel());
        params.compute("travelMode", (k, v) -> options.getTravelMode());
        params.compute("avoid", (k, v) -> options.getAvoid());
        params.compute("traffic", (k, v) -> options.getUseTrafficData());
        params.compute("routeType", (k, v) -> options.getRouteType());
        params.compute("vehicleLoadType", (k, v) -> options.getVehicleLoadType());
        params.compute("vehicleEngineType", (k, v) -> options.getVehicleEngineType());
        params.compute("constantSpeedConsumptionInLitersPerHundredkm", (k, v) -> options.getConstantSpeedConsumptionInLitersPerHundredKm());
        params.compute("currentFuelInLiters", (k, v) -> options.getCurrentFuelInLiters());
        params.compute("auxiliaryPowerInLitersPerHour", (k, v) -> options.getAuxiliaryPowerInLitersPerHour());
        params.compute("fuelEnergyDensityInMJoulesPerLiter", (k, v) -> options.getFuelEnergyDensityInMegajoulesPerLiter());
        params.compute("accelerationEfficiency", (k, v) -> options.getAccelerationEfficiency());
        params.compute("decelerationEfficiency", (k, v) -> options.getDecelerationEfficiency());
        params.compute("uphillEfficiency", (k, v) -> options.getUphillEfficiency());
        params.compute("downhillEfficiency", (k, v) -> options.getDownhillEfficiency());
        params.compute("constantSpeedConsumptionInkWhPerHundredkm", (k, v) -> options.getConstantSpeedConsumptionInKwHPerHundredKm());
        params.compute("currentChargeInkWh", (k, v) -> options.getCurrentChargeInKwH());
        params.compute("maxChargeInkWh", (k, v) -> options.getMaxChargeInKwH());
        params.compute("auxiliaryPowerInkW", (k, v) -> options.getAuxiliaryPowerInKw());

        // convert to batchrequestitem
        BatchRequestItem item = convertParametersToRequestItem(params);
        return item;
    }

    /**
     * Converts a map of parameters into a BatchRequestItem.
     *
     * @param params
     * @return
     */
    private static BatchRequestItem convertParametersToRequestItem(Map<String, Object> params) {
        // batch request item conversion
        BatchRequestItem item = new BatchRequestItem();
        UrlBuilder urlBuilder = new UrlBuilder();

        for (String key : params.keySet()) {
            try {
                urlBuilder.addQueryParameter(key, URLEncoder.encode(params.get(key).toString(), "UTF-8"));
            }
            catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        item.setQuery(urlBuilder.getQueryString());
        return item;
    }
}
