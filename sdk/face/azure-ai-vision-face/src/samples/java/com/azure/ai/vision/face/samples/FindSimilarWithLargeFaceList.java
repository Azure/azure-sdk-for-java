// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceAdministrationClient;
import com.azure.ai.vision.face.FaceAdministrationClientBuilder;
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.FaceCollectionTrainingResult;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.models.FindSimilarMatchMode;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.nimbusds.jose.util.Pair;

import java.util.List;

import static com.azure.ai.vision.face.samples.utils.Utils.log;
import static com.azure.ai.vision.face.samples.utils.Utils.logObject;

public class FindSimilarWithLargeFaceList {
    public static void main(String[] args) {
        //Create FaceAdministrationClient to create LargeFaceList.
        FaceAdministrationClient administrationClient = new FaceAdministrationClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildClient();

        //Create LargeFaceList
        String largeFaceListId = "lfl01";
        administrationClient.createLargeFaceList(
            largeFaceListId, "List of Face", "Large Face List for Test", FaceRecognitionModel.RECOGNITION_04);

        try {
            addFaceToLargeFaceListAndTrain(largeFaceListId, administrationClient);

            //Create client to run Detect and FindSimilar operations
            FaceClient client = new FaceClientBuilder()
                .endpoint(ConfigurationHelper.getEndpoint())
                .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
                .buildClient();

            // Detect faces to find similar faces in above collection.
            // There are two different faces in this image.
            BinaryData imageBinary = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FINDSIMAR_SAMPLE);
            List<FaceDetectionResult> faceToFindSimilar = client.detect(
                imageBinary, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);

            faceToFindSimilar.stream()
                .map(face -> Pair.of(face,
                    // Call FindSimilar for each face.
                    client.findSimilarFromLargeFaceList(
                        face.getFaceId(), largeFaceListId, 2, FindSimilarMatchMode.MATCH_FACE)))
                .forEach(result -> logObject("FindSimilar faces for " + result.getLeft().getFaceId() + ": ", result.getRight(), true));
        } finally {
            // Delete the LargeFaceList
            administrationClient.deleteLargeFaceList(largeFaceListId);
        }
    }

    private static void addFaceToLargeFaceListAndTrain(String largeFaceListId, FaceAdministrationClient administrationClient) {
        log("Add face to LargeFaceList ... ");
        //Add six image to the LargeFaceList
        administrationClient.addLargeFaceListFace(
            largeFaceListId, Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY1_MON1),
            null, FaceDetectionModel.DETECTION_02, "Lady1-1");

        administrationClient.addLargeFaceListFace(
            largeFaceListId, Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY1_MON2),
            null, FaceDetectionModel.DETECTION_02, "Lady1-2");

        administrationClient.addLargeFaceListFace(
            largeFaceListId, Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY2_LADY1),
            null, FaceDetectionModel.DETECTION_02, "Lady2-1");

        administrationClient.addLargeFaceListFace(
            largeFaceListId, Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY2_LADY2),
            null, FaceDetectionModel.DETECTION_02, "Lady2-2");

        administrationClient.addLargeFaceListFace(
            largeFaceListId, Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY3_LADY1),
            null, FaceDetectionModel.DETECTION_02, "Lady3-1");

        log("Done");

        log("Train LargeFaceList ... ");
        // We need to call /train to make all the newly added visible for the FindSimilar operation.
        SyncPoller<FaceCollectionTrainingResult, Void>  poller = administrationClient.beginTrainLargeFaceList(largeFaceListId);
        poller.waitForCompletion();
        log("Done");
    }
}
