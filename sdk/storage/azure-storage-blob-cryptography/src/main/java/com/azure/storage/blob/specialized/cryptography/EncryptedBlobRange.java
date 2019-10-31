// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.util.CoreUtils;
import com.azure.storage.blob.models.BlobRange;


import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_BLOCK_SIZE;

/**
 * This is a representation of a range of bytes on an encrypted blob, which may be expanded from the requested range to
 * included extra data needed for encryption. Note that this type is not strictly thread-safe as the download method
 * will update the count in case the user did not specify one. Passing null as an EncryptedBlobRange value will default
 * to the entire range of the blob.
 */
final class EncryptedBlobRange {

    /**
     * The BlobRange passed by the customer and the range we must actually return.
     */
    private final BlobRange originalRange;

    /**
     * Amount the beginning of the range, 0-31, needs to be adjusted in order to align along an encryption block
     * boundary and include the IV.
     */
    private final int offsetAdjustment;

    /**
     * How many bytes to download, including the adjustments for encryption block boundaries and the IV.
     * Must be greater than or equal to 0 if specified.
     */
    private Long adjustedDownloadCount;

    static EncryptedBlobRange getEncryptedBlobRangeFromHeader(String stringRange) {
        // Null case
        if (CoreUtils.isNullOrEmpty(stringRange)) {
            return new EncryptedBlobRange(null);
        }
        // Non-null case
        String trimmed = stringRange.substring(stringRange.indexOf("=") + 1); // Trim off the "bytes=" part
        String[] pieces = trimmed.split("-"); // Split on the "-"
        BlobRange range;
        long offset = Long.parseLong(pieces[0]);
        if (pieces.length == 1) {
            range = new BlobRange(offset);
        } else {
            long rangeEnd = Long.parseLong(pieces[1]);
            long count = rangeEnd - offset + 1;
            range = new BlobRange(offset, count);
        }
        return new EncryptedBlobRange(range);
    }

    EncryptedBlobRange(BlobRange originalRange) {
        if (originalRange == null) {
            this.originalRange = new BlobRange(0);
            this.offsetAdjustment = 0;
            return;
        }

        this.originalRange = originalRange;
        int tempOffsetAdjustment = 0;
        this.adjustedDownloadCount = this.originalRange.getCount();

        // Calculate offsetAdjustment.
        if (originalRange.getOffset() != 0) {

            // Align with encryption block boundary.
            if (originalRange.getOffset() % ENCRYPTION_BLOCK_SIZE != 0) {
                long diff = this.originalRange.getOffset() % ENCRYPTION_BLOCK_SIZE;
                tempOffsetAdjustment += diff;
                if (this.adjustedDownloadCount != null) {
                    this.adjustedDownloadCount += diff;
                }
            }

            // Account for IV.
            if (this.originalRange.getOffset() >= ENCRYPTION_BLOCK_SIZE) {
                tempOffsetAdjustment += ENCRYPTION_BLOCK_SIZE;
                // Increment adjustedDownloadCount if necessary.
                if (this.adjustedDownloadCount != null) {
                    this.adjustedDownloadCount += ENCRYPTION_BLOCK_SIZE;
                }
            }
        }

        this.offsetAdjustment = tempOffsetAdjustment;

        /*
        Align adjustedDownloadCount with encryption block boundary at the end of the range. Note that it is impossible
        to adjust past the end of the blob as an encrypted blob was padded to align to an encryption block boundary.
         */
        if (this.adjustedDownloadCount != null) {
            this.adjustedDownloadCount += ENCRYPTION_BLOCK_SIZE
                - (int) (this.adjustedDownloadCount % ENCRYPTION_BLOCK_SIZE);
        }

    }

    /**
     * @return The calculated {@link BlobRange}
     */
    BlobRange getOriginalRange() {
        return this.originalRange;
    }

    /**
     * @return Offset from beginning of BlobRange, 0-31.
     */
    int getOffsetAdjustment() {
        return this.offsetAdjustment;
    }

    /**
     * @return How many bytes to include in the range. Must be greater than or equal to 0 if specified.
     */
    Long getAdjustedDownloadCount() {
        return this.adjustedDownloadCount;
    }

    /**
     * @param count The adjustedDownloadCount
     */
    void setAdjustedDownloadCount(long count) {
        this.adjustedDownloadCount = count;
    }

    /**
     * For convenient interaction with the blobURL.download() method.
     *
     * @return A {@link BlobRange} object which includes the necessary adjustments to offset and count to effectively
     *        decrypt the blob.
     */
    BlobRange toBlobRange() {
        return new BlobRange(this.originalRange.getOffset() - this.offsetAdjustment, this.adjustedDownloadCount);
    }
}
