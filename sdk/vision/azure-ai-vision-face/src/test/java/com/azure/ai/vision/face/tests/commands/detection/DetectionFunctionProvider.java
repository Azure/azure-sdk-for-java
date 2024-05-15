// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.detection;

import com.azure.ai.vision.face.FaceAsyncClient;
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.tests.commands.CommandProvider;

import java.util.function.BiFunction;

public class DetectionFunctionProvider extends CommandProvider<FaceClient, FaceAsyncClient, DetectSyncFunction> {
    public DetectionFunctionProvider(String tag, BiFunction<FaceClient, FaceAsyncClient, DetectSyncFunction> creator) {
        super(tag, creator);
    }

    public static DetectionFunctionProvider[] getFunctionProviders(String path, String url) {
        return new DetectionFunctionProvider[] {
            new DetectionFunctionProvider("BinaryAndParametersSync", (faceClient, faceAsyncClient) -> new DetectFunctionWithBinaryAndParametersSync(faceClient, path)),
            new DetectionFunctionProvider("BinaryAndDetectOptionSync", (faceClient, faceAsyncClient) -> new DetectFunctionWithBinaryAndDetectOptionSync(faceClient, path)),
            new DetectionFunctionProvider("UrlAndParametersSync", (faceClient, faceAsyncClient) -> new DetectFunctionWithUrlAndParametersSync(faceClient, url)),
            new DetectionFunctionProvider("UrlAndDetectOptionSync", (faceClient, faceAsyncClient) -> new DetectFunctionWithUrlAndDetectOptionSync(faceClient, url)),
            new DetectionFunctionProvider("BinaryAndParametersAsync", (faceClient, faceAsyncClient) -> new DetectFunctionWithBinaryAndParametersAsync(faceAsyncClient, path)),
            new DetectionFunctionProvider("BinaryAndDetectOptionAsync", (faceClient, faceAsyncClient) -> new DetectFunctionWithBinaryAndDetectOptionAsync(faceAsyncClient, path)),
            new DetectionFunctionProvider("UrlAndParametersAsync", (faceClient, faceAsyncClient) -> new DetectFunctionWithUrlAndParametersAsync(faceAsyncClient, url)),
            new DetectionFunctionProvider("UrlAndDetectOptionAsync", (faceClient, faceAsyncClient) -> new DetectFunctionWithUrlAndDetectOptionAsync(faceAsyncClient, url))
        };
    }
}
