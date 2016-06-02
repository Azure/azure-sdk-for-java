/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.datalake.analytics.implementation.api;

import java.util.List;
import org.joda.time.DateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The account specific properties that are associated with an underlying Data
 * Lake Analytics account.
 */
public class DataLakeAnalyticsAccountProperties {
    /**
     * Gets the provisioning status of the Data Lake Analytics account.
     * Possible values include: 'Failed', 'Creating', 'Running', 'Succeeded',
     * 'Patching', 'Suspending', 'Resuming', 'Deleting', 'Deleted'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private DataLakeAnalyticsAccountStatus provisioningState;

    /**
     * Gets the state of the Data Lake Analytics account. Possible values
     * include: 'active', 'suspended'.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private DataLakeAnalyticsAccountState state;

    /**
     * Gets or sets the default data lake storage account associated with this
     * Data Lake Analytics account.
     */
    private String defaultDataLakeStoreAccount;

    /**
     * Gets or sets the maximum supported degree of parallelism for this
     * acocunt.
     */
    private Integer maxDegreeOfParallelism;

    /**
     * Gets or sets the maximum supported jobs running under the account at
     * the same time.
     */
    private Integer maxJobCount;

    /**
     * Gets or sets the list of Data Lake storage accounts associated with
     * this account.
     */
    private List<DataLakeStoreAccountInfoInner> dataLakeStoreAccounts;

    /**
     * Gets or sets the list of Azure Blob storage accounts associated with
     * this account.
     */
    private List<StorageAccountInfoInner> storageAccounts;

    /**
     * Gets or sets the account creation time.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private DateTime creationTime;

    /**
     * Gets or sets the account last modified time.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private DateTime lastModifiedTime;

    /**
     * Gets or sets the full CName endpoint for this account.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String endpoint;

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public DataLakeAnalyticsAccountStatus provisioningState() {
        return this.provisioningState;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public DataLakeAnalyticsAccountState state() {
        return this.state;
    }

    /**
     * Get the defaultDataLakeStoreAccount value.
     *
     * @return the defaultDataLakeStoreAccount value
     */
    public String defaultDataLakeStoreAccount() {
        return this.defaultDataLakeStoreAccount;
    }

    /**
     * Set the defaultDataLakeStoreAccount value.
     *
     * @param defaultDataLakeStoreAccount the defaultDataLakeStoreAccount value to set
     * @return the DataLakeAnalyticsAccountProperties object itself.
     */
    public DataLakeAnalyticsAccountProperties withDefaultDataLakeStoreAccount(String defaultDataLakeStoreAccount) {
        this.defaultDataLakeStoreAccount = defaultDataLakeStoreAccount;
        return this;
    }

    /**
     * Get the maxDegreeOfParallelism value.
     *
     * @return the maxDegreeOfParallelism value
     */
    public Integer maxDegreeOfParallelism() {
        return this.maxDegreeOfParallelism;
    }

    /**
     * Set the maxDegreeOfParallelism value.
     *
     * @param maxDegreeOfParallelism the maxDegreeOfParallelism value to set
     * @return the DataLakeAnalyticsAccountProperties object itself.
     */
    public DataLakeAnalyticsAccountProperties withMaxDegreeOfParallelism(Integer maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        return this;
    }

    /**
     * Get the maxJobCount value.
     *
     * @return the maxJobCount value
     */
    public Integer maxJobCount() {
        return this.maxJobCount;
    }

    /**
     * Set the maxJobCount value.
     *
     * @param maxJobCount the maxJobCount value to set
     * @return the DataLakeAnalyticsAccountProperties object itself.
     */
    public DataLakeAnalyticsAccountProperties withMaxJobCount(Integer maxJobCount) {
        this.maxJobCount = maxJobCount;
        return this;
    }

    /**
     * Get the dataLakeStoreAccounts value.
     *
     * @return the dataLakeStoreAccounts value
     */
    public List<DataLakeStoreAccountInfoInner> dataLakeStoreAccounts() {
        return this.dataLakeStoreAccounts;
    }

    /**
     * Set the dataLakeStoreAccounts value.
     *
     * @param dataLakeStoreAccounts the dataLakeStoreAccounts value to set
     * @return the DataLakeAnalyticsAccountProperties object itself.
     */
    public DataLakeAnalyticsAccountProperties withDataLakeStoreAccounts(List<DataLakeStoreAccountInfoInner> dataLakeStoreAccounts) {
        this.dataLakeStoreAccounts = dataLakeStoreAccounts;
        return this;
    }

    /**
     * Get the storageAccounts value.
     *
     * @return the storageAccounts value
     */
    public List<StorageAccountInfoInner> storageAccounts() {
        return this.storageAccounts;
    }

    /**
     * Set the storageAccounts value.
     *
     * @param storageAccounts the storageAccounts value to set
     * @return the DataLakeAnalyticsAccountProperties object itself.
     */
    public DataLakeAnalyticsAccountProperties withStorageAccounts(List<StorageAccountInfoInner> storageAccounts) {
        this.storageAccounts = storageAccounts;
        return this;
    }

    /**
     * Get the creationTime value.
     *
     * @return the creationTime value
     */
    public DateTime creationTime() {
        return this.creationTime;
    }

    /**
     * Get the lastModifiedTime value.
     *
     * @return the lastModifiedTime value
     */
    public DateTime lastModifiedTime() {
        return this.lastModifiedTime;
    }

    /**
     * Get the endpoint value.
     *
     * @return the endpoint value
     */
    public String endpoint() {
        return this.endpoint;
    }

}
