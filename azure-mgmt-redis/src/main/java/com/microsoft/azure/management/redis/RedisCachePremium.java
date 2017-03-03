/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.redis;

import java.util.List;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Method;

/**
 * An immutable client-side representation of an Azure Redis cache with Premium SKU.
 */
@Fluent
public interface RedisCachePremium extends RedisCache {
    /**
     * Reboot specified Redis node(s). This operation requires write permission to the cache resource. There can be potential data loss.
     *
     * @param rebootType specifies which Redis node(s) to reboot. Depending on this value data loss is
     *                   possible. Possible values include: 'PrimaryNode', 'SecondaryNode', 'AllNodes'.
     * @param shardId    In case of cluster cache, this specifies shard id which should be rebooted.
     */
    void forceReboot(RebootType rebootType, int shardId);

    /**
     * Reboot specified Redis node(s). This operation requires write permission to the cache resource. There can be potential data loss.
     *
     * @param rebootType specifies which Redis node(s) to reboot. Depending on this value data loss is
     *                   possible. Possible values include: 'PrimaryNode', 'SecondaryNode', 'AllNodes'.
     */
    void forceReboot(RebootType rebootType);

    /**
     * Import data into Redis Cache.
     *
     * @param files      files to import.
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
     * @param prefix          prefix to use for exported files.
     */
    void exportData(String containerSASUrl, String prefix);

    /**
     * Export data from Redis Cache.
     *
     * @param containerSASUrl container name to export to.
     * @param prefix          prefix to use for exported files.
     * @param fileFormat      specifies file format.
     */
    void exportData(String containerSASUrl, String prefix, String fileFormat);

    /**
     * Gets the patching schedule for Redis Cache.
     * @return List of patch schedules for current Redis Cache.
     */
    @Method
    List<ScheduleEntry> listPatchSchedules();

    /**
     * Deletes the patching schedule for Redis Cache.
     */
    void deletePatchSchedule();
}
