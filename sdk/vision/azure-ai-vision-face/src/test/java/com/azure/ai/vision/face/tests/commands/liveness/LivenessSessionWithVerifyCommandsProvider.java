// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.liveness;

import com.azure.ai.vision.face.FaceSessionAsyncClient;
import com.azure.ai.vision.face.FaceSessionClient;
import com.azure.ai.vision.face.tests.commands.CommandProvider;

import java.util.function.BiFunction;

public class LivenessSessionWithVerifyCommandsProvider
    extends CommandProvider<FaceSessionClient, FaceSessionAsyncClient, ILivenessWithVerifySessionSyncCommands> {

    public LivenessSessionWithVerifyCommandsProvider(
        String tag, BiFunction<FaceSessionClient, FaceSessionAsyncClient, ILivenessWithVerifySessionSyncCommands> creator) {
        super(tag, creator);
    }

    public static LivenessSessionWithVerifyCommandsProvider[] getFunctionProviders() {
        return new LivenessSessionWithVerifyCommandsProvider[] {
            new LivenessSessionWithVerifyCommandsProvider("Sync", (faceClient, faceAsyncClient) -> new LivenessWithVerifySessionSyncCommands(faceClient)),
            new LivenessSessionWithVerifyCommandsProvider("Async", (faceClient, faceAsyncClient) -> new LivenessWithVerifySessionAsyncCommands(faceAsyncClient)),
        };
    }
}
