// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceAdministrationClient;
import com.azure.ai.vision.face.FaceAdministrationClientBuilder;
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceIdentificationResult;
import com.azure.ai.vision.face.models.FaceOperationResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.models.PersonDirectoryFace;
import com.azure.ai.vision.face.models.PersonDirectoryPerson;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.vision.face.samples.utils.Utils.log;
import static com.azure.ai.vision.face.samples.utils.Utils.safelyRun;

public class IdentifyPersonAmongPersons {
    public static void main(String[] args) {
        //Create client to run detect and identify operations
        FaceClient client = new FaceClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildClient();

        //Create FaceAdministrationClient to create Persons later.
        FaceAdministrationClient administrationClient = new FaceAdministrationClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildClient();

        List<String> personIds = new ArrayList<>();

        try {
            // Create Persons
            createPersons(administrationClient, personIds);

            // Detect an image
            String faceId = detectImage(client);

            // Identify if the image belongs to one of the Persons
            List<FaceIdentificationResult> result = identifyAmongPersons(client, faceId, personIds);
            Utils.logObject("Identify result: ", result);
        } finally {
            // Delete All the Persons
            deletePersons(administrationClient, personIds);
        }
    }

    private static void createPersons(FaceAdministrationClient administrationClient, List<String> personIds) {
        List<SyncPoller<FaceOperationResult, PersonDirectoryFace>> list = new ArrayList<>();

        createPersonAndAddFace(administrationClient, personIds, list,
            "Ron", "Family1", Resources.TEST_IMAGE_PATH_FAMILY1_SON1,
            Resources.TEST_IMAGE_PATH_FAMILY1_SON2);

        createPersonAndAddFace(administrationClient, personIds, list,
            "Gill", "Family1", Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER1,
            Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER2);

        createPersonAndAddFace(administrationClient, personIds, list,
            "Anna", "Family2,Singing", Resources.TEST_IMAGE_PATH_FAMILY2_LADY1,
            Resources.TEST_IMAGE_PATH_FAMILY2_LADY2);

        log("Waiting for all the operation to add face to person complete ... ");
        list.forEach(poller -> poller
            .setPollInterval(Duration.ofSeconds(1))
            .waitForCompletion(Duration.ofSeconds(60)));
        log("Done");
    }

    private static void createPersonAndAddFace(
        FaceAdministrationClient client,
        List<String> personIds, List<SyncPoller<FaceOperationResult, PersonDirectoryFace>> pollers,
        String name, String userDataForPersons,
        String imagePath1, String imagePath2) {
        log("Create Person " + name);
        // Create Persons
        // Create the first person
        SyncPoller<FaceOperationResult, PersonDirectoryPerson> syncPoller =
            client.beginCreatePerson(name, userDataForPersons);

        PersonDirectoryPerson person = syncPoller
            .setPollInterval(Duration.ofSeconds(1))
            .getFinalResult(Duration.ofSeconds(60));

        String personId = person.getPersonId();
        personIds.add(personId);

        // Add face to first person, this is an async operation, we should wait for it to complete
        // However, we want to add two images to this person, we can only wait for the 2nd operation
        // complete and ignore the Poller of the first one.
        BinaryData imageData = Utils.loadFromFile(imagePath1);
        //Per above description, we don't wait for this request.
        client.beginAddPersonFace(
            personId, FaceRecognitionModel.RECOGNITION_04, imageData, null,
            FaceDetectionModel.DETECTION_03, "User 0001");

        imageData = Utils.loadFromFile(imagePath2);
        // We need to wait for this operation complete, we return the poller and wait for it to complete later.
        SyncPoller<FaceOperationResult, PersonDirectoryFace> poller =  client.beginAddPersonFace(
            personId, FaceRecognitionModel.RECOGNITION_04, imageData, null,
            FaceDetectionModel.DETECTION_03, "User 0002");

        pollers.add(poller);
    }

    private static String detectImage(FaceClient client) {
        BinaryData imageData = Utils.loadFromFile(Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER3);
        List<FaceDetectionResult> results = client.detect(
            imageData, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);

        FaceDetectionResult result = results.get(0);

        return result.getFaceId();
    }

    private static List<FaceIdentificationResult> identifyAmongPersons(FaceClient client, String faceId, List<String> personIds) {
        return client.identifyFromPersonDirectory(Collections.singletonList(faceId), personIds);
    }

    private static void deletePersons(FaceAdministrationClient administrationClient, List<String> personIds) {
        log("Deleting Result ...");
        List<SyncPoller<FaceOperationResult, Void>> pollers = personIds.stream()
            .map(id -> Utils.safelyRun(() -> administrationClient.beginDeletePerson(id)))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        pollers.forEach(poller -> safelyRun(poller::waitForCompletion));
        log("Done");
    }
}
