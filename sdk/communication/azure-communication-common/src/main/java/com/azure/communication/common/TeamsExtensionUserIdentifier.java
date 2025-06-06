// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.util.CoreUtils;

/**
 * Communication identifier for a Microsoft Teams Phone user who is using a Communication Services resource
 * to extend their Teams Phone set up.
 */
public final class TeamsExtensionUserIdentifier extends CommunicationIdentifier {

    private final String userId;
    private final String tenantId;
    private final String resourceId;
    private CommunicationCloudEnvironment cloudEnvironment;

    /**
     * Creates a TeamsExtensionUserIdentifier object with PUBLIC cloud environment.
     *
     * @param userId ID of the Microsoft Teams Extension user i.e. the Entra ID object id of the user.
     * @param tenantId Tenant ID of the Microsoft Teams Extension user.
     * @param resourceId The Communication Services resource id.
     * @throws IllegalArgumentException if any parameter fail the validation.
     */
    public TeamsExtensionUserIdentifier(String userId, String tenantId, String resourceId) {
        if (CoreUtils.isNullOrEmpty(userId)) {
            throw new IllegalArgumentException("The initialization parameter [userId] cannot be null or empty.");
        }
        if (CoreUtils.isNullOrEmpty(tenantId)) {
            throw new IllegalArgumentException("The initialization parameter [tenantId] cannot be null or empty.");
        }
        if (CoreUtils.isNullOrEmpty(resourceId)) {
            throw new IllegalArgumentException("The initialization parameter [resourceId] cannot be null or empty.");
        }

        this.userId = userId;
        this.tenantId = tenantId;
        this.resourceId = resourceId;
        this.cloudEnvironment = CommunicationCloudEnvironment.PUBLIC;
        generateRawId();
    }

    /**
     * Set full ID of the identifier
     * RawId is the encoded format for identifiers to store in databases or as stable keys in general.
     *
     * @param rawId full ID of the identifier.
     * @return TeamsExtensionUserIdentifier object itself.
     */
    @Override
    public TeamsExtensionUserIdentifier setRawId(String rawId) {
        super.setRawId(rawId);
        return this;
    }

    /**
     * Get Microsoft Teams Extension user
     * @return ID of the Microsoft Teams Extension user i.e. the Entra ID object id of the user.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get Microsoft Teams Extension user Tenant ID
     * @return Tenant ID of the Microsoft Teams Extension user.
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * Get Communication Services resource id.
     * @return the Communication Services resource id.
     */
    public String getResourceId() {
        return resourceId;
    }

    /**
     * Get cloud environment of the Teams Extension User identifier
     * @return cloud environment in which this identifier is created
     */
    public CommunicationCloudEnvironment getCloudEnvironment() {
        return cloudEnvironment;
    }

    /**
     * Set cloud environment of the Teams Extension User identifier
     *
     * @param cloudEnvironment the cloud environment in which this identifier is created
     * @return this object
     */
    public TeamsExtensionUserIdentifier setCloudEnvironment(CommunicationCloudEnvironment cloudEnvironment) {
        this.cloudEnvironment = cloudEnvironment != null ? cloudEnvironment : CommunicationCloudEnvironment.PUBLIC;
        generateRawId();
        return this;
    }

    /**
     * Generate rawId for TeamsExtensionUserIdentifier
     */
    private void generateRawId() {
        if (cloudEnvironment.equals(CommunicationCloudEnvironment.DOD)) {
            super.setRawId(ACS_USER_DOD_CLOUD_PREFIX + resourceId + "_" + tenantId + "_" + userId);
        } else if (cloudEnvironment.equals(CommunicationCloudEnvironment.GCCH)) {
            super.setRawId(ACS_USER_GCCH_CLOUD_PREFIX + resourceId + "_" + tenantId + "_" + userId);
        } else {
            super.setRawId(ACS_USER_PREFIX + resourceId + "_" + tenantId + "_" + userId);
        }
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }

        if (!(that instanceof TeamsExtensionUserIdentifier)) {
            return false;
        }

        return ((TeamsExtensionUserIdentifier) that).getRawId().equals(getRawId());
    }

    @Override
    public int hashCode() {
        return getRawId().hashCode();
    }
}
