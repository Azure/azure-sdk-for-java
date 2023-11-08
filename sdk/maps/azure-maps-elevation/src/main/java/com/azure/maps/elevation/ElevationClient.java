// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.elevation;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoBoundingBox;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.Context;
import com.azure.maps.elevation.models.ElevationResult;
import com.azure.maps.elevation.implementation.models.ErrorResponseException;
import java.util.List;

/** * {@link ElevationClient} instances are created via the {@link ElevationClientBuilder}, as shown below.
 * Creating a sync client using a {@link AzureKeyCredential}:
 * <!-- src_embed com.azure.maps.elevation.sync.builder.key.instantiation -->
 * <pre>
 * &#47;&#47; Authenticates using subscription key
 * AzureKeyCredential keyCredential = new AzureKeyCredential&#40;System.getenv&#40;&quot;SUBSCRIPTION_KEY&quot;&#41;&#41;;
 *
 * &#47;&#47; Creates a client
 * ElevationClient client = new ElevationClientBuilder&#40;&#41; 
 *     .credential&#40;keyCredential&#41;
 *     .elevationClientId&#40;System.getenv&#40;&quot;MAPS_CLIENT_ID&quot;&#41;&#41;
 *     .buildClient&#40;&#41;;
 * </pre>
 * <!-- end com.azure.maps.elevation.sync.builder.ad.instantiation -->
 */
@ServiceClient(builder = ElevationClientBuilder.class)
public final class ElevationClient {
    private final ElevationAsyncClient asyncClient;

    /**
     * Initializes an instance of ElevationClient client.
     *
     * @param serviceClient the service client implementation.
     */
    ElevationClient(ElevationAsyncClient asyncClient) {
        this.asyncClient = asyncClient;
    }

    /**
     * Get Data For Points
     * <!-- src_embed com.azure.maps.elevation.sync.get_data_for_points -->
     * <pre>
     * client.getDataForPoints&#40;Arrays.asList&#40;
     *     new GeoPosition&#40;-121.66853362143818, 46.84646479863713&#41;,
     *     new GeoPosition&#40;-121.68853362143818, 46.856464798637127&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.sync.get_data_for_points -->
     *
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Points API provides elevation data for one or more points. A point is defined in lat,long
     * coordinate format.
     *
     * <p>Due to the URL character length limit of 2048, it's not possible to pass more than 100 coordinates as a
     * pipeline delimited string in a URL GET request. If you intend to pass more than 100 coordinates as a pipeline
     * delimited string, use POST Data For Points.
     *
     * <p>The result will be in the same sequence of points listed in the request.
     *
     * @param points The string representation of a list of points. A point is defined in lon/lat WGS84 coordinate
     *     reference system format. If multiple points are requested, each of the points in a list should be separated
     *     by the pipe ('|') character. The maximum number of points that can be requested in a single request is 2,000.
     *     The resolution of the elevation data will be the highest for a single point and will decrease if multiple
     *     points are spread further apart.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ElevationResult getDataForPoints(List<GeoPosition> points) {
        return this.asyncClient.getDataForPoints(points).block();
    }

    /**
     * Get Data For Points
     * <!-- src_embed com.azure.maps.elevation.sync.get_data_for_points -->
     * <pre>
     * client.getDataForPoints&#40;Arrays.asList&#40;
     *     new GeoPosition&#40;-121.66853362143818, 46.84646479863713&#41;,
     *     new GeoPosition&#40;-121.68853362143818, 46.856464798637127&#41;&#41;&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.sync.get_data_for_points -->
     *
     * **Applies to**: S1 pricing tier.
     *
     * <p>The Get Data for Points API provides elevation data for one or more points. A point is defined in lat,long
     * coordinate format.
     *
     * <p>Due to the URL character length limit of 2048, it's not possible to pass more than 100 coordinates as a
     * pipeline delimited string in a URL GET request. If you intend to pass more than 100 coordinates as a pipeline
     * delimited string, use POST Data For Points.
     *
     * <p>The result will be in the same sequence of points listed in the request.
     *
     * @param points The string representation of a list of points. A point is defined in lon/lat WGS84 coordinate
     *     reference system format. If multiple points are requested, each of the points in a list should be separated
     *     by the pipe ('|') character. The maximum number of points that can be requested in a single request is 2,000.
     *     The resolution of the elevation data will be the highest for a single point and will decrease if multiple
     *     points are spread further apart.
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ElevationResult> getDataForPointsWithResponse(List<GeoPosition> points, Context context) {
        return this.asyncClient.getDataForPointsWithResponse(points, context).block();
    }

    /**
     * Get Data For Polyline
     * <!-- src_embed com.azure.maps.elevation.sync.get_data_for_polyline -->
     * <pre>
     * client.getDataForPolyline&#40;Arrays.asList&#40;
     *     new GeoPosition&#40;-121.66853362143818, 46.84646479863713&#41;,
     *     new GeoPosition&#40;-121.65853362143818, 46.85646479863713&#41;&#41;, 5&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.sync.get_data_for_polyline -->
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
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ElevationResult getDataForPolyline(List<GeoPosition> lines, Integer samples) {
        return this.asyncClient.getDataForPolyline(lines, samples).block();
    }

    /**
     * Get Data For Polyline
     * <!-- src_embed com.azure.maps.elevation.sync.get_data_for_polyline -->
     * <pre>
     * client.getDataForPolyline&#40;Arrays.asList&#40;
     *     new GeoPosition&#40;-121.66853362143818, 46.84646479863713&#41;,
     *     new GeoPosition&#40;-121.65853362143818, 46.85646479863713&#41;&#41;, 5&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.sync.get_data_for_polyline -->
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
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ElevationResult> getDataForPolylineWithResponse(List<GeoPosition> lines, Integer samples, Context context) {
        return this.asyncClient.getDataForPolylineWithResponse(lines, samples, context).block();
    }

    /**
     * Get Data For Bounding Box
     * <!-- src_embed com.azure.maps.elevation.sync.get_data_for_bounding_box -->
     * <pre>
     * client.getDataForBoundingBox&#40;new GeoBoundingBox&#40;-121.668533621438, 46.8464647986371,
     *     -121.658533621438, 46.8564647986371&#41;, 3, 3&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.sync.get_data_for_bounding_box -->
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
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public ElevationResult getDataForBoundingBox(GeoBoundingBox bounds, Integer rows, Integer columns) {
        return this.asyncClient.getDataForBoundingBox(bounds, rows, columns).block();
    }

    /**
     * Get Data For Bounding Box
     * <!-- src_embed com.azure.maps.elevation.sync.get_data_for_bounding_box -->
     * <pre>
     * client.getDataForBoundingBox&#40;new GeoBoundingBox&#40;-121.668533621438, 46.8464647986371,
     *     -121.658533621438, 46.8564647986371&#41;, 3, 3&#41;;
     * </pre>
     * <!-- end com.azure.maps.elevation.sync.get_data_for_bounding_box -->
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
     * @param context The context to associate with this operation.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws ErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return the response from a successful Get Data for Bounding Box API.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<ElevationResult> getDataForBoundingBoxWithResponse(GeoBoundingBox bounds, Integer rows, Integer columns, Context context) {
        return this.asyncClient.getDataForBoundingBoxWithResponse(bounds, rows, columns, context).block();
    }
}
