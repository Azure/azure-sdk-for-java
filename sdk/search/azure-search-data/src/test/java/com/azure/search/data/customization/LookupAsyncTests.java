// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.SearchIndexAsyncClient;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.test.environment.models.Hotel;
import com.azure.search.test.environment.models.HotelAddress;
import com.azure.search.test.environment.models.HotelRoom;
import com.azure.search.test.environment.models.ModelWithPrimitiveCollections;
import org.junit.Assert;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;


public class LookupAsyncTests extends LookupTestBase {
    private SearchIndexAsyncClient client;

    @Override
    public void canGetStaticallyTypedDocument() throws ParseException {
        Hotel expected = prepareExpectedHotel();
        uploadDocument(client, INDEX_NAME, expected);

        Mono<Document> result = client.getDocument(expected.hotelId());

        StepVerifier.create(result)
            .assertNext(res -> Assert.assertEquals(expected, res.as(Hotel.class)))
            .verifyComplete();
    }

    @Override
    public void canGetStaticallyTypedDocumentWithNullOrEmptyValues() {
        Hotel expected = prepareEmptyHotel();
        uploadDocument(client, INDEX_NAME, expected);

        Mono<Document> result = client.getDocument(expected.hotelId());

        StepVerifier.create(result)
            .assertNext(res -> Assert.assertEquals(expected, res.as(Hotel.class)))
            .verifyComplete();
    }

    @Override
    public void canGetStaticallyTypedDocumentWithPascalCaseFields() {
        Hotel expected = preparePascalCaseFieldsHotel();
        uploadDocument(client, INDEX_NAME, expected);

        Mono<Document> result = client.getDocument(expected.hotelId());

        StepVerifier.create(result)
            .assertNext(res -> Assert.assertEquals(expected, res.as(Hotel.class)))
            .verifyComplete();
    }

    @Override
    public void canRoundtripStaticallyTypedPrimitiveCollections() throws ParseException {
        ModelWithPrimitiveCollections expected = preparePrimitivesModel();
        uploadDocument(client, MODEL_WITH_VALUE_TYPES_INDEX_NAME, expected);

        Mono<Document> result = client.getDocument(expected.key);

        StepVerifier.create(result)
            .assertNext(res -> Assert.assertEquals(expected, res.as(ModelWithPrimitiveCollections.class)))
            .verifyComplete();
    }

    @Override
    public void getStaticallyTypedDocumentSetsUnselectedFieldsToNull() throws ParseException {
        Hotel indexedDoc = prepareSelectedFieldsHotel();

        Hotel expected = new Hotel()
            .hotelName("Countryside Hotel")
            .description(
                "Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, full kitchen, "
                    + "washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .address(new HotelAddress().city("Durham"))
            .rooms(Arrays.asList(new HotelRoom().baseRate(2.44), new HotelRoom().baseRate(7.69)));

        uploadDocument(client, INDEX_NAME, indexedDoc);

        List<String> selectedFields = Arrays.asList("Description", "HotelName", "Address/City", "Rooms/BaseRate");
        Mono<Document> result = client.getDocument(indexedDoc.hotelId(), selectedFields, new SearchRequestOptions());

        StepVerifier.create(result)
            .assertNext(res -> Assert.assertEquals(expected, res.as(Hotel.class)))
            .verifyComplete();
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildAsyncClient();
    }
}
