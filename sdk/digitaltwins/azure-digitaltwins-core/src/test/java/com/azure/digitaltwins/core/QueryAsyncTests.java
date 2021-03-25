package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.QueryOptions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class QueryAsyncTests extends QueryTestBase {

    private final ClientLogger logger = new ClientLogger(ComponentsTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void validQuerySucceeds(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) throws InterruptedException {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        int pageSize = 5;
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, asyncClient, randomIntegerStringGenerator);
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, asyncClient, randomIntegerStringGenerator);
        List<String> roomTwinIds = new ArrayList<>();

        try {
            String roomModelPayload = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);

            StepVerifier.create(asyncClient.createModels(new ArrayList<>(Arrays.asList(roomModelPayload))))
                .assertNext(response ->
                    assertThat(response)
                        .as("Created models successfully")
                        .isNotEmpty())
                .verifyComplete();

            // Create a room twin with property "IsOccupied" : true
            String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);

            for (int i = 0; i < pageSize + 1; i++) {
                String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient, randomIntegerStringGenerator);
                roomTwinIds.add(roomTwinId);
                StepVerifier.create(asyncClient.createOrReplaceDigitalTwinWithResponse(roomTwinId, roomTwin, String.class, null))
                    .assertNext(response ->
                        assertThat(response.getStatusCode())
                            .as("Created digitaltwin successfully")
                            .isEqualTo(HttpURLConnection.HTTP_OK))
                    .verifyComplete();
            }

            waitIfLive();

            String queryString = "SELECT * FROM digitaltwins where IsOccupied = true";

            StepVerifier.create(asyncClient.query(queryString, BasicDigitalTwin.class, null))
                .thenConsumeWhile(dt -> {
                    assertThat(dt.getContents().get("IsOccupied"))
                        .as("IsOccupied should be true")
                        .isEqualTo(true);
                    return true;
                })
                .verifyComplete();

            // Test that page size hint works, and that all returned pages either have the page size hint amount of
            // elements, or have no continuation token (signaling that it is the last page)
            AtomicInteger pageCount = new AtomicInteger(0);
            StepVerifier.create(asyncClient.query(queryString, BasicDigitalTwin.class, new QueryOptions().setMaxItemsPerPage(pageSize)).byPage())
                .thenConsumeWhile(digitalTwinsPage -> {
                    pageCount.incrementAndGet();
                    if (digitalTwinsPage.getContinuationToken() != null) {
                        assertFalse(digitalTwinsPage.getValue().size() < pageSize, "Unexpected page size for a non-terminal page");
                    }
                    return true;
                })
                .verifyComplete();

            assertTrue(pageCount.get() > 1, "Expected more than one page of query results");
        } finally {
            // Cleanup
            try {
                for (String roomTwinId : roomTwinIds) {
                    asyncClient.deleteDigitalTwin(roomTwinId).block();
                }
                if (roomModelId != null) {
                    asyncClient.deleteModel(roomModelId).block();
                }
            } catch (Exception ex) {
                fail("Failed to cleanup due to: ", ex);
            }
        }
    }
}
