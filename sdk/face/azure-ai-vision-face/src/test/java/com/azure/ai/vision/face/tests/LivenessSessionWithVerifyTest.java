// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests;

import com.azure.ai.vision.face.FaceServiceVersion;
import com.azure.ai.vision.face.FaceSessionAsyncClient;
import com.azure.ai.vision.face.FaceSessionClient;
import com.azure.ai.vision.face.models.CreateLivenessWithVerifySessionContent;
import com.azure.ai.vision.face.models.CreateLivenessWithVerifySessionResult;
import com.azure.ai.vision.face.models.LivenessOperationMode;
import com.azure.ai.vision.face.models.LivenessWithVerifyImage;
import com.azure.ai.vision.face.models.LivenessWithVerifySession;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.ai.vision.face.tests.commands.liveness.ILivenessWithVerifySessionSyncCommands;
import com.azure.ai.vision.face.tests.commands.liveness.LivenessSessionWithVerifyCommandsProvider;
import com.azure.ai.vision.face.tests.utils.FaceDisplayNameGenerator;
import com.azure.ai.vision.face.tests.utils.TestUtils;
import com.azure.core.test.annotation.RecordWithoutRequestBody;
import com.azure.core.util.BinaryData;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

@DisplayNameGeneration(FaceDisplayNameGenerator.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RecordWithoutRequestBody
public class LivenessSessionWithVerifyTest extends FaceClientTestBase {
    private ILivenessWithVerifySessionSyncCommands mCurrentCommand;
    private String mSessionId;

    @ParameterizedTest
    @MethodSource("getDataForTestSessionCreation")
    public void testCreateSession(
            String httpClientName, FaceServiceVersion serviceVersion,
            Supplier<ILivenessWithVerifySessionSyncCommands> commandProvider, String path) {
        String uuid = UUID.randomUUID().toString();
        CreateLivenessWithVerifySessionContent content = new CreateLivenessWithVerifySessionContent(LivenessOperationMode.PASSIVE)
            .setDeviceCorrelationId(uuid);
        createSessionAndVerify(commandProvider.get(), content, path);
    }

    @ParameterizedTest
    @MethodSource("getDataForTestSessionCreation")
    public void testCreateSessionDeviceIdOptional(
            String httpClientName, FaceServiceVersion serviceVersion, Supplier<ILivenessWithVerifySessionSyncCommands> commandProvider, String path) {
        CreateLivenessWithVerifySessionContent content = new CreateLivenessWithVerifySessionContent(LivenessOperationMode.PASSIVE)
            .setDeviceCorrelationIdSetInClient(true);
        createSessionAndVerify(commandProvider.get(), content, path);
    }

    @ParameterizedTest
    @MethodSource("getDataForTestSessionCreation")
    public void testCreateSessionWithTokenTTL(String httpClientName, FaceServiceVersion serviceVersion,
            Supplier<ILivenessWithVerifySessionSyncCommands> commandProvider, String path) {
        ILivenessWithVerifySessionSyncCommands livenessCommands = commandProvider.get();

        int authTokenTimeToLiveInSeconds = 60;
        String uuid = UUID.randomUUID().toString();

        CreateLivenessWithVerifySessionContent content = new CreateLivenessWithVerifySessionContent(LivenessOperationMode.PASSIVE)
            .setDeviceCorrelationId(uuid)
            .setAuthTokenTimeToLiveInSeconds(authTokenTimeToLiveInSeconds);

        CreateLivenessWithVerifySessionResult result = createSessionAndVerify(livenessCommands, content, path);
        LivenessWithVerifySession livenessSession = livenessCommands.getLivenessWithVerifySessionResultSync(result.getSessionId());
        Assertions.assertNotNull(livenessSession);
        Assertions.assertEquals(livenessSession.getAuthTokenTimeToLiveInSeconds(), authTokenTimeToLiveInSeconds);
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
        String[] imagePaths = new String[] {null, Resources.TEST_IMAGE_PATH_DETECTLIVENESS_VERIFYIMAGE};
        LivenessSessionWithVerifyCommandsProvider[] providers =  LivenessSessionWithVerifyCommandsProvider.getFunctionProviders();

        Stream<Triple<String, FaceServiceVersion, Supplier<ILivenessWithVerifySessionSyncCommands>>> clientArumentStream =
                createClientArgumentStream(FaceSessionClient.class, FaceSessionAsyncClient.class, providers);

        return TestUtils.createCombinationWithClientArguments(clientArumentStream, imagePaths);
    }

    private CreateLivenessWithVerifySessionResult createSessionAndVerify(
        ILivenessWithVerifySessionSyncCommands livenessCommands, CreateLivenessWithVerifySessionContent content, String path) {
        BinaryData imageData = path != null ? Utils.loadFromFile(path) : null;
        CreateLivenessWithVerifySessionResult result = livenessCommands.createLivenessWithVerifySessionSync(content, imageData);

        Assertions.assertNotNull(result);
        mSessionId = result.getSessionId();
        mCurrentCommand = livenessCommands;

        Assertions.assertNotNull(result.getSessionId());
        Assertions.assertNotNull(result.getAuthToken());

        LivenessWithVerifyImage verifyImage = result.getVerifyImage();
        if (null != path) {
            Assertions.assertNotNull(verifyImage);
            Assertions.assertNotNull(verifyImage.getFaceRectangle());
            Assertions.assertNotNull(verifyImage.getQualityForRecognition());
        } else {
            Assertions.assertNull(verifyImage);
        }

        return result;
    }
}
