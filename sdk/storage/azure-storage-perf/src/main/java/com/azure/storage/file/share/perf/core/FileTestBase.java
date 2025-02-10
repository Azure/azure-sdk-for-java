// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share.perf.core;

import com.azure.core.util.CoreUtils;
import com.azure.perf.test.core.PerfStressOptions;
import com.azure.storage.file.share.ShareFileAsyncClient;
import com.azure.storage.file.share.ShareFileClient;

public abstract class FileTestBase<TOptions extends PerfStressOptions> extends DirectoryTest<TOptions> {

    public static final int DEFAULT_BUFFER_SIZE = 8192;
    protected final ShareFileClient shareFileClient;
    protected final ShareFileAsyncClient shareFileAsyncClient;

    public FileTestBase(TOptions options) {
        super(options);

        String fileName = "randomfiletest-" + CoreUtils.randomUuid();

        shareFileClient = shareDirectoryClient.getFileClient(fileName);
        shareFileAsyncClient = shareDirectoryAsyncClient.getFileClient(fileName);
    }
}
