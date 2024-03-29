// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.datalakeanalytics.fluent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.management.Resource;
import com.azure.resourcemanager.datalakeanalytics.models.DataLakeAnalyticsAccountState;
import com.azure.resourcemanager.datalakeanalytics.models.DataLakeAnalyticsAccountStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * A Data Lake Analytics account object, containing all information associated with the named Data Lake Analytics
 * account.
 */
@Fluent
public final class DataLakeAnalyticsAccountBasicInner extends Resource {
    /*
     * The properties defined by Data Lake Analytics all properties are specific to each resource provider.
     */
    @JsonProperty(value = "properties", access = JsonProperty.Access.WRITE_ONLY)
    private DataLakeAnalyticsAccountPropertiesBasic innerProperties;

    /** Creates an instance of DataLakeAnalyticsAccountBasicInner class. */
    public DataLakeAnalyticsAccountBasicInner() {
    }

    /**
     * Get the innerProperties property: The properties defined by Data Lake Analytics all properties are specific to
     * each resource provider.
     *
     * @return the innerProperties value.
     */
    private DataLakeAnalyticsAccountPropertiesBasic innerProperties() {
        return this.innerProperties;
    }

    /** {@inheritDoc} */
    @Override
    public DataLakeAnalyticsAccountBasicInner withLocation(String location) {
        super.withLocation(location);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public DataLakeAnalyticsAccountBasicInner withTags(Map<String, String> tags) {
        super.withTags(tags);
        return this;
    }

    /**
     * Get the accountId property: The unique identifier associated with this Data Lake Analytics account.
     *
     * @return the accountId value.
     */
    public UUID accountId() {
        return this.innerProperties() == null ? null : this.innerProperties().accountId();
    }

    /**
     * Get the provisioningState property: The provisioning status of the Data Lake Analytics account.
     *
     * @return the provisioningState value.
     */
    public DataLakeAnalyticsAccountStatus provisioningState() {
        return this.innerProperties() == null ? null : this.innerProperties().provisioningState();
    }

    /**
     * Get the state property: The state of the Data Lake Analytics account.
     *
     * @return the state value.
     */
    public DataLakeAnalyticsAccountState state() {
        return this.innerProperties() == null ? null : this.innerProperties().state();
    }

    /**
     * Get the creationTime property: The account creation time.
     *
     * @return the creationTime value.
     */
    public OffsetDateTime creationTime() {
        return this.innerProperties() == null ? null : this.innerProperties().creationTime();
    }

    /**
     * Get the lastModifiedTime property: The account last modified time.
     *
     * @return the lastModifiedTime value.
     */
    public OffsetDateTime lastModifiedTime() {
        return this.innerProperties() == null ? null : this.innerProperties().lastModifiedTime();
    }

    /**
     * Get the endpoint property: The full CName endpoint for this account.
     *
     * @return the endpoint value.
     */
    public String endpoint() {
        return this.innerProperties() == null ? null : this.innerProperties().endpoint();
    }

    /**
     * Validates the instance.
     *
     * @throws IllegalArgumentException thrown if the instance is not valid.
     */
    public void validate() {
        if (innerProperties() != null) {
            innerProperties().validate();
        }
    }
}
