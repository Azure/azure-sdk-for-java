package com.microsoft.azure.management.datalake.store.uploader;

import org.junit.Assert;

/**
 * Created by begoldsm on 4/15/2016.
 */
public class UploaderFrontEndMock implements FrontEndAdapter {

    private FrontEndAdapter BaseAdapter;
    private int createStreamCount;
    private boolean ThrowInConcat;
    private boolean ThrowInCreate;
    /// <summary>
    /// Constructor with base front end.
    /// </summary>
    /// <param name="baseAdapter">The front end.</param>
    public UploaderFrontEndMock(FrontEndAdapter baseAdapter, boolean throwInConcat, boolean throwInCreate)
    {
        createStreamCount = 0;
        ThrowInConcat = throwInConcat;
        ThrowInCreate = throwInCreate;
        BaseAdapter = baseAdapter;
    }

    public void CreateStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws Exception {

        if(ThrowInCreate) {
            createStreamCount++;
            if (createStreamCount > 1) {
                //we only allow 1 file to be created
                throw new IntentionalException();
            }
        }

        BaseAdapter.CreateStream(streamPath, overwrite, data, byteCount);
    }

    public void DeleteStream(String streamPath, boolean recurse) throws Exception {
        BaseAdapter.DeleteStream(streamPath, recurse);
    }

    public void AppendToStream(String streamPath, byte[] data, long offset, int byteCount) throws Exception {
        BaseAdapter.AppendToStream(streamPath, data, offset, byteCount);
    }

    public boolean StreamExists(String streamPath) throws Exception {
        return BaseAdapter.StreamExists(streamPath);
    }

    public long GetStreamLength(String streamPath) throws Exception {
        return BaseAdapter.GetStreamLength(streamPath);
    }

    public void Concatenate(String targetStreamPath, String[] inputStreamPaths) throws Exception {
        if(ThrowInConcat) {
            throw new IntentionalException();
        }

        Assert.assertTrue("Concatenate should not be called when using 1 segment", false);
        BaseAdapter.Concatenate(targetStreamPath, inputStreamPaths);
    }
}
