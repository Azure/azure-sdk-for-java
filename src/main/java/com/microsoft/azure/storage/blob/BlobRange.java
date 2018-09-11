/*
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

import java.util.Locale;

/**
 * This is a representation of a range of bytes on a blob, typically used during a download operation. This type is
 * immutable to ensure thread-safety of requests, so changing the values for a different operation requires construction
 * of a new object. Passing null as a BlobRange value will default to the entire range of the blob.
 */
public final class BlobRange {

    /**
     * An object which reflects the service's default range, which is the whole blob.
     */
    public static final BlobRange DEFAULT = new BlobRange();

    private long offset;

    private Long count;

    public BlobRange() {
    }

    /**
     * The start of the range. Must be greater than or equal to 0.
     */
    public long offset() {
        return offset;
    }

    /**
     * The start of the range. Must be greater than or equal to 0.
     */
    public BlobRange withOffset(long offset) {
        if (offset < 0) {
            throw new IllegalArgumentException("BlobRange offset must be greater than or equal to 0.");
        }
        this.offset = offset;
        return this;
    }

    /**
     * How many bytes to include in the range. Must be greater than or equal to 0 if specified.
     */
    public Long count() {
        return count;
    }

    /**
     * How many bytes to include in the range. Must be greater than or equal to 0 if specified.
     */
    public BlobRange withCount(Long count) {
        if (count != null && count < 0) {
            throw new IllegalArgumentException(
                    "BlobRange count must be greater than or equal to 0 if specified.");
        }
        this.count = count;
        return this;
    }

    /**
     * @return
     *      A {@code String} compliant with the format of the Azure Storage x-ms-range and Range headers.
     */
    @Override
    public String toString() {
        if (this.count != null) {
            long rangeEnd = this.offset + this.count - 1;
            return String.format(
                    Locale.ROOT, Constants.HeaderConstants.RANGE_HEADER_FORMAT, this.offset, rangeEnd);
        }

        return String.format(
                Locale.ROOT, Constants.HeaderConstants.BEGIN_RANGE_HEADER_FORMAT, this.offset);
    }
}
