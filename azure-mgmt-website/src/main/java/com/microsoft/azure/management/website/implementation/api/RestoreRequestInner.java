/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Description of a restore request.
 */
@JsonFlatten
public class RestoreRequestInner extends Resource {
    /**
     * SAS URL to the container.
     */
    @JsonProperty(value = "properties.storageAccountUrl")
    private String storageAccountUrl;

    /**
     * Name of a blob which contains the backup.
     */
    @JsonProperty(value = "properties.blobName")
    private String blobName;

    /**
     * True if the restore operation can overwrite target site. "True" needed
     * if trying to restore over an existing site.
     */
    @JsonProperty(value = "properties.overwrite")
    private Boolean overwrite;

    /**
     * Name of a site (Web App).
     */
    @JsonProperty(value = "properties.siteName")
    private String siteName;

    /**
     * Collection of databses which should be restored. This list has to match
     * the list of databases included in the backup.
     */
    @JsonProperty(value = "properties.databases")
    private List<DatabaseBackupSetting> databases;

    /**
     * Changes a logic when restoring a site with custom domains. If "true",
     * custom domains are removed automatically. If "false", custom domains
     * are added to
     * the site object when it is being restored, but that might
     * fail due to conflicts during the operation.
     */
    @JsonProperty(value = "properties.ignoreConflictingHostNames")
    private Boolean ignoreConflictingHostNames;

    /**
     * Operation type. Possible values include: 'Default', 'Clone',
     * 'Relocation'.
     */
    @JsonProperty(value = "properties.operationType")
    private BackupRestoreOperationType operationType;

    /**
     * Gets or sets a flag showing if SiteConfig.ConnectionStrings should be
     * set in new site.
     */
    @JsonProperty(value = "properties.adjustConnectionStrings")
    private Boolean adjustConnectionStrings;

    /**
     * App Service Environment name, if needed (only when restoring a site to
     * an App Service Environment).
     */
    @JsonProperty(value = "properties.hostingEnvironment")
    private String hostingEnvironment;

    /**
     * Get the storageAccountUrl value.
     *
     * @return the storageAccountUrl value
     */
    public String storageAccountUrl() {
        return this.storageAccountUrl;
    }

    /**
     * Set the storageAccountUrl value.
     *
     * @param storageAccountUrl the storageAccountUrl value to set
     * @return the RestoreRequestInner object itself.
     */
    public RestoreRequestInner withStorageAccountUrl(String storageAccountUrl) {
        this.storageAccountUrl = storageAccountUrl;
        return this;
    }

    /**
     * Get the blobName value.
     *
     * @return the blobName value
     */
    public String blobName() {
        return this.blobName;
    }

    /**
     * Set the blobName value.
     *
     * @param blobName the blobName value to set
     * @return the RestoreRequestInner object itself.
     */
    public RestoreRequestInner withBlobName(String blobName) {
        this.blobName = blobName;
        return this;
    }

    /**
     * Get the overwrite value.
     *
     * @return the overwrite value
     */
    public Boolean overwrite() {
        return this.overwrite;
    }

    /**
     * Set the overwrite value.
     *
     * @param overwrite the overwrite value to set
     * @return the RestoreRequestInner object itself.
     */
    public RestoreRequestInner withOverwrite(Boolean overwrite) {
        this.overwrite = overwrite;
        return this;
    }

    /**
     * Get the siteName value.
     *
     * @return the siteName value
     */
    public String siteName() {
        return this.siteName;
    }

    /**
     * Set the siteName value.
     *
     * @param siteName the siteName value to set
     * @return the RestoreRequestInner object itself.
     */
    public RestoreRequestInner withSiteName(String siteName) {
        this.siteName = siteName;
        return this;
    }

    /**
     * Get the databases value.
     *
     * @return the databases value
     */
    public List<DatabaseBackupSetting> databases() {
        return this.databases;
    }

    /**
     * Set the databases value.
     *
     * @param databases the databases value to set
     * @return the RestoreRequestInner object itself.
     */
    public RestoreRequestInner withDatabases(List<DatabaseBackupSetting> databases) {
        this.databases = databases;
        return this;
    }

    /**
     * Get the ignoreConflictingHostNames value.
     *
     * @return the ignoreConflictingHostNames value
     */
    public Boolean ignoreConflictingHostNames() {
        return this.ignoreConflictingHostNames;
    }

    /**
     * Set the ignoreConflictingHostNames value.
     *
     * @param ignoreConflictingHostNames the ignoreConflictingHostNames value to set
     * @return the RestoreRequestInner object itself.
     */
    public RestoreRequestInner withIgnoreConflictingHostNames(Boolean ignoreConflictingHostNames) {
        this.ignoreConflictingHostNames = ignoreConflictingHostNames;
        return this;
    }

    /**
     * Get the operationType value.
     *
     * @return the operationType value
     */
    public BackupRestoreOperationType operationType() {
        return this.operationType;
    }

    /**
     * Set the operationType value.
     *
     * @param operationType the operationType value to set
     * @return the RestoreRequestInner object itself.
     */
    public RestoreRequestInner withOperationType(BackupRestoreOperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    /**
     * Get the adjustConnectionStrings value.
     *
     * @return the adjustConnectionStrings value
     */
    public Boolean adjustConnectionStrings() {
        return this.adjustConnectionStrings;
    }

    /**
     * Set the adjustConnectionStrings value.
     *
     * @param adjustConnectionStrings the adjustConnectionStrings value to set
     * @return the RestoreRequestInner object itself.
     */
    public RestoreRequestInner withAdjustConnectionStrings(Boolean adjustConnectionStrings) {
        this.adjustConnectionStrings = adjustConnectionStrings;
        return this;
    }

    /**
     * Get the hostingEnvironment value.
     *
     * @return the hostingEnvironment value
     */
    public String hostingEnvironment() {
        return this.hostingEnvironment;
    }

    /**
     * Set the hostingEnvironment value.
     *
     * @param hostingEnvironment the hostingEnvironment value to set
     * @return the RestoreRequestInner object itself.
     */
    public RestoreRequestInner withHostingEnvironment(String hostingEnvironment) {
        this.hostingEnvironment = hostingEnvironment;
        return this;
    }

}
