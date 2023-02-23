// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.IterableStream;
import com.azure.data.appconfiguration.implementation.models.Snapshot;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * ConfigurationSettingSnapshot
 */
@Fluent
public final class ConfigurationSettingSnapshot {
    private Iterable<SnapshotFilter> filters;
    private CompositionType compositionType;
    private Duration retentionPeriod;
    private Map<String, String> tags;

    // READ-ONLY
    private String name;
    private String eTag;
    private SnapshotStatus status;
    private Integer statusCode;
    private OffsetDateTime createdAt;
    private OffsetDateTime expiresAt;
    private Long size;
    private Long itemCount;

    /**
     * Create an instance of Snapshot
     *
     * @param filters A list of filters used to filter the key-values included in the snapshot.
     */
    public ConfigurationSettingSnapshot(Iterable<SnapshotFilter> filters) {
        this.filters = filters;
    }

    /**
     * Get the name property: The name of the snapshot.
     *
     * @return the name value.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the filters property: A list of filters used to filter the key-values included in the snapshot.
     *
     * @return the filters value.
     */
    public IterableStream<SnapshotFilter> getFilters() {
        return IterableStream.of(filters);
    }

    /**
     * Get the etag property: A value representing the current state of the snapshot.
     *
     * @return the etag value.
     */
    public String getETag() {
        return eTag;
    }

    /**
     * Get the status property: The current status of the snapshot.
     *
     * @return the status value.
     */
    public SnapshotStatus getStatus() {
        return status;
    }

    /**
     * Get the statusCode property: Provides additional information about the status of the snapshot. The status code
     * values are modeled after HTTP status codes.
     *
     * @return the statusCode value.
     */
    public Integer getStatusCode() {
        return this.statusCode;
    }

    /**
     * Get the retentionPeriod property: The amount of time, in seconds, that a snapshot will remain in the archived
     * state before expiring. This property is only writable during the creation of a snapshot. If not specified, the
     * default lifetime of key-value revisions will be used.
     *
     * @return the retentionPeriod value.
     */
    public CompositionType getCompositionType() {
        return compositionType;
    }

    /**
     * Get the created property: The time that the snapshot was created.
     *
     * @return the created value.
     */
    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Get the expires property: The time that the snapshot will expire.
     *
     * @return the expires value.
     */
    public OffsetDateTime getExpiresAt() {
        return this.expiresAt;
    }

    /**
     * Get the size property: The size in bytes of the snapshot.
     *
     * @return the size value.
     */
    public Long getSize() {
        return size;
    }

    /**
     * Get the itemsCount property: The amount of key-values in the snapshot.
     *
     * @return the itemsCount value.
     */
    public Long getItemCount() {
        return itemCount;
    }

    /**
     * Get the tags property: The tags of the snapshot.
     *
     * @return the tags value.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Get the retentionPeriod property: The amount of time, in seconds, that a snapshot will remain in the archived
     * state before expiring. This property is only writable during the creation of a snapshot. If not specified, the
     * default lifetime of key-value revisions will be used.
     *
     * @return the retentionPeriod value.
     */
    public Duration getRetentionPeriod() {
        return retentionPeriod;
    }


    /**
     * Set the compositionType property: The composition type describes how the key-values within the snapshot are
     * composed. The 'all' composition type includes all key-values. The 'group_by_key' composition type ensures there
     * are no two key-values containing the same key.
     *
     * @param compositionType the compositionType value to set.
     * @return the ConfigurationSettingSnapshot object itself.
     */
    public ConfigurationSettingSnapshot setCompositionType(CompositionType compositionType) {
        this.compositionType = compositionType;
        return this;
    }

    /**
     * Set the retentionPeriod property: The amount of time, in seconds, that a snapshot will remain in the archived
     * state before expiring. This property is only writable during the creation of a snapshot. If not specified, the
     * default lifetime of key-value revisions will be used.
     *
     * @param retentionPeriod the retentionPeriod value to set.
     * @return the ConfigurationSettingSnapshot object itself.
     */
    public ConfigurationSettingSnapshot setRetentionPeriod(Duration retentionPeriod) {
        this.retentionPeriod = retentionPeriod;
        return this;
    }

    /**
     * Set the tags property: The tags of the snapshot.
     *
     * @param tags the tags value to set.
     * @return the ConfigurationSettingSnapshot object itself.
     */
    public ConfigurationSettingSnapshot setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
