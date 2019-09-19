// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.data.customization;

import com.azure.search.data.SearchIndexClient;
import com.azure.search.data.generated.models.SearchRequestOptions;
import com.azure.search.data.customization.models.Hotel;
import com.azure.search.data.customization.models.HotelAddress;
import com.azure.search.data.customization.models.HotelRoom;
import com.azure.search.data.customization.models.ModelWithPrimitiveCollections;
import org.junit.Assert;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class LookupSyncTests extends LookupTestBase {
    private SearchIndexClient client;

    @Override
    public void canGetStaticallyTypedDocument() throws ParseException {
        Hotel expected = prepareExpectedHotel();
        uploadDocuments(client, INDEX_NAME, expected);

        Document result = client.getDocument(expected.hotelId());
        Hotel actual = result.as(Hotel.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void canGetStaticallyTypedDocumentWithNullOrEmptyValues() {
        Hotel expected = prepareEmptyHotel();
        uploadDocuments(client, INDEX_NAME, expected);

        Document result = client.getDocument(expected.hotelId());
        Hotel actual = result.as(Hotel.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void canGetStaticallyTypedDocumentWithPascalCaseFields() {
        Hotel expected = preparePascalCaseFieldsHotel();
        uploadDocuments(client, INDEX_NAME, expected);

        Document result = client.getDocument(expected.hotelId());
        Hotel actual = result.as(Hotel.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void canRoundtripStaticallyTypedPrimitiveCollections() throws ParseException {
        ModelWithPrimitiveCollections expected = preparePrimitivesModel();
        uploadDocuments(client, MODEL_WITH_VALUE_TYPES_INDEX_NAME, expected);

        Document result = client.getDocument(expected.key);
        ModelWithPrimitiveCollections actual = result.as(ModelWithPrimitiveCollections.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    public void getStaticallyTypedDocumentSetsUnselectedFieldsToNull() throws ParseException {
        Hotel indexedDoc = prepareSelectedFieldsHotel();

        Hotel expected = new Hotel()
            .hotelName("Countryside Hotel")
            .description("Save up to 50% off traditional hotels.  Free WiFi, great location near downtown, full kitchen, washer & dryer, 24/7 support, bowling alley, fitness center and more.")
            .address(new HotelAddress().city("Durham"))
            .rooms(Arrays.asList(new HotelRoom().baseRate(2.44), new HotelRoom().baseRate(7.69)));

        uploadDocuments(client, INDEX_NAME, indexedDoc);

        List<String> selectedFields = Arrays.asList("Description", "HotelName", "Address/City", "Rooms/BaseRate");
        Document result = client.getDocument(indexedDoc.hotelId(), selectedFields, new SearchRequestOptions());
        Hotel actual = result.as(Hotel.class);
        Assert.assertEquals(expected, actual);
    }

    @Override
    protected void initializeClient() {
        client = builderSetup().indexName(INDEX_NAME).buildClient();
    }
}
