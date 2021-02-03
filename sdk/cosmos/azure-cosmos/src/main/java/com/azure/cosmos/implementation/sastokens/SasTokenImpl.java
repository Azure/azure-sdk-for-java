// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.sastokens;

import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.models.SasTokenPartitionKeyValueRange;
import com.azure.cosmos.models.SasTokenPermissionKind;
import com.azure.cosmos.models.SasTokenProperties;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.azure.cosmos.implementation.sastokens.ControlPlanePermissionScope.SCOPE_CONTAINER_READ;
import static com.azure.cosmos.implementation.sastokens.ControlPlanePermissionScope.SCOPE_CONTAINER_READ_OFFER;
import static com.azure.cosmos.implementation.sastokens.ControlPlanePermissionScope.SCOPE_CONTAINER_WRITE_ALL_ACCESS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_CREATE_ITEMS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_CREATE_STORED_PROCEDURES;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_CREATE_TRIGGERS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_CREATE_USER_DEFINED_FUNCTIONS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_DELETE_CONFLICTS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_DELETE_ITEMS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_DELETE_STORED_PROCEDURES;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_DELETE_TRIGGERS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_DELETE_USER_DEFINED_FUNCTIONS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_EXECUTE_QUERIES;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_EXECUTE_STORED_PROCEDURES;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_READ_ALL_ACCESS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_READ_CONFLICTS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_READ_FEEDS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_READ_STORED_PROCEDURES;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_READ_TRIGGERS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_READ_USER_DEFINED_FUNCTIONS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_REPLACE_ITEMS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_REPLACE_STORED_PROCEDURES;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_REPLACE_TRIGGERS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_REPLACE_USER_DEFINED_FUNCTIONS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_CONTAINER_UPSERT_ITEMS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_ITEM_DELETE;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_ITEM_READ;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_ITEM_READ_ALL_ACCESS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_ITEM_REPLACE;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_ITEM_UPSERT;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_ITEM_WRITE_ALL_ACCESS;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_STORED_PROCEDURE_DELETE;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_STORED_PROCEDURE_EXECUTE;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_STORED_PROCEDURE_READ;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_STORED_PROCEDURE_REPLACE;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_TRIGGER_DELETE;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_TRIGGER_READ;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_TRIGGER_REPLACE;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_USER_DEFINED_FUNCTION_DELETE;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_USER_DEFINED_FUNCTION_READ;
import static com.azure.cosmos.implementation.sastokens.DataPlanePermissionScope.SCOPE_USER_DEFINED_FUNCTION_REPLACE;

/**
 * Represents the implementation of a permission configuration object to be used when creating a Cosmos shared access
 *   signature token.
 */
public class SasTokenImpl implements SasTokenProperties {
    private static final String AUTH_PREFIX = "type=sas&ver=1.0&sig=";
    private static final String SAS_TOKEN_SEPARATOR = ";";

    String user;
    String userTag;
    String databaseName;
    String containerName;
    String resourceName;
    String resourcePath;
    CosmosContainerChildResourceKind resourceKind;
    List<SasTokenPartitionKeyValueRange> partitionKeyValueRanges;
    Instant startTime;
    Instant expiryTime;

    byte keyType;

    short controlPlaneReaderScope;
    short controlPlaneWriterScope;
    int dataPlaneReaderScope;
    int dataPlaneWriterScope;

    public SasTokenImpl() {
        this.user = "";
        this.userTag = "";
        this.databaseName = "";
        this.containerName = "";
        this.resourceName = "";
        this.resourcePath = "";
        this.startTime = Instant.now();
        this.expiryTime = null;
        this.controlPlaneReaderScope = 0;
        this.controlPlaneWriterScope = 0;
        this.dataPlaneReaderScope = 0;
        this.dataPlaneWriterScope = 0;
        this.keyType = 0;
    }

