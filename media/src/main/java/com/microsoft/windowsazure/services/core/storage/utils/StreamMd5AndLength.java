/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.core.storage.utils;

import java.security.MessageDigest;

/**
 * RESERVED FOR INTERNAL USE. Represents a stream descriptor that contains the
 * stream size and MD5 hash.
 */
public final class StreamMd5AndLength
{
    /**
     * Contains the MD5 hash for the stream data.
     */
    private String streamMd5;

    /**
     * Contains the length, in bytes, for the stream.
     */
    private long streamLength;

    private MessageDigest intermediateMD5;

    public void setDigest(MessageDigest digest)
    {
        this.intermediateMD5 = digest;
    }

    /**
     * @return the length
     */
    public long getLength()
    {
        return this.streamLength;
    }

    /**
     * @return the md5
     */
    public String getMd5()
    {
        if (this.streamMd5 == null && this.intermediateMD5 != null)
        {
            this.streamMd5 = Base64.encode(this.intermediateMD5.digest());
        }

        return this.streamMd5;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(final long length)
    {
        this.streamLength = length;
    }

    /**
     * @param md5
     *            the md5 to set
     */
    public void setMd5(final String md5)
    {
        this.streamMd5 = md5;
    }
}
