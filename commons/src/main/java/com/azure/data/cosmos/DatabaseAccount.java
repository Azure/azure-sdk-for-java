/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos;

import com.fasterxml.jackson.core.type.TypeReference;
import com.azure.data.cosmos.internal.Constants;
import com.azure.data.cosmos.internal.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

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
    DatabaseAccount() {
        this.selfLink("");
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
    void setDatabasesLink(String databasesLink) {
        super.set(Constants.Properties.DATABASES_LINK, databasesLink);
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
    void setMediaLink(String medialink) {
        super.set(Constants.Properties.MEDIA_LINK, medialink);
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
    void setAddressesLink(String addresseslink) {
        super.set(Constants.Properties.ADDRESS_LINK, addresseslink);
    }

    /**
     * Attachment content (media) storage quota in MBs Retrieved from gateway.
     *
     * @return the max media storage usage in MB.
     */
    public long getMaxMediaStorageUsageInMB() {
        return this.maxMediaStorageUsageInMB;
    }

    void setMaxMediaStorageUsageInMB(long value) {
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

    void setMediaStorageUsageInMB(long value) {
        this.mediaStorageUsageInMB = value;
    }

    /**
     * Gets the ConsistencyPolicy settings.
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
     * Gets the ReplicationPolicy settings.
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
     * Gets the SystemReplicationPolicy settings.
     *
     * @return the system replication policy.
     */
    ReplicationPolicy getSystemReplicationPolicy() {
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
     * Gets the QueryEngineConfiuration settings.
     *
     * @return the query engine configuration.
     */
    Map<String, Object> getQueryEngineConfiuration() {
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
    void setWritableLocations(Iterable<DatabaseAccountLocation> locations) {
        super.set(Constants.Properties.WRITABLE_LOCATIONS, locations);
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
    void setReadableLocations(Iterable<DatabaseAccountLocation> locations) {
        super.set(Constants.Properties.READABLE_LOCATIONS, locations);
    }

    boolean isEnableMultipleWriteLocations() {
        return ObjectUtils.defaultIfNull(super.getBoolean(Constants.Properties.ENABLE_MULTIPLE_WRITE_LOCATIONS), false);
    }

    void setEnableMultipleWriteLocations(boolean value) {
        super.set(Constants.Properties.ENABLE_MULTIPLE_WRITE_LOCATIONS, value);
    }

    @Override
    void populatePropertyBag() {
        if (this.consistencyPolicy != null) {
            this.consistencyPolicy.populatePropertyBag();
            super.set(Constants.Properties.USER_CONSISTENCY_POLICY, this.consistencyPolicy);
        }
    }
}
