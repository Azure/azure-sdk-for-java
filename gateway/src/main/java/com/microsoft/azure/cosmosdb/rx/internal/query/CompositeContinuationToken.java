/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.azure.cosmosdb.rx.internal.query;

import com.microsoft.azure.cosmosdb.JsonSerializable;
import com.microsoft.azure.cosmosdb.internal.routing.Range;
import com.microsoft.azure.cosmosdb.rx.internal.Utils.ValueHolder;

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
        super.set(TokenPropertyName, token);
    }

    /**
     * @param range
     *            the range to set
     */
    private void setRange(Range<String> range) {
        /* TODO: Don't stringify the range */
        super.set(RangePropertyName, range.toString());
    }
}
