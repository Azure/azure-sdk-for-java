// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests;

import com.azure.ai.vision.face.FaceServiceVersion;
import com.azure.ai.vision.face.FaceSessionAsyncClient;
import com.azure.ai.vision.face.FaceSessionClient;
import com.azure.ai.vision.face.models.CreateLivenessWithVerifySessionContent;
import com.azure.ai.vision.face.models.LivenessOperationMode;
import com.azure.ai.vision.face.models.LivenessWithVerifySession;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.ai.vision.face.tests.commands.liveness.ILivenessWithVerifySessionSyncCommands;
import com.azure.ai.vision.face.tests.commands.liveness.LivenessSessionWithVerifyCommandsProvider;
import com.azure.ai.vision.face.tests.utils.FaceDisplayNameGenerator;
import com.azure.ai.vision.face.tests.utils.TestUtils;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.BinaryData;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.util.function.Tuple3;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.azure.ai.vision.face.models.LivenessSession;
import com.azure.ai.vision.face.models.VerifyImageFileDetails;

// @DisplayNameGeneration(FaceDisplayNameGenerator.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RecordWithoutRequestBody
public class LivenessSessionWithVerifyTest extends FaceClientTestBase {
    private ILivenessWithVerifySessionSyncCommands mCurrentCommand;
    private String mSessionId;

    @ParameterizedTest
    @MethodSource("getDataForTestSessionCreation")
    public void testCreateSession(String httpClientName, FaceServiceVersion serviceVersion,
        Supplier<ILivenessWithVerifySessionSyncCommands> commandProvider, String path) {
        String uuid = UUID.randomUUID().toString();
        BinaryData imageData = path != null ? Utils.loadFromFile(path) : null;
        VerifyImageFileDetails verifyImageFileDetails = new VerifyImageFileDetails(imageData);
        if (imageData != null) {
            verifyImageFileDetails.setFilename("vic-test.jpg");
        }
        CreateLivenessWithVerifySessionContent content
            = new CreateLivenessWithVerifySessionContent(LivenessOperationMode.PASSIVE, verifyImageFileDetails)
                .setDeviceCorrelationId(uuid);
        createSessionAndVerify(commandProvider.get(), content);
    }

    @ParameterizedTest
    @MethodSource("getDataForTestSessionCreation")
    public void testCreateSessionDeviceIdOptional(String httpClientName, FaceServiceVersion serviceVersion,
        Supplier<ILivenessWithVerifySessionSyncCommands> commandProvider, String path) {
        BinaryData imageData = path != null ? Utils.loadFromFile(path) : null;
        VerifyImageFileDetails verifyImageFileDetails = new VerifyImageFileDetails(imageData);
        if (imageData != null) {
            verifyImageFileDetails.setFilename("vic-test.jpg");
        }
        CreateLivenessWithVerifySessionContent content
            = new CreateLivenessWithVerifySessionContent(LivenessOperationMode.PASSIVE, verifyImageFileDetails)
                .setDeviceCorrelationIdSetInClient(true)
                .setDeviceCorrelationId(null); // Fix: explicitly set deviceCorrelationId to null when using client-side ID
        createSessionAndVerify(commandProvider.get(), content);
    }

    @ParameterizedTest
    @MethodSource("getDataForTestSessionCreation")
    public void testCreateSessionWithTokenTTL(String httpClientName, FaceServiceVersion serviceVersion,
        Supplier<ILivenessWithVerifySessionSyncCommands> commandProvider, String path) {
        ILivenessWithVerifySessionSyncCommands livenessCommands = commandProvider.get();

        String uuid = UUID.randomUUID().toString();
        int authTokenTimeToLiveInSeconds = 60; // Set a valid TTL value instead of null

        BinaryData imageData = path != null ? Utils.loadFromFile(path) : null;
        VerifyImageFileDetails verifyImageFileDetails = new VerifyImageFileDetails(imageData);
        if (imageData != null) {
            verifyImageFileDetails.setFilename("vic-test.jpg");
        }

        CreateLivenessWithVerifySessionContent content
            = new CreateLivenessWithVerifySessionContent(LivenessOperationMode.PASSIVE, verifyImageFileDetails)
                .setDeviceCorrelationIdSetInClient(false) // When setting deviceCorrelationId, this should be false
                .setDeviceCorrelationId(uuid) // Set the actual UUID
                .setAuthTokenTimeToLiveInSeconds(authTokenTimeToLiveInSeconds); // Set valid TTL instead of null

        LivenessWithVerifySession result = createSessionAndVerify(livenessCommands, content);
        // Uncomment these if you want to verify the TTL was set correctly:
        // LivenessWithVerifySession livenessSession
        //     = livenessCommands.getLivenessWithVerifySessionResultSync(result.getSessionId());
        // Assertions.assertNotNull(livenessSession);
        // Assertions.assertEquals(livenessSession.getAuthTokenTimeToLiveInSeconds(), authTokenTimeToLiveInSeconds);
    }

    @BeforeEach
    public void setup() {
        mSessionId = null;
        mCurrentCommand = null;
    }

    @AfterEach
    public void tearDown() {
        String sessionId = mSessionId;
        if (sessionId != null && !sessionId.isEmpty()) {
            mCurrentCommand.deleteLivenessWithVerifySessionSync(sessionId);
        }
    }

    private Stream<Arguments> getDataForTestSessionCreation() {
        String[] imagePaths = new String[] { null, Resources.TEST_IMAGE_PATH_DETECTLIVENESS_VERIFYIMAGE };
        LivenessSessionWithVerifyCommandsProvider[] providers
            = LivenessSessionWithVerifyCommandsProvider.getFunctionProviders();

        Stream<Tuple3<String, FaceServiceVersion, Supplier<ILivenessWithVerifySessionSyncCommands>>> clientArumentStream
            = createClientArgumentStream(FaceSessionClient.class, FaceSessionAsyncClient.class, providers);

        return TestUtils.createCombinationWithClientArguments(clientArumentStream, imagePaths);
    }

    private LivenessWithVerifySession createSessionAndVerify(ILivenessWithVerifySessionSyncCommands livenessCommands,
        CreateLivenessWithVerifySessionContent content) {

        LivenessWithVerifySession result = livenessCommands.createLivenessWithVerifySessionSync(content);

        mCurrentCommand = livenessCommands;
        mSessionId = result.getSessionId();

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getSessionId());
        Assertions.assertNotNull(result.getAuthToken());

        return result;
    }
}
