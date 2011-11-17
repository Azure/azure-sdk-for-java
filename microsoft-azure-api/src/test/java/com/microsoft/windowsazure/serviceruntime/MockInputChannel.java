/**
 * 
 */
package com.microsoft.windowsazure.serviceruntime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;

/**
 *
 */
public class MockInputChannel implements InputChannel {
    private final ByteArrayInputStream inputStream;

    public MockInputChannel(String channelData) {
        byte data[] = null;

        try {
            data = channelData.getBytes("UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        this.inputStream = new ByteArrayInputStream(data);
    }

    public MockInputChannel(String chunks[]) {
        int finalLength = 0;
        LinkedList<byte[]> chunkBytes = new LinkedList<byte[]>();
        byte[] crlf = new byte[] { 0x0D, 0x0A };

        for (String chunk : chunks) {
            try {
                byte chunkLengthData[] = Integer.toHexString(chunk.length()).getBytes("US-ASCII");
                byte chunkData[] = chunk.getBytes("UTF-8");
                int chunkLength = chunkLengthData.length + chunkData.length + 4;

                chunkBytes.add(chunkLengthData);
                chunkBytes.add(chunkData);

                finalLength += chunkLength;
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        byte data[] = new byte[finalLength];
        int dataIndex = 0;

        for (byte[] chunk : chunkBytes) {
            System.arraycopy(chunk, 0, data, dataIndex, chunk.length);

            dataIndex += chunk.length;

            System.arraycopy(crlf, 0, data, dataIndex, crlf.length);

            dataIndex += crlf.length;
        }

        this.inputStream = new ByteArrayInputStream(data);
    }

    @Override
    public InputStream getInputStream(String name) {
        return this.inputStream;
    }

}
