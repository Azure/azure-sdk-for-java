// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.models.FaceVerificationResult;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;

import java.util.List;

public class StatelessVerification {

    public static void main(String[] args) {
        //Create client to run detect and identify operations
        FaceClient client = new FaceClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildClient();

        //Detect a face
        BinaryData imageData = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY1_DAD1);
        List<FaceDetectionResult> detectionResult = client.detect(
            imageData, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);
        String id1 = detectionResult.get(0).getFaceId();

        //Detect another we want to verify
        imageData = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY1_DAD2);
        detectionResult = client.detect(
                imageData, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);
        String id2 = detectionResult.get(0).getFaceId();

        //Verify if the two faces are the same
        FaceVerificationResult result = client.verifyFaceToFace(id1, id2);
        Utils.logObject("Verification: ", result);
    }
}
