package com.azure.maps.search.samples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.models.GeoLineString;
import com.azure.core.models.GeoLinearRing;
import com.azure.core.models.GeoPolygon;
import com.azure.core.models.GeoPosition;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.maps.search.MapsSearchClient;
import com.azure.maps.search.MapsSearchClientBuilder;
import com.azure.maps.search.models.BatchReverseSearchResult;
import com.azure.maps.search.models.BatchSearchResult;
import com.azure.maps.search.models.FuzzySearchOptions;
import com.azure.maps.search.models.GeographicEntityType;
import com.azure.maps.search.models.OperatingHoursRange;
import com.azure.maps.search.models.ReverseSearchAddressOptions;
import com.azure.maps.search.models.ReverseSearchCrossStreetAddressOptions;
import com.azure.maps.search.models.SearchAddressOptions;
import com.azure.maps.search.models.SearchAddressResult;
import com.azure.maps.search.models.SearchAlongRouteOptions;
import com.azure.maps.search.models.SearchInsideGeometryOptions;
import com.azure.maps.search.models.SearchNearbyPointsOfInterestOptions;
import com.azure.maps.search.models.SearchPointOfInterestCategoryOptions;
import com.azure.maps.search.models.SearchPointOfInterestOptions;
import com.azure.maps.search.models.SearchStructuredAddressOptions;
import com.azure.maps.search.models.StructuredAddress;

public class BuilderSamples {

