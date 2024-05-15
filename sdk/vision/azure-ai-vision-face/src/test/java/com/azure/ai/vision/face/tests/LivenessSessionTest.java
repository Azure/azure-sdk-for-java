// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests;

import com.azure.ai.vision.face.FaceServiceVersion;
import com.azure.ai.vision.face.FaceSessionAsyncClient;
import com.azure.ai.vision.face.FaceSessionClient;
import com.azure.ai.vision.face.models.CreateLivenessSessionContent;
import com.azure.ai.vision.face.models.CreateLivenessSessionResult;
import com.azure.ai.vision.face.models.LivenessOperationMode;
import com.azure.ai.vision.face.models.LivenessSession;
import com.azure.ai.vision.face.tests.commands.liveness.ILivenessSessionSyncCommands;
import com.azure.ai.vision.face.tests.commands.liveness.LivenessSessionCommandsProvider;
import com.azure.ai.vision.face.tests.utils.FaceDisplayNameGenerator;
import com.azure.ai.vision.face.tests.utils.TestUtils;
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
public class LivenessSessionTest extends FaceClientTestBase {
    private ILivenessSessionSyncCommands mCurrentCommand;
    private String mSessionId;

    @ParameterizedTest
    @MethodSource("getTestCommands")
    public void testCreateSession(String httpClientName, FaceServiceVersion serviceVersion,
            Supplier<ILivenessSessionSyncCommands> commandProvider) {
        String uuid = UUID.randomUUID().toString();
        CreateLivenessSessionContent content = new CreateLivenessSessionContent(LivenessOperationMode.PASSIVE)
            .setDeviceCorrelationId(uuid);
        createSessionAndVerify(commandProvider.get(), content);
    }

    @ParameterizedTest
    @MethodSource("getTestCommands")
    public void testCreateSessionDeviceIdOptional(String httpClientName, FaceServiceVersion serviceVersion,
            Supplier<ILivenessSessionSyncCommands> commandProvider) {
        CreateLivenessSessionContent content = new CreateLivenessSessionContent(LivenessOperationMode.PASSIVE)
            .setDeviceCorrelationIdSetInClient(true);
        createSessionAndVerify(commandProvider.get(), content);
    }

    @ParameterizedTest
    @MethodSource("getTestCommands")
    public void testCreateSessionWithTokenTTL(
            String httpClientName, FaceServiceVersion serviceVersion, Supplier<ILivenessSessionSyncCommands> commandProvider) {
        ILivenessSessionSyncCommands livenessCommands = commandProvider.get();

        int authTokenTimeToLiveInSeconds = 60;
        String uuid = UUID.randomUUID().toString();
        CreateLivenessSessionContent content = new CreateLivenessSessionContent(LivenessOperationMode.PASSIVE)
            .setDeviceCorrelationId(uuid)
            .setAuthTokenTimeToLiveInSeconds(authTokenTimeToLiveInSeconds);

        CreateLivenessSessionResult result = createSessionAndVerify(livenessCommands, content);
        LivenessSession livenessSession = livenessCommands.getLivenessSessionResultSync(result.getSessionId());
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
            mCurrentCommand.deleteLivenessSessionSync(sessionId);
        }
    }

    private Stream<Arguments> getTestCommands() {
        LivenessSessionCommandsProvider[] providers =  LivenessSessionCommandsProvider.getFunctionProviders();

        Stream<Triple<String, FaceServiceVersion, Supplier<ILivenessSessionSyncCommands>>> clientArumentStream =
                createClientArgumentStream(FaceSessionClient.class, FaceSessionAsyncClient.class, providers);

        return TestUtils.createCombinationWithClientArguments(clientArumentStream);
    }

    private CreateLivenessSessionResult createSessionAndVerify(
            ILivenessSessionSyncCommands livenessCommands, CreateLivenessSessionContent content) {
        CreateLivenessSessionResult result = livenessCommands.createLivenessSessionSync(content);

        mCurrentCommand = livenessCommands;
        mSessionId = result.getSessionId();

        Assertions.assertNotNull(result);
        Assertions.assertNotNull(result.getSessionId());
        Assertions.assertNotNull(result.getAuthToken());

        return result;
    }
}
