package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.QueryOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryTests extends QueryTestBase {

    private final ClientLogger logger = new ClientLogger(ComponentsTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void validQuerySucceeds(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws InterruptedException {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        int pageSize = 3;
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, client, randomIntegerStringGenerator);
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, client, randomIntegerStringGenerator);
        List<String> roomTwinIds = new ArrayList<>();

        try {
            String roomModelPayload = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
            client.createModelsWithResponse(new ArrayList<>(Arrays.asList(roomModelPayload)), Context.NONE);

            // Create a room twin with property "IsOccupied" : true
            String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
            for (int i = 0; i < pageSize + 1; i++) {
                String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, randomIntegerStringGenerator);
                roomTwinIds.add(roomTwinId);
                client.createOrReplaceDigitalTwinWithResponse(roomTwinId, roomTwin, String.class, null, Context.NONE);
            }

            waitIfLive();

            String queryString = "SELECT * FROM digitaltwins where IsOccupied = true";

            PagedIterable<BasicDigitalTwin> pagedQueryResponse = client.query(queryString, BasicDigitalTwin.class, new QueryOptions().setMaxItemsPerPage(pageSize), Context.NONE);

            for (BasicDigitalTwin digitalTwin : pagedQueryResponse) {
                assertThat(digitalTwin.getContents().get("IsOccupied"))
                    .as("IsOccupied should be true")
                    .isEqualTo(true);
            }

            pagedQueryResponse = client.query(queryString, BasicDigitalTwin.class,new QueryOptions().setMaxItemsPerPage(pageSize), Context.NONE);

            // Test that page size hint works, and that all returned pages either have the page size hint amount of
            // elements, or have no continuation token (signaling that it is the last page)
            int pageCount = 0;
            for (Page<BasicDigitalTwin> digitalTwinsPage : pagedQueryResponse.iterableByPage()) {
                pageCount++;
                int elementsPerPage = 0;
                for (BasicDigitalTwin ignored : digitalTwinsPage.getElements()) {
                    elementsPerPage++;
                }

                if (digitalTwinsPage.getContinuationToken() != null) {
                    assertFalse(elementsPerPage < pageSize, "Unexpected page size for a non-terminal page");
                }
            }

            assertTrue(pageCount > 1, "Expected more than one page of query results");
        } finally {
            // Cleanup
            try {
                for (String roomTwinId : roomTwinIds) {
                    client.deleteDigitalTwin(roomTwinId);
                }
                if (roomModelId != null) {
                    client.deleteModel(roomModelId);
                }
            } catch (Exception ex) {
                fail("Failed to cleanup due to: ", ex);
            }
        }
    }
}
