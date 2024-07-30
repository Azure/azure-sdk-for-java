// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceGroupingResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;

import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.vision.face.samples.utils.Utils.log;
import static com.azure.ai.vision.face.samples.utils.Utils.logObject;

public class Grouping {
    public static void main(String[] args) {
        FaceClient client = new FaceClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildClient();

        // Detect faces in an image.
        // There are nine faces in the image, divided among three individuals, with each person having three faces.
        // Note that we can collect the face IDs from different images then group them in real application. We don't need
        // to put all faces in an image.
        BinaryData imageBinary = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_NINE_FACES);
        List<FaceDetectionResult> results =
            client.detect(imageBinary, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);
        log("Detected faces: " + results.size());

        // Group the faces in the image.
        List<String> faceIds = results.stream().
            map(FaceDetectionResult::getFaceId)
            .collect(Collectors.toList());

        FaceGroupingResult groupingResult = client.group(faceIds);
        logObject("Grouping Result: ", groupingResult, true);
    }
}
