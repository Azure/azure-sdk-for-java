// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.sas;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Used to initialize parameters for a Shared Access Signature (SAS) for the Azure Table Storage service. Once all the
 * values here are set, use the {@code generateSas()} method on the desired Table client to obtain a representation
 * of the SAS which can then be applied to a new client using the {@code sasToken(String)} method on the desired
 * client builder.
 *
 * @see <a href=https://docs.microsoft.com/azure/storage/common/storage-sas-overview>Storage SAS overview</a>
 * @see <a href=https://docs.microsoft.com/rest/api/storageservices/constructing-a-service-sas>Constructing a Service SAS</a>
 */
@Fluent
public final class TableSasSignatureValues {
    private String version;
    private TableSasProtocol protocol;
    private OffsetDateTime startTime;
    private OffsetDateTime expiryTime;
    private String permissions;
    private TableSasIpRange sasIpRange;
    private String identifier;
    private String startPartitionKey;
    private String startRowKey;
    private String endPartitionKey;
    private String endRowKey;

    /**
     * Creates an object with the specified expiry time and permissions.
     *
     * @param expiryTime The time after which the SAS will no longer work.
     * @param permissions {@link TableSasPermission table permissions} allowed by the SAS.
     */
    public TableSasSignatureValues(OffsetDateTime expiryTime, TableSasPermission permissions) {
        Objects.requireNonNull(expiryTime, "'expiryTime' cannot be null");
        Objects.requireNonNull(permissions, "'permissions' cannot be null");

        this.expiryTime = expiryTime;
        this.permissions = permissions.toString();
    }

    /**
     * Creates an object with the specified identifier.
     *
     * @param identifier Name of the access policy.
     */
    public TableSasSignatureValues(String identifier) {
        Objects.requireNonNull(identifier, "'identifier' cannot be null");

        this.identifier = identifier;
    }

    /**
     * @return The version of the service this SAS will target. If not specified, it will default to the version
     * targeted by the library.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the service this SAS will target. If not specified, it will default to the version targeted
     * by the library.
     *
     * @param version Version to target
     *
     * @return The updated {@link TableSasSignatureValues} object.
     */
    public TableSasSignatureValues setVersion(String version) {
        this.version = version;

        return this;
    }

    /**
     * @return The {@link TableSasProtocol} which determines the protocols allowed by the SAS.
     */
    public TableSasProtocol getProtocol() {
        return protocol;
    }

    /**
     * Sets the {@link TableSasProtocol} which determines the protocols allowed by the SAS.
     *
     * @param protocol Protocol for the SAS
     *
     * @return The updated {@link TableSasSignatureValues} object.
     */
    public TableSasSignatureValues setProtocol(TableSasProtocol protocol) {
        this.protocol = protocol;

        return this;
    }

    /**
     * @return When the SAS will take effect.
     */
    public OffsetDateTime getStartTime() {
        return startTime;
    }

    /**
     * Sets when the SAS will take effect.
     *
     * @param startTime When the SAS takes effect
     *
     * @return The updated {@link TableSasSignatureValues} object.
     */
    public TableSasSignatureValues setStartTime(OffsetDateTime startTime) {
        this.startTime = startTime;

        return this;
    }

    /**
     * @return The time after which the SAS will no longer work.
     */
    public OffsetDateTime getExpiryTime() {
        return expiryTime;
    }

    /**
     * Sets the time after which the SAS will no longer work.
     *
     * @param expiryTime When the SAS will no longer work
     *
     * @return The updated {@link TableSasSignatureValues} object.
     */
    public TableSasSignatureValues setExpiryTime(OffsetDateTime expiryTime) {
        this.expiryTime = expiryTime;

        return this;
    }

    /**
     * @return The permissions string allowed by the SAS. Please refer to {@link TableSasPermission} for help
     * determining the permissions allowed.
     */
    public String getPermissions() {
        return permissions;
    }

    /**
     * Sets the permissions string allowed by the SAS. Please refer to {@link TableSasPermission} for help constructing
     * the permissions string.
     *
     * @param permissions Permissions for the SAS
     *
     * @return The updated {@link TableSasSignatureValues} object.
     *
     * @throws NullPointerException if {@code permissions} is null.
     */
    public TableSasSignatureValues setPermissions(TableSasPermission permissions) {
        Objects.requireNonNull(permissions, "'permissions' cannot be null");

        this.permissions = permissions.toString();

        return this;
    }

    /**
     * @return The {@link TableSasIpRange} which determines the IP ranges that are allowed to use the SAS.
     */
    public TableSasIpRange getSasIpRange() {
        return sasIpRange;
    }