    /**
     * Generates the payload representing the permission configuration for the sas token.
     * <p>
     * The payload is composed of the following:
     * {@literal
     *    user\n
     *    userTag\n
     *    resourcePathPrefix\n
     *    partitionRangesCommaSeparated\n
     *    epochStartTime\n
     *    epochExpiryTime\n
     *    keyKind\n
     *    shortAsHexControlPlaneReaderScope\n
     *    shortAsHexControlPlaneWriterScope\n
     *    intAsHexDataPlaneReaderScope\n
     *    intAsHexDataPlaneWriterScope\n
     * }
     *
     * @return the permission configuration payload.
     */
    private String generatePayload() {
        StringBuilder resourcePrefixPath = new StringBuilder();

        if (!this.databaseName.isEmpty()) {
            resourcePrefixPath.append(Paths.ROOT).append(Paths.DATABASES_PATH_SEGMENT)
                .append("/").append(this.databaseName);
        }

        if (!this.containerName.isEmpty()) {
            if (this.databaseName.isEmpty()) {
                throw new IllegalArgumentException("databaseName");
            }

            resourcePrefixPath.append(Paths.ROOT).append(Paths.COLLECTIONS_PATH_SEGMENT)
                .append(Paths.ROOT).append(this.containerName);
        }

        if (!this.resourceName.isEmpty()) {
            if (this.containerName.isEmpty()) {
                throw new IllegalArgumentException("containerName");
            }

            switch (this.resourceKind) {
                case ITEM:
                    resourcePrefixPath.append(Paths.ROOT).append(Paths.DOCUMENTS_PATH_SEGMENT);
                    break;
                case STORED_PROCEDURE:
                    resourcePrefixPath.append(Paths.ROOT).append(Paths.STORED_PROCEDURES_PATH_SEGMENT);
                    break;
                case USER_DEFINED_FUNCTION:
                    resourcePrefixPath.append(Paths.ROOT).append(Paths.USER_DEFINED_FUNCTIONS_PATH_SEGMENT);
                    break;
                case TRIGGER:
                    resourcePrefixPath.append(Paths.ROOT).append(Paths.TRIGGERS_PATH_SEGMENT);
                    break;
                default:
                    throw new IllegalArgumentException("resourceKind");
            }

            resourcePrefixPath.append(Paths.ROOT).append(this.resourceName);
        }

        resourcePrefixPath.append(Paths.ROOT);
        this.resourcePath = resourcePrefixPath.toString();

        StringBuilder partitionRanges = new StringBuilder();
        if (this.partitionKeyValueRanges != null && !this.partitionKeyValueRanges.isEmpty()) {
            if (this.resourceKind != CosmosContainerChildResourceKind.ITEM) {
                throw new IllegalArgumentException("partitionKeyValueRanges");
            }

            this.partitionKeyValueRanges.forEach(range -> partitionRanges.append(range.encode()).append(","));
        }

        if (this.expiryTime == null) {
            this.expiryTime = this.startTime.plus(Duration.ofHours(2));
        }

        if (this.controlPlaneReaderScope == 0) {
            this.controlPlaneReaderScope |= SCOPE_CONTAINER_READ.value();
            this.controlPlaneReaderScope |= SCOPE_CONTAINER_READ_OFFER.value();
        }

        if (this.dataPlaneReaderScope == 0 && this.dataPlaneWriterScope == 0) {
            this.dataPlaneReaderScope |= SCOPE_CONTAINER_READ_ALL_ACCESS.value();
        }

        StringBuilder payload = new StringBuilder(this.user).append("\n")
            .append(this.userTag).append("\n")
            .append(resourcePrefixPath).append("\n")
            .append(partitionRanges).append("\n")
            .append(String.format("%X", this.startTime.getEpochSecond())).append("\n")
            .append(String.format("%X", this.expiryTime.getEpochSecond())).append("\n")
            .append(String.format("%X", this.keyType)).append("\n")
            .append(String.format("%X", this.controlPlaneReaderScope)). append("\n")
            .append(String.format("%X", this.controlPlaneWriterScope)). append("\n")
            .append(String.format("%X", this.dataPlaneReaderScope)). append("\n")
            .append(String.format("%X", this.dataPlaneWriterScope)). append("\n");

        return Utils.encodeBase64String(payload.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String getSasTokenWithHMACSHA256(String key) {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        byte[] keyDecodedBytes = Utils.Base64Decoder.decode(key);
        SecretKey signingKey = new SecretKeySpec(keyDecodedBytes, "HMACSHA256");
        try {
            Mac macInstance = Mac.getInstance("HMACSHA256");
            macInstance.init(signingKey);

            // Get payload which is Base64 encoded
            String payload = this.generatePayload();
            byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);
            byte[] digest = macInstance.doFinal(payloadBytes);
            String authorizationToken = Utils.encodeBase64String(digest);

            StringBuilder token = new StringBuilder(AUTH_PREFIX)
                .append(authorizationToken)
                .append(SAS_TOKEN_SEPARATOR)
                .append(payload);

            return token.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Creates a Cosmos shared access signature token using the specified account key and a HMACSHA256 encoder.
     *
     * @param key the Cosmos key that will be used to generate a shared access signature token.
     * @return the shared access signature token.
     */
    @Override
    public String getSasTokenValueUsingHMAC(String key) {
        this.keyType = 0;
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key");
        }

        return getSasTokenWithHMACSHA256(key);
    }

    /**
     * Create a Cosmos shared access signature token using the specified account key and a HMACSHA256 encoder.
     * <p>
     * Providing key type will help expedite the authentication and authorization executed by the Cosmos service.
     *
     * @param key the Cosmos key that will be used to generate a shared access signature token.
     * @param keyType the Cosmos key type that will be used to generate a shared access signature token.
     * @return the shared access signature token.
     */
    @Override
    public String getSasTokenValueUsingHMAC(String key, CosmosKeyType keyType) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("key");
        }

        switch (keyType) {
            case PRIMARY_MASTER:
                this.keyType = 1;
                break;
            case SECONDARY_MASTER:
                this.keyType = 2;
                break;
            case PRIMARY_READONLY:
                this.keyType = 3;
                break;
            case SECONDARY_READONLY:
                this.keyType = 4;
                break;
            default:
                throw new IllegalArgumentException("keyType");
        }

        return getSasTokenWithHMACSHA256(key);
    }

