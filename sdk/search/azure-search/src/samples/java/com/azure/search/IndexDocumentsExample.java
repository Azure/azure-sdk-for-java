// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import java.util.ArrayList;
import java.util.List;

public class IndexDocumentsExample {
    /**
     * This is an example of using {@link SearchIndexClient} to upload, merge and delete documents in Azure Search.
     * This example assumes you have access to an existing Azure Search instance and its index.
     * @param args
     */
    public static void main(String[] args) {
        // Create client
        ApiKeyCredentials apiKeyCredentials = new ApiKeyCredentials("<api key>");
        SearchIndexClient client = new SearchIndexClientBuilder()
            .serviceName("<search service name>")
            .searchDnsSuffix("search.windows.net")
            .indexName("<index name>")
            .credential(apiKeyCredentials)
            .buildClient();

        // Upload
        Hotel hotel1 = new Hotel()
            .hotelId("1")
            .hotelName("hotel1");
        Hotel hotel2 = new Hotel()
            .hotelId("2")
            .hotelName("hotel2");
        List<Hotel> hotels = new ArrayList<>();
        hotels.add(hotel1);
        hotels.add(hotel2);
        client.uploadDocuments(hotels);

        // mergeOrUpload
        Hotel updatedHotel1 = new Hotel()
            .hotelId("1")
            .hotelName("updatedHotel1");
        List<Hotel> updatedHotels = new ArrayList<>();
        updatedHotels.add(updatedHotel1);
        updatedHotels.add(hotel2);
        client.mergeOrUploadDocuments(updatedHotels);

        // Get
        // expected output: {HotelId=1, HotelName=updatedHotel1}
        Document document = client.getDocument("1");
        System.out.println(document);

        // Delete
        client.deleteDocuments(updatedHotels);

        // Dynamic example
        Document hotel3 = new Document();
        hotel3.put("HotelId", "3");
        hotel3.put("HotelName", "hotel3");
        List<Document> dynamicHotels = new ArrayList<>();
        dynamicHotels.add(hotel3);
        client.uploadDocuments(dynamicHotels);
    }
}
