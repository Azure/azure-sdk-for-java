package com.microsoft.azure.management.datalake.store.uploader;

import org.junit.Assert;

/**
 * Created by begoldsm on 4/15/2016.
 */
public class SsuMockFrontEnd implements FrontEndAdapter {

    private FrontEndAdapter BaseAdapter;
    /// <summary>
    /// Constructor with base front end.
    /// </summary>
    /// <param name="baseAdapter">The front end.</param>
    public SsuMockFrontEnd(FrontEndAdapter baseAdapter)
    {
        BaseAdapter = baseAdapter;
    }

    public void CreateStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws Exception {
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
        Assert.assertTrue("Concatenate should not be called when using 1 segment", false);
        BaseAdapter.Concatenate(targetStreamPath, inputStreamPaths);
    }
}
