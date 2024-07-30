// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.liveness;

import com.azure.ai.vision.face.FaceSessionAsyncClient;
import com.azure.ai.vision.face.FaceSessionClient;
import com.azure.ai.vision.face.tests.commands.CommandProvider;

import java.util.function.BiFunction;

public final class LivenessSessionCommandsProvider
    extends CommandProvider<FaceSessionClient, FaceSessionAsyncClient, ILivenessSessionSyncCommands> {

    public LivenessSessionCommandsProvider(
        String tag, BiFunction<FaceSessionClient, FaceSessionAsyncClient, ILivenessSessionSyncCommands> creator) {
        super(tag, creator);
    }

    public static LivenessSessionCommandsProvider[] getFunctionProviders() {
        return new LivenessSessionCommandsProvider[] {
            new LivenessSessionCommandsProvider("Sync", (faceClient, faceAsyncClient) -> new LivenessSessionSyncCommands(faceClient)),
            new LivenessSessionCommandsProvider("Async", (faceClient, faceAsyncClient) -> new LivenessSessionAsyncCommands(faceAsyncClient)),
        };
    }
}
