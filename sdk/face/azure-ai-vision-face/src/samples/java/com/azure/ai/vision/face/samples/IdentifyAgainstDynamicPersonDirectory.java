// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

//import com.azure.ai.vision.face.*;

import com.azure.ai.vision.face.FaceAdministrationClient;
import com.azure.ai.vision.face.FaceAdministrationClientBuilder;
import com.azure.ai.vision.face.FaceClient;
import com.azure.ai.vision.face.FaceClientBuilder;
import com.azure.ai.vision.face.models.DynamicPersonGroup;
import com.azure.ai.vision.face.models.FaceDetectionModel;
import com.azure.ai.vision.face.models.FaceDetectionResult;
import com.azure.ai.vision.face.models.FaceIdentificationCandidate;
import com.azure.ai.vision.face.models.FaceIdentificationResult;
import com.azure.ai.vision.face.models.FaceOperationResult;
import com.azure.ai.vision.face.models.FaceRecognitionModel;
import com.azure.ai.vision.face.models.ListGroupReferenceResult;
import com.azure.ai.vision.face.models.ListPersonResult;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.ai.vision.face.samples.utils.Utils.log;
import static com.azure.ai.vision.face.samples.utils.Utils.logObject;
import static com.azure.ai.vision.face.samples.utils.Utils.safelyRun;


public class IdentifyAgainstDynamicPersonDirectory {
    private static final RuntimeException[] EXCEPTION_CONTAINER = new RuntimeException[1];
    private static final String GROUP_TAG_FAMILY1 = "Family1";
    private static final String GROUP_TAG_FAMILY2 = "Family2";
    private static final String GROUP_TAG_SINGING = "SINGING";
    private static final String DYNAMIC_PERSON_GROUP_ID_FAMILY1 = "pd_family1";
    private static final String DYNAMIC_PERSON_GROUP_ID_FAMILY2 = "pd_family2";
    private static final String DYNAMIC_PERSON_GROUP_ID_SINGING_CLUB = "pd_singing_club";

