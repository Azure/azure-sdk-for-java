// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.Strings;
import com.azure.cosmos.implementation.Utils;
import com.azure.cosmos.implementation.routing.HexConvert;
import com.azure.cosmos.implementation.routing.UInt128;
import com.azure.cosmos.implementation.JsonSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class DistinctContinuationToken extends JsonSerializable {

    private static final String LAST_HASH_PROPERTY_NAME = "lastHash";
    private static final String SOURCE_TOKEN_PROPERTY_NAME = "sourceToken";

    private static final Logger logger = LoggerFactory.getLogger(TakeContinuationToken.class);

    public DistinctContinuationToken(UInt128 lastHash, String sourceToken) {
        this.setLastHash(lastHash);
        this.setSourceToken(sourceToken);
    }

    private DistinctContinuationToken(String serializedDistinctContinuationToken) {
        super(serializedDistinctContinuationToken);
    }

    public static boolean tryParse(
        String serializedDistinctContinuationToken,
        Utils.ValueHolder<DistinctContinuationToken> outDistinctContinuationToken) {

        boolean parsed;
        try {
            DistinctContinuationToken distinctContinuationToken =
                new DistinctContinuationToken(serializedDistinctContinuationToken);
            distinctContinuationToken.getSourceToken();
            distinctContinuationToken.getLastHash();
            outDistinctContinuationToken.v = distinctContinuationToken;
            parsed = true;
        } catch (Exception ex) {
            logger.warn(
                "Received exception when trying to parse: {}",
                serializedDistinctContinuationToken,
                ex);
            parsed = false;
            outDistinctContinuationToken.v = null;
        }

        return parsed;
    }

    String getSourceToken() {
        return super.getString(SOURCE_TOKEN_PROPERTY_NAME);
    }

    /**
     * Setter for property 'sourceToken'.
     *
     * @param sourceToken Value to set for property 'sourceToken'.
     */
    public void setSourceToken(String sourceToken) {
        BridgeInternal.setProperty(this, SOURCE_TOKEN_PROPERTY_NAME, sourceToken);
    }

    UInt128 getLastHash() {
        ByteBuffer byteBuffer = null;
        String hexString = super.getString(LAST_HASH_PROPERTY_NAME);
        if (!Strings.isNullOrEmpty(hexString)) {
            byteBuffer = ByteBuffer.wrap(HexConvert.hexToByteBuffer(hexString));
        }

        if (byteBuffer != null && byteBuffer.capacity() > 0) {
            return new UInt128(byteBuffer);
        }
        return null;
    }

    /**
     * Setter for property 'lastHash'.
     *
     * @param lastHash Value to set for property 'lastHash'.
     */
    public void setLastHash(UInt128 lastHash) {
        if (lastHash != null) {
            BridgeInternal.setProperty(
                this,
                LAST_HASH_PROPERTY_NAME,
                HexConvert.bytesToHex(lastHash.toByteBuffer(), true));
        } else {
            this.set(LAST_HASH_PROPERTY_NAME, null);
        }
    }

}
