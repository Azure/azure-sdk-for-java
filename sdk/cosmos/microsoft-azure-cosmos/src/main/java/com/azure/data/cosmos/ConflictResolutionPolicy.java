// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;


import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.DocumentCollection;
import com.azure.data.cosmos.internal.StoredProcedure;
import com.azure.data.cosmos.internal.Strings;


/**
 * Represents the conflict resolution policy configuration for specifying how to resolve conflicts
 * in case writes from different regions result in conflicts on documents in the collection in the Azure Cosmos DB service.
 *
 * A collection with custom conflict resolution with no user-registered stored procedure.
 * <pre>{@code
 * DocumentCollection collectionSpec = new DocumentCollection();
 * collectionSpec.id("Multi-master collection");
 *
 * ConflictResolutionPolicy policy = ConflictResolutionPolicy.createCustomPolicy();
 * collectionSpec.conflictResolutionPolicy(policy);
 *
 * DocumentCollection collection = client.createCollection(databaseLink, collectionSpec, null)
 *         .toBlocking().single().getResource();
 *
 * }
 * </pre>
 *
 * A collection with custom conflict resolution with a user-registered stored procedure.
 * <pre>{@code
 * DocumentCollection collectionSpec = new DocumentCollection();
 * collectionSpec.id("Multi-master collection");
 *
 * ConflictResolutionPolicy policy = ConflictResolutionPolicy.createCustomPolicy(conflictResolutionSprocName);
 * collectionSpec.conflictResolutionPolicy(policy);
 *
 * DocumentCollection collection = client.createCollection(databaseLink, collectionSpec, null)
 *         .toBlocking().single().getResource();
 *
 * }
 * </pre>
 *
 * A collection with last writer wins conflict resolution, based on a path in the conflicting documents.
 * A collection with custom conflict resolution with a user-registered stored procedure.
 * <pre>{@code
 * DocumentCollection collectionSpec = new DocumentCollection();
 * collectionSpec.id("Multi-master collection");
 *
 * ConflictResolutionPolicy policy = ConflictResolutionPolicy.createLastWriterWinsPolicy("/path/for/conflict/resolution");
 * collectionSpec.conflictResolutionPolicy(policy);
 *
 * DocumentCollection collection = client.createCollection(databaseLink, collectionSpec, null)
 *         .toBlocking().single().getResource();
 *
 * }
 * </pre>
 */
public class ConflictResolutionPolicy extends JsonSerializable {

    /**
     * Creates a LAST_WRITER_WINS {@link ConflictResolutionPolicy} with "/_ts" as the resolution path.
     *
     * In case of a conflict occurring on a document, the document with the higher integer value in the default path
     * {@link Resource#timestamp()}, i.e., "/_ts" will be used.
     *
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createLastWriterWinsPolicy() {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.mode(ConflictResolutionMode.LAST_WRITER_WINS);
        return policy;
    }

    /**
     *
     * Creates a LAST_WRITER_WINS {@link ConflictResolutionPolicy} with path as the resolution path.
     *
     * The specified path must be present in each document and must be an integer value.
     * In case of a conflict occurring on a document, the document with the higher integer value in the specified path
     * will be picked.
     *
     * @param conflictResolutionPath The path to check values for last-writer wins conflict resolution.
     *                               That path is a rooted path of the property in the document, such as "/name/first".
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createLastWriterWinsPolicy(String conflictResolutionPath) {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.mode(ConflictResolutionMode.LAST_WRITER_WINS);
        if (conflictResolutionPath != null) {
            policy.conflictResolutionPath(conflictResolutionPath);
        }
        return policy;
    }

    /**
     * Creates a CUSTOM {@link ConflictResolutionPolicy} which uses the specified stored procedure
     * to perform conflict resolution
     *
     * This stored procedure may be created after the {@link DocumentCollection} is created and can be changed as required.
     *
     * <ul>
     * <li>In case the stored procedure fails or throws an exception,
     * the conflict resolution will default to registering conflicts in the conflicts feed</li>
     * <li>The user can provide the stored procedure @see {@link Resource#id()}</li>
     * </ul>
     * @param conflictResolutionSprocName stored procedure to perform conflict resolution.
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createCustomPolicy(String conflictResolutionSprocName) {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.mode(ConflictResolutionMode.CUSTOM);
        if (conflictResolutionSprocName != null) {
            policy.conflictResolutionProcedure(conflictResolutionSprocName);
        }
        return policy;
    }

    /**
     * Creates a CUSTOM {@link ConflictResolutionPolicy} without any {@link StoredProcedure}. User manually
     * should resolve conflicts.
     *
     * The conflicts will be registered in the conflicts feed and the user should manually resolve them.
     *
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createCustomPolicy() {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.mode(ConflictResolutionMode.CUSTOM);
        return policy;
    }

    /**
     * Initializes a new instance of the {@link ConflictResolutionPolicy} class for the Azure Cosmos DB service.
     */
    ConflictResolutionPolicy() {}

