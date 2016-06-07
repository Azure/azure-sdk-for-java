/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.microsoft.rest.RestException;

import java.io.IOException;

/**
 * Represents a mocked front end for testing the {@link SingleSegmentUploader}
 */
public class SsuMockFrontEnd implements FrontEndAdapter {

    private FrontEndAdapter BaseAdapter;

    private boolean DoNothing;

    private boolean TestRetry;

    private int CallCount;

    private int FailCount;

    /**
     * Constructor with base front end.
     * @param baseAdapter The base adapter to use for non-mocked methods
     * @param doNothing If true, indicates that all methods should perform no actions and return default values.
     * @param testRetry If true, indicates that method implementations should test for the retry code paths. Cannot be true if doNothing is true.
     * @param failCount This is required when testRetry is true. It indicates the number of failures to allow for retries.
     */
    public SsuMockFrontEnd(FrontEndAdapter baseAdapter, boolean doNothing, boolean testRetry, int failCount)
    {
        BaseAdapter = baseAdapter;
        DoNothing = doNothing;
        TestRetry = testRetry;
        CallCount = 0;
        FailCount = failCount;
    }

    public void CreateStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws RestException, IOException {
        if (!DoNothing && !TestRetry) {
            BaseAdapter.CreateStream(streamPath, overwrite, data, byteCount);
        }
        else if(TestRetry) {
            CallCount++;
            if (CallCount <= FailCount)
            {
                throw new IntentionalException();
            }
            BaseAdapter.CreateStream(streamPath, overwrite, data, byteCount);
        }
    }

    public void DeleteStream(String streamPath, boolean recurse) throws RestException, IOException {
        if (!DoNothing) {
            BaseAdapter.DeleteStream(streamPath, recurse);
        }
    }

    public void AppendToStream(String streamPath, byte[] data, long offset, int byteCount) throws RestException, IOException {
        if (!DoNothing && !TestRetry) {
            BaseAdapter.AppendToStream(streamPath, data, offset, byteCount);
        }
        else if(TestRetry) {
            CallCount++;
            if (CallCount <= FailCount)
            {
                throw new IntentionalException();
            }
            BaseAdapter.AppendToStream(streamPath, data, offset, byteCount);
        }
    }

    public boolean StreamExists(String streamPath) throws RestException, IOException {
        if (!DoNothing) {
            return BaseAdapter.StreamExists(streamPath);
        }

        return true;
    }

    public long GetStreamLength(String streamPath) throws RestException, IOException {
        if (!DoNothing) {
            return BaseAdapter.GetStreamLength(streamPath);
        }

        return 0;
    }

    public void Concatenate(String targetStreamPath, String[] inputStreamPaths) throws RestException, IOException {
        if (!DoNothing) {
            BaseAdapter.Concatenate(targetStreamPath, inputStreamPaths);
        }
    }
}
