// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.liveness;

import com.azure.ai.vision.face.models.CreateLivenessSessionContent;
import com.azure.ai.vision.face.models.CreateLivenessSessionResult;
import com.azure.ai.vision.face.models.LivenessSession;

public interface ILivenessSessionSyncCommands {
    CreateLivenessSessionResult createLivenessSessionSync(CreateLivenessSessionContent content);
    LivenessSession getLivenessSessionResultSync(String sessionId);
    void deleteLivenessSessionSync(String sessionId);
}
