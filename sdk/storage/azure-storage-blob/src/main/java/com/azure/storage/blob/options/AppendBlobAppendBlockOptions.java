// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.AppendBlobRequestConditions;
import com.azure.storage.common.implementation.StorageImplUtils;

public class AppendBlobAppendBlockOptions {
    private final BinaryData data;
    private byte[] contentMd5;
    private AppendBlobRequestConditions conditions;

    public AppendBlobAppendBlockOptions(BinaryData data) {
        StorageImplUtils.assertNotNull("data must not be null", data);
        StorageImplUtils.assertNotNull("data must have defined length", data.getLength());
        this.data = data;
    }

    public BinaryData getData() {
        return data;
    }

    public byte[] getContentMd5() {
        return contentMd5;
    }

    public AppendBlobAppendBlockOptions setContentMd5(byte[] contentMd5) {
        this.contentMd5 = contentMd5;
        return this;
    }

    public AppendBlobRequestConditions getConditions() {
        return conditions;
    }

    public AppendBlobAppendBlockOptions setConditions(AppendBlobRequestConditions conditions) {
        this.conditions = conditions;
        return this;
    }
}
