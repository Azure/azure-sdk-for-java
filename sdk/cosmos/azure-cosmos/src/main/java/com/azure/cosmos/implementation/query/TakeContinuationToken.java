// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public final class TakeContinuationToken extends JsonSerializable {
    private static final String LimitPropertyName = "limit";
    private static final String SourceTokenPropetryName = "sourceToken";
    private static final Logger logger = LoggerFactory.getLogger(TakeContinuationToken.class);

    public TakeContinuationToken(int takeCount, String sourceToken) {
        if (takeCount < 0) {
            throw new IllegalArgumentException("takeCount must be a non negative number.");
        }

        // sourceToken is allowed to be null.
        this.setTakeCount(takeCount);
        this.setSourceToken(sourceToken);
    }

    private TakeContinuationToken(String serializedTakeContinuationToken) {
        super(serializedTakeContinuationToken);
    }

    public static boolean tryParse(String serializedTakeContinuationToken,
            ValueHolder<TakeContinuationToken> outTakeContinuationToken) {
        boolean parsed;
        try {
            TakeContinuationToken takeContinuationToken = new TakeContinuationToken(serializedTakeContinuationToken);
            takeContinuationToken.getSourceToken();
            takeContinuationToken.getTakeCount();
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

    public int getTakeCount() {
        return super.getInt(LimitPropertyName);
    }

    public String getSourceToken() {
        return super.getString(SourceTokenPropetryName);
    }

    private void setTakeCount(int takeCount) {
        this.set(LimitPropertyName, takeCount);
    }

    private void setSourceToken(String sourceToken) {
        this.set(SourceTokenPropetryName, sourceToken);
    }

    @Override
    public String toJson() {
        return super.toJson();
    }
}
