// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.perf.BlobPerfStressOptions;

public abstract class AbstractUploadTest<TOptions extends BlobPerfStressOptions> extends BlobTestBase<TOptions> {

    /**
     * This ctor makes sure that each upload tests targets different blob.
     * @param options options
     */
    public AbstractUploadTest(TOptions options) {
        super(options, BLOB_NAME_PREFIX + CoreUtils.randomUuid());
    }
}
