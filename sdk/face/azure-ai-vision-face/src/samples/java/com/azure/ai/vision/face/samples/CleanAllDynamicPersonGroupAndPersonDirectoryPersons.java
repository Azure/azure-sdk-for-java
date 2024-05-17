// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples;

import com.azure.ai.vision.face.FaceAdministrationClient;
import com.azure.ai.vision.face.FaceAdministrationClientBuilder;
import com.azure.ai.vision.face.models.DynamicPersonGroup;
import com.azure.ai.vision.face.models.FaceOperationResult;
import com.azure.ai.vision.face.models.PersonDirectoryPerson;
import com.azure.ai.vision.face.samples.utils.ConfigurationHelper;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.polling.SyncPoller;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.ai.vision.face.samples.utils.Utils.log;

public class CleanAllDynamicPersonGroupAndPersonDirectoryPersons {
    public static void main(String[] args) {
        //Create FaceAdministrationClient to create Persons and DynamicPersonDirectory.
        FaceAdministrationClient administrationClient = new FaceAdministrationClientBuilder()
            .endpoint(ConfigurationHelper.getEndpoint())
            .credential(new AzureKeyCredential(ConfigurationHelper.getAccountKey()))
            .buildClient();


        try {
            createFakeData(administrationClient);
        } finally {
            deleteAllPersons(administrationClient);
            deleteAllDynamicPersonDirecroy(administrationClient);
        }
    }

    private static void deleteAllDynamicPersonDirecroy(FaceAdministrationClient administrationClient) {
        List<DynamicPersonGroup> dynamicPersonGroups = administrationClient.getDynamicPersonGroups();
        List<SyncPoller<FaceOperationResult, Void>>  pollers = dynamicPersonGroups.stream().map(group -> {
            log("Remove DynamicPersonGroup: " + group.getDynamicPersonGroupId());
            return administrationClient.beginDeleteDynamicPersonGroup(group.getDynamicPersonGroupId());
        }).collect(Collectors.toList());

        log("Wait for all deletion of DynamicPersonGroup to complete");
        pollers.forEach(poller -> poller
            .setPollInterval(Duration.ofSeconds(1))
            .waitForCompletion(Duration.ofSeconds(60)));
        log("Done");
    }

    private static void deleteAllPersons(FaceAdministrationClient administrationClient) {
        List<PersonDirectoryPerson> pdPersons = administrationClient.getPersons();
        for (PersonDirectoryPerson person : pdPersons) {
            log("Delete Person, name: " + person.getName() + ", data:" + person.getUserData() + ", ID:" + person.getPersonId());
            administrationClient.beginDeletePerson(person.getPersonId());
        }
    }

    private static void createFakeData(FaceAdministrationClient administrationClient) {
        log("Create fake data ...");
        List<SyncPoller<FaceOperationResult, PersonDirectoryPerson>> pollers = new ArrayList<>();
        pollers.add(
            administrationClient.beginCreatePerson("fake_person1", "Fake Person 1"));
        pollers.add(
            administrationClient.beginCreatePerson("fake_person2", "Fake Person 2"));
        administrationClient.createDynamicPersonGroup("fake1", "Fake group 1");
        administrationClient.createDynamicPersonGroup("fake2", "Fake group 2");

        pollers.forEach(poller -> poller.setPollInterval(Duration.ofSeconds(1))
            .waitForCompletion(Duration.ofSeconds(60)));

        log("Done");
    }
}
