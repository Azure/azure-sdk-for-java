// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.util.logging.ClientLogger;

import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V1;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.ENCRYPTION_PROTOCOL_V2_1;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.NONCE_LENGTH;
import static com.azure.storage.blob.specialized.cryptography.CryptographyConstants.TAG_LENGTH;

/**
 * This class provides helper methods for adjusting encrypted downloads.
 */
final class EncryptedBlobLength {
    private static final ClientLogger LOGGER = new ClientLogger(EncryptedBlobLength.class);

    static Long computeAdjustedBlobLength(EncryptionData encryptionData, Long encryptedLength) {
        switch (encryptionData.getEncryptionAgent().getProtocol()) {
            /*
             Technically, the total unencrypted length may be different for v1,
             but because this helper method is only used for partitioning ranged downloads,
             the size does not need to be adjusted for v1.
             */
            case ENCRYPTION_PROTOCOL_V1:
                return encryptedLength;

            case ENCRYPTION_PROTOCOL_V2:
            case ENCRYPTION_PROTOCOL_V2_1:
                long regionLength = encryptionData.getEncryptedRegionInfo().getDataLength();
                long region
                    = (long) Math.ceil((double) encryptedLength / (double) (regionLength + NONCE_LENGTH + TAG_LENGTH));
                long offset = (NONCE_LENGTH + TAG_LENGTH) * region;
                return encryptedLength - offset;

            default:
                throw LOGGER.logExceptionAsError(new IllegalArgumentException("Unexpected protocol version"));
        }
    }
}
