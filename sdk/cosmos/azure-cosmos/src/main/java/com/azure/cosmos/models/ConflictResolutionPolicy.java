// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;


import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Paths;
import com.azure.cosmos.implementation.Resource;
import com.azure.cosmos.implementation.StoredProcedure;
import com.azure.cosmos.implementation.Strings;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Represents the conflict resolution policy configuration for specifying how to resolve conflicts
 * in case writes from different regions result in conflicts on items in the container in the Azure Cosmos DB
 * service.
 *
 * Refer to: https://docs.microsoft.com/en-us/azure/cosmos-db/conflict-resolution-policies
 *
 * <p>
 * A container with custom conflict resolution with no user-registered stored procedure.
 * <pre>{@code
 *
 * CosmosContainerProperties containerProperties =
 *      new CosmosContainerProperties("Multi-master container", "Multi-master container partition key");
 * containerProperties.setConflictResolutionPolicy(ConflictResolutionPolicy.createCustomPolicy());
 *
 * CosmosAsyncDatabase database = client.createDatabase(databaseProperties).block().getDatabase();
 * CosmosAsyncContainer container = database.createContainer(containerProperties).block().getContainer();
 *
 * }
 * </pre>
 * <p>
 * A container with custom conflict resolution with a user-registered stored procedure.
 * <pre>{@code
 *
 * CosmosContainerProperties containerProperties =
 *      new CosmosContainerProperties("Multi-master container", "Multi-master container partition key");
 *
 * ConflictResolutionPolicy policy = ConflictResolutionPolicy.createCustomPolicy(conflictResolutionSprocName);
 * containerProperties.setConflictResolutionPolicy(policy);
 *
 * CosmosAsyncDatabase database = client.createDatabase(databaseProperties).block().getDatabase();
 * CosmosAsyncContainer container = database.createContainer(containerProperties).block().getContainer();
 *
 * }
 * </pre>
 * <p>
 * A container with last writer wins conflict resolution, based on a path in the conflicting items.
 * A container with custom conflict resolution with a user-registered stored procedure.
 * <pre>{@code
 *
 * CosmosContainerProperties containerProperties =
 *      new CosmosContainerProperties("Multi-master container", "Multi-master container partition key");
 *
 * ConflictResolutionPolicy policy = ConflictResolutionPolicy.createLastWriterWinsPolicy("/path/for/conflict/resolution");
 * containerProperties.setConflictResolutionPolicy(policy);
 *
 * CosmosAsyncDatabase database = client.createDatabase(databaseProperties).block().getDatabase();
 * CosmosAsyncContainer container = database.createContainer(containerProperties).block().getContainer();
 *
 * }
 * </pre>
 */
public final class ConflictResolutionPolicy {

    private JsonSerializable jsonSerializable;

