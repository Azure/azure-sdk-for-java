/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.microsoft.rest.RestException;
import org.junit.Assert;

import java.io.IOException;

/**
 * A mocked front end for testing out the code paths of the {@link MultipleSegmentUploader}
 */
public class MsuMockFrontEnd implements FrontEndAdapter {

    private FrontEndAdapter BaseAdapter;
    private boolean TestRetry;
    private int FailCount;
    private int CallCount;

    /**
     * Constructor with base front end.
     * @param baseAdapter The "real" front end to use for non-mocked calls
     * @param testRetry Indicates if it should mock out retry logic.
     * @param failCount Required if mocking retry logic, indicates the number of failures to allow.
     */
    public MsuMockFrontEnd(FrontEndAdapter baseAdapter, boolean testRetry, int failCount)
    {
        TestRetry = testRetry;
        BaseAdapter = baseAdapter;
        FailCount = failCount;
        CallCount = 0;
    }

    public void CreateStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws RestException, IOException {
        if (TestRetry) {
            CallCount++;
            if (CallCount <= FailCount)
            {
                throw new IntentionalException();
            }
        }

        BaseAdapter.CreateStream(streamPath, overwrite, data, byteCount);
    }

    public void DeleteStream(String streamPath, boolean recurse) throws IOException, RestException {
        BaseAdapter.DeleteStream(streamPath, recurse);
    }

    public void AppendToStream(String streamPath, byte[] data, long offset, int byteCount) throws IOException, RestException {
        if (TestRetry) {
            CallCount++;
            if (CallCount <= FailCount)
            {
                throw new IntentionalException();
            }
        }

        BaseAdapter.AppendToStream(streamPath, data, offset, byteCount);
    }

    public boolean StreamExists(String streamPath) throws IOException, RestException {
        return BaseAdapter.StreamExists(streamPath);
    }

    public long GetStreamLength(String streamPath) throws IOException, RestException {
        return BaseAdapter.GetStreamLength(streamPath);
    }

    public void Concatenate(String targetStreamPath, String[] inputStreamPaths) throws IOException, RestException {
        Assert.assertTrue("Concatenate should not be called when using 1 segment", false);
        BaseAdapter.Concatenate(targetStreamPath, inputStreamPaths);
    }
}
