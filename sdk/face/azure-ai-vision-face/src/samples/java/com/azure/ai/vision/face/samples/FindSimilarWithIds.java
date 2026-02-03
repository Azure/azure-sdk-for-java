// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceFindSimilarResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.models.FindSimilarMatchMode;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.util.BinaryData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.List;
import java.util.stream.Collectors;


public class FindSimilarWithIds {
    public static void main(String[] args) {
        FaceClient client = new FaceClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Detect faces in an image.
        // There are nine faces in the image, divided among three individuals, with each person having three faces.
        // Note that we can collect the face IDs from different images for FindSimilar Operation in real application.
        // We don't need to put all faces in an image.
        BinaryData imageBinary = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_NINE_FACES);
        List<FaceDetectionResult> results = client.detect(
            imageBinary, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);
        List<String> detectedFaces = results.stream()
            .map(FaceDetectionResult::getFaceId)
            .collect(Collectors.toList());

        // Detect faces to find similar faces in above collection.
        // There are two different faces in this image.
        imageBinary = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FINDSIMAR_SAMPLE);
        List<FaceDetectionResult> faceToFindSimilar = client.detect(
            imageBinary, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);

        faceToFindSimilar.forEach(face -> {
            // Call FindSimilar for each face.
            List<FaceFindSimilarResult> similarFaces = client.findSimilar(face.getFaceId(), detectedFaces, 9, FindSimilarMatchMode.MATCH_PERSON);
            Utils.logObject("FindSimilar faces for " + face.getFaceId(), similarFaces, true);
        });
    }
}
