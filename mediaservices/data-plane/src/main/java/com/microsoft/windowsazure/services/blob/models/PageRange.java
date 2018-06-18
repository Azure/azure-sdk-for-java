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
package com.microsoft.windowsazure.services.blob.models;

import javax.xml.bind.annotation.XmlElement;

/**
 * Represents the range of bytes in a single page within a page blob.
 * <p>
 * For a page update operation, the page range can be up to 4 MB in size. For a
 * page clear operation, the page range can be up to the value of the blob's
 * full size.
 * <p>
 * Pages are aligned with 512-byte boundaries. When specifying a page range, the
 * start offset must be a modulus of 512 and the end offset must be a modulus of
 * 512 - 1. Examples of valid byte ranges are 0-511, 512-1023, etc.
 */
public class PageRange {
    private long start;
    private long end;

    /**
     * Default constructor. The start and end values must be set for this
     * {@link PageRange} instance to be valid.
     */
    public PageRange() {
    }

    /**
     * Creates a page range from the specified start and end byte offsets,
     * inclusive.
     * <p>
     * Pages are aligned with 512-byte boundaries. When specifying a page range,
     * the start offset must be a modulus of 512 and the end offset must be a
     * modulus of 512 - 1. Examples of valid byte ranges are 0-511, 512-1023,
     * etc.
     * 
     * @param start
     *            The beginning offset value in bytes for the page range,
     *            inclusive.
     * @param end
     *            The ending offset value in bytes for the page range,
     *            inclusive.
     */
    public PageRange(long start, long end) {
        this.start = start;
        this.end = end;
    }

    /**
     * Gets the byte offset of the start of the page range within the blob,
     * inclusive.
     * 
     * @return The beginning offset value in bytes for the page range,
     *         inclusive.
     */
    @XmlElement(name = "Start")
    public long getStart() {
        return start;
    }

    /**
     * Sets the byte offset of the start of the page range within the blob,
     * inclusive.
     * <p>
     * Pages are aligned with 512-byte boundaries. When specifying a page range,
     * the start offset must be a modulus of 512 and the end offset must be a
     * modulus of 512 - 1. Examples of valid byte ranges are 0-511, 512-1023,
     * etc.
     * 
     * @param start
     *            The beginning offset value in bytes for the page range,
     *            inclusive.
     * @return A reference to this {@link PageRange} instance.
     */
    public PageRange setStart(long start) {
        this.start = start;
        return this;
    }

    /**
     * Gets the byte offset of the end of the page range within the blob,
     * inclusive.
     * 
     * @return The ending offset value in bytes for the page range, inclusive.
     */
    @XmlElement(name = "End")
    public long getEnd() {
        return end;
    }

    /**
     * Sets the byte offset of the end of the page range within the blob,
     * inclusive.
     * <p>
     * Pages are aligned with 512-byte boundaries. When specifying a page range,
     * the start offset must be a modulus of 512 and the end offset must be a
     * modulus of 512 - 1. Examples of valid byte ranges are 0-511, 512-1023,
     * etc.
     * 
     * @param end
     *            The ending offset value in bytes for the page range,
     *            inclusive.
     * @return A reference to this {@link PageRange} instance.
     */
    public PageRange setEnd(long end) {
        this.end = end;
        return this;
    }

    /**
     * Gets the size of the page range in bytes.
     * 
     * @return The size of the page range in bytes.
     */
    public long getLength() {
        return end - start + 1;
    }

    /**
     * Sets the length of the page range in bytes. This updates the byte offset
     * of the end of the page range to the start value plus the length specified
     * by the <em>value</em> parameter. The length must be a positive multiple
     * of 512 bytes.
     * <p>
     * Pages are aligned with 512-byte boundaries. When specifying a page range,
     * the start offset must be a modulus of 512 and the end offset must be a
     * modulus of 512 - 1. Examples of valid byte ranges are 0-511, 512-1023,
     * etc.
     * 
     * @param value
     *            The ending offset value in bytes for the page range,
     *            inclusive.
     * @return A reference to this {@link PageRange} instance.
     */
    public PageRange setLength(long value) {
        this.end = this.start + value - 1;
        return this;
    }
}
