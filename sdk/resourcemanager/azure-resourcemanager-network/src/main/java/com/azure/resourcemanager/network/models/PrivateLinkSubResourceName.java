// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.core.util.ExpandableStringEnum;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Collection;

/**
 * The name of sub resource for private link.
 *
 * It can be converted from {@link PrivateLinkResource#groupId()} via {@link #fromString(String)}.
 */
public class PrivateLinkSubResourceName extends ExpandableStringEnum<PrivateLinkSubResourceName> {

    /** Static value Blob (of storage account) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName STORAGE_BLOB = fromString("blob");

    /** Static value Table (of storage account) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName STORAGE_TABLE = fromString("table");

    /** Static value Queue (of storage account) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName STORAGE_QUEUE = fromString("queue");

    /** Static value File (of storage account) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName STORAGE_FILE = fromString("file");

    /** Static value Web (of storage account) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName STORAGE_WEB = fromString("web");

    /** Static value Sql Server (of SQL database) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName SQL_SERVER = fromString("Sql Server");

    /** Static value Sql (of Cosmos) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName COSMOS_SQL = fromString("Sql");

    /** Static value MongoDB (of Cosmos) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName COSMOS_MONGO_DB = fromString("MongoDB");

    /** Static value Cassandra (of Cosmos) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName COSMOS_CASSANDRA = fromString("Cassandra");

    /** Static value Gremlin (of Cosmos) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName COSMOS_GREMLIN = fromString("Gremlin");

    /** Static value Table (of Cosmos) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName COSMOS_TABLE = fromString("Table");

    /** Static value vault (of key vault) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName VAULT = fromString("vault");

    /** Static value management (of Kubernetes) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName KUBERNETES_MANAGEMENT = fromString("management");

    /** Static value sites (of app service) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName WEB_SITES = fromString("sites");

    /** Static value searchService (of cognitive search) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName SEARCH = fromString("searchService");

    /** Static value redisCache (of Redis) for PrivateLinkSubResourceName. */
    public static final PrivateLinkSubResourceName REDIS_CACHE = fromString("redisCache");

    /**
     * Creates or finds a PrivateLinkSubResourceName from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding PrivateLinkSubResourceName.
     */
    @JsonCreator
    public static PrivateLinkSubResourceName fromString(String name) {
        return fromString(name, PrivateLinkSubResourceName.class);
    }

    /** @return known PrivateLinkSubResourceName values. */
    public static Collection<PrivateLinkSubResourceName> values() {
        return values(PrivateLinkSubResourceName.class);
    }
}
