// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.ai.vision.face.models.DetectOptions;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;

import java.nio.file.FileSystems;
import java.util.Arrays;
import java.util.List;

import static com.azure.ai.vision.face.samples.utils.Utils.log;
import static com.azure.ai.vision.face.models.FaceAttributeType.ModelDetection01;
import static com.azure.ai.vision.face.models.FaceAttributeType.ModelDetection03;
import static com.azure.ai.vision.face.models.FaceAttributeType.ModelRecognition04;

public class DetectFaces {
    public static void main(String[] args) {
        FaceClient client = new FaceClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildClient();

        BinaryData imageBinary = BinaryData.fromFile(FileSystems.getDefault().getPath(Resources.TEST_IMAGE_PATH_DETECT_SAMPLE_IMAGE));
        List<FaceDetectionResult> detectResult = client.detect(
            imageBinary,
            FaceDetectionModel.DETECTION_03,
            FaceRecognitionModel.RECOGNITION_04,
            true,
            Arrays.asList(ModelDetection03.HEAD_POSE, ModelDetection03.MASK, ModelDetection03.BLUR, ModelRecognition04.QUALITY_FOR_RECOGNITION),
            false,
            true,
            120);

        detectResult.forEach(face -> log("Detected Face by file:" + Utils.toString(face) + "\n"));

        DetectOptions options = new DetectOptions(FaceDetectionModel.DETECTION_01, FaceRecognitionModel.RECOGNITION_04, false)
            .setReturnFaceAttributes(Arrays.asList(ModelDetection01.ACCESSORIES, ModelDetection01.GLASSES, ModelDetection01.EXPOSURE, ModelDetection01.NOISE))
            .setReturnFaceLandmarks(true);

        detectResult = client.detect(Resources.TEST_IMAGE_URL_DETECT_SAMPLE, options);
        detectResult.forEach(face -> log("Detected Faces from URL:" + Utils.toString(face) + "\n"));
    }
}
