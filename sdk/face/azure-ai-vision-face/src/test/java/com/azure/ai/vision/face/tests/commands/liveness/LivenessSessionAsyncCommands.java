// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.liveness;

import com.azure.ai.vision.face.FaceSessionAsyncClient;
import com.azure.ai.vision.face.models.CreateLivenessSessionContent;
import com.azure.ai.vision.face.models.CreateLivenessSessionResult;
import com.azure.ai.vision.face.models.LivenessSession;
import com.azure.ai.vision.face.tests.function.FunctionUtils;
import reactor.core.publisher.Mono;

class LivenessSessionAsyncCommands implements ILivenessSessionSyncCommands {
    private final FaceSessionAsyncClient mAsyncClient;

    LivenessSessionAsyncCommands(FaceSessionAsyncClient faceSessionClient) {
        this.mAsyncClient = faceSessionClient;
    }

    public Mono<LivenessSession> getLivenessSessionResult(String sessionId) {
        return mAsyncClient.getLivenessSessionResult(sessionId);
    }

    public Mono<CreateLivenessSessionResult> createLivenessSession(CreateLivenessSessionContent content) {
        return mAsyncClient.createLivenessSession(content);
    }

    public Mono<Void> deleteLivenessSession(String sessionId) {
        return mAsyncClient.deleteLivenessSession(sessionId);
    }

    @Override
    public CreateLivenessSessionResult createLivenessSessionSync(CreateLivenessSessionContent createLivenessSessionContent) {
        return FunctionUtils.callAndAwait(() -> createLivenessSession(createLivenessSessionContent));
    }

    @Override
    public LivenessSession getLivenessSessionResultSync(String sessionId) {
        return FunctionUtils.callAndAwait(() -> getLivenessSessionResult(sessionId));
    }

    @Override
    public void deleteLivenessSessionSync(String sessionId) {
        FunctionUtils.callAndAwait(() -> deleteLivenessSession(sessionId));
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
