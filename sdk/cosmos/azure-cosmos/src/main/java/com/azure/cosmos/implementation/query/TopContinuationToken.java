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
public final class TopContinuationToken extends JsonSerializable {
    private static final String TOP_PROPERTY_NAME = "top";
    private static final String SOURCE_TOKEN_PROPERTY_NAME = "sourceToken";
    private static final Logger logger = LoggerFactory.getLogger(TopContinuationToken.class);

    public TopContinuationToken(int topCount, String sourceToken) {
        if (topCount < 0) {
            throw new IllegalArgumentException("takeCount must be a non negative number.");
        }

        this.setTopCount(topCount);
        this.setSourceToken(sourceToken);
    }

    private TopContinuationToken(String serializedTakeContinuationToken) {
        super(serializedTakeContinuationToken);
    }

    public static boolean tryParse(String serializedTakeContinuationToken, ValueHolder<TopContinuationToken> outTakeContinuationToken) {
        boolean parsed;
        try {
            TopContinuationToken topContinuationToken = new TopContinuationToken(serializedTakeContinuationToken);
            topContinuationToken.getSourceToken();
            topContinuationToken.getTopCount();
            outTakeContinuationToken.v = topContinuationToken;
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

    public int getTopCount() {
        return super.getInt(TOP_PROPERTY_NAME);
    }

    public String getSourceToken() {
        return super.getString(SOURCE_TOKEN_PROPERTY_NAME);
    }

    private void setTopCount(int topCount) {
        this.set(TOP_PROPERTY_NAME, topCount, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    private void setSourceToken(String sourceToken) {
        this.set(SOURCE_TOKEN_PROPERTY_NAME, sourceToken, CosmosItemSerializer.DEFAULT_SERIALIZER);
    }

    @Override
    public String toJson() {
        return super.toJson();
    }
}
