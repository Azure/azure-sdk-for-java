package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.BasicDigitalTwin;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class QueryAsyncTests extends QueryTestBase{

    private final ClientLogger logger = new ClientLogger(ComponentsTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void validQuerySucceeds(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, asyncClient, randomIntegerStringGenerator);
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, asyncClient, randomIntegerStringGenerator);
        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient, randomIntegerStringGenerator);

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

            StepVerifier.create(asyncClient.createDigitalTwinWithResponse(roomTwinId, roomTwin, String.class, null))
                .assertNext(response ->
                    assertThat(response.getStatusCode())
                        .as("Created digitaltwin successfully")
                        .isEqualTo(HttpURLConnection.HTTP_OK))
                .verifyComplete();

            String queryString = "SELECT * FROM digitaltwins where IsOccupied = true";

            StepVerifier.create(asyncClient.query(queryString, BasicDigitalTwin.class, null))
                .thenConsumeWhile(dt ->  {
                    assertThat(dt.getCustomProperties().get("IsOccupied"))
                        .as("IsOccupied should be true")
                        .isEqualTo(true);
                    return true;
                })
                .verifyComplete();
        }
        finally {
            // Cleanup
            try {
                if (roomTwinId != null) {
                    asyncClient.deleteDigitalTwin(roomTwinId).block();
                }
                if (roomModelId != null){
                    asyncClient.deleteModel(roomModelId).block();
                }
            }
            catch (Exception ex)
            {
                fail("Failed to cleanup due to: ", ex);
            }
        }
    }
}
