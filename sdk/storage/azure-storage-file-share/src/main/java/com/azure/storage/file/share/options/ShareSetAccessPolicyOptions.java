// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.options;

import com.azure.core.annotation.Fluent;
import com.azure.storage.file.share.models.ShareRequestConditions;
import com.azure.storage.file.share.models.ShareSignedIdentifier;

import java.util.Collections;
import java.util.List;

/**
 * Extended options that may be passed when setting access policy on a share.
 */
@Fluent
public class ShareSetAccessPolicyOptions {

    private List<ShareSignedIdentifier> permissions;
    private ShareRequestConditions requestConditions;

    /**
     * @return Access policies to set on the share.
     */
    public List<ShareSignedIdentifier> getPermissions() {
        return permissions == null ? null : Collections.unmodifiableList(permissions);
    }

    /**
     * @param permissions Access policies to set on the share.
     * @return The updated options.
     */
    public ShareSetAccessPolicyOptions setPermissions(List<ShareSignedIdentifier> permissions) {
        this.permissions = permissions == null ? null : Collections.unmodifiableList(permissions);
        return this;
    }

    /**
     * @return {@link ShareRequestConditions}.
     */
    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    /**
     * @param requestConditions {@link ShareRequestConditions}.
     * @return The updated options.
     */
    public ShareSetAccessPolicyOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }
}
