package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.BasicDigitalTwin;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class QueryTests extends QueryTestBase{

    private final ClientLogger logger = new ClientLogger(ComponentsTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void validQuerySucceeds(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion) {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID_PREFIX, client, randomIntegerStringGenerator);
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID_PREFIX, client, randomIntegerStringGenerator);
        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, randomIntegerStringGenerator);

        try {
            String roomModelPayload = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
            client.createModelsWithResponse(new ArrayList<>(Arrays.asList(roomModelPayload)), Context.NONE);

            // Create a room twin with property "IsOccupied" : true
            String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
            client.createDigitalTwinWithResponse(roomTwinId, roomTwin, String.class, Context.NONE);

            String queryString = "SELECT * FROM digitaltwins where IsOccupied = true";

            PagedIterable<BasicDigitalTwin> pagedQueryResponse = client.query(queryString, BasicDigitalTwin.class);

            for(BasicDigitalTwin digitalTwin : pagedQueryResponse){
                assertThat(digitalTwin.getCustomProperties().get("IsOccupied"))
                    .as("IsOccupied should be true")
                    .isEqualTo(true);
            }
        }
        finally {
            // Cleanup
            try {
                if (roomTwinId != null) {
                    client.deleteDigitalTwin(roomTwinId);
                }
                if (roomModelId != null){
                    client.deleteModel(roomModelId);
                }
            }
            catch (Exception ex)
            {
                fail("Failed to cleanup due to: ", ex);
            }
        }
    }
}
