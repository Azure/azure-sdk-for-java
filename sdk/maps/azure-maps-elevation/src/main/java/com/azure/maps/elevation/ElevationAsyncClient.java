// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation;

import java.util.List;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.maps.elevation.implementation.ElevationsImpl;
import com.azure.maps.elevation.implementation.helpers.Utility;
import com.azure.maps.elevation.models.ElevationResult;
import com.azure.maps.elevation.implementation.models.JsonFormat;
import com.azure.maps.elevation.implementation.models.ErrorResponseException;

import reactor.core.publisher.Mono;

/** Initializes a new instance of the asynchronous ElevationClient type. 
* Creating an async client using a {@link com.azure.core.credential.AzureKeyCredential}:
* <!-- src_embed com.azure.maps.elevation.async.builder.key.instantiation -->
* <pre>
* &#47;&#47; Authenticates using subscription key
* AzureKeyCredential keyCredential = new AzureKeyCredential&#40;System.getenv&#40;&quot;SUBSCRIPTION_KEY&quot;&#41;&#41;;
*
* &#47;&#47; Creates a client
* ElevationAsyncClient asyncClient = new ElevationClientBuilder&#40;&#41;
*     .credential&#40;keyCredential&#41;
*     .elevationClientId&#40;System.getenv&#40;&quot;MAPS_CLIENT_ID&quot;&#41;&#41;
*     .buildAsyncClient&#40;&#41;;
* </pre>
* <!-- end com.azure.maps.elevation.async.builder.key.instantiation -->
*/
@ServiceClient(builder = ElevationClientBuilder.class, isAsync = true)
public final class ElevationAsyncClient {
    private final ElevationsImpl serviceClient;
    private static final int ELEVATION_DATA_SMALL_SIZE = 100;

    /**
     * 
     * Initializes an instance of ElevationClient client.
     *
     * @param serviceClient the service client implementation.
     */
    ElevationAsyncClient(ElevationsImpl serviceClient) {
        this.serviceClient = serviceClient;
    }