    public static void main(String[] args) {
        //Create FaceAdministrationClient to create Persons and DynamicPersonDirectory.
        FaceAdministrationClient administrationClient = createAdministrationClient();
        //Create client to run Detect and Identify operations
        FaceClient client = createFaceClient();

        PersonData personDataBill = new PersonData("Bill", createUserData(GROUP_TAG_FAMILY1, GROUP_TAG_SINGING),
            new String[]{ Resources.TEST_IMAGE_PATH_FAMILY1_DAD1,  Resources.TEST_IMAGE_PATH_FAMILY1_DAD2});

        List<PersonData> personDataList = new ArrayList<>(Arrays.asList(
            personDataBill,
            new PersonData("Clare", createUserData(GROUP_TAG_FAMILY1, GROUP_TAG_SINGING),
                new String[]{ Resources.TEST_IMAGE_PATH_FAMILY1_MON1,  Resources.TEST_IMAGE_PATH_FAMILY1_MON2}),
            new PersonData("Ron", createUserData(GROUP_TAG_FAMILY1),
                new String[]{ Resources.TEST_IMAGE_PATH_FAMILY1_SON1,  Resources.TEST_IMAGE_PATH_FAMILY1_SON2}),
            new PersonData("Anna", createUserData(GROUP_TAG_FAMILY2, GROUP_TAG_SINGING),
                new String[]{ Resources.TEST_IMAGE_PATH_FAMILY2_LADY1,  Resources.TEST_IMAGE_PATH_FAMILY2_LADY2}
            )));

        List<String> dynamicPersonGroupsIds = new ArrayList<>();

        try {
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

            // Create DynamicPersonGroup with Persons. Creating DynamicPersonGroup is also async operation.
            // We have to wait for it to complete later when we are about to use them.
            // When we create a DynamicPersonGroup with Persons, we have to wait for all the operations to create Person
            // to complete
            SyncPoller<FaceOperationResult, DynamicPersonGroup> dynamicPersonGroupPollerFamily1 = createDynamicPersonGroup(
                administrationClient, DYNAMIC_PERSON_GROUP_ID_FAMILY1, "Family 1",
                GROUP_TAG_FAMILY1, personDataList, dynamicPersonGroupsIds);

            List<SyncPoller<FaceOperationResult, DynamicPersonGroup>> dynamicPersonGroupPollerList = Arrays.asList(
                dynamicPersonGroupPollerFamily1,
                createDynamicPersonGroup(
                    administrationClient, DYNAMIC_PERSON_GROUP_ID_SINGING_CLUB, "Euphonic Voices",
                    GROUP_TAG_SINGING, personDataList, dynamicPersonGroupsIds)
            );

            if (EXCEPTION_CONTAINER[0] != null) {
                throw EXCEPTION_CONTAINER[0];
            }

            // We are going to run identify, so we wait for AddFace operations to complete now.
            // We don't need the result for now.
            addFacesPollers.forEach(poller -> {
                log("Wait for operations to add faces complete ...");
                PersonDirectoryFace face = poller
                    .setPollInterval(Duration.ofSeconds(1))
                    .getFinalResult(Duration.ofSeconds(60));
                log("Add face complete: " + face.getUserData());
            });

            // We are going to run identify, so we wait for all operations to create DynamicPersonGroup to complete now.
            log("Wait for creating DynamicPersonGroup to complete ...");
            List<DynamicPersonGroup> dynamicPersonGroupList = dynamicPersonGroupPollerList.stream()
                .map(poller -> poller
                    .setPollInterval(Duration.ofSeconds(1))
                    .getFinalResult(Duration.ofSeconds(60)))
                .collect(Collectors.toList());

            DynamicPersonGroup dynamicPersonGroupFamily1 = dynamicPersonGroupPollerFamily1.getFinalResult();
            log("done");

            // Detect an image, there are four faces in this image.
            List<String> faceIds = detectFaces(client, Resources.TEST_IMAGE_PATH_IDENTIFICATION);

            // Identify the four faces against the DynamicPersonGroups
            // Note that we before we perform identify, we have to ensure
            // 1. the creation of DynamicPersonGroup completes.
            // 2. all the operation to add faces to Person completes.
            identifyAgainstDynamicPersonGroup(client, administrationClient, faceIds, dynamicPersonGroupList);

            // Create Another person Gill and wait for the action to complete.
            PersonData personDataGill = createPersonGillAndWaitForCompletion(administrationClient, personDataList);
            if (EXCEPTION_CONTAINER[0] != null) {
                throw EXCEPTION_CONTAINER[0];
            }

            // Add faces to Gill, but not wait for completion.
            SyncPoller<FaceOperationResult, PersonDirectoryFace>  addFacesToGillPoller = addFacesToPersons(
                administrationClient, personDataGill);

            //List all DynamicPersonGroup with Bill
            listDynamicPersonGroupReferences(administrationClient, personDataBill);

            // Add Gill to DynamicPersonGroup - Family1 and remove Bill from it
            log("Add " + personDataGill.getName() + " to '" +  dynamicPersonGroupFamily1.getName() + "' and remove "
                + personDataBill.getName() + " from it.");
            administrationClient.beginUpdateDynamicPersonGroupWithPersonChanges(
                    dynamicPersonGroupFamily1.getDynamicPersonGroupId(), null, null,
                Collections.singletonList(personDataGill.getPersonId()),
                Collections.singletonList(personDataBill.getPersonId()))
                .setPollInterval(Duration.ofSeconds(1))
                .waitForCompletion(Duration.ofSeconds(60));

            //List all DynamicPersonGroup with Bill again
            listDynamicPersonGroupReferences(administrationClient, personDataBill);

            // Wait for the completion to add faces to Gill
            addFacesToGillPoller.setPollInterval(Duration.ofSeconds(1))
                .waitForCompletion(Duration.ofSeconds(60));

            // Get all Persons in DynamicPersonGroup 'Family 1'
            listPersonsInDynamicPersonGroup(administrationClient, dynamicPersonGroupFamily1);

            // Identify the faces again
            identifyAgainstDynamicPersonGroup(client, administrationClient, faceIds, dynamicPersonGroupList);
        } finally {
            // Delete All the Persons
            deletePersons(administrationClient, personDataList);
            // Delete All Dynamic PersonGroup
            deleteDynamicPersonGroups(administrationClient, dynamicPersonGroupsIds);
        }
    }

