// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.liveness;

import com.azure.ai.vision.face.models.CreateLivenessWithVerifySessionJsonContent;
import com.azure.ai.vision.face.models.CreateLivenessWithVerifySessionResult;
import com.azure.ai.vision.face.models.LivenessWithVerifySession;
import com.azure.core.util.BinaryData;

public interface ILivenessWithVerifySessionSyncCommands {
    CreateLivenessWithVerifySessionResult createLivenessWithVerifySessionSync(
        CreateLivenessWithVerifySessionJsonContent content, BinaryData verifyImage);
    LivenessWithVerifySession getLivenessWithVerifySessionResultSync(String sessionId);
    void deleteLivenessWithVerifySessionSync(String sessionId);
}
