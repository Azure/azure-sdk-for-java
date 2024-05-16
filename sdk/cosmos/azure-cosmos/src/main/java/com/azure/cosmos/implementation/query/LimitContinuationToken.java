// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public final class LimitContinuationToken extends JsonSerializable {
    private static final String LIMIT_PROPERTY_NAME = "limit";
    private static final String SOURCE_TOKEN_PROPERTY_NAME = "sourceToken";
    private static final Logger logger = LoggerFactory.getLogger(LimitContinuationToken.class);

    public LimitContinuationToken(int limitCount, String sourceToken) {
        if (limitCount < 0) {
            throw new IllegalArgumentException("limitCount must be a non negative number.");
        }

        this.setLimitCount(limitCount);
        this.setSourceToken(sourceToken);
    }

    private LimitContinuationToken(String serializedTakeContinuationToken) {
        super(serializedTakeContinuationToken);
    }

    public static boolean tryParse(String serializedTakeContinuationToken, ValueHolder<LimitContinuationToken> outTakeContinuationToken) {
        boolean parsed;
        try {
            LimitContinuationToken takeContinuationToken = new LimitContinuationToken(serializedTakeContinuationToken);
            takeContinuationToken.getSourceToken();
            takeContinuationToken.getLimitCount();
            outTakeContinuationToken.v = takeContinuationToken;
            parsed = true;
        } catch (Exception ex) {
            logger.debug(
                "Received exception {} when trying to parse: {}",
                ex.getMessage(),
                serializedTakeContinuationToken);
            parsed = false;
            outTakeContinuationToken.v = null;
        }

        return parsed;
    }

    public int getLimitCount() {
        return super.getInt(LIMIT_PROPERTY_NAME);
    }

    public String getSourceToken() {
        return super.getString(SOURCE_TOKEN_PROPERTY_NAME);
    }

    private void setLimitCount(int limitCount) {
        this.set(LIMIT_PROPERTY_NAME, limitCount, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    private void setSourceToken(String sourceToken) {
        this.set(SOURCE_TOKEN_PROPERTY_NAME, sourceToken, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    @Override
    public String toJson() {
        return super.toJson();
    }
}