    @Override
    public String getDatabaseName() {
        return this.databaseName;
    }

    @Override
    public SasTokenProperties setDatabaseName(String databaseName) {
        if (databaseName == null || Utils.trimBeginningAndEndingSlashes(databaseName).isEmpty()) {
            throw new IllegalArgumentException("databaseName");
        }

        this.databaseName = Utils.trimBeginningAndEndingSlashes(databaseName);

        return this;
    }

    @Override
    public String getContainerName() {
        return this.containerName;
    }

    @Override
    public SasTokenProperties setContainerName(String containerName) {
        if (containerName == null || Utils.trimBeginningAndEndingSlashes(containerName).isEmpty()) {
            throw new IllegalArgumentException("containerName");
        }

        this.containerName = Utils.trimBeginningAndEndingSlashes(containerName);

        return this;
    }

    @Override
    public CosmosContainerChildResourceKind getResourceKind() {
        return this.resourceKind;
    }

    @Override
    public String getResourceName() {
        return this.resourceName;
    }

    @Override
    public SasTokenProperties setResourceName(CosmosContainerChildResourceKind kind, String resourceName) {
        if (resourceName == null) {
            throw new IllegalArgumentException("resourceName");
        }

        this.resourceName = Utils.trimBeginningAndEndingSlashes(resourceName);
        this.resourceKind = kind;

        return this;
    }

    @Override
    public String getUser() {
        return this.user;
    }

    @Override
    public SasTokenProperties setUser(String user) {
        if (user == null || user.isEmpty()) {
            throw new IllegalArgumentException("user");
        }

        this.user = user;

        return this;
    }

    @Override
    public String getUserTag() {
        return this.userTag;
    }

    @Override
    public SasTokenProperties setUserTag(String userTag) {
        if (userTag == null) {
            throw new IllegalArgumentException("userTag");
        }

        this.userTag = userTag;

        return this;
    }

    @Override
    public Instant getExpiryTime() {
        return this.expiryTime;
    }

    @Override
    public SasTokenProperties setExpiryTime(Duration expiryTime) {
        if (expiryTime == null) {
            throw new IllegalArgumentException("expiryTime");
        }

        this.expiryTime = startTime.plus(expiryTime);

        return this;
    }

    @Override
    public Instant getStartTime() {
        return this.startTime;
    }

    @Override
    public SasTokenProperties setStartTime(Instant startTime) {
        if (startTime == null) {
            throw new IllegalArgumentException("startTime");
        }

        this.startTime = startTime;

        return this;
    }

    @Override
    public Iterable<SasTokenPartitionKeyValueRange> getPartitionKeyValueRanges() {
        return this.partitionKeyValueRanges;
    }

