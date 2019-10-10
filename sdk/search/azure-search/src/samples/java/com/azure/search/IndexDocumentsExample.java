// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class IndexDocumentsExample {
    /**
     * This is an example of using {@link SearchIndexClient} to upload, merge and delete documents in Azure Search.
     * This example assumes you have access to an existing Azure Search instance and its index.
     * @param args
     */
    public static void main(String[] args) {
        // User input
        String apiKey = "<api key>";
        String searchServiceName = "<search service name>";
        String indexName = "<index name>";

        // Create client
        ApiKeyCredentials apiKeyCredentials = new ApiKeyCredentials(apiKey);
        SearchIndexClient client = new SearchIndexClientBuilder()
            .serviceName(searchServiceName)
            .searchDnsSuffix("search.windows.net")
            .indexName(indexName)
            .credential(apiKeyCredentials)
            .buildClient();

        // Upload
        Hotel hotel1 = new Hotel()
            .hotelId("1")
            .tags(Arrays.asList("tag1"));
        Hotel hotel2 = new Hotel()
            .hotelId("2");
        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel1);
        hotels.add(hotel2);
        client.uploadDocuments(hotels);

        // mergeOrUpload
        Hotel updatedHotel1 = new Hotel()
            .hotelId("1")
            .tags(Arrays.asList("tag1", "tag2"));
        List<Hotel> updatedHotels = new ArrayList<>();
        updatedHotels.add(updatedHotel1);
        updatedHotels.add(hotel2);
        client.mergeOrUploadDocuments(updatedHotels);

        // Get
        // expected output: {HotelId=1, Tags=[tag1, tag2]}
        Document document = client.getDocument("1");
        System.out.println(document);

        // Delete
        client.deleteDocuments(updatedHotels);

        // Dynamic example
        Document hotel3 = new Document();
        hotel3.put("HotelId", "3");
        hotel3.put("Tags", Arrays.asList("tag3"));
        List<Document> dynamicHotels = new ArrayList<>();
        dynamicHotels.add(hotel3);
        client.uploadDocuments(dynamicHotels);

        // Upload from json file
        try {
            InputStream s = IndexDocumentsExample.class.getResourceAsStream("HotelsData.json");
            Reader docsData = new InputStreamReader(s);
            List<Map> hotelsFromFile = new ObjectMapper().readValue(docsData, List.class);
            client.uploadDocuments(hotelsFromFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