    /**
     * Creates a LAST_WRITER_WINS {@link ConflictResolutionPolicy} with "/_ts" as the resolution path.
     * <p>
     * In case of a conflict occurring on an item, the item with the higher integer value in the default path
     * {@link Resource#getTimestamp()} ()}, i.e., "/_ts" will be used.
     * {@link Resource#getTimestamp()}, i.e., "/_ts" will be used.
     *
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createLastWriterWinsPolicy() {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.setMode(ConflictResolutionMode.LAST_WRITER_WINS);
        return policy;
    }

    /**
     * Creates a LAST_WRITER_WINS {@link ConflictResolutionPolicy} with path as the resolution path.
     * <p>
     * The specified path must be present in each item and must be an integer value.
     * In case of a conflict occurring on an item, the item with the higher integer value in the specified path
     * will be picked.
     *
     * @param conflictResolutionPath The path to check values for last-writer wins conflict resolution.
     * That path is a rooted path of the property in the item, such as "/name/first".
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createLastWriterWinsPolicy(String conflictResolutionPath) {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.setMode(ConflictResolutionMode.LAST_WRITER_WINS);
        if (conflictResolutionPath != null) {
            policy.setConflictResolutionPath(conflictResolutionPath);
        }
        return policy;
    }

    /**
     * Creates a CUSTOM {@link ConflictResolutionPolicy} which uses the specified stored procedure
     * to perform conflict resolution
     * <p>
     * This stored procedure may be created after the {@link CosmosContainerProperties} is created and can be changed as
     * required.
     *
     * <ul>
     * <li>This method requires conflictResolutionStoredProcFullPath in format
     * dbs/%s/colls/%s/sprocs/%s. User can also use equivalent method {@link #createCustomPolicy(String, String, String)}</li>
     * <li>In case the stored procedure fails or throws an exception,
     * the conflict resolution will default to registering conflicts in the conflicts feed</li>
     * <li>The user can provide the stored procedure @see {@link Resource#getId()} </li>
     * </ul>
     *
     * @param conflictResolutionStoredProcFullPath stored procedure full path to perform conflict resolution.
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createCustomPolicy(String conflictResolutionStoredProcFullPath) {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.setMode(ConflictResolutionMode.CUSTOM);
        if (conflictResolutionStoredProcFullPath != null) {
            policy.setConflictResolutionProcedure(conflictResolutionStoredProcFullPath);
        }
        return policy;
    }

    /**
     * Creates a CUSTOM {@link ConflictResolutionPolicy} which uses the specified stored procedure
     * to perform conflict resolution
     * <p>
     * This stored procedure may be created after the {@link CosmosContainerProperties} is created and can be changed as
     * required.
     *
     * <ul>
     * <li>In case the stored procedure fails or throws an exception,
     * the conflict resolution will default to registering conflicts in the conflicts feed</li>
     * <li>The user can provide the stored procedure @see {@link Resource#getId()} </li>
     * </ul>
     *
     * @param dbName database name.
     * @param containerName container name.
     * @param sprocName stored procedure name to perform conflict resolution.
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createCustomPolicy(String dbName, String containerName, String sprocName) {
        return createCustomPolicy(getFullPath(dbName, containerName, sprocName));
    }

    /**
     * Creates a CUSTOM {@link ConflictResolutionPolicy} without any {@link StoredProcedure}. User manually
     * should resolve conflicts.
     * <p>
     * The conflicts will be registered in the conflicts feed and the user should manually resolve them.
     *
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createCustomPolicy() {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.setMode(ConflictResolutionMode.CUSTOM);
        return policy;
    }

    /**
     * Initializes a new instance of the {@link ConflictResolutionPolicy} class for the Azure Cosmos DB service.
     */
    ConflictResolutionPolicy() {
        this.jsonSerializable = new JsonSerializable();
    }

    /**
     * Instantiates a new Conflict resolution policy.
     *
     * @param jsonString the json string
     */
    ConflictResolutionPolicy(String jsonString) {
        this.jsonSerializable = new JsonSerializable(jsonString);
    }

    /**
     * Instantiates a new Conflict resolution policy.
     *
     * @param objectNode the object node.
     */
    ConflictResolutionPolicy(ObjectNode objectNode) {
        this.jsonSerializable = new JsonSerializable(objectNode);
    }

    /**
     * Gets the {@link ConflictResolutionMode} in the Azure Cosmos DB service.
     * By default it is {@link ConflictResolutionMode#LAST_WRITER_WINS}.
     *
     * @return ConflictResolutionMode.
     */
    public ConflictResolutionMode getMode() {

        String strValue = this.jsonSerializable.getString(Constants.Properties.MODE);

        if (!Strings.isNullOrEmpty(strValue)) {
            try {
                return ConflictResolutionMode
                           .valueOf(Strings.fromCamelCaseToUpperCase(this.jsonSerializable.getString(Constants.Properties.MODE)));
            } catch (IllegalArgumentException e) {
                this.jsonSerializable.getLogger().warn("INVALID ConflictResolutionMode getValue {}.",
                    this.jsonSerializable.getString(Constants.Properties.MODE));
                return ConflictResolutionMode.INVALID;
            }
        }

        return ConflictResolutionMode.INVALID;
    }

