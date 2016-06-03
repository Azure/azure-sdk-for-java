/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Parameters for a CloudPoolOperations.UpdateProperties request.
 */
public class PoolUpdatePropertiesParameter {
    /**
     * A task to run on each compute node as it joins the pool. If omitted,
     * any existing start task is removed from the pool.
     */
    private StartTask startTask;

    /**
     * A list of certificates to be installed on each compute node in the
     * pool. If you specify an empty collection, any existing certificate
     * references are removed from the pool.
     */
    @JsonProperty(required = true)
    private List<CertificateReference> certificateReferences;

    /**
     * A list of application packages to be installed on each compute node in
     * the pool. If you specify an empty collection, any existing application
     * packages references are removed from the pool.
     */
    @JsonProperty(required = true)
    private List<ApplicationPackageReference> applicationPackageReferences;

    /**
     * A list of name-value pairs associated with the pool as metadata. If you
     * specify an empty collection, any existing metadata is removed from the
     * pool.
     */
    @JsonProperty(required = true)
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
     * @return the PoolUpdatePropertiesParameter object itself.
     */
    public PoolUpdatePropertiesParameter withStartTask(StartTask startTask) {
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
     * @return the PoolUpdatePropertiesParameter object itself.
     */
    public PoolUpdatePropertiesParameter withCertificateReferences(List<CertificateReference> certificateReferences) {
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
     * @return the PoolUpdatePropertiesParameter object itself.
     */
    public PoolUpdatePropertiesParameter withApplicationPackageReferences(List<ApplicationPackageReference> applicationPackageReferences) {
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
     * @return the PoolUpdatePropertiesParameter object itself.
     */
    public PoolUpdatePropertiesParameter withMetadata(List<MetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

}
