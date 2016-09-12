/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.redis;

import com.microsoft.azure.CloudException;

import java.io.IOException;
import java.util.List;

/**
 * An immutable client-side representation of an Azure Redis cache with Premium SKU.
 */
public interface RedisCachePremium extends RedisCache
{
    /**
     * Reboot specified redis node(s). This operation requires write permission to the cache resource. There can be potential data loss.
     *
     * @param rebootType specifies which redis node(s) to reboot. Depending on this value data loss is
     * possible. Possible values include: 'PrimaryNode', 'SecondaryNode', 'AllNodes'.
     * @param shardId In case of cluster cache, this specifies shard id which should be rebooted.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    void forceReboot(RebootType rebootType, int shardId) throws CloudException, IOException;

    /**
     * Reboot specified redis node(s). This operation requires write permission to the cache resource. There can be potential data loss.
     *
     * @param rebootType specifies which redis node(s) to reboot. Depending on this value data loss is
     * possible. Possible values include: 'PrimaryNode', 'SecondaryNode', 'AllNodes'.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    void forceReboot(RebootType rebootType) throws CloudException, IOException;

    /**
     * Import data into redis cache.
     *
     * @param files files to import.
     * @param fileFormat specifies file format.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    void importData(List<String> files, String fileFormat) throws CloudException, IOException, InterruptedException;

    /**
     * Import data into redis cache.
     *
     * @param files files to import.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    void importData(List<String> files) throws CloudException, IOException, InterruptedException;

    /**
     * Export data from redis cache.
     *
     * @param containerSASUrl container name to export to.
     * @param prefix prefix to use for exported files.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    void exportData(String containerSASUrl, String prefix) throws CloudException, IOException, InterruptedException;

    /**
     * Export data from redis cache.
     *
     * @param containerSASUrl container name to export to.
     * @param prefix prefix to use for exported files.
     * @param fileFormat specifies file format.
     * @throws CloudException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     * @throws IllegalArgumentException exception thrown from invalid parameters
     * @throws InterruptedException exception thrown when long running operation is interrupted
     * @return the ServiceResponse object if successful.
     */
    void exportData(String containerSASUrl, String prefix, String fileFormat) throws CloudException, IOException, InterruptedException;
}