    @Override
    public SasTokenProperties setPartitionKeyValueRanges(Iterable<String> partitionKeyValues) {
        if (partitionKeyValues != null) {
            this.partitionKeyValueRanges = new ArrayList<>();
            partitionKeyValues.forEach(partitionKey -> this.partitionKeyValueRanges.add(SasTokenPartitionKeyValueRange.create(partitionKey)));
        } else {
            this.partitionKeyValueRanges = null;
        }

        return this;
    }

    @Override
    public SasTokenProperties addPartitionKeyValue(String partitionKeyValue) {
        if (this.partitionKeyValueRanges == null) {
            this.partitionKeyValueRanges = new ArrayList<>();
        }

        this.partitionKeyValueRanges.add(SasTokenPartitionKeyValueRange.create(partitionKeyValue));

        return this;
    }

    @Override
    public SasTokenProperties addPermission(SasTokenPermissionKind permissionKind) {
        switch (permissionKind) {

            // Container data all.
            case CONTAINER_CREATE_ITEMS: {
                this.dataPlaneWriterScope |= SCOPE_CONTAINER_CREATE_ITEMS.value();
                break;
            }
            case CONTAINER_REPLACE_ITEMS: {
                this.dataPlaneWriterScope |= SCOPE_CONTAINER_REPLACE_ITEMS.value();
                break;
            }
            case CONTAINER_UPSERT_ITEMS: {
                this.dataPlaneWriterScope |= SCOPE_CONTAINER_UPSERT_ITEMS.value();
                break;
            }
            case CONTAINER_DELETE_ITEMS: {
                this.dataPlaneWriterScope |= SCOPE_CONTAINER_DELETE_ITEMS.value();
                break;
            }
            case CONTAINER_EXECUTE_QUERIES: {
                this.dataPlaneWriterScope |= SCOPE_CONTAINER_EXECUTE_QUERIES.value();
                break;
            }
            case CONTAINER_READ_FEEDS: {
                this.dataPlaneReaderScope |= SCOPE_CONTAINER_READ_FEEDS.value();
                break;
            }
            case CONTAINER_EXECUTE_STORED_PROCEDURES: {
                this.dataPlaneWriterScope |= SCOPE_CONTAINER_EXECUTE_STORED_PROCEDURES.value();
                break;
            }
            case CONTAINER_MANAGE_CONFLICTS: {
                this.dataPlaneReaderScope |= SCOPE_CONTAINER_READ_CONFLICTS.value();
                this.dataPlaneWriterScope |= SCOPE_CONTAINER_DELETE_CONFLICTS.value();
                break;
            }
            case CONTAINER_READ_ANY: {
                this.dataPlaneReaderScope |= SCOPE_CONTAINER_READ_ALL_ACCESS.value();
                break;
            }
            case CONTAINER_FULL_ACCESS: {
                this.dataPlaneReaderScope |= SCOPE_CONTAINER_READ_ALL_ACCESS.value();
                this.dataPlaneWriterScope |= SCOPE_CONTAINER_WRITE_ALL_ACCESS.value();
                break;
            }


            // Cosmos container item scope.
            case ITEM_FULL_ACCESS: {
                this.dataPlaneWriterScope |= SCOPE_ITEM_WRITE_ALL_ACCESS.value();
                this.addPermission(SasTokenPermissionKind.ITEM_READ_ANY);
                break;
            }
            case ITEM_READ_ANY: {
                this.dataPlaneReaderScope |= SCOPE_ITEM_READ_ALL_ACCESS.value();
                break;
            }
            case ITEM_READ: {
                this.dataPlaneReaderScope |= SCOPE_ITEM_READ.value();
                break;
            }
            case ITEM_REPLACE: {
                this.dataPlaneWriterScope |= SCOPE_ITEM_REPLACE.value();
                break;
            }
            case ITEM_UPSERT: {
                this.dataPlaneWriterScope |= SCOPE_ITEM_UPSERT.value();
                break;
            }
            case ITEM_DELETE: {
                this.dataPlaneWriterScope |= SCOPE_ITEM_DELETE.value();
                break;
            }

            case STORE_PROCEDURE_EXECUTE: {
                this.dataPlaneWriterScope |= SCOPE_STORED_PROCEDURE_EXECUTE.value();
                break;
            }

            default:
                throw new IllegalArgumentException("permissionKind");
        }

        return this;
    }
}
