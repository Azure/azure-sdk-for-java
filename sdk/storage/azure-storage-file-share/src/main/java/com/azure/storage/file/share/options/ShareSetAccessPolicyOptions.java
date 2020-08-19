// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.file.share.models.ShareSignedIdentifier;

import java.util.Collections;
import java.util.List;

/**
 * Extended options that may be passed when setting access policy on a share.
 */
@Fluent
public class ShareSetAccessPolicyOptions {

    private List<ShareSignedIdentifier> permissions;
    private String leaseId;

    /**
     * @return Access policies to set on the share.
     */
    public List<ShareSignedIdentifier> getPermissions() {
        return Collections.unmodifiableList(permissions);
    }

    /**
     * @return The lease id that the share must match.
     */
    public String getLeaseId() {
        return leaseId;
    }

    /**
     * @param permissions Access policies to set on the share.
     * @return The updated options.
     */
    public ShareSetAccessPolicyOptions setPermissions(List<ShareSignedIdentifier> permissions) {
        this.permissions = Collections.unmodifiableList(permissions);
        return this;
    }

    /**
     * @param leaseId The lease id that the share must match.
     * @return The updated options.
     */
    public ShareSetAccessPolicyOptions setLeaseId(String leaseId) {
        this.leaseId = leaseId;
        return this;
    }
}