    private static void listPersonsInDynamicPersonGroup(FaceAdministrationClient administrationClient,
        DynamicPersonGroup dynamicPersonGroup) {
        ListPersonResult listPersonResult = administrationClient.getDynamicPersonGroupPersons(
            dynamicPersonGroup.getDynamicPersonGroupId());
        List<String> personNames = listPersonResult
            .getPersonIds().stream()
            .map(administrationClient::getPerson)
            .map(PersonDirectoryPerson::getName)
            .collect(Collectors.toList());
        logObject("List Persons in DynamicPersonGroup '" + dynamicPersonGroup.getName() + "' ", personNames);
    }

    private static void listDynamicPersonGroupReferences(
        FaceAdministrationClient administrationClient, PersonData personData) {
        ListGroupReferenceResult listGroupReferenceResult = administrationClient.getDynamicPersonGroupReferences(
            personData.getPersonId());
        List<String> dynamicPersonGroupNames = listGroupReferenceResult.getDynamicPersonGroupIds().stream()
            .map(administrationClient::getDynamicPersonGroup)
            .map(DynamicPersonGroup::getName)
            .collect(Collectors.toList());
        logObject("List DynamicPersonGroup with " + personData.getName() + " ", dynamicPersonGroupNames);
    }

    private static void deleteDynamicPersonGroups(
        FaceAdministrationClient administrationClient, List<String> dynamicPersonGroupsIds) {

        log("Deleting DynamicPeronGroups: " + dynamicPersonGroupsIds.size());
        List<SyncPoller<FaceOperationResult, Void>> pollers = dynamicPersonGroupsIds.stream()
            .map(id ->
                Utils.safelyRun(
                    () -> administrationClient.beginDeleteDynamicPersonGroup(id)))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        pollers.forEach(poller -> safelyRun(
            () -> poller
                .setPollInterval(Duration.ofSeconds(1))
                .waitForCompletion(Duration.ofSeconds(60))));
        log("Done");
    }

    private static void identifyAgainstDynamicPersonGroup(FaceClient client,
        FaceAdministrationClient administrationClient, List<String> faceIds, List<DynamicPersonGroup> dynamicPersonGroupList) {
        log("=== Identify " + faceIds.size() + " faces against " +  dynamicPersonGroupList.size()
            + " DynamicPersonGroups ===");
        dynamicPersonGroupList
            .forEach(dynamicPersonGroup -> {
                log("Identify if a face belongs to '" + dynamicPersonGroup.getName() + "'");
                faceIds.forEach(faceId -> identifyAgainstDynamicPersonGroup(
                        client, administrationClient, dynamicPersonGroup, faceId));
            });
    }

