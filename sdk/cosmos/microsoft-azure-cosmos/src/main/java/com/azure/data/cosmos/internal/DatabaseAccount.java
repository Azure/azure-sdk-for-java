// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.ConsistencyPolicy;
import com.azure.data.cosmos.Resource;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.azure.data.cosmos.BridgeInternal.setProperty;
import static com.azure.data.cosmos.BridgeInternal.setResourceSelfLink;
import static com.azure.data.cosmos.BridgeInternal.populatePropertyBagJsonSerializable;

/**
 * Represents a database account in the Azure Cosmos DB database service.
 */
public class DatabaseAccount extends Resource {
    private ConsistencyPolicy consistencyPolicy;

    private long maxMediaStorageUsageInMB;
    private long mediaStorageUsageInMB;
    private ReplicationPolicy replicationPolicy;
    private ReplicationPolicy systemReplicationPolicy;
    private Map<String, Object> queryEngineConfiguration;

    /**
     * Constructor.
     */
    public DatabaseAccount() {
        setResourceSelfLink(this, "");
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
    public String getDatabasesLink() {
        return super.getString(Constants.Properties.DATABASES_LINK);
    }

    /**
     * Set the databases of the databaseAccount.
     *
     * @param databasesLink the databases link.
     */
    public void setDatabasesLink(String databasesLink) {
        setProperty(this, Constants.Properties.DATABASES_LINK, databasesLink);
    }

    /**
     * Get the medialink of the databaseAccount.
     *
     * @return the media link.
     */
    public String getMediaLink() {
        return super.getString(Constants.Properties.MEDIA_LINK);
    }

    /**
     * Set the medialink of the databaseAccount.
     *
     * @param medialink the media link.
     */
    public void setMediaLink(String medialink) {
        setProperty(this, Constants.Properties.MEDIA_LINK, medialink);
    }

    /**
     * Get the addresseslink of the databaseAccount.
     *
     * @return the addresses link.
     */
    public String getAddressesLink() {
        return super.getString(Constants.Properties.ADDRESS_LINK);
    }

    /**
     * Set the addresseslink of the databaseAccount.
     *
     * @param addresseslink the addresses link.
     */
    public void setAddressesLink(String addresseslink) {
        setProperty(this, Constants.Properties.ADDRESS_LINK, addresseslink);
    }

    /**
     * Attachment content (media) storage quota in MBs Retrieved from gateway.
     *
     * @return the max media storage usage in MB.
     */
    public long getMaxMediaStorageUsageInMB() {
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
    public long getMediaStorageUsageInMB() {
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
     * Gets the QueryEngineConfiuration properties.
     *
     * @return the query engine configuration.
     */
    public Map<String, Object> getQueryEngineConfiuration() {
        if (this.queryEngineConfiguration == null) {
            String queryEngineConfigurationJsonString = super.getObject(Constants.Properties.QUERY_ENGINE_CONFIGURATION,
                    String.class);
            if (StringUtils.isNotEmpty(queryEngineConfigurationJsonString)) {
                TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
                };
                try {
                    this.queryEngineConfiguration = Utils.getSimpleObjectMapper()
                            .readValue(queryEngineConfigurationJsonString, typeRef);
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
        setProperty(this, Constants.Properties.WRITABLE_LOCATIONS, locations);
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
        setProperty(this, Constants.Properties.READABLE_LOCATIONS, locations);
    }

    public boolean isEnableMultipleWriteLocations() {
        return ObjectUtils.defaultIfNull(super.getBoolean(Constants.Properties.ENABLE_MULTIPLE_WRITE_LOCATIONS), false);
    }

    public void setEnableMultipleWriteLocations(boolean value) {
        setProperty(this, Constants.Properties.ENABLE_MULTIPLE_WRITE_LOCATIONS, value);
    }

    public void populatePropertyBag() {
        if (this.consistencyPolicy != null) {
            populatePropertyBagJsonSerializable(this.consistencyPolicy);
            setProperty(this, Constants.Properties.USER_CONSISTENCY_POLICY, this.consistencyPolicy);
        }
    }

    @Override
    public String toJson() {
        this.populatePropertyBag();
        return super.toJson();
    }
}
