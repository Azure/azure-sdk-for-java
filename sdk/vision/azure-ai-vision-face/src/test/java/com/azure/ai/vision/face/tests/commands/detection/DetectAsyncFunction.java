// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.detection;

import com.azure.ai.vision.face.models.FaceAttributeType;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import reactor.core.publisher.Mono;

import java.util.List;

public abstract class DetectAsyncFunction extends DetectSyncFunction {

    @Override
    public final List<FaceDetectionResult> execute(
        FaceDetectionModel detectionModel, FaceRecognitionModel recognitionModel, boolean returnFaceId,
        List<FaceAttributeType> returnFaceAttributes, Boolean returnFaceLandmarks, Boolean returnRecognitionModel,
        Integer faceIdTimeToLive) {
        Mono<List<FaceDetectionResult>> mono = executeAsync(detectionModel, recognitionModel, returnFaceId, returnFaceAttributes,
            returnFaceLandmarks, returnRecognitionModel, faceIdTimeToLive);

        return mono.block();
    }

    public abstract Mono<List<FaceDetectionResult>> executeAsync(
        FaceDetectionModel detectionModel, FaceRecognitionModel recognitionModel, boolean returnFaceId,
        List<FaceAttributeType> returnFaceAttributes, Boolean returnFaceLandmarks, Boolean returnRecognitionModel,
        Integer faceIdTimeToLive);
}
