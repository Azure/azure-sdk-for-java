// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareAccessTier;
import com.azure.storage.file.share.models.ShareProtocols;
import com.azure.storage.file.share.models.ShareRootSquash;

import java.util.Map;

/**
 * Extended options that may be passed when creating a share.
 */
@Fluent
public class ShareCreateOptions {

    private Integer quotaInGb;
    private Map<String, String> metadata;
    private ShareAccessTier accessTier;
    private ShareProtocols protocols;
    private ShareRootSquash rootSquash;

    /**
     * @return Size in GB to limit the share's growth.
     */
    public Integer getQuotaInGb() {
        return quotaInGb;
    }

    /**
     * @param quotaInGb Size in GB to limit the share's growth. The quota in GB must be between 1 and 5120.
     * @return The updated options.
     */
    public ShareCreateOptions setQuotaInGb(Integer quotaInGb) {
        this.quotaInGb = quotaInGb;
        return this;
    }

    /**
     * @return Metadata to associate with the share
     */
    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * @param metadata Metadata to associate with the share. If there is leading or trailing whitespace in any
     * metadata key or value, it must be removed or encoded.
     * @return The updated options.
     */
    public ShareCreateOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * @return {@link ShareAccessTier}.
     */
    public ShareAccessTier getAccessTier() {
        return accessTier;
    }

    /**
     * @param accessTier {@link ShareAccessTier}.
     * @return The updated options.
     */
    public ShareCreateOptions setAccessTier(ShareAccessTier accessTier) {
        this.accessTier = accessTier;
        return this;
    }

    /**
     * @return {@link ShareProtocols}
     */
    public ShareProtocols getProtocols() {
        return protocols;
    }

    /**
     * @param protocols {@link ShareProtocols}
     * @return The updated options.
     */
    public ShareCreateOptions setProtocols(ShareProtocols protocols) {
        this.protocols = protocols;
        return this;
    }

    /**
     * @return The root squash to set for the share. Only valid for NFS.
     */
    public ShareRootSquash getRootSquash() {
        return rootSquash;
    }

    /**
     * @param rootSquash The root squash to set for the share. Only valid for NFS.
     * @return The updated options.
     */
    public ShareCreateOptions setRootSquash(ShareRootSquash rootSquash) {
        this.rootSquash = rootSquash;
        return this;
    }
}