    /**
     * Sets the {@link TableSasIpRange} which determines the IP ranges that are allowed to use the SAS.
     *
     * @param sasIpRange Allowed IP range to set
     *
     * @return The updated {@link TableSasSignatureValues} object.
     *
     * @see <a href=https://docs.microsoft.com/rest/api/storageservices/create-service-sas#specifying-ip-address-or-ip-range>Specifying
     * IP Address or IP range</a>
     */
    public TableSasSignatureValues setSasIpRange(TableSasIpRange sasIpRange) {
        this.sasIpRange = sasIpRange;

        return this;
    }

    /**
     * @return The name of the access policy on the table this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Sets the name of the access policy on the table this SAS references if any. Please see
     * <a href="https://docs.microsoft.com/rest/api/storageservices/establishing-a-stored-access-policy">here</a>
     * for more information.
     *
     * @param identifier Name of the access policy
     *
     * @return The updated {@link TableSasSignatureValues} object.
     */
    public TableSasSignatureValues setIdentifier(String identifier) {
        this.identifier = identifier;

        return this;
    }

    /**
     * Get the minimum partition key accessible with this shared access signature. Key values are inclusive. If omitted,
     * there is no lower bound on the table entities that can be accessed. If provided, it must accompany a start row
     * key that can be set via {@code setStartRowKey()}.
     *
     * @return The start partition key.
     */
    public String getStartPartitionKey() {
        return this.startPartitionKey;
    }

    /**
     * Set the minimum partition key accessible with this shared access signature. Key values are inclusive. If omitted,
     * there is no lower bound on the table entities that can be accessed. If provided, it must accompany a start row
     * key that can be set via {@code setStartRowKey()}.
     *
     * @param startPartitionKey The start partition key to set.
     *
     * @return The updated {@link TableSasSignatureValues} object.
     */
    public TableSasSignatureValues setStartPartitionKey(String startPartitionKey) {
        this.startPartitionKey = startPartitionKey;
        return this;
    }

    /**
     * Get the minimum row key accessible with this shared access signature. Key values are inclusive. If omitted, there
     * is no lower bound on the table entities that can be accessed. If provided, it must accompany a start row key
     * that can be set via {@code setStartPartitionKey()}.
     *
     * @return The start row key.
     */
    public String getStartRowKey() {
        return this.startRowKey;
    }

    /**
     * Set the minimum row key accessible with this shared access signature. Key values are inclusive. If omitted, there
     * is no lower bound on the table entities that can be accessed. If provided, it must accompany a start row key
     * that can be set via {@code setStartPartitionKey()}.
     *
     * @param startRowKey The start row key to set.
     *
     * @return The updated {@link TableSasSignatureValues} object.
     */
    public TableSasSignatureValues setStartRowKey(String startRowKey) {
        this.startRowKey = startRowKey;

        return this;
    }

    /**
     * Get the maximum partition key accessible with this shared access signature. Key values are inclusive. If omitted,
     * there is no upper bound on the table entities that can be accessed. If provided, it must accompany an ending row
     * key that can be set via {@code setEndRowKey()}.
     *
     * @return The end partition key.
     */
    public String getEndPartitionKey() {
        return this.endPartitionKey;
    }

    /**
     * Set the maximum partition key accessible with this shared access signature. Key values are inclusive. If omitted,
     * there is no upper bound on the table entities that can be accessed. If provided, it must accompany an ending row
     * key that can be set via {@code setEndRowKey()}.
     *
     * @param endPartitionKey The end partition key to set.
     *
     * @return The updated {@link TableSasSignatureValues} object.
     */
    public TableSasSignatureValues setEndPartitionKey(String endPartitionKey) {
        this.endPartitionKey = endPartitionKey;

        return this;
    }

    /**
     * Get the maximum row key accessible with this shared access signature. Key values are inclusive. If omitted, there
     * is no upper bound on the table entities that can be accessed. If provided, it must accompany an ending row key
     * that can be set via {@code setEndPartitionKey()}.
     *
     * @return The end row key.
     */
    public String getEndRowKey() {
        return this.endRowKey;
    }

    /**
     * Set the maximum row key accessible with this shared access signature. Key values are inclusive. If omitted, there
     * is no upper bound on the table entities that can be accessed. If provided, it must accompany an ending row key
     * that can be set via {@code setEndPartitionKey()}.
     *
     * @param endRowKey The end row key to set.
     *
     * @return The updated {@link TableSasSignatureValues} object.
     */
    public TableSasSignatureValues setEndRowKey(String endRowKey) {
        this.endRowKey = endRowKey;

        return this;
    }
}
