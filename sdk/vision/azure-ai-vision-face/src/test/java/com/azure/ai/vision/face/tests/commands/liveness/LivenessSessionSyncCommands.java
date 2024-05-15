// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.liveness;

import com.azure.ai.vision.face.FaceSessionClient;
import com.azure.ai.vision.face.models.CreateLivenessSessionContent;
import com.azure.ai.vision.face.models.CreateLivenessSessionResult;
import com.azure.ai.vision.face.models.LivenessSession;


class LivenessSessionSyncCommands implements ILivenessSessionSyncCommands {
    private final FaceSessionClient mSyncClient;

    LivenessSessionSyncCommands(FaceSessionClient faceSessionClient) {
        this.mSyncClient = faceSessionClient;
    }

    public CreateLivenessSessionResult createLivenessSessionSync(CreateLivenessSessionContent content) {
        return mSyncClient.createLivenessSession(content);
    }

    @Override
    public LivenessSession getLivenessSessionResultSync(String sessionId) {
        return mSyncClient.getLivenessSessionResult(sessionId);
    }

    @Override
    public void deleteLivenessSessionSync(String sessionId) {
        mSyncClient.deleteLivenessSession(sessionId);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
