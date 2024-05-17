// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceAdministrationClient;
import com.azure.ai.vision.face.FaceAdministrationClientBuilder;
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceIdentificationCandidate;
import com.azure.ai.vision.face.models.FaceIdentificationResult;
import com.azure.ai.vision.face.models.FaceOperationResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.vision.face.samples.utils.Utils.log;
import static com.azure.ai.vision.face.samples.utils.Utils.safelyRun;

public class IdentifyPersonAmongPersons {
    private static final RuntimeException[] EXCEPTION_CONTAINER = new RuntimeException[1];

    public static void main(String[] args) {
        //Create client to run Detect and Identify operations
        FaceClient client = createFaceClient();
        //Create FaceAdministrationClient to create Persons later.
        FaceAdministrationClient administrationClient = createAdministrationClient();


        List<PersonData> personDataList = new ArrayList<>();
        try {
            // Create Persons
            createPersonsAndAddFaces(administrationClient, personDataList);

            // Detect an image
            log("Detect a face");
            String faceId = detectImage(client, Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER3);

            // Identify if the image belongs to one of the Persons
            Utils.log("Identify the face belongs to whom ... ");
            FaceIdentificationCandidate candidate = identifyAmongPersons(client, faceId, personDataList);
            PersonDirectoryPerson person = administrationClient.getPerson(candidate.getPersonId());
            Utils.log("Result: " + person.getName() + ", Confidence:" + candidate.getConfidence());
        } finally {
            // Delete All the Persons
            deletePersons(administrationClient, personDataList);
        }
    }

    private static void createPersonsAndAddFaces(FaceAdministrationClient administrationClient,
        List<PersonData> personDataList) {
        personDataList.add(new PersonData("Ron", "Family1",
                new String[]{ Resources.TEST_IMAGE_PATH_FAMILY1_SON1,  Resources.TEST_IMAGE_PATH_FAMILY1_SON2}));
        personDataList.add(new PersonData("Gill", "Family1",
                new String[]{ Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER1,  Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER2}));
        personDataList.add(new PersonData("Anna", "Family2,Singing",
                new String[]{ Resources.TEST_IMAGE_PATH_FAMILY2_LADY1,  Resources.TEST_IMAGE_PATH_FAMILY2_LADY2}));

        // Create Persons, this is an async operation, we don't wait for it to complete now.
        // Note that we store
        List<Pair<PersonData, SyncPoller<FaceOperationResult, PersonDirectoryPerson>>> createPersonPollers
                = personDataList.stream()
            .map(personData -> createPerson(administrationClient, personData))
            .collect(Collectors.toList());

        List<SyncPoller<FaceOperationResult, PersonDirectoryFace>> addFacesPollers =
                waitForCreatePersonCompleteAndAddFaces(administrationClient, createPersonPollers);

        if (EXCEPTION_CONTAINER[0] != null) {
            throw EXCEPTION_CONTAINER[0];
        }

        log("Waiting for all the operations of AddPersonFace complete ... ");
        addFacesPollers.forEach(poller -> poller
                .setPollInterval(Duration.ofSeconds(1))
                .waitForCompletion(Duration.ofSeconds(60)));
        log("Done");
    }

    private static Pair<PersonData, SyncPoller<FaceOperationResult, PersonDirectoryPerson>> createPerson(
            FaceAdministrationClient administrationClient, PersonData personData) {
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
                .map(personData -> addFacesToPersons(
                        administrationClient, personData.getPersonId(), personData.getImagePaths()))
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
        FaceAdministrationClient client, String personId, String[] imagePaths) {
        //Skip the operation if we meet any exception before
        if (EXCEPTION_CONTAINER[0] != null) {
            return null;
        }

        SyncPoller<FaceOperationResult, PersonDirectoryFace> poller = null;
        // AddPersonFace is an async operation, we can send couples of them and only wait for the last request to
        // complete
        for (int i = 0; i < imagePaths.length; i++) {
            BinaryData imageData = Utils.loadFromFile(imagePaths[i]);
            poller = client.beginAddPersonFace(
                personId, FaceRecognitionModel.RECOGNITION_04, imageData, null,
                FaceDetectionModel.DETECTION_03, "User " + i);
        }

        //Only the poller for the last request will be returned.
        return poller;
    }

    private static String detectImage(FaceClient client, String imagePath) {
        BinaryData imageData = Utils.loadFromFile(imagePath);
        List<FaceDetectionResult> results = client.detect(
            imageData, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);

        FaceDetectionResult result = results.get(0);

        return result.getFaceId();
    }

    private static FaceIdentificationCandidate identifyAmongPersons(FaceClient client, String faceId,
        List<PersonData> personDataList) {
        List<String> faceIds = Collections.singletonList(faceId);
        List<String> personIds = personDataList.stream()
            .map(PersonData::getPersonId)
            .collect(Collectors.toList());

        List<FaceIdentificationResult> results = client.identifyFromPersonDirectory(
            faceIds, personIds, 1, null);

        FaceIdentificationResult result = results.get(0);
        return result.getCandidates().get(0);
    }

    private static void deletePersons(FaceAdministrationClient administrationClient, List<PersonData> personDataList) {
        log("Deleting Persons: " + personDataList.size());
        List<SyncPoller<FaceOperationResult, Void>> pollers = personDataList.stream()
            .map(personData -> Utils.safelyRun(
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

