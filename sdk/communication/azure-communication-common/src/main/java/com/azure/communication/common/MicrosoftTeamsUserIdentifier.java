// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Microsoft Teams User
 */
public class MicrosoftTeamsUserIdentifier extends CommunicationIdentifier {

    private final String userId;
    private final boolean isAnonymous;
    private String id;
    private CommunicationCloudEnvironment cloudEnvironment = CommunicationCloudEnvironment.PUBLIC;

    /**
     * Creates a MicrosoftTeamsUserIdentifier object
     *
     * @param userId Id of the Microsoft Teams user. If the user isn't anonymous, the id is the AAD object id of the user.
     * @param isAnonymous set this to true if the user is anonymous,
     *                    for example when joining a meeting with a share link
     * @param cloudEnvironment the cloud environment in which this identifier is created
     * @throws IllegalArgumentException thrown if userId parameter fail the validation.
     */
    public MicrosoftTeamsUserIdentifier(String userId, boolean isAnonymous, CommunicationCloudEnvironment  cloudEnvironment) {
        this(userId, isAnonymous);
        this.cloudEnvironment = cloudEnvironment;
    }

    /**
     * Creates a MicrosoftTeamsUserIdentifier object
     *
     * @param userId Id of the Microsoft Teams user. If the user isn't anonymous, the id is the AAD object id of the user.
     * @param isAnonymous set this to true if the user is anonymous,
     *                    for example when joining a meeting with a share link
     * @throws IllegalArgumentException thrown if userId parameter fail the validation.
    */
    public MicrosoftTeamsUserIdentifier(String userId, boolean isAnonymous) {
        if (CoreUtils.isNullOrEmpty(userId)) {
            throw new IllegalArgumentException("The initialization parameter [userId] cannot be null or empty.");
        }
        this.userId = userId;
        this.isAnonymous = isAnonymous;
    }

    /**
     * Creates a MicrosoftTeamsUserIdentifier object
     *
     * @param userId Id of the Microsoft Teams user. If the user isn't anonymous, the id is the AAD object id of the user.
     * @throws IllegalArgumentException thrown if userId parameter fail the validation.
    */
    public MicrosoftTeamsUserIdentifier(String userId) {
        this(userId, false);
    }

    /**
     * Get Teams User Id
     * @return userId Id of the Microsoft Teams user. If the user isn't anonymous, the id is the AAD object id of the user.
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * @return True if the user is anonymous, for example when joining a meeting with a share link.
     */
    public boolean isAnonymous() {
        return this.isAnonymous;
    }

    /**
     * Get full id of the Microsoft Teams user
     * @return full id of the Microsoft Teams user
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Set full id of the Microsoft Teams user
     * @param id full id of the Microsoft Teams user
     * @return the MicrosoftTeamsUserIdentifier object itself
     */
    public MicrosoftTeamsUserIdentifier setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Set cloud environment of the Teams user identifier
     * @param cloudEnvironment the cloud environment in which this identifier is created
     * @return this object
     */
    public MicrosoftTeamsUserIdentifier setCloudEnvironment(CommunicationCloudEnvironment  cloudEnvironment) {
        this.cloudEnvironment = cloudEnvironment;
        return this;
    }

    /**
     * Get cloud environment of the Teams user identifier
     * @return cloud environment in which this identifier is created
     */
    public CommunicationCloudEnvironment getCloudEnvironment() {
        return cloudEnvironment;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof MicrosoftTeamsUserIdentifier)) {
            return false;
        }

        MicrosoftTeamsUserIdentifier thatId = (MicrosoftTeamsUserIdentifier) that;
        if (!thatId.getUserId().equals(this.getUserId())
            || thatId.isAnonymous != this.isAnonymous) {
            return false;
        }

        if (cloudEnvironment != null && !cloudEnvironment.equals(thatId.cloudEnvironment)) {
            return false;
        }

        if (thatId.cloudEnvironment != null && !thatId.cloudEnvironment.equals(this.cloudEnvironment)) {
            return false;
        }

        return id == null
            || thatId.id == null
            || thatId.id.equals(this.id);
    }


    @Override
    public int hashCode() {
        return userId.hashCode();
    }

}
