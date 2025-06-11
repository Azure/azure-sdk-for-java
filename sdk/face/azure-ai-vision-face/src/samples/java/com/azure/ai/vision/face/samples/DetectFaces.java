// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.ai.vision.face.models.DetectOptions;
import com.azure.ai.vision.face.models.FaceAttributeType;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.vision.face.samples.utils.Utils.log;

public class DetectFaces {

    public static void main(String[] args) {
        FaceClient client = new FaceClientBuilder()
                .endpoint(ConfigurationHelper.getEndpoint())
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

        BinaryData imageBinary = BinaryData.fromFile(FileSystems.getDefault().getPath(Resources.TEST_IMAGE_PATH_DETECT_SAMPLE_IMAGE));
        List<FaceDetectionResult> detectionResults = client.detect(
                imageBinary,
                FaceDetectionModel.DETECTION_03,
                FaceRecognitionModel.RECOGNITION_04,
                Boolean.TRUE,
                Arrays.asList(
                        FaceAttributeType.HEAD_POSE,
                        FaceAttributeType.MASK,
                        FaceAttributeType.BLUR,
                        FaceAttributeType.QUALITY_FOR_RECOGNITION),
                Boolean.FALSE,
                Boolean.TRUE,
                Integer.valueOf(120));

        detectionResults.forEach(face -> log("Detected Face by file:" + Utils.toString(face) + "\n"));

        DetectOptions options = new DetectOptions(FaceDetectionModel.DETECTION_01, FaceRecognitionModel.RECOGNITION_04, false)
            .setReturnFaceAttributes(Arrays.asList(FaceAttributeType.ACCESSORIES, FaceAttributeType.GLASSES, FaceAttributeType.EXPOSURE, FaceAttributeType.NOISE))
            .setReturnFaceLandmarks(true);
        detectionResults = client.detect(Resources.TEST_IMAGE_URL_DETECT_SAMPLE, options);
        detectionResults.forEach(face -> log("Detected Faces from URL:" + Utils.toString(face) + "\n"));
    }
}
