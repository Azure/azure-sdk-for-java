// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.resourcemanager.redis.models.RedisConfiguration;

import java.util.HashMap;
import java.util.Map;

class ConfigurationUtils {

    static RedisConfiguration toConfiguration(Map<String, String> configuration) {
        RedisConfiguration c = new RedisConfiguration();
        if (configuration != null) {
            configuration.forEach((k, v) -> putConfiguration(c, k, v));
        }
        return c;
    }

    static Map<String, String> toMap(RedisConfiguration configuration) {
        Map<String, String> map = new HashMap<>();
        if (configuration != null) {
            if (configuration.maxmemoryPolicy() != null) {
                map.put("maxmemory-policy", configuration.maxmemoryPolicy());
            }
            if (configuration.rdbBackupEnabled() != null) {
                map.put("rdb-backup-enabled", configuration.rdbBackupEnabled());
            }
            if (configuration.preferredDataPersistenceAuthMethod() != null) {
                map.put("preferred-data-persistence-auth-method", configuration.preferredDataPersistenceAuthMethod());
            }
            if (configuration.rdbBackupMaxSnapshotCount() != null) {
                map.put("rdb-backup-max-snapshot-count", configuration.rdbBackupMaxSnapshotCount());
            }
            if (configuration.aofStorageConnectionString0() != null) {
                map.put("aof-storage-connection-string-0", configuration.aofStorageConnectionString0());
            }
            if (configuration.aofStorageConnectionString1() != null) {
                map.put("aof-storage-connection-string-1", configuration.aofStorageConnectionString1());
            }
            if (configuration.maxclients() != null) {
                map.put("maxclients", configuration.maxclients());
            }
            if (configuration.rdbStorageConnectionString() != null) {
                map.put("rdb-storage-connection-string", configuration.rdbStorageConnectionString());
            }
            if (configuration.maxmemoryDelta() != null) {
                map.put("maxmemory-delta", configuration.maxmemoryDelta());
            }
            if (configuration.maxfragmentationmemoryReserved() != null) {
                map.put("maxfragmentationmemory-reserved", configuration.maxfragmentationmemoryReserved());
            }
            if (configuration.maxmemoryReserved() != null) {
                map.put("maxmemory-reserved", configuration.maxmemoryReserved());
            }
            if (configuration.preferredDataArchiveAuthMethod() != null) {
                map.put("preferred-data-archive-auth-method", configuration.preferredDataArchiveAuthMethod());
            }
            if (configuration.zonalConfiguration() != null) {
                map.put("zonal-configuration", configuration.zonalConfiguration());
            }
            if (configuration.rdbBackupFrequency() != null) {
                map.put("rdb-backup-frequency", configuration.rdbBackupFrequency());
            }
            if (configuration.additionalProperties() != null) {
                map.putAll(configuration.additionalProperties());
            }
        }
        return map;
    }

    static void putConfiguration(RedisConfiguration configuration,
                                 String key, String value) {
        if (configuration == null) {
            return;
        }
        switch (key) {
            case "rdb-storage-connection-string":
                configuration.withRdbStorageConnectionString(value);
                break;
            case "maxmemory-policy":
                configuration.withMaxmemoryPolicy(value);
                break;
            case "maxmemory-delta":
                configuration.withMaxmemoryDelta(value);
                break;
            case "rdb-backup-enabled":
                configuration.withRdbBackupEnabled(value);
                break;
            case "maxfragmentationmemory-reserved":
                configuration.withMaxfragmentationmemoryReserved(value);
                break;
            case "maxmemory-reserved":
                configuration.withMaxmemoryReserved(value);
                break;
            case "rdb-backup-frequency":
                configuration.withRdbBackupFrequency(value);
                break;
            case "rdb-backup-max-snapshot-count":
                configuration.withRdbBackupMaxSnapshotCount(value);
                break;
            case "aof-storage-connection-string-0":
                configuration.withAofStorageConnectionString0(value);
                break;
            case "aof-storage-connection-string-1":
                configuration.withAofStorageConnectionString1(value);
                break;
            default:
                if (configuration.additionalProperties() == null) {
                    configuration.withAdditionalProperties(new HashMap<>());
                }
                configuration.additionalProperties().put(key, value);
                break;
        }
    }

    static void removeConfiguration(RedisConfiguration configuration, String key) {
        if (configuration == null) {
            return;
        }
        if (configuration.additionalProperties() != null) {
            configuration.additionalProperties().remove(key);
        }
        switch (key) {
            case "rdb-storage-connection-string":
                configuration.withRdbStorageConnectionString(null);
                break;
            case "maxmemory-policy":
                configuration.withMaxmemoryPolicy(null);
                break;
            case "maxmemory-delta":
                configuration.withMaxmemoryDelta(null);
                break;
            case "rdb-backup-enabled":
                configuration.withRdbBackupEnabled(null);
                break;
            case "maxfragmentationmemory-reserved":
                configuration.withMaxfragmentationmemoryReserved(null);
                break;
            case "maxmemory-reserved":
                configuration.withMaxmemoryReserved(null);
                break;
            case "rdb-backup-frequency":
                configuration.withRdbBackupFrequency(null);
                break;
            case "rdb-backup-max-snapshot-count":
                configuration.withRdbBackupMaxSnapshotCount(null);
                break;
            case "aof-storage-connection-string-0":
                configuration.withAofStorageConnectionString0(null);
                break;
            case "aof-storage-connection-string-1":
                configuration.withAofStorageConnectionString1(null);
                break;
            default:
                break;
        }
    }
}
