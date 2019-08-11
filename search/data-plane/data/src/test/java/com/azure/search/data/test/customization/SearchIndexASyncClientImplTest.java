package com.azure.search.data.test.customization;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceNotFoundException;
import com.azure.search.data.common.SearchPipelinePolicy;
import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.customization.SearchIndexClientBuilder;
import com.azure.search.data.env.AzureSearchResources;
import com.azure.search.data.env.SearchIndexDocs;
import com.azure.search.data.env.SearchIndexService;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.ApplicationTokenCredentials;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SearchIndexASyncClientImplTest {

    private String serviceName = "";
    private String apiAdminKey = "";
    private String indexName = "hotels";
    private AzureSearchResources azureSearchResources;
    private String apiVersion = "2019-05-06";
    private String dnsSuffix = "search.windows.net";;
    private SearchIndexAsyncClient searchClient;

    @Before
    public void initialize() throws IOException {
        ApplicationTokenCredentials applicationTokenCredentials = new ApplicationTokenCredentials(
            "clientId",
            "domain",
            "secret",
            AzureEnvironment.AZURE);

        String subscriptionId = "subscriptionID";
        Region location = Region.US_EAST;

        azureSearchResources = new AzureSearchResources(
            applicationTokenCredentials, subscriptionId, location);
        azureSearchResources.initialize();

        serviceName = azureSearchResources.getSearchServiceName();
        apiAdminKey = azureSearchResources.getSearchAdminKey();

        //Creating Index:
        SearchIndexService searchIndexService;
        try {
            searchIndexService = new SearchIndexService(serviceName, apiAdminKey);
            searchIndexService.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        // Uploading Documents:
        try {
            SearchIndexDocs searchIndexDocs = new SearchIndexDocs(serviceName, apiAdminKey,
                searchIndexService.indexName(),
                dnsSuffix,
                apiVersion);
            searchIndexDocs.initialize();

        } catch (Exception e) {
            e.printStackTrace();
        }

        searchClient = new SearchIndexClientBuilder()
            .serviceName(serviceName)
            .searchDnsSuffix(dnsSuffix)
            .indexName(searchIndexService.indexName())
            .apiVersion(apiVersion)
            .policy(new SearchPipelinePolicy(apiAdminKey))
            .buildAsyncClient();


    }

    @After
    public void cleanup() {
        try {
            System.out.println("Waiting 100 secs before cleaning the created Azure Search resource");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        azureSearchResources.cleanup();
    }

    @Test
    public void canGetDynamicDocument() {

        Map<String, Object> addressDoc = new HashMap<String, Object>();
        addressDoc.put("StreetAddress", "677 5th Ave");
        addressDoc.put("City", "New York");
        addressDoc.put("StateProvince", "NY");
        addressDoc.put("Country", "USA");
        addressDoc.put("PostalCode", "10022");

        ArrayList<String> room1Tags = new ArrayList<String>();
        room1Tags.add("vcr/dvd");

        HashMap<String, Object> room1Doc = new HashMap<String, Object>();
        room1Doc.put("Description", "Budget Room, 1 Queen Bed (Cityside)");
        room1Doc.put("Description_fr", "Chambre Économique, 1 grand lit (côté ville)");
        room1Doc.put("Type", "Budget Room");
        room1Doc.put("BaseRate", 9.69);
        room1Doc.put("BedOptions", "1 Queen Bed");
        room1Doc.put("SleepsCount", 2);
        room1Doc.put("SmokingAllowed", true);
        room1Doc.put("Tags", room1Tags);

        ArrayList<String> room2Tags = new ArrayList<String>();
        room2Tags.add("vcr/dvd");
        room2Tags.add("jacuzzi tub");

        HashMap<String, Object> room2Doc = new HashMap<String, Object>();
        room2Doc.put("Description", "Budget Room, 1 King Bed (Mountain View)");
        room2Doc.put("Description_fr", "Chambre Économique, 1 très grand lit (Mountain View)");
        room2Doc.put("Type", "Budget Room");
        room2Doc.put("BaseRate", 8.09);
        room2Doc.put("BedOptions", "1 King Bed");
        room2Doc.put("SleepsCount", 2);
        room2Doc.put("SmokingAllowed", true);
        room2Doc.put("Tags", room2Tags);

        ArrayList<HashMap<String, Object>> rooms = new ArrayList<HashMap<String, Object>>();
        rooms.add(room1Doc);
        rooms.add(room2Doc);


        ArrayList<String> tags = new ArrayList<String>();
        tags.add("pool");
        tags.add("air conditioning");
        tags.add("concierge");


        Map<String, Object> expectedDoc = new HashMap<String, Object>();
        expectedDoc.put("HotelId", "1000000000");
        expectedDoc.put("HotelName", "Secret Point Motel");
        expectedDoc.put("Description", "The hotel is ideally located on the main commercial artery of the city in the heart of New York. A few minutes away is Time's Square and the historic centre of the city, as well as other places of interest that make New York one of America's most attractive and cosmopolitan cities.");
        expectedDoc.put("Description_fr", "L'hôtel est idéalement situé sur la principale artère commerciale de la ville en plein cœur de New York. A quelques minutes se trouve la place du temps et le centre historique de la ville, ainsi que d'autres lieux d'intérêt qui font de New York l'une des villes les plus attractives et cosmopolites de l'Amérique.");
        expectedDoc.put("Category", "Boutique");
        expectedDoc.put("Tags", tags);
        expectedDoc.put("ParkingIncluded", false);
        expectedDoc.put("SmokingAllowed", true);
        expectedDoc.put("LastRenovationDate", "1970-01-18T00:00:00Z");
        expectedDoc.put("Rating", 3);
        expectedDoc.put("Address", addressDoc);
        expectedDoc.put("Rooms", rooms);

        try {
            SearchIndexDocs searchIndexDocs = new SearchIndexDocs(serviceName, apiAdminKey,
                indexName,
                "search.windows.net",
                "2019-05-06");
            searchIndexDocs.addSingleDocData(expectedDoc);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Mono<Map<String, Object>> futureDoc = searchClient.getDocument("1000000000");


        StepVerifier
            .create(futureDoc)
            .assertNext(result -> {
                result.remove("Location");
                Assert.assertEquals(expectedDoc, result);
            })
            .verifyComplete();

    }

    @Test
    public void getDocumentThrowsWhenDocumentNotFound() {
        Mono<Map<String, Object>> futureDoc = searchClient.getDocument("1000000001");
        StepVerifier
            .create(futureDoc)
            .verifyErrorSatisfies(error -> assertEquals(ResourceNotFoundException.class , error.getClass()));

    }

    @Test
    public void getDocumentThrowsWhenRequestIsMalformed() {

        Map<String, Object> hotelDoc = new HashMap<String, Object>();
        hotelDoc.put("HotelId", "1000000002");
        hotelDoc.put("Description", "Surprisingly expensive");

        ArrayList<String> selectedFields=new ArrayList<String>();
        selectedFields.add("HotelId");
        selectedFields.add("ThisFieldDoesNotExist");

        try {
            SearchIndexDocs searchIndexDocs = new SearchIndexDocs(serviceName, apiAdminKey,
                indexName,
                "search.windows.net",
                "2019-05-06");
            searchIndexDocs.addSingleDocData(hotelDoc);

        } catch (Exception e) {
            e.printStackTrace();
        }

        Mono futureDoc = searchClient.getDocument("1000000002", selectedFields, null);

        StepVerifier
            .create(futureDoc)
            .verifyErrorSatisfies(error -> {
                assertEquals(HttpResponseException.class, error.getClass());
                assertEquals(HttpResponseStatus.BAD_REQUEST.code(), ((HttpResponseException) error).response().statusCode());
                assertTrue(error.getMessage().contains("Invalid expression: Could not find a property named 'ThisFieldDoesNotExist' on type 'search.document'."));
            });


    }


}