    public MapsSearchClient createMapsSyncClientUsingAzureKeyCredential() {
        // BEGIN: com.azure.maps.search.sync.builder.key.instantiation
        // Authenticates using subscription key
        AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));

        // Creates a builder
        MapsSearchClientBuilder builder = new MapsSearchClientBuilder();
        builder.credential(keyCredential);
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds the client
        MapsSearchClient client = builder.buildClient();
        // END: com.azure.maps.search.sync.builder.key.instantiation

        return client;
    }

    public MapsSearchClient createMapsSyncClientUsingAzureADCredential() {
        // BEGIN: com.azure.maps.search.sync.builder.ad.instantiation
        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        // Creates a builder
        MapsSearchClientBuilder builder = new MapsSearchClientBuilder();
        builder.credential(tokenCredential);
        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));

        // Builds a client
        MapsSearchClient client = builder.buildClient();
        // END: com.azure.maps.search.sync.builder.ad.instantiation

        return client;
    }

    public static void main(String[] args) throws IOException {
        // BEGIN: com.azure.maps.search.sync.builder.instantiation
        MapsSearchClientBuilder builder = new MapsSearchClientBuilder();

        // Authenticates using subscription key
        // AzureKeyCredential keyCredential = new AzureKeyCredential(System.getenv("SUBSCRIPTION_KEY"));
        // builder.credential(keyCredential);

        // Authenticates using Azure AD building a default credential
        // This will look for AZURE_CLIENT_ID, AZURE_TENANT_ID, and AZURE_CLIENT_SECRET env variables
        DefaultAzureCredential tokenCredential = new DefaultAzureCredentialBuilder().build();

        builder.credential(tokenCredential);
        builder.mapsClientId(System.getenv("MAPS_CLIENT_ID"));
        builder.httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS));
        MapsSearchClient client = builder.buildClient();
        // END: com.azure.maps.search.sync.builder.instantiation

        /* Stand-alone, one-shot operations */
        // Search address -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address
        System.out.println("Search Address:");

        // simple
        MapsCommon.print(client.searchAddress(
            new SearchAddressOptions("15127 NE 24th Street, Redmond, WA 98052")));

        // options
        MapsCommon.print(client.searchAddress(
            new SearchAddressOptions("1 Main Street")
                .setCoordinates(new GeoPosition(-74.011454, 40.706270))
                .setRadiusInMeters(40000)
                .setTop(5)));

        // complete
        MapsCommon.print(client.searchAddressWithResponse(
            new SearchAddressOptions("1 Main Street")
                .setCoordinates(new GeoPosition(-74.011454, 40.706270))
                .setRadiusInMeters(40000)
                .setTop(5), null).getStatusCode());

        // Search address reverse -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address-reverse
        System.out.println("Search Address Reverse:");

        // simple
        MapsCommon.print(client.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337))));

        MapsCommon.print(client.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337))));

        // options
        MapsCommon.print(client.reverseSearchAddress(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337))
                .setIncludeSpeedLimit(true)
                .setEntityType(GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION) // returns only city
        ));

        // complete
        MapsCommon.print(client.reverseSearchAddressWithResponse(
            new ReverseSearchAddressOptions(new GeoPosition(-121.89, 37.337))
                .setIncludeSpeedLimit(true)
                .setEntityType(GeographicEntityType.COUNTRY_SECONDARY_SUBDIVISION) // returns only city
            , null).getStatusCode());

        // Search address reverse cross street -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address-reverse-cross-street
        System.out.println("Revere Search Cross Street Address:");

        // options
        MapsCommon.print(client.reverseSearchCrossStreetAddress(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337))));

        // options
        MapsCommon.print(client.reverseSearchCrossStreetAddress(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337))
                .setTop(2)
                .setHeading(5)));

        // complete
        MapsCommon.print(client.reverseSearchCrossStreetAddressWithResponse(
            new ReverseSearchCrossStreetAddressOptions(new GeoPosition(-121.89, 37.337))
                .setTop(2)
                .setHeading(5),
            null).getStatusCode());

        // Search address structured -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-address-structured
        System.out.println("Search Address Structured:");

        // simple
        MapsCommon.print(client.searchStructuredAddress(new StructuredAddress("US")
            .setPostalCode("98121")
            .setStreetNumber("15127")
            .setStreetName("NE 24th Street")
            .setMunicipality("Redmond")
            .setCountrySubdivision("WA"), null));

        // complete
        MapsCommon.print(client.searchStructuredAddressWithResponse(new StructuredAddress("US")
            .setPostalCode("98121")
            .setStreetNumber("15127")
            .setStreetName("NE 24th Street")
            .setMunicipality("Redmond")
            .setCountrySubdivision("WA"),
            new SearchStructuredAddressOptions()
                    .setTop(2)
                    .setRadiusInMeters(1000),
            null).getStatusCode());

        // Search fuzzy -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-fuzzy
        System.out.println("Search Fuzzy:");

        // simple
        MapsCommon.print(client.fuzzySearch(new FuzzySearchOptions("starbucks")));

        // with options
        SearchAddressResult results = client.fuzzySearch(
            new FuzzySearchOptions("1 Microsoft Way", new GeoPosition(-74.011454, 40.706270))
                .setTop(5));
        MapsCommon.print(results);

        // with response
        Response<SearchAddressResult> response = client.fuzzySearchWithResponse(
            new FuzzySearchOptions("Monaco").setEntityType(GeographicEntityType.COUNTRY)
                .setTop(5), null);
        MapsCommon.print(response.getStatusCode());
        String id = response.getValue().getResults().get(0).getDataSource().getGeometry().getId();

        // Get polygon -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-polygon
        List<String> ids = results.getResults().stream()
            .filter(item -> item.getDataSource() != null && item.getDataSource().getGeometry() != null)
            .map(item -> item.getDataSource().getGeometry().getId())
            .collect(Collectors.toList());
        ids.add(id);

        if (ids != null && !ids.isEmpty()) {
           System.out.println("Get Polygon: " + ids);
           // MapsCommon.print(client.getPolygons(ids).get(1));
           MapsCommon.print(client.getPolygons(ids));
           MapsCommon.print(client.getPolygonsWithResponse(ids, null).getValue().getClass());
        }

        // Search POI -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-poi
        System.out.println("Search Points of Interest:");

        // coordinates
        MapsCommon.print(client.searchPointOfInterest(
            new SearchPointOfInterestOptions("pizza", new GeoPosition(-121.97483, 36.98844))));

        // options
        MapsCommon.print(client.searchPointOfInterest(
            new SearchPointOfInterestOptions("pizza", new GeoPosition(-121.97483, 36.98844))
                .setTop(10)
                .setOperatingHours(OperatingHoursRange.NEXT_SEVEN_DAYS)));

        // with response
        MapsCommon.print(client.searchPointOfInterestWithResponse(
            new SearchPointOfInterestOptions("pizza", new GeoPosition(-121.97483, 36.98844))
                .setTop(10)
                .setOperatingHours(OperatingHoursRange.NEXT_SEVEN_DAYS),
            null).getStatusCode());

        // Search nearby -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-nearby
        System.out.println("Search Nearby:");

        // options
        MapsCommon.print(client.searchNearbyPointOfInterest(
            new SearchNearbyPointsOfInterestOptions(new GeoPosition(-74.011454, 40.706270))
                .setCountryFilter(Arrays.asList("US"))
                .setTop(10)));

        // response
        MapsCommon.print(client.searchNearbyPointOfInterestWithResponse(
            new SearchNearbyPointsOfInterestOptions(new GeoPosition(-74.011454, 40.706270))
                .setCountryFilter(Arrays.asList("US"))
                .setTop(10),
            null).getStatusCode());

        // Search POI Category -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-poi-category
        System.out.println("Get Point of Interest Category:");

        // complete - search for italian restaurant in NYC
        MapsCommon.print(client.searchPointOfInterestCategory(
            new SearchPointOfInterestCategoryOptions("pasta", new GeoPosition(-74.011454, 40.706270))
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(3)));

        // with response
        MapsCommon.print(client.searchPointOfInterestCategoryWithResponse(
            new SearchPointOfInterestCategoryOptions("pasta", new GeoPosition(-74.011454, 40.706270))
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(3),
            null).getStatusCode());

        // Get POI Category Tree -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/get-search-poi-category-tree-preview
        // client.getSearchPoiCategoryTreePreviewWithResponse() offers a complete
        // version including more parameters and
        // the underlying HttpResponse object.
        System.out.println("Get Search POI Category Tree:");
        MapsCommon.print(client.getPointOfInterestCategoryTree(null));

        // Post search along route -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-along-route
        System.out.println("Search Along Route");

        // create route points
        List<GeoPosition> points = new ArrayList<>();
        points.add(new GeoPosition(-122.143035, 47.653536));
        points.add(new GeoPosition(-122.187164, 47.617556));
        points.add(new GeoPosition(-122.114981, 47.570599));
        points.add(new GeoPosition(-122.132756, 47.654009));
        GeoLineString route = new GeoLineString(points);

        // simple
        MapsCommon.print(client.searchAlongRoute(new SearchAlongRouteOptions("burger", 1000, route)));

        // options
        MapsCommon.print(client.searchAlongRoute(
            new SearchAlongRouteOptions("burger", 1000, route)
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(5)));

        // complete
        MapsCommon.print(client.searchAlongRouteWithResponse(
            new SearchAlongRouteOptions("burger", 1000, route)
                .setCategoryFilter(Arrays.asList(7315))
                .setTop(5),
            null).getStatusCode());

        // Search inside geometry -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-along-route
        System.out.println("Search Inside Geometry");

        // create GeoPolygon
        List<GeoPosition> coordinates = new ArrayList<>();
        coordinates.add(new GeoPosition(-122.43576049804686, 37.7524152343544));
        coordinates.add(new GeoPosition(-122.43301391601562, 37.70660472542312));
        coordinates.add(new GeoPosition(-122.36434936523438, 37.712059855877314));
        coordinates.add(new GeoPosition(-122.43576049804686, 37.7524152343544));
        GeoLinearRing ring = new GeoLinearRing(coordinates);
        GeoPolygon polygon = new GeoPolygon(ring);

        // simple
        MapsCommon.print(client.searchInsideGeometry(
            new SearchInsideGeometryOptions("Leland Avenue", polygon)));

        // options
        MapsCommon.print(client.searchInsideGeometry(
            new SearchInsideGeometryOptions("Leland Avenue", polygon)
                .setTop(5)));

        // complete
        MapsCommon.print(client.searchInsideGeometryWithResponse(
            new SearchInsideGeometryOptions("Leland Avenue", polygon)
                .setTop(5),
            null).getStatusCode());

        /* Batch operations. */

        // Search address batch sync -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-address-batch
        // This call posts addresses for search using the Synchronous Batch API.
        // All results will be available when the call returns. A maximum of 100
        // addresses can be searched this way.
        List<SearchAddressOptions> optionsList = new ArrayList<>();
        optionsList.add(new SearchAddressOptions("400 Broad St, Seattle, WA 98109").setTop(3));
        optionsList.add(new SearchAddressOptions("One, Microsoft Way, Redmond, WA 98052").setTop(3));
        optionsList.add(new SearchAddressOptions("350 5th Ave, New York, NY 10118").setTop(3));
        optionsList.add(new SearchAddressOptions("1 Main Street")
            .setCountryFilter(Arrays.asList("GB", "US", "AU")).setTop(3));

        // Search address batch async -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-address-batch
        // This call posts addresses for search using the Asynchronous Batch API.
        // SyncPoller will do the polling automatically and you can retrieve the result
        // with getFinalResult()
        System.out.println("Search Address Batch Async");
        MapsCommon.print(client.beginSearchAddressBatch(optionsList).getFinalResult());
        SyncPoller<BatchSearchResult, BatchSearchResult> poller = client.beginSearchAddressBatch(optionsList);
        BatchSearchResult result = poller.getFinalResult();
        MapsCommon.print(result.getBatchSummary());
        MapsCommon.print(result.getBatchItems().get(0).getResult().getResults().get(0).getBoundingBox());

        // Search address reverse batch -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-address-reverse-batch
        // This is also a batch API like searchAddressBatch(), so the same calling
        // patterns apply.
        List<ReverseSearchAddressOptions> reverseOptionsList = new ArrayList<>();
        reverseOptionsList.add(new ReverseSearchAddressOptions(new GeoPosition(2.294911, 48.858561)));
        reverseOptionsList.add(
            new ReverseSearchAddressOptions(new GeoPosition(-122.127896, 47.639765))
                .setRadiusInMeters(5000)
            );
        reverseOptionsList.add(new ReverseSearchAddressOptions(new GeoPosition(-122.348170, 47.621028)));

        System.out.println("Reverse Search Address Batch Async");
        BatchReverseSearchResult br1 =
            client.beginReverseSearchAddressBatch(reverseOptionsList).getFinalResult();
        MapsCommon.print(br1);
        MapsCommon.print(br1.getBatchItems());
        MapsCommon.print(br1.getBatchItems().get(0).getResult().getAddresses().get(0).getAddress());

        // Post search address reverse batch -
        // https://docs.microsoft.com/en-us/rest/api/maps/search/post-search-fuzzy-batch
        // This is also a batch API like postSearchAddressBatch(), so the same calling
        // patterns apply.
        List<FuzzySearchOptions> fuzzyOptionsList = new ArrayList<>();
        fuzzyOptionsList.add(new FuzzySearchOptions("atm", new GeoPosition(-122.128362, 47.639769))
            .setRadiusInMeters(5000).setTop(5));
        fuzzyOptionsList.add(new FuzzySearchOptions("Statue of Liberty").setTop(2));
        fuzzyOptionsList.add(new FuzzySearchOptions("Starbucks", new GeoPosition(-122.128362, 47.639769))
            .setRadiusInMeters(5000));

        System.out.println("Post Search Fuzzy Batch Async");
        MapsCommon.print(client.beginFuzzySearchBatch(fuzzyOptionsList).getFinalResult());
    }

}
