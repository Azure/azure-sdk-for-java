// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.test.model;

import com.azure.core.test.utils.TestResourceNamer;

import java.util.Objects;

/**
 * Azure user information.
 */
public class AzureUser {
    private final String id;
    private final String userPrincipalName;
    private final TestResourceNamer testResourceNamer;

    /**
     * Creates a new AzureUser with null properties, for Playback Test.
     * @param testResourceNamer {@link TestResourceNamer} used for this test run.
     */
    public AzureUser(TestResourceNamer testResourceNamer) {
        this(Objects.requireNonNull(testResourceNamer), null, null);
    }

    /**
     * Creates a new AzureUser with all necessary properties, for Live Test.
     * @param testResourceNamer {@link TestResourceNamer} used for this test run.
     * @param id user ID
     * @param userPrincipalName user principal name
     */
    public AzureUser(TestResourceNamer testResourceNamer, String id, String userPrincipalName) {
        this.testResourceNamer = testResourceNamer;
        this.id = id;
        this.userPrincipalName = userPrincipalName;
    }

    /**
     * ObjectId of the signed-in user.
     * @return objectId of the signed-in user.
     */
    public String id() {
        return testResourceNamer.recordValueFromConfig(id);
    }

    /**
     * Principal name of the signed-in user.
     * @return principal name of the signed-in user
     */
    public String userPrincipalName() {
        return testResourceNamer.recordValueFromConfig(userPrincipalName);
    }
}
