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
package com.microsoft.azure.storage.core;

import java.security.MessageDigest;

/**
 * RESERVED FOR INTERNAL USE. Represents a stream descriptor that contains the stream size and MD5 hash.
 */
public final class StreamMd5AndLength {
    /**
     * Contains the MD5 hash for the stream data.
     */
    private String streamMd5;

    /**
     * Contains the length, in bytes, for the stream.
     */
    private long streamLength;

    /**
     * Contains the length, in bytes, that have been downloaded. Used by download resume.
     */
    private long currentOperationByteCount;

    /**
     * The MessageDigest, used to calculate MD5.
     */
    private MessageDigest intermediateMD5;

    /**
     * @return the intermediateMD5
     */
    public MessageDigest getDigest() {
        return this.intermediateMD5;
    }

    /**
     * @return the length
     */
    public long getLength() {
        return this.streamLength;
    }

    /**
     * @return the currentOperationByteCount
     */
    public long getCurrentOperationByteCount() {
        return this.currentOperationByteCount;
    }

    /**
     * @return the md5
     */
    public String getMd5() {
        if (this.streamMd5 == null && this.intermediateMD5 != null) {
            this.streamMd5 = Base64.encode(this.intermediateMD5.digest());
        }

        return this.streamMd5;
    }

    /**
     * Sets the MessageDigest, used to calculate MD5
     * 
     * @param digest
     *            the digest to set
     */
    public void setDigest(MessageDigest digest) {
        this.intermediateMD5 = digest;
    }

    /**
     * @param length
     *            the length to set
     */
    public void setLength(final long length) {
        this.streamLength = length;
    }

    /**
     * @param currentOperationByteCount
     *            the currentOperationByteCount to set
     */
    public void setCurrentOperationByteCount(final long currentOperationByteCount) {
        this.currentOperationByteCount = currentOperationByteCount;
    }

    /**
     * @param md5
     *            the md5 to set
     */
    public void setMd5(final String md5) {
        this.streamMd5 = md5;
    }
}
