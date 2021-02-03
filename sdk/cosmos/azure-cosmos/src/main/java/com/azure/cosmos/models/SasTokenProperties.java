// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.sastokens.SasTokenImpl;
import com.azure.cosmos.util.Beta;

import java.time.Duration;
import java.time.Instant;

/**
 * Represents a permission configuration object to be used when creating a Cosmos DB shared access signature token.
 */
@Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public interface SasTokenProperties {
    /**
     * Gets the name of the Cosmos DB database to grant access to.
     *
     * If database name is an empty string then the access is granted at account level.
     *
     * @return the name of the database to grant access to.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getDatabaseName();

    /**
     * Sets the name of the Cosmos DB database within which the target resource belongs to and for which to grant access to.
     *
     * If database name is an empty string then the access is granted at account level.
     *
     * @param databaseName the name of the database to grant access to.
     * @return the current permission configuration object.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SasTokenProperties setDatabaseName(String databaseName);

    /**
     * Gets the name of the Cosmos DB container to grant access to or as the parent resource of the target reosource.
     *
     * A valid non-empty database name must be set; if the container name is an empty string then
     *   the access is granted at database level.
     *
     * @return the name of the container to grant access to.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getContainerName();

    /**
     * Gets the name of the Cosmos DB container to grant access to.
     *
     * A valid non-empty database name must be set; if the container name is an empty string then
     *   the access is granted at database level.
     *
     * @param containerName the name of the container to grant access to.
     * @return the current permission configuration object.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SasTokenProperties setContainerName(String containerName);

    /**
     * Gets the type of the Cosmos resources to grant access to.
     *
     * A valid non-empty container name must be set in combination with this setting.
     *
     * @return the type of the resources to grant access to.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    CosmosContainerChildResourceKind getResourceKind();

    /**
     * Gets the name of the Cosmos DB resources to grant access to.
     *
     * A valid non-empty container name must be set in combination with this setting.
     *
     * @return the name of the resources to grant access to.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getResourceName();

    /**
     * Sets the name of the Cosmos DB resource to grant access to.
     *
     * A valid non-empty container name must be set in combination with this setting.
     *
     * @param kind the type of the resource (item, stored procedure etc) to grant access to.
     * @param resourceName the prefix name of the resources to grant access to.
     * @return the current permission configuration object.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SasTokenProperties setResourceName(CosmosContainerChildResourceKind kind, String resourceName);

    /**
     * Gets the user name or ID registered with this permission object.
     *
     * A user name or ID is a unique identifier which will be used for tracing and auditing purposes in combination
     *   with the permission token when authenticating and authorizing Cosmos operations.
     *
     * @return the name or ID of the user associated with this token.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getUser();

    /**
     * Sets the user name or ID registered with this permission object.
     *
     * A user name or ID is a unique identifier which will be used for tracing and auditing purposes in combination
     *   with the permission token when authenticating and authorizing Cosmos operations. A non-empty and maximum of
     *   40 characters in length string can be set for the user; if a user name or ID is not specified, a random 10
     *   length string will be used instead.
     *
     * @param user the name or ID of the user associated with this token.
     * @return the current permission configuration object.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SasTokenProperties setUser(String user);

    /**
     * Gets the tag for the user name or ID registered with this permission object.
     *
     * The user tag is a unique identifier which will be used for tracing and auditing purposes in combination
     *   with the permission token when authenticating and authorizing Cosmos operations.
     *
     * @return the tag corresponding to the user associated with this token.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getUserTag();

    /**
     * Sets the tag for the user name or ID registered with this permission object.
     *
     * The user tag is a unique identifier which will be used for tracing and auditing purposes in combination
     *   with the permission token when authenticating and authorizing Cosmos operations.
     *
     * @param userTag the tag corresponding to the user associated with this token.
     * @return the current permission configuration object.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SasTokenProperties setUserTag(String userTag);

    /**
     * Gets the expiry time (GMT time zone) for the shared access signature associated with the permission instance.
     *
     * @return the expiry time (GMT time zone) for a shared access signature associated with the permission instance.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    Instant getExpiryTime();

    /**
     * Sets the expiry time for the shared access signature associated with the permission instance.
     *<p>
     * Default is 2 hours from start time; the maximum duration allowed to be set as expiry time is 24 hours from
     *   the specified start time.
     *
     * @param expiryTime the expiry time for a shared access signature token associated with the permission
     *   instance, up to 24 hours from the start time.
     * @return the current permission configuration object.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SasTokenProperties setExpiryTime(Duration expiryTime);

    /**
     * Gets the start time (GMT time zone) for the shared access signature associated with the permission instance.
     *
     * @return the start time (GMT time zone) for a shared access signature associated with the permission instance.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    Instant getStartTime();

    /**
     * Sets the start time for the shared access signature associated with the permission instance.
     *
     * Default is current time (now).
     *
     * @param startTime the start time for a shared access signature associated with the permission instance.
     * @return the current permission configuration object.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SasTokenProperties setStartTime(Instant startTime);

    /**
     * Gets the list of partition key value ranges to be used when creating a shared access signature token.
     *
     * @return the set of partition key value ranges to be used when creating a shared access signature token.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    Iterable<SasTokenPartitionKeyValueRange> getPartitionKeyValueRanges();

    /**
     * Sets the list of partition key values to be used when creating a shared access signature token.
     *
     * In the presence of this setting only operation using documents with the partition key value within these ranges
     *   are allowed; default is empty set, any partition key value is allowed.
     *
     * @param partitionKeyValues the list of partition key values to be used when creating a shared access signature token.
     * @return the current permission configuration object.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SasTokenProperties setPartitionKeyValueRanges(Iterable<String> partitionKeyValues);

    /**
     * Adds a partition key value to be used when creating a shared access signature token.
     *
     * In the presence of this setting only operation using documents with the partition key value within these ranges
     *   are allowed; default is empty set, any partition key value is allowed.
     *
     * @param partitionKeyValue the partition key value to be used when creating a shared access signature token.
     * @return the current permission configuration object.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SasTokenProperties addPartitionKeyValue(String partitionKeyValue);

    /**
     * Adds a permission setting to execute specific Cosmos operation or set of operations.
     * If no specific permission was set, default is read permissions at the container level.
     *
     * @param permissionKind the permission setting which allows execution of specific Cosmos operation or set of operations.
     * @return the current permission configuration object.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    SasTokenProperties addPermission(SasTokenPermissionKind permissionKind);

    /**
     * Creates a permission configuration to be used when creating a Cosmos shared access signature token.
     *
     * @param user the user that will be associated with this shared access signature token.
     * @param databaseName the database name that will be associated with this shared access signature token.
     * @param containerName the container name that will be associated with this shared access signature token.
     * @return an instance of {@link SasTokenProperties} that will be used to generated the token.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    static SasTokenProperties create(String user, String databaseName, String containerName) {
        return new SasTokenImpl()
            .setUser(user)
            .setDatabaseName(databaseName)
            .setContainerName(containerName);
    }

    /**
     * Creates a Cosmos shared access signature token using the specified account key and a HMACSHA256 encoder.
     *
     * @param key the Cosmos key that will be used to generate a shared access signature token.
     * @return the shared access signature token.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getSasTokenValueUsingHMAC(String key);

    /**
     * Creates a Cosmos shared access signature token using the specified account key and a HMACSHA256 encoder.
     * <p>
     * Providing key type will help expedite the authentication and authorization executed by the Cosmos service.
     *
     * @param key the Cosmos key that will be used to generate a shared access signature token.
     * @param keyType the Cosmos key type that will be used to generate a shared access signature token.
     * @return the shared access signature token.
     */
    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getSasTokenValueUsingHMAC(String key, CosmosKeyType keyType);

    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    enum CosmosContainerChildResourceKind {
        ITEM,
        STORED_PROCEDURE,
        USER_DEFINED_FUNCTION,
        TRIGGER
    }

    @Beta(value = Beta.SinceVersion.V4_11_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    enum CosmosKeyType {
        PRIMARY_MASTER,
        SECONDARY_MASTER,
        PRIMARY_READONLY,
        SECONDARY_READONLY
    }
}
