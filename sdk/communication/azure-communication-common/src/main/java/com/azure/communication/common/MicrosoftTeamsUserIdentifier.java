// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Microsoft Teams User
 */
public final class MicrosoftTeamsUserIdentifier extends CommunicationIdentifier {

    private final String userId;
    private final boolean isAnonymous;
    private CommunicationCloudEnvironment cloudEnvironment = CommunicationCloudEnvironment.PUBLIC;

    private String rawId;

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
     * Set cloud environment of the Teams user identifier
     * @param cloudEnvironment the cloud environment in which this identifier is created
     * @return this object
     */
    public MicrosoftTeamsUserIdentifier setCloudEnvironment(CommunicationCloudEnvironment cloudEnvironment) {
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

    /**
     * Get full id of the identifier. This id is optional.
     * @return full id of the identifier
     */
    public String getRawId() {
        return rawId;
    }

    /**
     * Set full id of the identifier
     * @param rawId full id of the identifier
     * @return CommunicationIdentifier object itself
     */
    public MicrosoftTeamsUserIdentifier setRawId(String rawId) {
        this.rawId = rawId;
        return this;
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

        return getRawId() == null
            || thatId.getRawId() == null
            || thatId.getRawId().equals(this.getRawId());
    }


    @Override
    public int hashCode() {
        return userId.hashCode();
    }

}
