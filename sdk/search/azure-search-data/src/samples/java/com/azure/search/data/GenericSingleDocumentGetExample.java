// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data;

import com.azure.search.data.common.credentials.ApiKeyCredentials;
import com.azure.search.data.customization.Document;
import com.azure.search.data.customization.SearchIndexClientBuilder;
import reactor.core.publisher.Mono;

public class GenericSingleDocumentGetExample {

    public static void main(String[] args) {
        GetSingleDocument();
    }

    public static void GetSingleDocument(){
        SearchIndexAsyncClient searchClient = getSearchClient();

        Mono<Document> result = searchClient.getDocument("22")
            .doOnSuccess(val -> System.out.println("success"))
            .doOnError(err -> System.out.println("error:"+err));
        Document document = result.block();
        for(String key: document.keySet()) {
            System.out.println(key + ":" + document.get(key));
        }

        /** Output:
         * Document with key: 22 was retrieved successfully
         * HotelId:22
         * HotelName:Stone Lion Inn
         * Description:Full breakfast buffet for 2 for only $1.  Excited to show off our room upgrades, faster high speed WiFi, updated corridors & meeting space. Come relax and enjoy your stay.
         * Description_fr:Petit déjeuner buffet complet pour 2 pour seulement $1.  Excité de montrer nos mises à niveau de la chambre, plus rapide WiFi à haute vitesse, les couloirs et l'espace de réunion. Venez vous détendre et profiter de votre séjour.
         * Category:Budget
         * Tags:[laundry service, air conditioning, restaurant]
         * ParkingIncluded:false
         * LastRenovationDate:1975-09-18T00:00:00Z
         * Rating:3.9
         * Location:{type=Point, coordinates=[-122.126381, 47.640656], crs={type=name, properties={name=EPSG:4326}}}
         * Address:{StreetAddress=16021 NE 36th Way, City=Redmond, StateProvince=WA, PostalCode=98052, Country=USA}
         * Rooms:[{Description=Deluxe Room, 1 Queen Bed (Amenities), Description_fr=Chambre Deluxe, 1 grand lit (Services), ........]
         */
    }

    private static SearchIndexAsyncClient getSearchClient() {
        ApiKeyCredentials apiKeyCredentials = new ApiKeyCredentials("<apiKeyCredentials>");
        String searchServiceName = "<searchServiceName>";
        String dnsSuffix = "search.windows.net";
        String indexName = "<indexName>";

        return new SearchIndexClientBuilder()
            .serviceName(searchServiceName)
            .searchDnsSuffix(dnsSuffix)
            .indexName(indexName)
            .credential(apiKeyCredentials)
            .buildAsyncClient();
    }
}
