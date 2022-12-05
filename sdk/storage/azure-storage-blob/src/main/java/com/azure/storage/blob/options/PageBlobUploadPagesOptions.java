// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.options;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.PageBlobRequestConditions;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.common.implementation.StorageImplUtils;

public class PageBlobUploadPagesOptions {
    private final PageRange pageRange;
    private final BinaryData data;
    private byte[] contentMd5;
    private PageBlobRequestConditions conditions;

    public PageBlobUploadPagesOptions(PageRange pageRange, BinaryData data) {
        StorageImplUtils.assertNotNull("pageRange must not be null", pageRange);
        StorageImplUtils.assertNotNull("data must not be null", data);
        this.pageRange = pageRange;
        this.data = data;
    }

    public PageRange getPageRange() {
        return pageRange;
    }

    public BinaryData getData() {
        return data;
    }

    public byte[] getContentMd5() {
        return contentMd5;
    }

    public PageBlobUploadPagesOptions setContentMd5(byte[] contentMd5) {
        this.contentMd5 = contentMd5;
        return this;
    }

    public PageBlobRequestConditions getConditions() {
        return conditions;
    }

    public PageBlobUploadPagesOptions setConditions(PageBlobRequestConditions conditions) {
        this.conditions = conditions;
        return this;
    }
}
