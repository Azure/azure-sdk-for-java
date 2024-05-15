// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.detection;

import com.azure.ai.vision.face.models.FaceAttributeType;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;

import java.util.List;

public abstract class DetectSyncFunction {
    public abstract List<FaceDetectionResult> execute(FaceDetectionModel detectionModel, FaceRecognitionModel recognitionModel,
        boolean returnFaceId, List<FaceAttributeType> returnFaceAttributes, Boolean returnFaceLandmarks, Boolean returnRecognitionModel,
        Integer faceIdTimeToLive);

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
