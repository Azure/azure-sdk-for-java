/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
        } catch (UnsupportedEncodingException e) {
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
                byte chunkLengthData[] = Integer.toHexString(chunk.length())
                        .getBytes("US-ASCII");
                byte chunkData[] = chunk.getBytes("UTF-8");
                int chunkLength = chunkLengthData.length + chunkData.length + 4;

                chunkBytes.add(chunkLengthData);
                chunkBytes.add(chunkData);

                finalLength += chunkLength;
            } catch (UnsupportedEncodingException e) {
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
