// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for Microsoft Teams Application
 */
public final class MicrosoftTeamsAppIdentifier extends CommunicationIdentifier {
    private final String appId;

    private final CommunicationCloudEnvironment cloudEnvironment;

    /**
     * Creates a MicrosoftTeamsAppIdentifier object
     *
     * @param appId ID of the Microsoft Teams Application.
     * @param cloudEnvironment the cloud environment in which this identifier is created.
     * @throws IllegalArgumentException thrown if appId parameter fails the validation.
     */
    public MicrosoftTeamsAppIdentifier(String appId, CommunicationCloudEnvironment cloudEnvironment) {
        if (CoreUtils.isNullOrEmpty(appId)) {
            throw new IllegalArgumentException("The initialization parameter [appId] cannot be null or empty.");
        }
        this.appId = appId;
        this.cloudEnvironment = cloudEnvironment;
        generateRawId();
    }

    /**
     * Creates a MicrosoftTeamsAppIdentifier object
     *
     * @param appId ID of the Microsoft Teams Application.
     * @throws IllegalArgumentException thrown if appId parameter fails the validation.
     */
    public MicrosoftTeamsAppIdentifier(String appId) {
        this(appId, CommunicationCloudEnvironment.PUBLIC);
    }

    /**
     * Get application ID
     * @return ID of the Microsoft Teams Application.
     */
    public String getAppId() {
        return this.appId;
    }

    /**
     * Get cloud environment of the application identifier
     *
     * @return cloud environment in which this identifier is created.
     */
    public CommunicationCloudEnvironment getCloudEnvironment() {
        return cloudEnvironment;
    }

    /**
     * Set full ID of the identifier
     * RawId is the encoded format for identifiers to store in databases or as stable keys in general.
     *
     * @param rawId full ID of the identifier.
     * @return MicrosoftTeamsAppIdentifier object itself.
     */
    @Override
    protected MicrosoftTeamsAppIdentifier setRawId(String rawId) {
        super.setRawId(rawId);
        return this;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof MicrosoftTeamsAppIdentifier)) {
            return false;
        }

        return ((MicrosoftTeamsAppIdentifier) that).getRawId().equals(getRawId());
    }

    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }

    private void generateRawId() {
        if (cloudEnvironment.equals(CommunicationCloudEnvironment.DOD)) {
            super.setRawId(TEAMS_APP_DOD_CLOUD_PREFIX + this.appId);
        } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.GCCH)) {
            super.setRawId(TEAMS_APP_GCCH_CLOUD_PREFIX + this.appId);
        } else {
            super.setRawId(TEAMS_APP_PUBLIC_CLOUD_PREFIX + this.appId);
        }
    }
}
