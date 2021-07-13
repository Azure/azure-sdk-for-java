package com.azure.maps.service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.maps.service.models.BatchRequestBody;
import com.azure.maps.service.models.ResponseFormat;
import com.azure.maps.service.models.SearchAlongRouteRequestBody;
import com.azure.maps.service.models.SearchCommonResponse;
import com.azure.maps.service.models.SearchInsideGeometryRequestBody;
import com.azure.maps.service.models.TextFormat;

public class SearchSample {

    public static void main(String[] args) throws IOException {
        HttpPipelinePolicy policy = new AzureKeyInQueryPolicy("subscription-key",
                new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY")));
        MapsClient client = new MapsClientBuilder().addPolicy(policy).buildClient();

        System.out.println("Get Search Address:");
        MapsCommon.print(
                client.getSearches().getSearchAddress(TextFormat.JSON, "15127 NE 24th Street, Redmond, WA 98052"));

        System.out.println("Get Search Address Reverse:");
        MapsCommon.print(client.getSearches().getSearchAddressReverse(TextFormat.JSON, "37.337,-121.89"));

        System.out.println("Get Search Address Reverse Cross Street:");
        MapsCommon.print(client.getSearches().getSearchAddressReverseCrossStreet(TextFormat.JSON, "37.337,-121.89"));

        System.out.println("Get Search Address Structured:");
        MapsCommon.print(client.getSearches().getSearchAddressStructured(TextFormat.JSON, null, "US", null, null,
                "15127", "NE 24th Street", null, "Redmond", null, null, null, "WA", "98052", null, null));

        System.out.println("Get Search Fuzzy:");
        SearchCommonResponse results = client.getSearches().getSearchFuzzy(TextFormat.JSON, "seattle");
        MapsCommon.print(results);
        List<String> ids = results.getResults().stream().map(item -> item.getDataSources().getGeometry().getId())
                .collect(Collectors.toList());
        System.out.println("Get Search Polygon:");
        MapsCommon.print(client.getSearches().getSearchPolygon(ResponseFormat.JSON, ids));

        System.out.println("Get Search Nearby:");
        MapsCommon.print(client.getSearches().getSearchNearby(TextFormat.JSON, 40.706270f, -74.011454f, 10, null, null,
                null, 8046f, null, null, ids, null, null));

        System.out.println("Get Search POI:");
        MapsCommon.print(client.getSearches().getSearchPOI(TextFormat.JSON, "juice bars", null, 5, null, null, null,
                47.606038f, -122.333345f, 8046f, null, null, null, null, null, null, null, null));

        System.out.println("Get Search POI Category:");
        MapsCommon.print(client.getSearches().getSearchPOICategory(TextFormat.JSON, "atm", null, 5, null, null, null,
                47.606038f, -122.333345f, 8046f, null, null, null, null, null, null, null, null));

        System.out.println("Get Search POI Category Tree:");
        MapsCommon.print(client.getSearches().getSearchPOICategoryTreePreview(ResponseFormat.JSON));

        System.out.println("Post Search Address Batch Sync");
        BatchRequestBody contentJson = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/search_address_batch_request_body.json")),
                BatchRequestBody.class);
        MapsCommon.print(client.getSearches().postSearchAddressBatchSync(ResponseFormat.JSON, contentJson));

        System.out.println("Post Search Address Reverse Batch Sync");
        contentJson = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/search_address_reverse_batch_request_body.json")),
                BatchRequestBody.class);
        MapsCommon.print(client.getSearches().postSearchAddressReverseBatchSync(ResponseFormat.JSON, contentJson));

        System.out.println("Post Search Fuzzy Batch Sync");
        contentJson = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/search_fuzzy_batch_request_body.json")),
                BatchRequestBody.class);
        MapsCommon.print(client.getSearches().postSearchFuzzyBatchSync(ResponseFormat.JSON, contentJson));

        System.out.println("Post Search Along Route");
        SearchAlongRouteRequestBody searchAlongRouteRequestBody = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/search_along_route_request_body.json")),
                SearchAlongRouteRequestBody.class);
        MapsCommon.print(client.getSearches().postSearchAlongRoute(TextFormat.JSON, "burger", 1000,
                searchAlongRouteRequestBody, null, 2, null, null, null, null));

        System.out.println("Post Search Inside Geometry");
        SearchInsideGeometryRequestBody searchInsideGeometryRequestBody = MapsCommon.readJson(
                MapsCommon.readContent(MapsCommon.getResource("/search_inside_geometry_request_body.json")),
                SearchInsideGeometryRequestBody.class);
        MapsCommon.print(client.getSearches().postSearchInsideGeometry(TextFormat.JSON, "burger",
                searchInsideGeometryRequestBody, 2, null, null, null, null, null, null));

    }
}
