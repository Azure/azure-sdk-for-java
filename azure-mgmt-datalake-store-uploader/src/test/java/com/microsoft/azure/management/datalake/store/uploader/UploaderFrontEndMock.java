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
 * A front end mock used for unit testing {@link DataLakeStoreUploader}
 */
public class UploaderFrontEndMock implements FrontEndAdapter {

    private FrontEndAdapter BaseAdapter;
    private int createStreamCount;
    private boolean ThrowInConcat;
    private boolean ThrowInCreate;

    /**
     * Constructor with base front end.
     * @param baseAdapter The real front end to use when methods are not mocked.
     * @param throwInConcat If true, indicates that concatenation implementation should throw instead of doing work.
     * @param throwInCreate If true, indicates that the create implementation should throw instead of doing work.
     */
    public UploaderFrontEndMock(FrontEndAdapter baseAdapter, boolean throwInConcat, boolean throwInCreate)
    {
        createStreamCount = 0;
        ThrowInConcat = throwInConcat;
        ThrowInCreate = throwInCreate;
        BaseAdapter = baseAdapter;
    }

    public void createStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws RestException, IOException {

        if(ThrowInCreate) {
            createStreamCount++;
            if (createStreamCount > 1) {
                //we only allow 1 file to be created
                throw new IntentionalException();
            }
        }

        BaseAdapter.createStream(streamPath, overwrite, data, byteCount);
    }

    public void deleteStream(String streamPath, boolean recurse) throws RestException, IOException {
        BaseAdapter.deleteStream(streamPath, recurse);
    }

    public void appendToStream(String streamPath, byte[] data, long offset, int byteCount) throws RestException, IOException {
        BaseAdapter.appendToStream(streamPath, data, offset, byteCount);
    }

    public boolean streamExists(String streamPath) throws RestException, IOException {
        return BaseAdapter.streamExists(streamPath);
    }

    public long getStreamLength(String streamPath) throws RestException, IOException {
        return BaseAdapter.getStreamLength(streamPath);
    }

    public void concatenate(String targetStreamPath, String[] inputStreamPaths) throws RestException, IOException {
        if(ThrowInConcat) {
            throw new IntentionalException();
        }

        Assert.assertTrue("concatenate should not be called when using 1 segment", false);
        BaseAdapter.concatenate(targetStreamPath, inputStreamPaths);
    }
}
