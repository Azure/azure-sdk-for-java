// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

// The logic here is a minimal implementation borrowed from the implementation at
// https://github.com/microsoft/CorrelationVector-Java/blob/1012460386acb6a91b304d3e43daba6a07fffb58/src/main/java/com/microsoft/correlationvector/CorrelationVector.java.
// License is MIT: https://github.com/microsoft/CorrelationVector-Java/blob/1012460386acb6a91b304d3e43daba6a07fffb58/LICENSE

package com.azure.mixedreality.authentication;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

class CorrelationVector {
    private static final byte CV_BASE_LENGTH_V2 = 22;

    /**
     * Gets the CV base.
     *
     * @return A generated CV base.
     */
    public static String generateCvBase() {
        UUID uuid = UUID.randomUUID();
        return generateCvBaseFromUUID(uuid);
    }

    /**
     * Gets the CV base.
     *
     * @param uuid A UUID to seed the correlation vector.
     * @return A generated CV base.
     */
    public static String generateCvBaseFromUUID(UUID uuid) {
        final ByteBuffer uuidBytes = ByteBuffer.wrap(new byte[16]);
        uuidBytes.putLong(uuid.getMostSignificantBits());
        uuidBytes.putLong(uuid.getLeastSignificantBits());
        // Removes the base64 padding
        final String cvBase = Base64.getEncoder().encodeToString(uuidBytes.array());
        return cvBase.substring(0, CV_BASE_LENGTH_V2);
    }
}