    private static void identifyAgainstDynamicPersonGroup(FaceClient client, FaceAdministrationClient administrationClient,
        DynamicPersonGroup dynamicPersonGroup, String faceId) {
        List<FaceIdentificationResult> identificationResults = client.identifyFromDynamicPersonGroup(
            Collections.singletonList(faceId), dynamicPersonGroup.getDynamicPersonGroupId(), 1, null);

        List<FaceIdentificationCandidate> candidates = identificationResults.get(0).getCandidates();
        if (!candidates.isEmpty()) {
            FaceIdentificationCandidate candidate = candidates.get(0);
            // Query Person Data
            PersonDirectoryPerson peron = administrationClient.getPerson(candidate.getPersonId());
            log("Found!! confidence: " + candidate.getConfidence() + ". Face ID " + faceId + " belongs to [" + peron.getName()
                + "]");
        } else {
            log("No such person for Face ID " + faceId);
        }
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

    private static SyncPoller<FaceOperationResult, DynamicPersonGroup>  createDynamicPersonGroup(
        FaceAdministrationClient administrationClient, String dynamicPersonGroupId, String groupName,
        String groupTag, List<PersonData> personDataList, List<String> dynamicPersonGroupsIds) {
        dynamicPersonGroupsIds.add(dynamicPersonGroupId);

        List<PersonData> personDataInGroup = personDataList.stream()
            .filter(personData -> personData.getUserData() != null)
            .filter(personData -> Arrays.asList(
                personData
                    .getUserData()
                    .split(","))
                    .contains(groupTag))
            .collect(Collectors.toList());

        List<String> names = personDataInGroup.stream().map(PersonData::getName).collect(Collectors.toList());
        List<String> personIds = personDataInGroup.stream().map(PersonData::getPersonId).collect(Collectors.toList());

        String groupNameString = "'" + groupName + "'";
        logObject("Send request to create DynamicPersonGroup: " + groupNameString + " with ", names);
        return administrationClient.beginCreateDynamicPersonGroupWithPerson(
            dynamicPersonGroupId, groupName, personIds, groupTag);
    }

    private static PersonData createPersonGillAndWaitForCompletion(
        FaceAdministrationClient administrationClient, List<PersonData> personDataList) {
        PersonData personData = new PersonData("Gill", createUserData(GROUP_TAG_FAMILY1),
            new String[]{ Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER1,  Resources.TEST_IMAGE_PATH_FAMILY1_DAUGHTER2});
        log("Create Person: " + personData.getName() + " and wait for the operation completed ...");
        personDataList.add(personData);

        // Create Person and wait for completion.
        Pair<PersonData, SyncPoller<FaceOperationResult, PersonDirectoryPerson>> pair = createPerson(administrationClient, personData);
        personData = waitForCreatePersonComplete(pair);
        log("Done");

        return personData;
    }

    private static String createUserData(String... tags) {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(tags).forEach(tag -> sb.append(tag).append(','));
        Utils.removeLastComma(sb);

        return sb.toString();
    }

    private static Pair<PersonData, SyncPoller<FaceOperationResult, PersonDirectoryPerson>> createPerson(
        FaceAdministrationClient administrationClient, PersonData personData) {
        log("Create Person: " + personData.getName());
        return Utils.safelyRunWithExceptionCheck(EXCEPTION_CONTAINER, "createPerson", () ->
            Pair.of(personData,
                administrationClient.beginCreatePerson(personData.getName(), personData.getUserData())));
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

    private static List<String> detectFaces(FaceClient client, String imagePath) {
        log("Detect faces");
        BinaryData imageData = Utils.loadFromFile(imagePath);
        List<FaceDetectionResult> results = client.detect(
            imageData, FaceDetectionModel.DETECTION_03, FaceRecognitionModel.RECOGNITION_04, true);

        return results.stream().map(FaceDetectionResult::getFaceId).collect(Collectors.toList());
    }

    private static void deletePersons(FaceAdministrationClient administrationClient, List<PersonData> personDataList) {
        List<String> personsIds = personDataList.stream()
            .map(PersonData::getPersonId)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        log("Deleting Persons: " + personDataList.size());
        List<SyncPoller<FaceOperationResult, Void>> pollers = personsIds.stream()
            .map(personId -> Utils.safelyRun(
                () -> administrationClient.beginDeletePerson(personId)))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        pollers.forEach(poller -> safelyRun(
            () -> poller
                .setPollInterval(Duration.ofSeconds(1))
                .waitForCompletion(Duration.ofSeconds(60))));
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
