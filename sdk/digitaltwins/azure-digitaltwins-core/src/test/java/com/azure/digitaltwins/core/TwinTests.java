package com.azure.digitaltwins.core;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import com.azure.core.http.HttpClient;
import org.opentest4j.AssertionFailedError;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TwinTests extends TwinTestBase{

    private final ClientLogger logger = new ClientLogger(TwinTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void DigitalTwins_Lifecycle(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion)
    {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, client, randomIntegerStringGenerator);
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, client, randomIntegerStringGenerator);
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, client, randomIntegerStringGenerator);

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);

        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            List<ModelData> createdList = client.createModels(modelsList);
            logger.info("Created {} models successfully", createdList.size());

            BasicDigitalTwin createdTwin = client.createDigitalTwin(roomTwinId, roomTwin,BasicDigitalTwin.class);

            logger.info("Created {} twin successfully", createdTwin.getId());
            assertEquals(createdTwin.getId(), roomTwinId);

            // Get Twin.
            DigitalTwinsResponse<String> getTwinResponse = client.getDigitalTwinWithResponse(roomTwinId, Context.NONE);
            assertEquals(getTwinResponse.getStatusCode(), HttpURLConnection.HTTP_OK);

            // Update Twin.
            DigitalTwinsResponse<Void> updateTwinResponse = client.updateDigitalTwinWithResponse(
                roomTwinId,
                TestAssetsHelper.getRoomTwinUpdatePayload(),
                new UpdateDigitalTwinRequestOptions(),
                Context.NONE);

            assertEquals(updateTwinResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
        }
        // clean up
        finally {
            try
            {
                if (roomTwinId != null)
                {
                    client.deleteDigitalTwin(roomTwinId);
                }
                if (roomModelId != null)
                {
                    client.deleteModel(roomModelId);
                }
            }
            catch (Exception ex)
            {
                throw new AssertionFailedError("Test cleanup failed", ex);
            }
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void DigitalTwins_TwinNotExist_ThrowsNotFoundException(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion)
    {
        DigitalTwinsClient client = getClient(httpClient, serviceVersion);
        String twinId = testResourceNamer.randomUuid();

        assertRestException(() -> client.getDigitalTwin(twinId), HttpURLConnection.HTTP_NOT_FOUND);
    }
}
