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
 * Represents a range of pages in a page blob which have changed since the previous snapshot specified.
 */
public final class PageRangeDiff extends PageRange {
    /**
     * True if the page range is a cleared range, false otherwise.
     */
    private boolean isCleared;

    /**
     * Creates an instance of the <code>{@link PageRangeDiff}</code> class.
     * 
     * @param start
     *        A <code>long</code> which represents the starting offset.
     * @param end
     *        A <code>long</code> which represents the ending offset.
     * @param isCleared
     *        <code>True</code> if the page range is a cleared range, <code>false</code> otherwise.
     */
    protected PageRangeDiff(final long start, final long end, final boolean isCleared) {
        super(start, end);
        this.setIsCleared(isCleared);
    }

    /**
     * Indicates whether the page range was cleared.
     * 
     * @return <code>True</code> if the page range is a cleared range, <code>false</code> otherwise.
     */
    public boolean isCleared() {
        return this.isCleared;
    }

    /**
     * Sets the ending offset.
     * 
     * @param isCleared
     *        <code>True</code> if the page range is a cleared range, <code>false</code> otherwise.
     */
    protected void setIsCleared(final boolean isCleared) {
        this.isCleared = isCleared;
    }
}