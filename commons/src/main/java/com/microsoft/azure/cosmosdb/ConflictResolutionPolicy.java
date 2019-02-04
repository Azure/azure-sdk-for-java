package com.microsoft.azure.cosmosdb;


import com.microsoft.azure.cosmosdb.internal.Constants;
import com.microsoft.azure.cosmosdb.rx.internal.Strings;
import org.apache.commons.text.WordUtils;


/**
 * Represents the conflict resolution policy configuration for specifying how to resolve conflicts
 * in case writes from different regions result in conflicts on documents in the collection in the Azure Cosmos DB service.
 *
 * A collection with custom conflict resolution with no user-registered stored procedure.
 * <pre>{@code
 * DocumentCollection collectionSpec = new DocumentCollection();
 * collectionSpec.setId("Multi-master collection");
 *
 * ConflictResolutionPolicy policy = ConflictResolutionPolicy.createCustomPolicy();
 * collectionSpec.setConflictResolutionPolicy(policy);
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
 * collectionSpec.setId("Multi-master collection");
 *
 * ConflictResolutionPolicy policy = ConflictResolutionPolicy.createCustomPolicy(conflictResolutionSprocName);
 * collectionSpec.setConflictResolutionPolicy(policy);
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
 * collectionSpec.setId("Multi-master collection");
 *
 * ConflictResolutionPolicy policy = ConflictResolutionPolicy.createLastWriterWinsPolicy("/path/for/conflict/resolution");
 * collectionSpec.setConflictResolutionPolicy(policy);
 *
 * DocumentCollection collection = client.createCollection(databaseLink, collectionSpec, null)
 *         .toBlocking().single().getResource();
 *
 * }
 * </pre>
 */
public class ConflictResolutionPolicy extends JsonSerializable {

    /**
     * Creates a LastWriterWins {@link ConflictResolutionPolicy} with "/_ts" as the resolution path.
     *
     * In case of a conflict occurring on a document, the document with the higher integer value in the default path
     * {@link Resource#getTimestamp()}, i.e., "/_ts" will be used.
     *
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createLastWriterWinsPolicy() {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.setMode(ConflictResolutionMode.LastWriterWins);
        return policy;
    }

    /**
     *
     * Creates a LastWriterWins {@link ConflictResolutionPolicy} with path as the resolution path.
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
        policy.setMode(ConflictResolutionMode.LastWriterWins);
        if (conflictResolutionPath != null) {
            policy.setConflictResolutionPath(conflictResolutionPath);
        }
        return policy;
    }

    /**
     * Creates a Custom {@link ConflictResolutionPolicy} which uses the specified stored procedure
     * to perform conflict resolution
     *
     * This stored procedure may be created after the {@link DocumentCollection} is created and can be changed as required.
     *
     * <ul>
     * <li>In case the stored procedure fails or throws an exception,
     * the conflict resolution will default to registering conflicts in the conflicts feed</li>
     * <li>The user can provide the stored procedure @see {@link Resource#getId()}</li>
     * </ul>
     * @param conflictResolutionSprocName stored procedure to perform conflict resolution.
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createCustomPolicy(String conflictResolutionSprocName) {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.setMode(ConflictResolutionMode.Custom);
        if (conflictResolutionSprocName != null) {
            policy.setConflictResolutionProcedure(conflictResolutionSprocName);
        }
        return policy;
    }

    /**
     * Creates a Custom {@link ConflictResolutionPolicy} without any {@link StoredProcedure}. User manually
     * should resolve conflicts.
     *
     * The conflicts will be registered in the conflicts feed and the user should manually resolve them.
     *
     * @return ConflictResolutionPolicy.
     */
    public static ConflictResolutionPolicy createCustomPolicy() {
        ConflictResolutionPolicy policy = new ConflictResolutionPolicy();
        policy.setMode(ConflictResolutionMode.Custom);
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
     * By default it is {@link ConflictResolutionMode#LastWriterWins}.
     *
     * @return ConflictResolutionMode.
     */
    public ConflictResolutionMode getConflictResolutionMode() {

        String strValue = super.getString(Constants.Properties.MODE);

        if (!Strings.isNullOrEmpty(strValue)) {
            try {
                return ConflictResolutionMode.valueOf(WordUtils.capitalize(super.getString(Constants.Properties.MODE)));
            } catch (IllegalArgumentException e) {
                this.getLogger().warn("Invalid ConflictResolutionMode value {}.", super.getString(Constants.Properties.MODE));
                return ConflictResolutionMode.Invalid;
            }
        }

        return ConflictResolutionMode.Invalid;
    }

    /**
     * Sets the {@link ConflictResolutionMode} in the Azure Cosmos DB service.
     * By default it is {@link ConflictResolutionMode#LastWriterWins}.
     *
     * @param mode One of the values of the {@link ConflictResolutionMode} enum.
     */
    void setMode(ConflictResolutionMode mode) {
        super.set(Constants.Properties.MODE, mode.name());
    }

    /**
     * Gets the path which is present in each document in the Azure Cosmos DB service for last writer wins conflict-resolution.
     * This path must be present in each document and must be an integer value.
     * In case of a conflict occurring on a document, the document with the higher integer value in the specified path will be picked.
     * If the path is unspecified, by default the {@link Resource#getTimestamp()} path will be used.
     *
     * This value should only be set when using {@link ConflictResolutionMode#LastWriterWins}
     *
     * @return The path to check values for last-writer wins conflict resolution.
     * That path is a rooted path of the property in the document, such as "/name/first".
     */
    public String getConflictResolutionPath() {
        return super.getString(Constants.Properties.CONFLICT_RESOLUTION_PATH);
    }

    /**
     * Sets the path which is present in each document in the Azure Cosmos DB service for last writer wins conflict-resolution.
     * This path must be present in each document and must be an integer value.
     * In case of a conflict occurring on a document, the document with the higher integer value in the specified path will be picked.
     * If the path is unspecified, by default the {@link Resource#getTimestamp()} path will be used.
     *
     * This value should only be set when using {@link ConflictResolutionMode#LastWriterWins}
     *
     * @param value The path to check values for last-writer wins conflict resolution.
     *              That path is a rooted path of the property in the document, such as "/name/first".
     */
    void setConflictResolutionPath(String value) {
        super.set(Constants.Properties.CONFLICT_RESOLUTION_PATH, value);
    }

    /**
     * Gets the {@link StoredProcedure} which is used for conflict resolution in the Azure Cosmos DB service.
     * This stored procedure may be created after the {@link DocumentCollection} is created and can be changed as required.
     *
     * <ul>
     * <li>This value should only be set when using {@link ConflictResolutionMode#Custom}</li>
     * <li>In case the stored procedure fails or throws an exception,
     * the conflict resolution will default to registering conflicts in the conflicts feed</li>
     * <li>The user can provide the stored procedure @see {@link Resource#getId()}</li>
     * </ul>
     **
     * @return the stored procedure to perform conflict resolution.]
     */
    public String getConflictResolutionProcedure() {
        return super.getString(Constants.Properties.CONFLICT_RESOLUTION_PROCEDURE);
    }

    void setConflictResolutionProcedure(String value) {
        super.set(Constants.Properties.CONFLICT_RESOLUTION_PROCEDURE, value);
    }
}
