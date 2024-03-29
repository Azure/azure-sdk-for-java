// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.devspaces.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.devspaces.models.ProvisioningState;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The ControllerProperties model. */
@Fluent
public final class ControllerProperties {
    /*
     * Provisioning state of the Azure Dev Spaces Controller.
     */
    @JsonProperty(value = "provisioningState", access = JsonProperty.Access.WRITE_ONLY)
    private ProvisioningState provisioningState;

    /*
     * DNS suffix for public endpoints running in the Azure Dev Spaces Controller.
     */
    @JsonProperty(value = "hostSuffix", access = JsonProperty.Access.WRITE_ONLY)
    private String hostSuffix;

    /*
     * DNS name for accessing DataPlane services
     */
    @JsonProperty(value = "dataPlaneFqdn", access = JsonProperty.Access.WRITE_ONLY)
    private String dataPlaneFqdn;

    /*
     * DNS of the target container host's API server
     */
    @JsonProperty(value = "targetContainerHostApiServerFqdn", access = JsonProperty.Access.WRITE_ONLY)
    private String targetContainerHostApiServerFqdn;

    /*
     * Resource ID of the target container host
     */
    @JsonProperty(value = "targetContainerHostResourceId", required = true)
    private String targetContainerHostResourceId;

    /*
     * Credentials of the target container host (base64).
     */
    @JsonProperty(value = "targetContainerHostCredentialsBase64", required = true)
    private String targetContainerHostCredentialsBase64;

    /** Creates an instance of ControllerProperties class. */
    public ControllerProperties() {
    }

    /**
     * Get the provisioningState property: Provisioning state of the Azure Dev Spaces Controller.
     *
     * @return the provisioningState value.
     */
    public ProvisioningState provisioningState() {
        return this.provisioningState;
    }

    /**
     * Get the hostSuffix property: DNS suffix for public endpoints running in the Azure Dev Spaces Controller.
     *
     * @return the hostSuffix value.
     */
    public String hostSuffix() {
        return this.hostSuffix;
    }

    /**
     * Get the dataPlaneFqdn property: DNS name for accessing DataPlane services.
     *
     * @return the dataPlaneFqdn value.
     */
    public String dataPlaneFqdn() {
        return this.dataPlaneFqdn;
    }

    /**
     * Get the targetContainerHostApiServerFqdn property: DNS of the target container host's API server.
     *
     * @return the targetContainerHostApiServerFqdn value.
     */
    public String targetContainerHostApiServerFqdn() {
        return this.targetContainerHostApiServerFqdn;
    }

    /**
     * Get the targetContainerHostResourceId property: Resource ID of the target container host.
     *
     * @return the targetContainerHostResourceId value.
     */
    public String targetContainerHostResourceId() {
        return this.targetContainerHostResourceId;
    }

    /**
     * Set the targetContainerHostResourceId property: Resource ID of the target container host.
     *
     * @param targetContainerHostResourceId the targetContainerHostResourceId value to set.
     * @return the ControllerProperties object itself.
     */
    public ControllerProperties withTargetContainerHostResourceId(String targetContainerHostResourceId) {
        this.targetContainerHostResourceId = targetContainerHostResourceId;
        return this;
    }

    /**
     * Get the targetContainerHostCredentialsBase64 property: Credentials of the target container host (base64).
     *
     * @return the targetContainerHostCredentialsBase64 value.
     */
    public String targetContainerHostCredentialsBase64() {
        return this.targetContainerHostCredentialsBase64;
    }

    /**
     * Set the targetContainerHostCredentialsBase64 property: Credentials of the target container host (base64).
     *
     * @param targetContainerHostCredentialsBase64 the targetContainerHostCredentialsBase64 value to set.
     * @return the ControllerProperties object itself.
     */
    public ControllerProperties withTargetContainerHostCredentialsBase64(String targetContainerHostCredentialsBase64) {
        this.targetContainerHostCredentialsBase64 = targetContainerHostCredentialsBase64;
        return this;
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (targetContainerHostResourceId() == null) {
            throw LOGGER
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property targetContainerHostResourceId in model ControllerProperties"));
        }
        if (targetContainerHostCredentialsBase64() == null) {
            throw LOGGER
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "Missing required property targetContainerHostCredentialsBase64 in model"
                            + " ControllerProperties"));
        }
    }

    private static final ClientLogger LOGGER = new ClientLogger(ControllerProperties.class);
}
