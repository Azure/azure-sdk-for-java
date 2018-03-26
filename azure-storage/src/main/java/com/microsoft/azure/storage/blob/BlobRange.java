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
    public static final BlobRange DEFAULT = new BlobRange(0, null);

    private final long offset;

    private final Long count;

    /**
     * A {@code BlobRange} object.
     *
     * @param offset
     *      The start of the range. Must be greater than or equal to 0.
     * @param count
     *      How many bytes to include in the range. Must be greater than or equal to 0 if specified. If specified,
     *      offset must also be specified.
     */
    public BlobRange(long offset, Long count) {
        if (offset < 0) {
            throw new IllegalArgumentException("BlobRange offset must be greater than or equal to 0.");
        }
        if (count != null && count < 0) {
            throw new IllegalArgumentException(
                    "BlobRange count must be greater than or equal to 0 if specified. Cannot be specified without an " +
                            "offset.");
        }
        this.offset = offset;
        this.count = count;
    }

    /**
     * @return
     *      A {@code long} that indicates the start of the range.
     */
    public long getOffset() {
        return offset;
    }

    /**
     * @return
     *      A {@code long} that indicates how many bytes to include in the range.
     */
    public Long getCount() {
        return count;
    }

    @Override
    /**
     * @returns
     *      A {@code String} compliant with the format of the Azure Storage x-ms-range and Range headers.
     */
    public String toString() {

        if (count != null) {
            long rangeEnd = this.offset + this.count - 1;
            return String.format(
                    Locale.US, Constants.HeaderConstants.RANGE_HEADER_FORMAT, this.offset, rangeEnd);
        }

        return String.format(
                Locale.US, Constants.HeaderConstants.BEGIN_RANGE_HEADER_FORMAT, this.offset);
    }
}