    /**
     * Get Data For Points
     * <!-- src_embed com.azure.maps.elevation.async.get_data_for_points -->
     * <pre>
     * asyncClient.getDataForPoints&#40;Arrays.asList&#40;
     *     new GeoPosition&#40;-121.66853362143818, 46.84646479863713&#41;,
     *     new GeoPosition&#40;-121.68853362143818, 46.856464798637127&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.async.get_data_for_points -->
     * 
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Points API provides elevation data for one or more points. A point is defined in lat,long
     * coordinate format.
     *
     * <p>Due to the URL character length limit of 2048, it's not possible to pass more than 100 coordinates as a
     * pipeline delimited string in a URL GET request. If you intend to pass more than 100 coordinates as a pipeline
     * delimited string, use the [POST Data For
     * Points](https://docs.microsoft.com/rest/api/maps/elevation/postdataforpoints).
     *
     * <p>The result will be in the same sequence of points listed in the request.
     *
     * @param points The string representation of a list of points. A point is defined in lon/lat WGS84 coordinate
     *     reference system format. If multiple points are requested, each of the points in a list should be separated
     *     by the pipe ('|') character. The maximum number of points that can be requested in a single request is 2,000.
     *     The resolution of the elevation data will be the highest for a single point and will decrease if multiple
     *     points are spread further apart.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ElevationResult> getDataForPoints(List<GeoPosition> points) {
        Mono<Response<ElevationResult>> result = this.getDataForPointsWithResponse(points);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Get Data For Points
     * <!-- src_embed com.azure.maps.elevation.async.get_data_for_points -->
     * <pre>
     * asyncClient.getDataForPoints&#40;Arrays.asList&#40;
     *     new GeoPosition&#40;-121.66853362143818, 46.84646479863713&#41;,
     *     new GeoPosition&#40;-121.68853362143818, 46.856464798637127&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.async.get_data_for_points -->
     * 
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Points API provides elevation data for one or more points. A point is defined in lat,long
     * coordinate format.
     *
     * <p>Due to the URL character length limit of 2048, it's not possible to pass more than 100 coordinates as a
     * pipeline delimited string in a URL GET request. If you intend to pass more than 100 coordinates as a pipeline
     * delimited string, use the [POST Data For
     * Points](https://docs.microsoft.com/rest/api/maps/elevation/postdataforpoints).
     *
     * <p>The result will be in the same sequence of points listed in the request.
     *
     * @param points The string representation of a list of points. A point is defined in lon/lat WGS84 coordinate
     *     reference system format. If multiple points are requested, each of the points in a list should be separated
     *     by the pipe ('|') character. The maximum number of points that can be requested in a single request is 2,000.
     *     The resolution of the elevation data will be the highest for a single point and will decrease if multiple
     *     points are spread further apart.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ElevationResult>> getDataForPointsWithResponse(List<GeoPosition> points) {
        return this.getDataForPointsWithResponse(points, null);
    }

    /**
     * Get Data For Points
     * <!-- src_embed com.azure.maps.elevation.async.get_data_for_points -->
     * <pre>
     * asyncClient.getDataForPoints&#40;Arrays.asList&#40;
     *     new GeoPosition&#40;-121.66853362143818, 46.84646479863713&#41;,
     *     new GeoPosition&#40;-121.68853362143818, 46.856464798637127&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.async.get_data_for_points -->
     * 
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Points API provides elevation data for one or more points. A point is defined in lat,long
     * coordinate format.
     *
     * <p>Due to the URL character length limit of 2048, it's not possible to pass more than 100 coordinates as a
     * pipeline delimited string in a URL GET request. If you intend to pass more than 100 coordinates as a pipeline
     * delimited string, use the [POST Data For
     * Points](https://docs.microsoft.com/rest/api/maps/elevation/postdataforpoints).
     *
     * <p>The result will be in the same sequence of points listed in the request.
     *
     * @param format Desired format of the response. Only `json` format is supported.
     * @param points The string representation of a list of points. A point is defined in lon/lat WGS84 coordinate
     *     reference system format. If multiple points are requested, each of the points in a list should be separated
     *     by the pipe ('|') character. The maximum number of points that can be requested in a single request is 2,000.
     *     The resolution of the elevation data will be the highest for a single point and will decrease if multiple
     *     points are spread further apart.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    Mono<Response<ElevationResult>> getDataForPointsWithResponse(List<GeoPosition> points, Context context) {
        if (points.size() < ELEVATION_DATA_SMALL_SIZE) {
            return this.serviceClient.getDataForPointsWithResponseAsync(JsonFormat.JSON, Utility.geoPositionToString(points), context);
        } 
        return this.serviceClient.postDataForPointsWithResponseAsync(JsonFormat.JSON, Utility.toLatLongPairAbbreviated(points), context).onErrorMap(throwable -> {
            if (!(throwable instanceof ErrorResponseException)) {
                return throwable;
            }
            ErrorResponseException exception = (ErrorResponseException) throwable;
            return new HttpResponseException(exception.getMessage(), exception.getResponse());
        });
    }

    /**
     * Get Data For Polyline
     * <!-- src_embed com.azure.maps.elevation.async.get_data_for_polyline -->
     * <pre>
     * asyncClient.getDataForPolyline&#40;Arrays.asList&#40;
     *     new GeoPosition&#40;-121.66853362143818, 46.84646479863713&#41;,
     *     new GeoPosition&#40;-121.65853362143818, 46.85646479863713&#41;&#41;, 5&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.async.get_data_for_polyline -->
     * 
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Polyline API provides elevation data along a polyline.
     *
     * <p>A polyline is defined by passing in between 2 and N endpoint coordinates separated by a pipe ('|') character.
     * In addition to passing in endpoints, customers can specify the number of sample points that will be used to
     * divide polyline into equally spaced segments.
     *
     * <p>Elevation data at both start and endpoints, as well as equally spaced points along the polyline will be
     * returned. The results will be listed in the direction from the first endpoint towards the last endpoint. A line
     * between two endpoints is a straight Cartesian line, the shortest line between those two points in the coordinate
     * reference system. Note that the point is chosen based on Euclidean distance and may markedly differ from the
     * geodesic path along the curved surface of the reference ellipsoid.
     *
     * @param lines The string representation of a polyline path. A polyline is defined by endpoint coordinates, with
     *     each endpoint separated by a pipe ('|') character. The polyline should be defined in the following format:
     *     `[longitude_point1, latitude_point1 | longitude_point2, latitude_point2, ..., longitude_pointN,
     *     latitude_pointN]`.
     *     <p>The longitude and latitude values refer to the World Geodetic System (WGS84) coordinate reference system.
     *     The resolution of the data used to compute the elevation depends on the distance between the endpoints.
     * @param samples The samples parameter specifies the number of equally spaced points at which elevation values
     *     should be provided along a polyline path. The number of samples should range from 2 to 2,000. Default value
     *     is 10.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ElevationResult> getDataForPolyline(List<GeoPosition> lines, Integer samples) {
        Mono<Response<ElevationResult>> result = this.getDataForPolylineWithResponse(lines, samples);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Get Data For Polyline
     * <!-- src_embed com.azure.maps.elevation.async.get_data_for_polyline -->
     * <pre>
     * asyncClient.getDataForPolyline&#40;Arrays.asList&#40;
     *     new GeoPosition&#40;-121.66853362143818, 46.84646479863713&#41;,
     *     new GeoPosition&#40;-121.65853362143818, 46.85646479863713&#41;&#41;, 5&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.async.get_data_for_polyline -->
     * 
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Polyline API provides elevation data along a polyline.
     *
     * <p>A polyline is defined by passing in between 2 and N endpoint coordinates separated by a pipe ('|') character.
     * In addition to passing in endpoints, customers can specify the number of sample points that will be used to
     * divide polyline into equally spaced segments.
     *
     * <p>Elevation data at both start and endpoints, as well as equally spaced points along the polyline will be
     * returned. The results will be listed in the direction from the first endpoint towards the last endpoint. A line
     * between two endpoints is a straight Cartesian line, the shortest line between those two points in the coordinate
     * reference system. Note that the point is chosen based on Euclidean distance and may markedly differ from the
     * geodesic path along the curved surface of the reference ellipsoid.
     *
     * @param lines The string representation of a polyline path. A polyline is defined by endpoint coordinates, with
     *     each endpoint separated by a pipe ('|') character. The polyline should be defined in the following format:
     *     `[longitude_point1, latitude_point1 | longitude_point2, latitude_point2, ..., longitude_pointN,
     *     latitude_pointN]`.
     *     <p>The longitude and latitude values refer to the World Geodetic System (WGS84) coordinate reference system.
     *     The resolution of the data used to compute the elevation depends on the distance between the endpoints.
     * @param samples The samples parameter specifies the number of equally spaced points at which elevation values
     *     should be provided along a polyline path. The number of samples should range from 2 to 2,000. Default value
     *     is 10.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ElevationResult>> getDataForPolylineWithResponse(List<GeoPosition> lines, Integer samples) {
        return this.getDataForPolylineWithResponse(lines, samples, null);
    }

    /**
     * Get Data For Polyline
     * <!-- src_embed com.azure.maps.elevation.async.get_data_for_polyline -->
     * <pre>
     * asyncClient.getDataForPolyline&#40;Arrays.asList&#40;
     *     new GeoPosition&#40;-121.66853362143818, 46.84646479863713&#41;,
     *     new GeoPosition&#40;-121.65853362143818, 46.85646479863713&#41;&#41;, 5&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.async.get_data_for_polyline -->
     * 
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Polyline API provides elevation data along a polyline.
     *
     * <p>A polyline is defined by passing in between 2 and N endpoint coordinates separated by a pipe ('|') character.
     * In addition to passing in endpoints, customers can specify the number of sample points that will be used to
     * divide polyline into equally spaced segments.
     *
     * <p>Elevation data at both start and endpoints, as well as equally spaced points along the polyline will be
     * returned. The results will be listed in the direction from the first endpoint towards the last endpoint. A line
     * between two endpoints is a straight Cartesian line, the shortest line between those two points in the coordinate
     * reference system. Note that the point is chosen based on Euclidean distance and may markedly differ from the
     * geodesic path along the curved surface of the reference ellipsoid.
     *
     * @param format Desired format of the response. Only `json` format is supported.
     * @param lines The string representation of a polyline path. A polyline is defined by endpoint coordinates, with
     *     each endpoint separated by a pipe ('|') character. The polyline should be defined in the following format:
     *     `[longitude_point1, latitude_point1 | longitude_point2, latitude_point2, ..., longitude_pointN,
     *     latitude_pointN]`.
     *     <p>The longitude and latitude values refer to the World Geodetic System (WGS84) coordinate reference system.
     *     The resolution of the data used to compute the elevation depends on the distance between the endpoints.
     * @param samples The samples parameter specifies the number of equally spaced points at which elevation values
     *     should be provided along a polyline path. The number of samples should range from 2 to 2,000. Default value
     *     is 10.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    Mono<Response<ElevationResult>> getDataForPolylineWithResponse(List<GeoPosition> lines, Integer samples, Context context) {
        if (lines.size() < ELEVATION_DATA_SMALL_SIZE) {
            return this.serviceClient.getDataForPolylineWithResponseAsync(JsonFormat.JSON, Utility.geoPositionToString(lines), samples, context);
        } 
        return this.serviceClient.postDataForPolylineWithResponseAsync(JsonFormat.JSON, Utility.toLatLongPairAbbreviated(lines), samples, context).onErrorMap(throwable -> {
            if (!(throwable instanceof ErrorResponseException)) {
                return throwable;
            }
            ErrorResponseException exception = (ErrorResponseException) throwable;
            return new HttpResponseException(exception.getMessage(), exception.getResponse());
        });
    }

    /**
     * Get Data For Bounding Box
     * <!-- src_embed com.azure.maps.elevation.async.get_data_for_bounding_box -->
     * <pre>
     * asyncClient.getDataForBoundingBox&#40;new GeoBoundingBox&#40;-121.668533621438f, 46.8464647986371f,
     *     -121.658533621438f, 46.8564647986371f&#41;, 3, 3&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.async.get_data_for_bounding_box -->
     * 
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Bounding Box API provides elevation data at equally spaced locations within a bounding box. A
     * bounding box is defined by the coordinates for two corners (southwest, northeast) and then subsequently divided
     * into rows and columns.
     *
     * <p>Elevations are returned for the vertices of the grid created by the rows and columns. Up to 2,000 elevations
     * can be returned in a single request. The returned elevation values are ordered, starting at the southwest corner,
     * and then proceeding west to east along the row. At the end of the row, it moves north to the next row, and
     * repeats the process until it reaches the far northeast corner.
     *
     * @param bounds The string that represents the rectangular area of a bounding box. The bounds parameter is defined
     *     by the 4 bounding box coordinates, with WGS84 longitude and latitude of the southwest corner followed by
     *     WGS84 longitude and latitude of the northeast corner. The string is presented in the following format:
     *     `[SouthwestCorner_Longitude, SouthwestCorner_Latitude, NortheastCorner_Longitude, NortheastCorner_Latitude]`.
     * @param rows Specifies the number of rows to use to divide the bounding box area into a grid. The number of
     *     vertices (rows x columns) in the grid should be less than 2,000.
     * @param columns Specifies the number of columns to use to divide the bounding box area into a grid. The number of
     *     vertices (rows x columns) in the grid should be less than 2,000.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ElevationResult> getDataForBoundingBox(GeoBoundingBox bounds, Integer rows, Integer columns) {
        Mono<Response<ElevationResult>> result = this.getDataForBoundingBoxWithResponse(bounds, rows, columns);
        return result.flatMap(response -> {
            return Mono.just(response.getValue());
        });
    }

    /**
     * Get Data For Bounding Box
     * <!-- src_embed com.azure.maps.elevation.async.get_data_for_bounding_box -->
     * <pre>
     * asyncClient.getDataForBoundingBox&#40;new GeoBoundingBox&#40;-121.668533621438f, 46.8464647986371f,
     *     -121.658533621438f, 46.8564647986371f&#41;, 3, 3&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.async.get_data_for_bounding_box -->
     * 
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Bounding Box API provides elevation data at equally spaced locations within a bounding box. A
     * bounding box is defined by the coordinates for two corners (southwest, northeast) and then subsequently divided
     * into rows and columns.
     *
     * <p>Elevations are returned for the vertices of the grid created by the rows and columns. Up to 2,000 elevations
     * can be returned in a single request. The returned elevation values are ordered, starting at the southwest corner,
     * and then proceeding west to east along the row. At the end of the row, it moves north to the next row, and
     * repeats the process until it reaches the far northeast corner.
     *
     * @param bounds The string that represents the rectangular area of a bounding box. The bounds parameter is defined
     *     by the 4 bounding box coordinates, with WGS84 longitude and latitude of the southwest corner followed by
     *     WGS84 longitude and latitude of the northeast corner. The string is presented in the following format:
     *     `[SouthwestCorner_Longitude, SouthwestCorner_Latitude, NortheastCorner_Longitude, NortheastCorner_Latitude]`.
     * @param rows Specifies the number of rows to use to divide the bounding box area into a grid. The number of
     *     vertices (rows x columns) in the grid should be less than 2,000.
     * @param columns Specifies the number of columns to use to divide the bounding box area into a grid. The number of
     *     vertices (rows x columns) in the grid should be less than 2,000.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ElevationResult>> getDataForBoundingBoxWithResponse(GeoBoundingBox bounds, Integer rows, Integer columns) {
        return this.getDataForBoundingBoxWithResponse(bounds, rows, columns, null);
    }

    /**
     * Get Data For Bounding Box
     * <!-- src_embed com.azure.maps.elevation.async.get_data_for_bounding_box -->
     * <pre>
     * asyncClient.getDataForBoundingBox&#40;new GeoBoundingBox&#40;-121.668533621438f, 46.8464647986371f,
     *     -121.658533621438f, 46.8564647986371f&#41;, 3, 3&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.async.get_data_for_bounding_box -->
     * 
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Bounding Box API provides elevation data at equally spaced locations within a bounding box. A
     * bounding box is defined by the coordinates for two corners (southwest, northeast) and then subsequently divided
     * into rows and columns.
     *
     * <p>Elevations are returned for the vertices of the grid created by the rows and columns. Up to 2,000 elevations
     * can be returned in a single request. The returned elevation values are ordered, starting at the southwest corner,
     * and then proceeding west to east along the row. At the end of the row, it moves north to the next row, and
     * repeats the process until it reaches the far northeast corner.
     *
     * @param format Desired format of the response. Only `json` format is supported.
     * @param bounds The string that represents the rectangular area of a bounding box. The bounds parameter is defined
     *     by the 4 bounding box coordinates, with WGS84 longitude and latitude of the southwest corner followed by
     *     WGS84 longitude and latitude of the northeast corner. The string is presented in the following format:
     *     `[SouthwestCorner_Longitude, SouthwestCorner_Latitude, NortheastCorner_Longitude, NortheastCorner_Latitude]`.
     * @param rows Specifies the number of rows to use to divide the bounding box area into a grid. The number of
     *     vertices (rows x columns) in the grid should be less than 2,000.
     * @param columns Specifies the number of columns to use to divide the bounding box area into a grid. The number of
     *     vertices (rows x columns) in the grid should be less than 2,000.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    Mono<Response<ElevationResult>> getDataForBoundingBoxWithResponse(GeoBoundingBox bounds, Integer rows, Integer columns, Context context) {
        return this.serviceClient.getDataForBoundingBoxWithResponseAsync(JsonFormat.JSON, Utility.geoBoundingBoxAsList(bounds), rows, columns, context).onErrorMap(throwable -> {
            if (!(throwable instanceof ErrorResponseException)) {
                return throwable;
            }
            ErrorResponseException exception = (ErrorResponseException) throwable;
            return new HttpResponseException(exception.getMessage(), exception.getResponse());
        });
    }
}
