// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosItemSerializer;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.azure.cosmos.implementation.apachecommons.lang.ObjectUtils;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a database account in the Azure Cosmos DB database service.
 */
public final class DatabaseAccount extends Resource {
    private ConsistencyPolicy consistencyPolicy;

    private long maxMediaStorageUsageInMB;
    private long mediaStorageUsageInMB;
    private ReplicationPolicy replicationPolicy;
    private ReplicationPolicy systemReplicationPolicy;
    private Map<String, Object> queryEngineConfiguration;

    /**
     * Constructor.
     *
     * @param objectNode the {@link ObjectNode} that represent the
     * {@link JsonSerializable}
     */
    public DatabaseAccount(ObjectNode objectNode) {
        super(objectNode);
    }

    /**
     * Constructor.
     */
    public DatabaseAccount() {
        this.setSelfLink("");
    }

    /**
     * Initialize a database account object from json string.
     *
     * @param jsonString the json string that represents the database account.
     */
    public DatabaseAccount(String jsonString) {
        super(jsonString);
    }

    /**
     * Get the databases link of the databaseAccount.
     *
     * @return the databases link.
     */
    String getDatabasesLink() {
        return super.getString(Constants.Properties.DATABASES_LINK);
    }

    /**
     * Set the databases of the databaseAccount.
     *
     * @param databasesLink the databases link.
     */
    void setDatabasesLink(String databasesLink) {
        this.set(Constants.Properties.DATABASES_LINK, databasesLink, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    /**
     * Get the medialink of the databaseAccount.
     *
     * @return the media link.
     */
    String getMediaLink() {
        return super.getString(Constants.Properties.MEDIA_LINK);
    }

    /**
     * Set the medialink of the databaseAccount.
     *
     * @param medialink the media link.
     */
    void setMediaLink(String medialink) {
        this.set(Constants.Properties.MEDIA_LINK, medialink, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    /**
     * Get the addresseslink of the databaseAccount.
     *
     * @return the addresses link.
     */
    String getAddressesLink() {
        return super.getString(Constants.Properties.ADDRESS_LINK);
    }

    /**
     * Set the addresseslink of the databaseAccount.
     *
     * @param addresseslink the addresses link.
     */
    void setAddressesLink(String addresseslink) {
        this.set(Constants.Properties.ADDRESS_LINK, addresseslink, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    /**
     * Attachment content (media) storage quota in MBs Retrieved from gateway.
     *
     * @return the max media storage usage in MB.
     */
    long getMaxMediaStorageUsageInMB() {
        return this.maxMediaStorageUsageInMB;
    }

    public void setMaxMediaStorageUsageInMB(long value) {
        this.maxMediaStorageUsageInMB = value;
    }

    /**
     * Current attachment content (media) usage in MBs.
     * <p>
     * Retrieved from gateway. Value is returned from cached information updated
     * periodically and is not guaranteed to be real time.
     *
     * @return the media storage usage in MB.
     */
    long getMediaStorageUsageInMB() {
        return this.mediaStorageUsageInMB;
    }

    public void setMediaStorageUsageInMB(long value) {
        this.mediaStorageUsageInMB = value;
    }

    /**
     * Gets the ConsistencyPolicy properties.
     *
     * @return the consistency policy.
     */
    public ConsistencyPolicy getConsistencyPolicy() {
        if (this.consistencyPolicy == null) {
            this.consistencyPolicy = super.getObject(Constants.Properties.USER_CONSISTENCY_POLICY,
                ConsistencyPolicy.class);

            if (this.consistencyPolicy == null) {
                this.consistencyPolicy = new ConsistencyPolicy();
            }
        }
        return this.consistencyPolicy;
    }

    /**
     * Gets the ReplicationPolicy properties.
     *
     * @return the replication policy.
     */
    public ReplicationPolicy getReplicationPolicy() {
        if (this.replicationPolicy == null) {
            this.replicationPolicy = super.getObject(Constants.Properties.USER_REPLICATION_POLICY,
                ReplicationPolicy.class);

            if (this.replicationPolicy == null) {
                this.replicationPolicy = new ReplicationPolicy();
            }
        }

        return this.replicationPolicy;
    }

    /**
     * Gets the SystemReplicationPolicy properties.
     *
     * @return the system replication policy.
     */
    public ReplicationPolicy getSystemReplicationPolicy() {
        if (this.systemReplicationPolicy == null) {
            this.systemReplicationPolicy = super.getObject(Constants.Properties.SYSTEM_REPLICATION_POLICY,
                ReplicationPolicy.class);

            if (this.systemReplicationPolicy == null) {
                this.systemReplicationPolicy = new ReplicationPolicy();
            }
        }

        return this.systemReplicationPolicy;
    }

    /**
     * Gets the QueryEngineConfiguration properties.
     *
     * @return the query engine configuration.
     */
    public Map<String, Object> getQueryEngineConfiguration() {
        if (this.queryEngineConfiguration == null) {
            String queryEngineConfigurationJsonString = super.getObject(Constants.Properties.QUERY_ENGINE_CONFIGURATION,
                String.class);
            if (StringUtils.isNotEmpty(queryEngineConfigurationJsonString)) {
                try {
                    this.queryEngineConfiguration = Utils
                        .getSimpleObjectMapper()
                        .readValue(queryEngineConfigurationJsonString, ObjectNodeMap.JACKSON_MAP_TYPE);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
                if (this.queryEngineConfiguration == null) {
                    this.queryEngineConfiguration = new HashMap<>();
                }
            }
        }

        return this.queryEngineConfiguration;
    }

    /**
     * Gets the list of writable locations for this database account.
     *
     * @return the list of writable locations.
     */
    public Iterable<DatabaseAccountLocation> getWritableLocations() {
        return super.getCollection(Constants.Properties.WRITABLE_LOCATIONS, DatabaseAccountLocation.class);
    }

    /**
     * Sets the list of writable locations for this database account.
     * <p>
     * The list of writable locations are returned by the service.
     *
     * @param locations the list of writable locations.
     */
    public void setWritableLocations(Iterable<DatabaseAccountLocation> locations) {
        this.set(Constants.Properties.WRITABLE_LOCATIONS, locations, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    /**
     * Gets the list of readable locations for this database account.
     *
     * @return the list of readable locations.
     */
    public Iterable<DatabaseAccountLocation> getReadableLocations() {
        return super.getCollection(Constants.Properties.READABLE_LOCATIONS, DatabaseAccountLocation.class);
    }

    /**
     * Sets the list of readable locations for this database account.
     * <p>
     * The list of readable locations are returned by the service.
     *
     * @param locations the list of readable locations.
     */
    public void setReadableLocations(Iterable<DatabaseAccountLocation> locations) {
        this.set(Constants.Properties.READABLE_LOCATIONS, locations, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    /**
     * Gets if enable multiple write locations is set.
     *
     * @return the true if multiple write locations are set
     */
    public boolean getEnableMultipleWriteLocations() {
        return ObjectUtils.defaultIfNull(super.getBoolean(Constants.Properties.ENABLE_MULTIPLE_WRITE_LOCATIONS), false);
    }

    public void setEnableMultipleWriteLocations(boolean value) {
        this.set(Constants.Properties.ENABLE_MULTIPLE_WRITE_LOCATIONS, value, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    public void populatePropertyBag() {
        super.populatePropertyBag();
        if (this.consistencyPolicy != null) {
            this.consistencyPolicy.populatePropertyBag();
            this.set(
                Constants.Properties.USER_CONSISTENCY_POLICY,
                this.consistencyPolicy,
                CosmosItemSerializer.DEFAULT_SERIALIZER);
        }
    }

    @Override
    public String toJson() {
        this.populatePropertyBag();
        return super.toJson();
    }

    @Override
    public Object get(String propertyName) {
        return super.get(propertyName);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
