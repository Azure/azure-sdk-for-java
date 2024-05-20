// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.samples.model;

import com.azure.ai.vision.face.models.FaceOperationResult;
import com.azure.ai.vision.face.models.PersonDirectoryFace;
import com.azure.core.util.polling.SyncPoller;

public class PersonData {
    private final String name;
    private final String userData;
    private final String[] imagePaths;
    private String personId;
    private SyncPoller<FaceOperationResult, PersonDirectoryFace> pollerForAddFaceToPerson;


    public PersonData(String name, String userData, String[] imagePaths) {
        this.name = name;
        this.userData = userData;
        this.imagePaths = imagePaths;
    }

    public String getName() {
        return name;
    }

    public String getUserData() {
        return userData;
    }

    public String[] getImagePaths() {
        return imagePaths;
    }

    public SyncPoller<FaceOperationResult, PersonDirectoryFace> getPollerForAddFaceToPerson() {
        return pollerForAddFaceToPerson;
    }

    public void setPollerForAddFaceToPerson(SyncPoller<FaceOperationResult, PersonDirectoryFace> pollerForAddFaceToPerson) {
        this.pollerForAddFaceToPerson = pollerForAddFaceToPerson;
    }

    public String getPersonId() {
        return this.personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }
}
