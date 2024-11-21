// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.sas;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Used to initialize parameters for a Shared Access Signature (SAS) for an Azure Storage account. Once all the values
 * here are set, use the {@code generateAccountSas()} method on the desired service client to obtain a
 * representation of the SAS which can then be applied to a new client using the {@code sasToken(String)} method on
 * the desired client builder.
 *
 * @see <a href=https://docs.microsoft.com/azure/storage/common/storage-sas-overview>Storage SAS overview</a>
 * @see <a href=https://docs.microsoft.com/rest/api/storageservices/create-account-sas>Create an account SAS</a>
 */
@Fluent
public final class TableAccountSasSignatureValues {
    private final OffsetDateTime expiryTime;
    private final String permissions;
    private final String services;
    private final String resourceTypes;
    private String version;
    private TableSasProtocol protocol;
    private OffsetDateTime startTime;
    private TableSasIpRange sasIpRange;

    /**
     * Initializes a new {@link TableAccountSasSignatureValues} object.
     *
     * @param expiryTime The time after which the SAS will no longer work.
     * @param permissions {@link TableAccountSasPermission account permissions} allowed by the SAS.
     * @param services {@link TableAccountSasService account services} targeted by the SAS.
     * @param resourceTypes {@link TableAccountSasResourceType account resource types} targeted by the SAS.
     */
    public TableAccountSasSignatureValues(OffsetDateTime expiryTime, TableAccountSasPermission permissions,
                                          TableAccountSasService services, TableAccountSasResourceType resourceTypes) {

        Objects.requireNonNull(expiryTime, "'expiryTime' cannot be null");
        Objects.requireNonNull(services, "'services' cannot be null");
        Objects.requireNonNull(permissions, "'permissions' cannot be null");
        Objects.requireNonNull(resourceTypes, "'resourceTypes' cannot be null");

        this.expiryTime = expiryTime;
        this.services = services.toString();
        this.resourceTypes = resourceTypes.toString();
        this.permissions = permissions.toString();
    }

    /**
     * Get The time after which the SAS will no longer work.
     *
     * @return The time after which the SAS will no longer work.
     */
    public OffsetDateTime getExpiryTime() {
        return expiryTime;
    }

    /**
     * Gets the operations the SAS user may perform. Please refer to {@link TableAccountSasPermission} to help determine
     * which permissions are allowed.
     *
     * @return The operations the SAS user may perform.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Get the services accessible with this SAS. Please refer to {@link TableAccountSasService} to help determine which
     * services are accessible.
     *
     * @return The services accessible with this SAS.
     */
    public String getServices() {
        return services;
    }

    /**
     * Get the resource types accessible with this SAS. Please refer to {@link TableAccountSasResourceType} to help determine
     * the resource types that are accessible.
     *
     * @return The resource types accessible with this SAS.
     */
    public String getResourceTypes() {
        return resourceTypes;
    }

    /**
     * Get the service version that is targeted, if {@code null} or empty the latest service version targeted by the
     * library will be used.
     *
     * @return The service version that is targeted.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the service version that is targeted. Leave this {@code null} or empty to target the version used by the
     * library.
     *
     * @param version The target version to set.
     *
     * @return The updated {@link TableAccountSasSignatureValues} object.
     */
    public TableAccountSasSignatureValues setVersion(String version) {
        this.version = version;

        return this;
    }

    /**
     * Get the {@link TableSasProtocol} which determines the HTTP protocol that will be used.
     *
     * @return The {@link TableSasProtocol}.
     */
    public TableSasProtocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the {@link TableSasProtocol} which determines the HTTP protocol that will be used.
     *
     * @param protocol The {@link TableSasProtocol} to set.
     *
     * @return The updated {@link TableAccountSasSignatureValues} object.
     */
    public TableAccountSasSignatureValues setProtocol(TableSasProtocol protocol) {
        this.protocol = protocol;

        return this;
    }

    /**
     * Get when the SAS will take effect.
     *
     * @return When the SAS will take effect.
     */
    public OffsetDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets when the SAS will take effect.
     *
     * @param startTime The start time to set.
     *
     * @return The updated {@link TableAccountSasSignatureValues} object.
     */
    public TableAccountSasSignatureValues setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;

        return this;
    }

    /**
     * Get the {@link TableSasIpRange} which determines the IP ranges that are allowed to use the SAS.
     *
     * @return The {@link TableSasIpRange}.
     */
    public TableSasIpRange getSasIpRange() {
        return sasIpRange;
    }

    /**
     * Sets the {@link TableSasIpRange} which determines the IP ranges that are allowed to use the SAS.
     *
     * @param sasIpRange The {@link TableSasIpRange allowed IP range} to set.
     *
     * @return The updated {@link TableAccountSasSignatureValues} object.
     *
     * @see <a href=https://docs.microsoft.com/rest/api/storageservices/create-service-sas#specifying-ip-address-or-ip-range>Specifying
     * IP Address or IP range</a>
     */
    public TableAccountSasSignatureValues setSasIpRange(TableSasIpRange sasIpRange) {
        this.sasIpRange = sasIpRange;

        return this;
    }
}
