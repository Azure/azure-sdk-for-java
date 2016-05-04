/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import com.microsoft.azure.CloudException;

import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Test front-end, fully in-memory.
 */
public class InMemoryFrontEnd implements FrontEndAdapter {
    private Hashtable<String, StreamData> _streams = new Hashtable<>();

    /**
     *
     * @param streamPath The relative path to the stream.
     * @param overwrite  Whether to overwrite an existing stream.
     * @param data
     * @param byteCount
     * @Throws CloudException
     */
    public void CreateStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws CloudException {
        if (overwrite)
        {
            _streams.put(streamPath, new StreamData(streamPath));
        }
        else
        {
            if (StreamExists(streamPath))
            {
                throw new CloudException("stream exists");
            }

            _streams.put(streamPath, new StreamData(streamPath));
        }

        // if there is data passed in, we should do the same operation as in append
        if (data != null)
        {
            if (byteCount > data.length)
            {
                throw new CloudException("invalid byteCount");
            }

            StreamData stream = _streams.get(streamPath);

            //always make a copy of the original buffer since it is reused
            byte[] toAppend = new byte[byteCount];
            System.arraycopy(data, 0, toAppend, 0, byteCount);

            stream.Append(toAppend);
        }
    }

    /**
     *
     * @param streamPath The relative path to the stream.
     * @param recurse    if set to true recursively delete. This is used for folder streams only.
     * @Throws CloudException
     */
    public void DeleteStream(String streamPath, boolean recurse) throws CloudException {
        if (!StreamExists(streamPath))
        {
            throw new CloudException("stream does not exist");
        }
        _streams.remove(streamPath);
    }

    /**
     *
     * @param streamPath The relative path to the stream.
     * @param data An array of bytes to be appended to the stream.
     * @param offset The offset at which to append to the stream.
     * @param byteCount
     * @Throws CloudException
     */
    public void AppendToStream(String streamPath, byte[] data, long offset, int byteCount) throws CloudException {
        if (!StreamExists(streamPath))
        {
            throw new CloudException("stream does not exist");
        }

        if (byteCount > data.length)
        {
            throw new CloudException("invalid byteCount");
        }

        StreamData stream = _streams.get(streamPath);
        if (stream.Length != offset)
        {
            throw new CloudException("offset != stream.length");
        }

        //always make a copy of the original buffer since it is reused
        byte[] toAppend = new byte[byteCount];
        System.arraycopy(data, 0, toAppend, 0, byteCount);

        stream.Append(toAppend);
    }

    /**
     *
     * @param streamPath The relative path to the stream.
     * @return True or false if the stream exists
     */
    public boolean StreamExists(String streamPath)
    {
        return _streams.containsKey(streamPath);
    }

    /**
     *
     * @param streamPath The relative path to the stream.
     * @return
     * @Throws CloudException
     */
    public long GetStreamLength(String streamPath) throws CloudException {
        if (!StreamExists(streamPath))
        {
            throw new CloudException("stream does not exist");
        }

        return _streams.get(streamPath).Length;
    }

    /**
     *
     * @param targetStreamPath The relative path to the target stream.
     * @param inputStreamPaths An ordered array of paths to the input streams.
     * @Throws CloudException
     */
    public void Concatenate(String targetStreamPath, String[] inputStreamPaths) throws CloudException {
        if (StreamExists(targetStreamPath))
        {
            throw new CloudException("target stream exists");
        }

        final int bufferSize = 4 * 1024 * 1024;
        byte[] buffer = new byte[bufferSize];

        try
        {
            CreateStream(targetStreamPath, true, null, 0);
            StreamData targetStream = _streams.get(targetStreamPath);

            for (String inputStreamPath: inputStreamPaths)
            {
                if (!StreamExists(inputStreamPath))
                {
                    throw new CloudException("input stream does not exist");
                }

                StreamData stream = _streams.get(inputStreamPath);
                for (byte[] chunk: stream.GetDataChunks())
                {
                    targetStream.Append(chunk);
                }
            }
        }
        catch (CloudException e)
        {
            if (StreamExists(targetStreamPath))
            {
                DeleteStream(targetStreamPath, false);
            }
            throw e;
        }

        for (String inputStreamPath: inputStreamPaths)
        {
            DeleteStream(inputStreamPath, false);
        }
    }

    /**
     *
     * @param streamPath
     * @return
     * @Throws CloudException
     */
    public Iterable<byte[]> GetAppendBlocks(String streamPath) throws CloudException {
        if (!StreamExists(streamPath))
        {
            throw new CloudException("stream does not exist");
        }

        StreamData sd = _streams.get(streamPath);
        return sd.GetDataChunks();
    }

    /**
     *
     * @param streamPath
     * @return
     * @Throws CloudException
     */
    public byte[] GetStreamContents(String streamPath) throws CloudException {
        if (!StreamExists(streamPath))
        {
            throw new CloudException("stream does not exist");
        }

        StreamData sd = _streams.get(streamPath);

        if (sd.Length > Integer.MAX_VALUE)
        {
            throw new OutOfMemoryError("Stream has too much data and cannot be fit into a single array");
        }

        byte[] result = new byte[(int)sd.Length];
        int position = 0;
        for (byte[] chunk: sd.GetDataChunks())
        {
            System.arraycopy(chunk, 0, result, position, chunk.length);
            position += chunk.length;
        }

        return result;
    }

    /**
     * Returns the number of "streams" that have been created by this adapter.
     *
     * @return the number of streams.
     */
    public int getStreamCount()
    {
        return _streams.size();
    }

    /**
     * Represents stream data for unit testing purposes.
     */
    private class StreamData
    {
        private LinkedList<byte[]> _data;

        /**
         * Initializes new stream data with the given name.
         * @param name
         */
        public StreamData(String name)
        {
            _data = new LinkedList<byte[]>();
            this.Name = name;
            this.Length = 0;
        }

        public String getName() {
            return Name;
        }

        public void setName(String name) {
            Name = name;
        }

        public String Name;
        public long Length;

        public void Append(byte[] data)
        {
            _data.addLast(data);
            this.Length += data.length;
        }

        public Iterable<byte[]> GetDataChunks()
        {
            return _data;
        }
    }
}
