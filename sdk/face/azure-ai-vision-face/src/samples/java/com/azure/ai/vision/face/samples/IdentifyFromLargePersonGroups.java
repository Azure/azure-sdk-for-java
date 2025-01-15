// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.administration.FaceAdministrationClientBuilder;
import com.azure.ai.vision.face.administration.LargePersonGroupClient;
import com.azure.ai.vision.face.models.CreatePersonResult;
import com.azure.ai.vision.face.models.FaceTrainingResult;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceIdentificationCandidate;
import com.azure.ai.vision.face.models.FaceIdentificationResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.models.FaceVerificationResult;
import com.azure.ai.vision.face.models.LargePersonGroupPerson;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Collections;
import java.util.List;

import static com.azure.ai.vision.face.samples.utils.Utils.log;

public class IdentifyFromLargePersonGroups {
    public static void main(String[] args) {
        String largePersonGroupId = "lpg_family1";
        //Create LargePersonGroupClient
        LargePersonGroupClient largePersonGroupClient = new FaceAdministrationClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient()
            .getLargePersonGroupClient(largePersonGroupId);

        largePersonGroupClient.create(
            "Family 1", "A sweet family", FaceRecognitionModel.RECOGNITION_04);
        try {
            //Add 3 Persons with 2 faces for each of them to LargePersonGroup
            createThreePersonInLargePersonGroupAndTrain(largePersonGroupClient);

            //Create client to run Detect and Verify and Identify operations
            FaceClient client = createFaceClient();

            // Detect a face to execute Verify and Identify operations
            final String faceIdBill = detectOneFaceFromImage(client, Resources.TEST_IMAGE_PATH_FAMILY1_DAD3);

            // Get all Persons in LargePersonGroup
            List<LargePersonGroupPerson> persons =
                largePersonGroupClient.getPersons();

            persons.forEach(person -> {
                // Verify if the face belongs to any one of the Person.
                FaceVerificationResult verificationResult = client.verifyFromLargePersonGroup(
                    faceIdBill, largePersonGroupId, person.getPersonId());
                log("Verify if the face belongs to " + person.getName() + ", result: "
                    + verificationResult.isIdentical() + ", confidence:" + verificationResult.getConfidence());
            });

            // Call Identify to find out this face belong to whom
            log("Identify the first face");
            identifyIfFaceBelongsToAPerson(client, largePersonGroupClient, largePersonGroupId, faceIdBill);

            // Detect another faces to execute Verify and Identify operations
            String faceIdGill = detectOneFaceFromImage(client, Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER3);

            // Call Identify to again with another face, we expect 'No such person'.
            log("Identify the second face");
            identifyIfFaceBelongsToAPerson(client, largePersonGroupClient, largePersonGroupId, faceIdGill);

            // Add another Person to whom the second face belongs to.
            createAnotherPersonInLargePersonGroupAndTrain(largePersonGroupClient);

            // Call Identify to again, we expect that we can find Gill this time.
            log("Identify the second face again");
            identifyIfFaceBelongsToAPerson(client, largePersonGroupClient, largePersonGroupId, faceIdGill);

        } finally {
            largePersonGroupClient.delete();
        }
    }

    private static String detectOneFaceFromImage(FaceClient client, String imagePath) {
        List<FaceDetectionResult> detectionResultList = client.detect(
            Utils.loadFromFile(imagePath), FaceDetectionModel.DETECTION_03,
            FaceRecognitionModel.RECOGNITION_04, true);
        FaceDetectionResult result = detectionResultList.get(0);
        return result.getFaceId();
    }

    private static void identifyIfFaceBelongsToAPerson(
        FaceClient client, LargePersonGroupClient largePersonGroupClient, String largePersonGroupId, String faceId) {
        List<FaceIdentificationResult> identificationResults = client.identifyFromLargePersonGroup(
            Collections.singletonList(faceId), largePersonGroupId, 1, null);

        List<FaceIdentificationCandidate> candidates = identificationResults.get(0).getCandidates();
        if (!candidates.isEmpty()) {
            FaceIdentificationCandidate candidate = candidates.get(0);
            // Query Person Data
            LargePersonGroupPerson peron = largePersonGroupClient.getPerson(candidate.getPersonId());
            log("The face belongs to " + peron.getName() + ", confidence: " + candidate.getConfidence());
        } else {
            log("No such person");
        }
    }

    private static void createThreePersonInLargePersonGroupAndTrain(
        LargePersonGroupClient client) {

        log("Add 3 Persons with 2 faces for each of them to LargePersonGroup... ");
        //Add 3 People with 2 faces from two different images to the LargePersonGroup
        createPersonToLargePersonGroupAndAddFaces(
            client, "Bill", "Dad",
            Resources.TEST_IMAGE_PATH_FAMILY1_DAD1, Resources.TEST_IMAGE_PATH_FAMILY1_DAD2);

        createPersonToLargePersonGroupAndAddFaces(
            client, "Clare", "Mon",
            Resources.TEST_IMAGE_PATH_FAMILY1_MON1, Resources.TEST_IMAGE_PATH_FAMILY1_MON2);

        createPersonToLargePersonGroupAndAddFaces(
            client, "Ron", "Son",
            Resources.TEST_IMAGE_PATH_FAMILY1_SON1, Resources.TEST_IMAGE_PATH_FAMILY1_SON2);
        log("Done");

        log("Train LargePersonGroup ... ");
        // We need to call /train to make all the newly added visible for Identify operation.
        SyncPoller<FaceTrainingResult, Void> poller = client.beginTrain();
        poller.waitForCompletion();
        log("Done");
    }

    private static void createAnotherPersonInLargePersonGroupAndTrain(
        LargePersonGroupClient client) {

        log("Add another Person to LargePersonGroup and add faces to the Person... ");
        //Add 3 People with 2 faces from two different images to the LargePersonGroup
        createPersonToLargePersonGroupAndAddFaces(
            client, "Gill", "Daughter",
            Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER1, Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER2);
        log("Done");

        log("Train LargePersonGroup ... ");
        // We need to call /train to make all the newly added visible for Identify operation.
        SyncPoller<FaceTrainingResult, Void> poller = client.beginTrain();
        poller.waitForCompletion();
        log("Done");
    }

    private static void createPersonToLargePersonGroupAndAddFaces(LargePersonGroupClient client,
        String name, String userData, String... paths) {
        CreatePersonResult resultForPersonCreation = client.createPerson(name, userData);
        String personId = resultForPersonCreation.getPersonId();

        int index = 0;
        for (String path : paths) {
            ++index;
            client.addFace(personId, Utils.loadFromFile(path), null, FaceDetectionModel.DETECTION_03,
                userData + "-" + index);
        }
    }

    private static FaceClient createFaceClient() {
        return new FaceClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }
}
