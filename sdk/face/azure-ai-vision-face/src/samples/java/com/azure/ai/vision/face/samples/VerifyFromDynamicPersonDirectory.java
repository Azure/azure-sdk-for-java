// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceAdministrationClient;
import com.azure.ai.vision.face.FaceAdministrationClientBuilder;
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceOperationResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.models.FaceVerificationResult;
import com.azure.ai.vision.face.models.PersonDirectoryFace;
import com.azure.ai.vision.face.models.PersonDirectoryPerson;
import com.azure.ai.vision.face.samples.model.PersonData;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.ai.vision.face.samples.utils.Resources;
import com.azure.ai.vision.face.samples.utils.Utils;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.vision.face.samples.utils.Utils.log;
import static com.azure.ai.vision.face.samples.utils.Utils.safelyRun;

public class VerifyFromDynamicPersonDirectory {
    private static final RuntimeException[] EXCEPTION_CONTAINER = new RuntimeException[1];

    public static void main(String[] args) {
        //Create FaceAdministrationClient to create Persons and DynamicPersonDirectory.
        FaceAdministrationClient administrationClient = createAdministrationClient();
        //Create client to run Detect and Identify operations
        FaceClient client = createFaceClient();

        PersonData personDataBill = new PersonData("Bill", "Dad",
            new String[]{ Resources.TEST_IMAGE_PATH_FAMILY1_DAD1,  Resources.TEST_IMAGE_PATH_FAMILY1_DAD2});
        PersonData personDataRon = new PersonData("Ron", "Son",
            new String[]{ Resources.TEST_IMAGE_PATH_FAMILY1_SON1,  Resources.TEST_IMAGE_PATH_FAMILY1_SON2});
        List<PersonData> personDataList = Arrays.asList(personDataBill, personDataRon);

        try {
            createPersons(administrationClient, personDataList);
            String faceId = detectFaces(client, Resources.TEST_IMAGE_PATH_FAMILY1_DAD3);

            FaceVerificationResult result = client.verifyFromPersonDirectory(faceId, personDataBill.getPersonId());
            log("Verify if the face belongs to " + personDataBill.getName() + ", result:" + result.isIdentical()
                + ", confidence: " + result.getConfidence());

            result = client.verifyFromPersonDirectory(faceId, personDataRon.getPersonId());
            log("Verify if the face belongs to " + personDataRon.getName() + ", result:" + result.isIdentical()
                + ", confidence: " + result.getConfidence());

        } finally {
            // Delete All the Persons
            deletePersons(administrationClient, personDataList);
        }
    }

    private static String detectFaces(FaceClient client, String imagePath) {
        log("Detect a face");
        BinaryData imageData = Utils.loadFromFile(imagePath);
        List<FaceDetectionResult> results = client.detect(
            imageData, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);

        FaceDetectionResult result = results.get(0);
        return result.getFaceId();
    }

    private static void createPersons(FaceAdministrationClient administrationClient, List<PersonData> personDataList) {
        // Create Persons, this is an async operation, we don't wait for it to complete now.
        // Note that we store
        List<Pair<PersonData, SyncPoller<FaceOperationResult, PersonDirectoryPerson>>> createPersonPollers =
            personDataList.stream()
                .map(personData -> createPerson(administrationClient, personData))
                .collect(Collectors.toList());

        // Wait for the CreatePerson operations to complete. When the operation complete, we trigger AddFace
        // operations immediately but not wait for them to complete for the time being. We wait for them to complete
        // later when we are about to use them.
        List<SyncPoller<FaceOperationResult, PersonDirectoryFace>> addFacesPollers =
            waitForCreatePersonCompleteAndAddFaces(administrationClient, createPersonPollers);

        if (EXCEPTION_CONTAINER[0] != null) {
            throw EXCEPTION_CONTAINER[0];
        }

        // We are going to run Verify operation, so we wait for AddFace operations to complete now.
        // We don't need the result for now.
        addFacesPollers.forEach(poller -> {
            log("Wait for operations to add faces complete ...");
            PersonDirectoryFace face = poller
                .setPollInterval(Duration.ofSeconds(1))
                .getFinalResult(Duration.ofSeconds(60));
            log("Add face complete: " + face.getUserData());
        });
    }

    private static Pair<PersonData, SyncPoller<FaceOperationResult, PersonDirectoryPerson>> createPerson(
        FaceAdministrationClient administrationClient, PersonData personData) {
        log("Create Person: " + personData.getName());
        return Utils.safelyRunWithExceptionCheck(EXCEPTION_CONTAINER, "createPerson", () ->
            Pair.of(personData,
                administrationClient.beginCreatePerson(personData.getName(), personData.getUserData())));
    }

    private static List<SyncPoller<FaceOperationResult, PersonDirectoryFace>> waitForCreatePersonCompleteAndAddFaces(
        FaceAdministrationClient administrationClient,
        List<Pair<PersonData, SyncPoller<FaceOperationResult, PersonDirectoryPerson>>> createPersonDataPairs) {

        return createPersonDataPairs.stream().map(pair ->
                Utils.safelyRunWithExceptionCheck(EXCEPTION_CONTAINER, "waitForCreatePersonComplete",
                    () -> waitForCreatePersonComplete(pair)))
            .filter(Objects::nonNull)
            .map(personData -> addFacesToPersons(administrationClient, personData))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private static PersonData waitForCreatePersonComplete(
        Pair<PersonData, SyncPoller<FaceOperationResult, PersonDirectoryPerson>> pair) {
        return Utils.safelyRunWithExceptionCheck(EXCEPTION_CONTAINER, "waitForCreatePersonComplete", () -> {
            PersonData personData = pair.getLeft();
            PersonDirectoryPerson person = pair.getRight()
                .setPollInterval(Duration.ofSeconds(1))
                .getFinalResult(Duration.ofSeconds(60));
            personData.setPersonId(person.getPersonId());
            return personData;
        });
    }

    private static SyncPoller<FaceOperationResult, PersonDirectoryFace> addFacesToPersons(
        FaceAdministrationClient client, PersonData personData) {
        String name = personData.getName();
        log("Add faces to " + name);

        SyncPoller<FaceOperationResult, PersonDirectoryFace> poller = null;
        // AddPersonFace is an async operation, we can send couples of them and only wait for the last request to
        // complete
        String[] imagePaths = personData.getImagePaths();
        String personId = personData.getPersonId();
        for (int i = 0; i < imagePaths.length; i++) {
            BinaryData imageData = Utils.loadFromFile(imagePaths[i]);
            poller = client.beginAddPersonFace(
                personId, FaceRecognitionModel.RECOGNITION_04, imageData, null,
                FaceDetectionModel.DETECTION_03, String.format("Image_%s_%04d", name, (i + 1)));
        }

        //Only the poller for the last request will be returned.
        return poller;
    }

    private static void deletePersons(FaceAdministrationClient administrationClient, List<PersonData> personDataList) {
        log("Deleting Persons: " + personDataList.size());
        List<SyncPoller<FaceOperationResult, Void>> pollers = personDataList.stream()
            .map(personData -> safelyRun(
                () -> administrationClient
                    .beginDeletePerson(personData.getPersonId())))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        pollers.forEach(poller -> safelyRun(
            () -> poller
                .setPollInterval(Duration.ofSeconds(1))
                .waitForCompletion()));
        log("Done");
    }

    private static FaceAdministrationClient createAdministrationClient() {
        return new FaceAdministrationClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildClient();
    }

    private static FaceClient createFaceClient() {
        return new FaceClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildClient();
    }
}
