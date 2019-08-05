// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.JsonSerializable;
import com.azure.data.cosmos.internal.Utils.ValueHolder;
import com.azure.data.cosmos.internal.routing.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public final class CompositeContinuationToken extends JsonSerializable {
    private static final String TokenPropertyName = "token";
    private static final String RangePropertyName = "range";
    private static final Logger logger = LoggerFactory.getLogger(CompositeContinuationToken.class);

    public CompositeContinuationToken(String token, Range<String> range) {
        // token is allowed to be null
        if (range == null) {
            throw new IllegalArgumentException("range must not be null.");
        }

        this.setToken(token);
        this.setRange(range);
    }

    private CompositeContinuationToken(String serializedCompositeContinuationToken) {
        super(serializedCompositeContinuationToken);
    }

    public static boolean tryParse(String serializedCompositeContinuationToken,
            ValueHolder<CompositeContinuationToken> outCompositeContinuationToken) {
        boolean parsed;
        try {
            CompositeContinuationToken compositeContinuationToken = new CompositeContinuationToken(
                    serializedCompositeContinuationToken);
            compositeContinuationToken.getToken();

            Range<String> range = compositeContinuationToken.getRange();
            if (range == null) {
                throw new IllegalArgumentException("range must not be null.");
            }

            range.getMax();
            range.getMin();
            range.isEmpty();
            range.isMaxInclusive();
            range.isMinInclusive();
            range.isSingleValue();

            outCompositeContinuationToken.v = compositeContinuationToken;
            parsed = true;
        } catch (Exception ex) {
            logger.debug(
                    "Received exception {} when trying to parse: {}", 
                    ex.getMessage(), 
                    serializedCompositeContinuationToken);
            parsed = false;
            outCompositeContinuationToken.v = null;
        }

        return parsed;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return super.getString(TokenPropertyName);
    }

    /**
     * @return the range
     */
    public Range<String> getRange() {
        return new Range<String>(super.getString(RangePropertyName));
    }

    /**
     * @param token
     *            the token to set
     */
    private void setToken(String token) {
        BridgeInternal.setProperty(this, TokenPropertyName, token);
    }

    /**
     * @param range
     *            the range to set
     */
    private void setRange(Range<String> range) {
        /* TODO: Don't stringify the range */
        BridgeInternal.setProperty(this, RangePropertyName, range.toString());
    }
}
