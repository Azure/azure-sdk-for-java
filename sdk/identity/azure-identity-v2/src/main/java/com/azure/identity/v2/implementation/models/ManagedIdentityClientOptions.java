// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2.implementation.models;

/**
 * Represents Managed Identity Client Options used in Managed Identity OAuth Flow .
 */
public class ManagedIdentityClientOptions extends ClientOptionsBase {
    private String resourceId;
    private String objectId;

    /**
     * Creates an instance of IdentityClientOptions with default settings.
     */
    public ManagedIdentityClientOptions() { super(); }

    /**
     * Gets the configured resource ID.
     * @return the client secret
     */
    public String getResourceId() {
        return this.resourceId;
    }

    /**
     * Sets the resource ID.
     * @param resourceId The resource ID
     * @return the ConfidentialClientOptions itself.
     */
    public ManagedIdentityClientOptions setResourceId(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    /**
     * Gets the configured object ID.
     * @return the client secret
     */
    public String getObjectId() {
        return this.objectId;
    }

    /**
     * Sets the object ID.
     * @param objectId The object ID
     * @return the ConfidentialClientOptions itself.
     */
    public ManagedIdentityClientOptions setObjectId(String objectId) {
        this.objectId = objectId;
        return this;
    }


    public ManagedIdentityClientOptions clone() {
        ManagedIdentityClientOptions clone
            = (ManagedIdentityClientOptions) new ManagedIdentityClientOptions()
            .setResourceId(resourceId)
            .setObjectId(objectId)
            .setHttpPipelineOptions(this.getHttpPipelineOptions().clone())
            .setMsalCommonOptions(this.getMsalCommonOptions().clone());
        return clone;
    }
}
