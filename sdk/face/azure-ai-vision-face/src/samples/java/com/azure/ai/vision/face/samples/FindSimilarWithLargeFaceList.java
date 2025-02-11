// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.administration.FaceAdministrationClientBuilder;
import com.azure.ai.vision.face.administration.LargeFaceListClient;
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.FaceTrainingResult;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceFindSimilarResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.models.FindSimilarMatchMode;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.List;

import static com.azure.ai.vision.face.samples.utils.Utils.log;

public class FindSimilarWithLargeFaceList {
    public static void main(String[] args) {
        //Create LargeFaceList
        String largeFaceListId = "lfl01";

        //Create LargeFaceListClient
        LargeFaceListClient largeFaceListClient = new FaceAdministrationClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient()
            .getLargeFaceListClient(largeFaceListId);

        
        largeFaceListClient.create("List of Face", "Large Face List for Test", FaceRecognitionModel.RECOGNITION_04);

        try {
            addFaceToLargeFaceListAndTrain(largeFaceListClient);

            //Create client to run Detect and FindSimilar operations
            FaceClient client = new FaceClientBuilder()
                .endpoint(ConfigurationHelper.getEndpoint())
                .credential(new DefaultAzureCredentialBuilder().build())
                .buildClient();

            // Detect faces to find similar faces in above collection.
            // There are two different faces in this image.
            BinaryData imageBinary = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FINDSIMAR_SAMPLE);
            List<FaceDetectionResult> faceToFindSimilar = client.detect(
                imageBinary, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);

            for (FaceDetectionResult face : faceToFindSimilar) {
                List<FaceFindSimilarResult> results = client.findSimilarFromLargeFaceList(face.getFaceId(), largeFaceListId, 2, FindSimilarMatchMode.MATCH_FACE);
                for (FaceFindSimilarResult result : results) {
                    log("FindSimilar face for " + face.getFaceId() + ": " + result.getConfidence() + " with persistedFaceId: " + result.getPersistedFaceId());
                }
            }
        } finally {
            // Delete the LargeFaceList
            largeFaceListClient.delete();
        }
    }

    private static void addFaceToLargeFaceListAndTrain(LargeFaceListClient client) {
        log("Add face to LargeFaceList ... ");
        //Add six image to the LargeFaceList
        client.addFace(
            Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY1_MON1),
            null, FaceDetectionModel.DETECTION_02, "Lady1-1");

        client.addFace(
            Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY1_MON2),
            null, FaceDetectionModel.DETECTION_02, "Lady1-2");

        client.addFace(
            Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY2_LADY1),
            null, FaceDetectionModel.DETECTION_02, "Lady2-1");

        client.addFace(
            Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY2_LADY2),
            null, FaceDetectionModel.DETECTION_02, "Lady2-2");

        client.addFace(
            Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY3_LADY1),
            null, FaceDetectionModel.DETECTION_02, "Lady3-1");

        log("Done");

        log("Train LargeFaceList ... ");
        // We need to call /train to make all the newly added visible for the FindSimilar operation.
        SyncPoller<FaceTrainingResult, Void>  poller = client.beginTrain();
        poller.waitForCompletion();
        log("Done");
    }
}