    public ConflictResolutionPolicy(String jsonString) {
        super(jsonString);
    }

    /**
     * Gets the {@link ConflictResolutionMode} in the Azure Cosmos DB service.
     * By default it is {@link ConflictResolutionMode#LAST_WRITER_WINS}.
     *
     * @return ConflictResolutionMode.
     */
    public ConflictResolutionMode mode() {

        String strValue = super.getString(Constants.Properties.MODE);

        if (!Strings.isNullOrEmpty(strValue)) {
            try {
                return ConflictResolutionMode.valueOf(Strings.fromCamelCaseToUpperCase(super.getString(Constants.Properties.MODE)));
            } catch (IllegalArgumentException e) {
                this.getLogger().warn("INVALID ConflictResolutionMode value {}.", super.getString(Constants.Properties.MODE));
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
    ConflictResolutionPolicy mode(ConflictResolutionMode mode) {
        super.set(Constants.Properties.MODE, mode.toString());
        return this;
    }

    /**
     * Gets the path which is present in each document in the Azure Cosmos DB service for last writer wins conflict-resolution.
     * This path must be present in each document and must be an integer value.
     * In case of a conflict occurring on a document, the document with the higher integer value in the specified path will be picked.
     * If the path is unspecified, by default the {@link Resource#timestamp()} path will be used.
     *
     * This value should only be set when using {@link ConflictResolutionMode#LAST_WRITER_WINS}
     *
     * @return The path to check values for last-writer wins conflict resolution.
     * That path is a rooted path of the property in the document, such as "/name/first".
     */
    public String conflictResolutionPath() {
        return super.getString(Constants.Properties.CONFLICT_RESOLUTION_PATH);
    }

    /**
     * Sets the path which is present in each document in the Azure Cosmos DB service for last writer wins conflict-resolution.
     * This path must be present in each document and must be an integer value.
     * In case of a conflict occurring on a document, the document with the higher integer value in the specified path will be picked.
     * If the path is unspecified, by default the {@link Resource#timestamp()} path will be used.
     *
     * This value should only be set when using {@link ConflictResolutionMode#LAST_WRITER_WINS}
     *
     * @param value The path to check values for last-writer wins conflict resolution.
     *              That path is a rooted path of the property in the document, such as "/name/first".
     */
    ConflictResolutionPolicy conflictResolutionPath(String value) {
        super.set(Constants.Properties.CONFLICT_RESOLUTION_PATH, value);
        return this;
    }

    /**
     * Gets the {@link StoredProcedure} which is used for conflict resolution in the Azure Cosmos DB service.
     * This stored procedure may be created after the {@link DocumentCollection} is created and can be changed as required.
     *
     * <ul>
     * <li>This value should only be set when using {@link ConflictResolutionMode#CUSTOM}</li>
     * <li>In case the stored procedure fails or throws an exception,
     * the conflict resolution will default to registering conflicts in the conflicts feed</li>
     * <li>The user can provide the stored procedure @see {@link Resource#id()}</li>
     * </ul>
     **
     * @return the stored procedure to perform conflict resolution.]
     */
    public String conflictResolutionProcedure() {
        return super.getString(Constants.Properties.CONFLICT_RESOLUTION_PROCEDURE);
    }

    ConflictResolutionPolicy conflictResolutionProcedure(String value) {
        super.set(Constants.Properties.CONFLICT_RESOLUTION_PROCEDURE, value);
        return this;
    }
}
