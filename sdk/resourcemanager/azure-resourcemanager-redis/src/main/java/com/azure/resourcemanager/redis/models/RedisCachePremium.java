// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.models;

import com.azure.core.annotation.Fluent;
import java.util.List;
import java.util.Map;

/** An immutable client-side representation of an Azure Redis cache with Premium SKU. */
@Fluent
public interface RedisCachePremium extends RedisCache {
    /**
     * Reboot specified Redis node(s). This operation requires write permission to the cache resource. There can be
     * potential data loss.
     *
     * @param rebootType specifies which Redis node(s) to reboot. Depending on this value data loss is possible.
     *     Possible values include: 'PrimaryNode', 'SecondaryNode', 'AllNodes'.
     * @param shardId In case of cluster cache, this specifies shard id which should be rebooted.
     */
    void forceReboot(RebootType rebootType, int shardId);

    /**
     * Import data into Redis Cache.
     *
     * @param files files to import.
     * @param fileFormat specifies file format.
     */
    void importData(List<String> files, String fileFormat);

    /**
     * Import data into Redis Cache.
     *
     * @param files files to import.
     */
    void importData(List<String> files);

    /**
     * Export data from Redis Cache.
     *
     * @param containerSASUrl container name to export to.
     * @param prefix prefix to use for exported files.
     */
    void exportData(String containerSASUrl, String prefix);

    /**
     * Export data from Redis Cache.
     *
     * @param containerSASUrl container name to export to.
     * @param prefix prefix to use for exported files.
     * @param fileFormat specifies file format.
     */
    void exportData(String containerSASUrl, String prefix, String fileFormat);

    /**
     * Gets the patching schedule for Redis Cache.
     *
     * @return List of patch schedules for current Redis Cache.
     */
    List<ScheduleEntry> listPatchSchedules();

    /** Deletes the patching schedule for Redis Cache. */
    void deletePatchSchedule();

    /**
     * Adds a linked server to the current Redis cache instance.
     *
     * @param linkedRedisCacheId the resource Id of the Redis instance to link with.
     * @param linkedServerLocation the location of the linked Redis instance.
     * @param role the role of the linked server.
     * @return name of the linked server.
     */
    String addLinkedServer(String linkedRedisCacheId, String linkedServerLocation, ReplicationRole role);

    /**
     * Removes the linked server from the current Redis cache instance.
     *
     * @param linkedServerName the name of the linked server.
     */
    void removeLinkedServer(String linkedServerName);

    /**
     * Gets the role for the linked server of the current Redis cache instance.
     *
     * @param linkedServerName the name of the linked server.
     * @return the role of the linked server.
     */
    ReplicationRole getLinkedServerRole(String linkedServerName);

    /**
     * Gets the list of linked servers associated with this redis cache.
     *
     * @return the Roles of the linked servers, indexed by name
     */
    Map<String, ReplicationRole> listLinkedServers();
}
