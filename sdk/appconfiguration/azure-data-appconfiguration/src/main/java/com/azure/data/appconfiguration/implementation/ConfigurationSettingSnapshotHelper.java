package com.azure.data.appconfiguration.implementation;

import com.azure.data.appconfiguration.models.ConfigurationSettingSnapshot;
import com.azure.data.appconfiguration.models.SnapshotStatus;

import java.time.OffsetDateTime;

/**
 * Helper class to access private values of {@link ConfigurationSettingSnapshot} across package boundaries.
 */
public class ConfigurationSettingSnapshotHelper {
    private static ConfigurationSettingSnapshotAccessor accessor;

    private ConfigurationSettingSnapshotHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ConfigurationSettingSnapshot} instance.
     */
    public interface ConfigurationSettingSnapshotAccessor {
        ConfigurationSettingSnapshot setName(ConfigurationSettingSnapshot snapshot, String name);
        ConfigurationSettingSnapshot setStatus(ConfigurationSettingSnapshot snapshot, SnapshotStatus status);
        ConfigurationSettingSnapshot setCreatedAt(ConfigurationSettingSnapshot snapshot, OffsetDateTime createdAt);
        ConfigurationSettingSnapshot setExpiresAt(ConfigurationSettingSnapshot snapshot, OffsetDateTime expiresAt);
        ConfigurationSettingSnapshot setSize(ConfigurationSettingSnapshot snapshot, Long size);
        ConfigurationSettingSnapshot setItemCount(ConfigurationSettingSnapshot snapshot, Long itemCount);
        ConfigurationSettingSnapshot setEtag(ConfigurationSettingSnapshot snapshot, String etag);
    }

    /**
     * The method called from {@link ConfigurationSettingSnapshot} to set its accessor.
     *
     * @param snapshotAccessor The accessor.
     */
    public static void setAccessor(final ConfigurationSettingSnapshotAccessor snapshotAccessor) {
        accessor = snapshotAccessor;
    }

    public static ConfigurationSettingSnapshot setName(ConfigurationSettingSnapshot snapshot, String name) {
        return accessor.setName(snapshot, name);
    }

    public static ConfigurationSettingSnapshot setStatus(ConfigurationSettingSnapshot snapshot, SnapshotStatus status) {
        return accessor.setStatus(snapshot, status);
    }

    public static ConfigurationSettingSnapshot setCreatedAt(ConfigurationSettingSnapshot snapshot,
                                                            OffsetDateTime createdAt) {
        return accessor.setCreatedAt(snapshot, createdAt);
    }

    public static ConfigurationSettingSnapshot setExpiresAt(ConfigurationSettingSnapshot snapshot,
                                                            OffsetDateTime expiresAt) {
        return accessor.setExpiresAt(snapshot, expiresAt);
    }

    public static ConfigurationSettingSnapshot setSize(ConfigurationSettingSnapshot snapshot, Long size) {
        return accessor.setSize(snapshot, size);
    }

    public static ConfigurationSettingSnapshot setItemCount(ConfigurationSettingSnapshot snapshot, Long itemCount) {
        return accessor.setItemCount(snapshot, itemCount);
    }

    public static ConfigurationSettingSnapshot setEtag(ConfigurationSettingSnapshot snapshot, String etag) {
        return accessor.setEtag(snapshot, etag);
    }
}
