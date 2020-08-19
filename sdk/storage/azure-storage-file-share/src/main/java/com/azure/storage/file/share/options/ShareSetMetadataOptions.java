// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/**
 * Extended options that may be passed when setting metadata on a share.
 */
@Fluent
public class ShareSetMetadataOptions {

    private Map<String, String> metadata;
    private String leaseId;

    /**
     * @return Metadata to set on the share, if null is passed the metadata for the share is cleared.
     */
    public Map<String, String> getMetadata() {
        return Map.copyOf(metadata);
    }

    /**
     * @return The lease id that the share must match.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * @param metadata Metadata to set on the share, if null is passed the metadata for the share is cleared.
     * @return The updated options.
     */
    public ShareSetMetadataOptions setMetadata(Map<String, String> metadata) {
        this.metadata = Map.copyOf(metadata);
        return this;
    }

    /**
     * @param leaseId The lease id that the share must match.
     * @return The updated options.
     */
    public ShareSetMetadataOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }
}
