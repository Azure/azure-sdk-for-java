// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.detection;

import com.azure.ai.vision.face.FaceAsyncClient;
import com.azure.ai.vision.face.models.DetectOptions;
import com.azure.ai.vision.face.models.FaceAttributeType;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.util.BinaryData;
import reactor.core.publisher.Mono;

import java.util.List;

class DetectFunctionWithBinaryAndDetectOptionAsync extends DetectAsyncFunction {
    private final FaceAsyncClient mFaceAsyncClient;
    private final BinaryData mImageContent;

    DetectFunctionWithBinaryAndDetectOptionAsync(FaceAsyncClient faceClient, String path) {
        this.mFaceAsyncClient = faceClient;
        this.mImageContent = Utils.loadFromFile(path);
    }

    @Override
    public Mono<List<FaceDetectionResult>> executeAsync(
        FaceDetectionModel detectionModel, FaceRecognitionModel recognitionModel, boolean returnFaceId,
        List<FaceAttributeType> returnFaceAttributes, Boolean returnFaceLandmarks, Boolean returnRecognitionModel,
        Integer faceIdTimeToLive) {
        return mFaceAsyncClient.detect(mImageContent,
            new DetectOptions(detectionModel, recognitionModel, returnFaceId)
                .setReturnFaceAttributes(returnFaceAttributes)
                .setReturnFaceLandmarks(returnFaceLandmarks)
                .setReturnRecognitionModel(returnRecognitionModel)
                .setFaceIdTimeToLive(faceIdTimeToLive));
    }
}
