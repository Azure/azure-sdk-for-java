// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.storage.common.BaseClientBuilder;

abstract class BaseBlobClientBuilder<T extends BaseClientBuilder<T>> extends BaseClientBuilder<T> {

    private static final String BLOB_ENDPOINT_MIDFIX = "blob";

    @Override
    protected final UserAgentPolicy getUserAgentPolicy() {
        return new UserAgentPolicy(BlobConfiguration.NAME, BlobConfiguration.VERSION, super.getConfiguration());
    }

    @Override
    protected final String getServiceUrlMidfix() {
        return BLOB_ENDPOINT_MIDFIX;
    }
}
