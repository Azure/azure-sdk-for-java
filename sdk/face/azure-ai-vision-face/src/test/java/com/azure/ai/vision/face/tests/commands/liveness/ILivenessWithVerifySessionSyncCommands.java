// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.liveness;

import com.azure.ai.vision.face.models.CreateLivenessWithVerifySessionContent;
import com.azure.ai.vision.face.models.LivenessWithVerifySession;

public interface ILivenessWithVerifySessionSyncCommands {
    LivenessWithVerifySession createLivenessWithVerifySessionSync(CreateLivenessWithVerifySessionContent content);

    LivenessWithVerifySession getLivenessWithVerifySessionResultSync(String sessionId);

    void deleteLivenessWithVerifySessionSync(String sessionId);
}
