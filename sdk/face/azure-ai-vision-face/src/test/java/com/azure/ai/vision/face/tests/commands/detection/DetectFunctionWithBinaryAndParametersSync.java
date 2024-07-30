// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands.detection;

import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.models.FaceAttributeType;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.util.BinaryData;

import java.util.List;

class DetectFunctionWithBinaryAndParametersSync extends DetectSyncFunction {
    private final FaceClient mFaceClient;
    private final BinaryData mImageContent;

    DetectFunctionWithBinaryAndParametersSync(FaceClient faceClient, String path) {
        this.mFaceClient = faceClient;
        this.mImageContent = Utils.loadFromFile(path);
    }

    @Override
    public List<FaceDetectionResult> execute(
        FaceDetectionModel detectionModel, FaceRecognitionModel recognitionModel, boolean returnFaceId,
        List<FaceAttributeType> returnFaceAttributes, Boolean returnFaceLandmarks, Boolean returnRecognitionModel,
        Integer faceIdTimeToLive) {
        return mFaceClient.detect(mImageContent, detectionModel, recognitionModel, returnFaceId,
            returnFaceAttributes, returnFaceLandmarks, returnRecognitionModel, faceIdTimeToLive);
    }
}
