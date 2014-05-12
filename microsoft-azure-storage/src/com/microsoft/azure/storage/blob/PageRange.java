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
package com.microsoft.azure.storage.blob;

/**
 * Represents a range of pages in a page blob.
 */
public final class PageRange {
    /**
     * Represents the ending offset of the page range.
     */
    private long endOffset;

    /**
     * Represents the starting offset of the page range.
     */
    private long startOffset;

    /**
     * Creates an instance of the <code>{@link PageRange}</code> class.
     * 
     * @param start
     *        A <code>long</code> which represents the starting offset.
     * @param end
     *        A <code>long</code> which represents the ending offset.
     */
    public PageRange(final long start, final long end) {
        this.setStartOffset(start);
        this.setEndOffset(end);
    }

    /**
     * Gets the ending offset.
     * 
     * @return A <code>long</code> which represents the ending offset.
     */
    public long getEndOffset() {
        return this.endOffset;
    }

    /**
     * Gets the starting offset.
     * 
     * @return A <code>long</code> which represents the starting offset.
     */
    public long getStartOffset() {
        return this.startOffset;
    }

    /**
     * Sets the ending offset.
     * 
     * @param endOffset
     *        A <code>long</code> which specifies the ending offset.
     */
    public void setEndOffset(final long endOffset) {
        this.endOffset = endOffset;
    }

    /**
     * Sets the starting offset.
     * 
     * @param startOffset
     *        A <code>long</code> which specifies the starting offset.
     */
    public void setStartOffset(final long startOffset) {
        this.startOffset = startOffset;
    }

    /**
     * Returns the content of the page range as a string.
     * 
     * @return A <code>String</code> which represents the contents of the page range.
     */
    @Override
    public String toString() {
        return String.format("bytes=%d-%d", this.getStartOffset(), this.getEndOffset());
    }
}
