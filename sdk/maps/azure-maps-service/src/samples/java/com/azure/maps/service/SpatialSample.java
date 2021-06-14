package com.azure.maps.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.BufferRequestBody;
import com.azure.maps.service.models.GeoJsonFeatureCollection;
import com.azure.maps.service.models.GeofenceMode;
import com.azure.maps.service.models.ResponseFormat;
import com.fasterxml.jackson.core.JsonProcessingException;

public class SpatialSample {
    public static void main(String[] args) throws JsonProcessingException {
        if (args.length != 2) {
            System.out.println("Usage SpatialSample.java <udid> <device_id>");
            return;
        }
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();
        String udid = args[0];
        String deviceId = args[1];

        System.out.println("Get Buffer");
        MapsCommon.print(client.getSpatials().getBuffer(ResponseFormat.JSON, udid, "176.3"));

        System.out.println("Get Closest Point");
        MapsCommon.print(client.getSpatials().getClosestPoint(ResponseFormat.JSON, udid, 47.622942f, -122.316456f));

        System.out.println("Get Geofence");
        MapsCommon.print(client.getSpatials().getGeofence(ResponseFormat.JSON, deviceId, udid, 48.36f, -124.63f, null,
                OffsetDateTime.of(2018, 9, 9, 10, 0, 0, 0, ZoneOffset.UTC), 50f, false, GeofenceMode.ENTER_AND_EXIT));

        System.out.println("Get Great Circle Distance");
        MapsCommon.print(client.getSpatials().getGreatCircleDistance(ResponseFormat.JSON,
                "47.622942,-122.316456:47.610378,-122.200676"));

        System.out.println("Get Point In Polygon");
        MapsCommon.print(client.getSpatials().getPointInPolygon(ResponseFormat.JSON, udid, 47.622942f, -122.316456f));

        System.out.println("Post Buffer");
        BufferRequestBody bufferRequestBody = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/spatial_buffer_request_body.json")),
                BufferRequestBody.class);
        MapsCommon.print(client.getSpatials().postBuffer(ResponseFormat.JSON, bufferRequestBody));

        System.out.println("Post Closest Point");
        GeoJsonFeatureCollection geoJsonFeatureCollection = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/spatial_closest_point_request_body.json")),
                GeoJsonFeatureCollection.class);
        MapsCommon.print(client.getSpatials().postClosestPoint(ResponseFormat.JSON, 47.622942f, -122.316456f,
                geoJsonFeatureCollection));

        System.out.println("Post Geofence");
        geoJsonFeatureCollection = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/spatial_geofence_request_body.json")),
                GeoJsonFeatureCollection.class);
        MapsCommon.print(client.getSpatials().postGeofence(ResponseFormat.JSON, deviceId, 48.36f, -124.63f,
                geoJsonFeatureCollection, null, OffsetDateTime.of(2018, 9, 9, 10, 0, 0, 0, ZoneOffset.UTC), null, false,
                GeofenceMode.ENTER_AND_EXIT));

        System.out.println("Post Point In Polygon");
        GeoJsonFeatureCollection pointInPolygonResponse = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/spatial_point_in_polygon_request_body.json")),
                GeoJsonFeatureCollection.class);
        MapsCommon.print(
                client.getSpatials().postPointInPolygon(ResponseFormat.JSON, 48.36f, -124.63f, pointInPolygonResponse));
    }
}