    /**
     * Sets the {@link ConflictResolutionMode} in the Azure Cosmos DB service.
     * By default it is {@link ConflictResolutionMode#LAST_WRITER_WINS}.
     *
     * @param mode One of the values of the {@link ConflictResolutionMode} enum.
     */
    ConflictResolutionPolicy setMode(ConflictResolutionMode mode) {
        this.jsonSerializable.set(Constants.Properties.MODE, mode.toString());
        return this;
    }

    /**
     * Gets the path which is present in each item in the Azure Cosmos DB service for last writer wins
     * conflict-resolution.
     * This path must be present in each item and must be an integer value.
     * In case of a conflict occurring on an item, the item with the higher integer value in the specified
     * path will be picked.
     * If the path is unspecified, by default the {@link Resource#getTimestamp()} ()} path will be used.
     * <p>
     * This value should only be set when using {@link ConflictResolutionMode#LAST_WRITER_WINS}
     *
     * @return The path to check values for last-writer wins conflict resolution.
     * That path is a rooted path of the property in the item, such as "/name/first".
     */
    public String getConflictResolutionPath() {
        return this.jsonSerializable.getString(Constants.Properties.CONFLICT_RESOLUTION_PATH);
    }

    /**
     * Sets the path which is present in each item in the Azure Cosmos DB service for last writer wins
     * conflict-resolution.
     * This path must be present in each item and must be an integer value.
     * In case of a conflict occurring on an item, the item with the higher integer value in the specified
     * path will be picked.
     * If the path is unspecified, by default the {@link Resource#getTimestamp()} ()} path will be used.
     * <p>
     * This value should only be set when using {@link ConflictResolutionMode#LAST_WRITER_WINS}
     *
     * @param value The path to check values for last-writer wins conflict resolution.
     * That path is a rooted path of the property in the item, such as "/name/first".
     */
    ConflictResolutionPolicy setConflictResolutionPath(String value) {
        this.jsonSerializable.set(Constants.Properties.CONFLICT_RESOLUTION_PATH, value);
        return this;
    }

    /**
     * Gets the {@link StoredProcedure} which is used for conflict resolution in the Azure Cosmos DB service.
     * This stored procedure may be created after the {@link CosmosContainerProperties} is created and can be changed as
     * required.
     *
     * <ul>
     * <li>This value should only be set when using {@link ConflictResolutionMode#CUSTOM}</li>
     * <li>In case the stored procedure fails or throws an exception,
     * the conflict resolution will default to registering conflicts in the conflicts feed</li>
     * <li>The user can provide the stored procedure @see {@link Resource#getId()} ()}</li>
     * </ul>
     * *
     *
     * @return the stored procedure to perform conflict resolution.]
     */
    public String getConflictResolutionProcedure() {
        return this.jsonSerializable.getString(Constants.Properties.CONFLICT_RESOLUTION_PROCEDURE);
    }

    ConflictResolutionPolicy setConflictResolutionProcedure(String value) {
        this.jsonSerializable.set(Constants.Properties.CONFLICT_RESOLUTION_PROCEDURE, value);
        return this;
    }

    void populatePropertyBag() {
        this.jsonSerializable.populatePropertyBag();
    }

    JsonSerializable getJsonSerializable() { return this.jsonSerializable; }

    private static String getFullPath(String dbName, String containerName, String sprocName) {
        if (dbName == null) {
            throw new IllegalArgumentException("dbName cannot be null");
        }

        if (containerName == null) {
            throw new IllegalArgumentException("containerName cannot be null");
        }

        if (sprocName == null) {
            throw new IllegalArgumentException("sprocName cannot be null");
        }

        StringBuilder builder = new StringBuilder();
        builder.append(Paths.DATABASES_PATH_SEGMENT);
        builder.append("/");
        builder.append(dbName);
        builder.append("/");
        builder.append(Paths.COLLECTIONS_PATH_SEGMENT);
        builder.append("/");
        builder.append(containerName);
        builder.append("/");
        builder.append(Paths.STORED_PROCEDURES_PATH_SEGMENT);
        builder.append("/");
        builder.append(sprocName);
        return builder.toString();
    }
}
