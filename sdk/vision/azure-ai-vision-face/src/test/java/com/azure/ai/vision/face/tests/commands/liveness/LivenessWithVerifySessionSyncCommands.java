// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.liveness;

import com.azure.ai.vision.face.FaceSessionClient;
import com.azure.ai.vision.face.models.CreateLivenessSessionContent;
import com.azure.ai.vision.face.models.CreateLivenessWithVerifySessionResult;
import com.azure.ai.vision.face.models.LivenessWithVerifySession;
import com.azure.core.util.BinaryData;

class LivenessWithVerifySessionSyncCommands implements ILivenessWithVerifySessionSyncCommands {
    private final FaceSessionClient mSyncClient;

    LivenessWithVerifySessionSyncCommands(FaceSessionClient syncClient) {
        mSyncClient = syncClient;
    }

    public CreateLivenessWithVerifySessionResult createLivenessWithVerifySessionSync(
        CreateLivenessSessionContent content, BinaryData verifyImage) {
        return mSyncClient.createLivenessWithVerifySession(content, verifyImage);
    }

    @Override
    public LivenessWithVerifySession getLivenessWithVerifySessionResultSync(String sessionId) {
        return mSyncClient.getLivenessWithVerifySessionResult(sessionId);
    }

    @Override
    public void deleteLivenessWithVerifySessionSync(String sessionId) {
        mSyncClient.deleteLivenessWithVerifySession(sessionId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
