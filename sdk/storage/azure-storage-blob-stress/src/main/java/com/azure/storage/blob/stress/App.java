// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress;

import com.azure.perf.test.core.PerfStressProgram;
import com.azure.storage.stress.TelemetryHelper;

public class App {
    public static void main(String[] args) {
        TelemetryHelper.init();
        PerfStressProgram.run(new Class<?>[]{
//            AppendBlock.class,
//            AppendBlobOutputStream.class,
//            BlockBlobOutputStream.class,
//            BlockBlobUpload.class,
//            BlockBlobUploadFromUrl.class,
//            CommitBlockList.class,
            DownloadToFile.class
//            DownloadStream.class,
//            DownloadContent.class,
//            OpenInputStream.class,
//            OpenSeekableByteChannelRead.class,
//            OpenSeekableByteChannelWrite.class,
//            PageBlobOutputStream.class,
//            StageBlock.class,
//            StageBlockFromUrl.class,
//            Upload.class,
//            UploadFromFile.class,
//            UploadPages.class,
//            UploadPagesFromUrl.class
        }, args);
    }
}
