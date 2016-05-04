/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.implementation.api;

import java.util.List;

/**
 * Parameters for a CloudPoolOperations.Patch request.
 */
public class PoolPatchParameterInner {
    /**
     * Sets a task to run on each compute node as it joins the pool. If
     * omitted, any existing start task is left unchanged.
     */
    private StartTask startTask;

    /**
     * Sets a list of certificates to be installed on each compute node in the
     * pool. If omitted, any existing certificate references are left
     * unchanged.
     */
    private List<CertificateReference> certificateReferences;

    /**
     * Sets a list of application packages to be installed on each compute
     * node in the pool. If omitted, any existing application package
     * references are left unchanged.
     */
    private List<ApplicationPackageReference> applicationPackageReferences;

    /**
     * Sets a list of name-value pairs associated with the pool as metadata.
     * If omitted, any existing metadata is left unchanged.
     */
    private List<MetadataItem> metadata;

    /**
     * Get the startTask value.
     *
     * @return the startTask value
     */
    public StartTask startTask() {
        return this.startTask;
    }

    /**
     * Set the startTask value.
     *
     * @param startTask the startTask value to set
     * @return the PoolPatchParameterInner object itself.
     */
    public PoolPatchParameterInner setStartTask(StartTask startTask) {
        this.startTask = startTask;
        return this;
    }

    /**
     * Get the certificateReferences value.
     *
     * @return the certificateReferences value
     */
    public List<CertificateReference> certificateReferences() {
        return this.certificateReferences;
    }

    /**
     * Set the certificateReferences value.
     *
     * @param certificateReferences the certificateReferences value to set
     * @return the PoolPatchParameterInner object itself.
     */
    public PoolPatchParameterInner setCertificateReferences(List<CertificateReference> certificateReferences) {
        this.certificateReferences = certificateReferences;
        return this;
    }

    /**
     * Get the applicationPackageReferences value.
     *
     * @return the applicationPackageReferences value
     */
    public List<ApplicationPackageReference> applicationPackageReferences() {
        return this.applicationPackageReferences;
    }

    /**
     * Set the applicationPackageReferences value.
     *
     * @param applicationPackageReferences the applicationPackageReferences value to set
     * @return the PoolPatchParameterInner object itself.
     */
    public PoolPatchParameterInner setApplicationPackageReferences(List<ApplicationPackageReference> applicationPackageReferences) {
        this.applicationPackageReferences = applicationPackageReferences;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<MetadataItem> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the PoolPatchParameterInner object itself.
     */
    public PoolPatchParameterInner setMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

}
