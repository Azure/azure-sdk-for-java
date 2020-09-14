package com.azure.digitaltwins.core;

import com.azure.core.http.HttpClient;
import com.azure.core.util.logging.ClientLogger;
import com.azure.digitaltwins.core.helpers.UniqueIdHelper;
import com.azure.digitaltwins.core.models.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.AssertionFailedError;
import reactor.test.StepVerifier;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.azure.digitaltwins.core.TestHelper.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.digitaltwins.core.TestHelper.assertRestException;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TwinAsyncTests extends TwinTestBase
{
    private final ClientLogger logger = new ClientLogger(TwinAsyncTests.class);

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.digitaltwins.core.TestHelper#getTestParameters")
    @Override
    public void DigitalTwins_Lifecycle(HttpClient httpClient, DigitalTwinsServiceVersion serviceVersion)
    {
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);

        String roomTwinId = UniqueIdHelper.getUniqueDigitalTwinId(TestAssetDefaults.ROOM_TWIN_ID_PREFIX, asyncClient, randomIntegerStringGenerator);
        String floorModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.FLOOR_MODEL_ID, asyncClient, randomIntegerStringGenerator);
        String roomModelId = UniqueIdHelper.getUniqueModelId(TestAssetDefaults.ROOM_MODEL_ID, asyncClient, randomIntegerStringGenerator);

        String roomTwin = TestAssetsHelper.getRoomTwinPayload(roomModelId);
        String roomModel = TestAssetsHelper.getRoomModelPayload(roomModelId, floorModelId);
        List<String> modelsList = new ArrayList<>(Arrays.asList(roomModel));

        try {
            // Create models to test the Twin lifecycle.
            StepVerifier
                .create(asyncClient.createModels(modelsList))
                .assertNext(createResponseList -> logger.info("Created {} models successfully", createResponseList.size()))
                .verifyComplete();

            // Create a Twin
            StepVerifier.create(asyncClient.createDigitalTwin(roomTwinId, roomTwin, BasicDigitalTwin.class))
                .assertNext(createdTwin -> {
                    assertEquals(createdTwin.getId(), roomTwinId);
                    logger.info("Created {} twin successfully", createdTwin.getId());
                })
                .verifyComplete();

            // Get a Twin
            StepVerifier.create(asyncClient.getDigitalTwinWithResponse(roomTwinId))
                .assertNext(getResponse -> {
                    assertEquals(getResponse.getStatusCode(), HttpURLConnection.HTTP_OK);
                    logger.info("Got Twin successfully");

                })
                .verifyComplete();

            // Update Twin
            StepVerifier.create(asyncClient.updateDigitalTwinWithResponse(roomTwinId, TestAssetsHelper.getRoomTwinUpdatePayload(), new UpdateDigitalTwinRequestOptions()))
                .assertNext(updateResponse -> {
                    assertEquals(updateResponse.getStatusCode(), HttpURLConnection.HTTP_NO_CONTENT);
                    logger.info("Updated the twin successfully");
                })
                .verifyComplete();
        }
        // clean up
        finally {
            try
            {
                if (roomTwinId != null)
                {
                    asyncClient.deleteDigitalTwin(roomTwinId);
                }
                if (roomModelId != null)
                {
                    asyncClient.deleteModel(roomModelId);
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
        DigitalTwinsAsyncClient asyncClient = getAsyncClient(httpClient, serviceVersion);
        String twinId = testResourceNamer.randomUuid();

        StepVerifier.create(asyncClient.getDigitalTwin(twinId))
            .verifyErrorSatisfies(ex -> assertRestException(ex, HttpURLConnection.HTTP_NOT_FOUND));
    }
}
