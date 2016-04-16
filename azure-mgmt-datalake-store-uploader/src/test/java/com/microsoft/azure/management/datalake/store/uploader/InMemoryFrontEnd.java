package com.microsoft.azure.management.datalake.store.uploader;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Created by begoldsm on 4/14/2016.
 */
public class InMemoryFrontEnd implements FrontEndAdapter {
    private Hashtable<String, StreamData> _streams = new Hashtable<>();

    public void CreateStream(String streamPath, boolean overwrite, byte[] data, int byteCount) throws Exception {
        if (overwrite)
        {
            _streams.put(streamPath, new StreamData(streamPath));
        }
        else
        {
            if (StreamExists(streamPath))
            {
                throw new Exception("stream exists");
            }

            _streams.put(streamPath, new StreamData(streamPath));
        }

        // if there is data passed in, we should do the same operation as in append
        if (data != null)
        {
            if (byteCount > data.length)
            {
                throw new Exception("invalid byteCount");
            }

            StreamData stream = _streams.get(streamPath);

            //always make a copy of the original buffer since it is reused
            byte[] toAppend = new byte[byteCount];
            System.arraycopy(data, 0, toAppend, 0, byteCount);

            stream.Append(toAppend);
        }
    }

    public void DeleteStream(String streamPath, boolean recurse) throws Exception {
        if (!StreamExists(streamPath))
        {
            throw new Exception("stream does not exist");
        }
        _streams.remove(streamPath);
    }

    public void AppendToStream(String streamPath, byte[] data, long offset, int byteCount) throws Exception {
        if (!StreamExists(streamPath))
        {
            throw new Exception("stream does not exist");
        }

        if (byteCount > data.length)
        {
            throw new Exception("invalid byteCount");
        }

        StreamData stream = _streams.get(streamPath);
        if (stream.Length != offset)
        {
            throw new Exception("offset != stream.length");
        }

        //always make a copy of the original buffer since it is reused
        byte[] toAppend = new byte[byteCount];
        System.arraycopy(data, 0, toAppend, 0, byteCount);

        stream.Append(toAppend);
    }

    public boolean StreamExists(String streamPath)
    {
        return _streams.containsKey(streamPath);
    }

    public long GetStreamLength(String streamPath) throws Exception {
        if (!StreamExists(streamPath))
        {
            throw new Exception("stream does not exist");
        }

        return _streams.get(streamPath).Length;
    }

    public void Concatenate(String targetStreamPath, String[] inputStreamPaths) throws Exception {
        if (StreamExists(targetStreamPath))
        {
            throw new Exception("target stream exists");
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
                    throw new Exception("input stream does not exist");
                }

                StreamData stream = _streams.get(inputStreamPath);
                for (byte[] chunk: stream.GetDataChunks())
                {
                    targetStream.Append(chunk);
                }
            }
        }
        catch (Exception e)
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

    public Iterable<byte[]> GetAppendBlocks(String streamPath) throws Exception {
        if (!StreamExists(streamPath))
        {
            throw new Exception("stream does not exist");
        }

        StreamData sd = _streams.get(streamPath);
        return sd.GetDataChunks();
    }

    public byte[] GetStreamContents(String streamPath) throws Exception {
        if (!StreamExists(streamPath))
        {
            throw new Exception("stream does not exist");
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

    public int getStreamCount()
    {
        return _streams.size();
    }

    private class StreamData
    {
        private LinkedList<byte[]> _data;

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
