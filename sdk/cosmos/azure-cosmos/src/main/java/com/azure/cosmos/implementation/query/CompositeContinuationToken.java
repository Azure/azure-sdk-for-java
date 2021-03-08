// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.query;

import com.azure.cosmos.implementation.routing.Range;
import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.implementation.JsonSerializable;
import com.azure.cosmos.implementation.Utils.ValueHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.azure.cosmos.BridgeInternal.setProperty;

/**
 * While this class is public, but it is not part of our published public APIs.
 * This is meant to be internally used only by our sdk.
 */
public final class CompositeContinuationToken extends JsonSerializable implements IPartitionedToken {
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

    public CompositeContinuationToken(ObjectNode node) {
        super(node);
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
        ObjectNode propertyBag = super.getPropertyBag();
        if (propertyBag.has(RangePropertyName) && propertyBag.hasNonNull(RangePropertyName)) {
            JsonNode rangeNode = propertyBag.get(RangePropertyName);

            // Initially we serialized the json by stringifying the range
            // So keeping the option to parse json with that old model here
            // but converting it to the cleaner format
            if (rangeNode.isTextual()) {
                Range<String> parsedRange = new Range<>(rangeNode.textValue());
                setProperty(this, RangePropertyName, parsedRange);
                return parsedRange;
            }

            return new Range<String>((ObjectNode)rangeNode);
        }

        return null;
    }

    /**
     * @param token
     *            the token to set
     */
    public void setToken(String token) {
        BridgeInternal.setProperty(this, TokenPropertyName, token);
    }

    /**
     * @param range
     *            the range to set
     */
    public void setRange(Range<String> range) {
        BridgeInternal.setProperty(this, RangePropertyName, range);
    }

    @Override
    public String toJson() {
        return super.toJson();
    }
}
