// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.maps.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.azure.core.http.rest.StreamResponse;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.CollectionFormat;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.maps.service.models.ComputeTravelTimeFor;
import com.azure.maps.service.models.GeoJsonMultiPoint;
import com.azure.maps.service.models.JsonFormat;
import com.azure.maps.service.models.MultiPoint;
import com.azure.maps.service.models.RouteAvoidType;
import com.azure.maps.service.models.RouteMatrixRequestBody;
import com.azure.maps.service.models.RouteMatrixResponse;
import com.azure.maps.service.models.SectionType;
import com.azure.maps.service.models.TileSize;
import com.azure.maps.service.models.TilesetID;

public class HelloTest {
    @Test
    public void testMessage() {
    	MapsClient client = new MapsClientBuilder()
				.subscriptionKey("ZWLByDSFufxmSdnu5Sh1B8U84FkiltUdlRRMiJ6JIKU")
    			.buildClient();
    	RouteMatrixRequestBody routeMatrixRequestBody = new RouteMatrixRequestBody();
    	GeoJsonMultiPoint points = new GeoJsonMultiPoint();
    	points.setCoordinates(Arrays.asList(Arrays.asList(2.0, 3.0)));
    	GeoJsonMultiPoint points2 = new GeoJsonMultiPoint();
    	points2.setCoordinates(Arrays.asList(Arrays.asList(3.0, 4.0)));
    	routeMatrixRequestBody.setDestinations(points);
    	routeMatrixRequestBody.setOrigins(points);
    	RouteMatrixResponse response = client.getRoutes().postRouteMatrixAsync(JsonFormat.JSON,
    			routeMatrixRequestBody,
    			null,
    			null,
    			null,
    			null,
    			null,
    			null,
    			null,
    			null,
    			null,
    			null,
    			null,
    			null,
    			null,
    			null,
    			Arrays.asList(RouteAvoidType.ALREADY_USED_ROADS,RouteAvoidType.BORDER_CROSSINGS),
    			null,
    			null,
    			null).block();
    	System.out.println(response.getMatrix());
    }
}
