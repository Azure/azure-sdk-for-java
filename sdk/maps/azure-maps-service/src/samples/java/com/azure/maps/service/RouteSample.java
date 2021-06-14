package com.azure.maps.service;

import java.io.IOException;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.BatchRequestBody;
import com.azure.maps.service.models.PostRouteDirectionsRequestBody;
import com.azure.maps.service.models.PostRouteMatrixRequestBody;
import com.azure.maps.service.models.ResponseFormat;
import com.azure.maps.service.models.RouteType;
import com.azure.maps.service.models.TextFormat;

public class RouteSample {

    public static void main(String[] args) throws IOException {
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

        System.out.println("Get route directions");
        MapsCommon.print(client.getRoutes().getRouteDirections(TextFormat.JSON, "52.50931,13.42936:52.50274,13.43872"));

        System.out.println("Get route range");
        MapsCommon.print(client.getRoutes().getRouteRange(TextFormat.JSON, "50.97452,5.86605", null, null, 6000.0f,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null));

        PostRouteDirectionsRequestBody postRouteDirectionsRequestBody = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/route_directions_request_body.json")),
                PostRouteDirectionsRequestBody.class);
        System.out.println("Post route directions");
        MapsCommon.print(client.getRoutes().postRouteDirections(TextFormat.JSON, "52.50931,13.42936:52.50274,13.43872",
                postRouteDirectionsRequestBody));

        BatchRequestBody batchRequestBody = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/route_directions_batch_request_body.json")),
                BatchRequestBody.class);
        System.out.println("Post route directions batch sync");
        MapsCommon.print(client.getRoutes().postRouteDirectionsBatchSync(ResponseFormat.JSON, batchRequestBody));

        PostRouteMatrixRequestBody postRouteMatrixRequestBody = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/route_matrix_request_body.json")),
                PostRouteMatrixRequestBody.class);
        System.out.println("Post route matrix");
        MapsCommon.print(client.getRoutes().postRouteMatrix(ResponseFormat.JSON, postRouteMatrixRequestBody,
                Boolean.TRUE, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                RouteType.SHORTEST, null));
    }
}
