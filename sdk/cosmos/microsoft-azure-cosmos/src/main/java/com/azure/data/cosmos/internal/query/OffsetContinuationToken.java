// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.query;

import com.azure.data.cosmos.BridgeInternal;
import com.azure.data.cosmos.JsonSerializable;
import com.azure.data.cosmos.internal.Utils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OffsetContinuationToken extends JsonSerializable {
    private static final String TOKEN_PROPERTY_NAME = "sourceToken";
    private static final String OFFSET_PROPERTY_NAME = "offset";
    private static final Logger logger = LoggerFactory.getLogger(CompositeContinuationToken.class);

    public OffsetContinuationToken(int offset, String sourceToken) {

        if (offset < 0) {
            throw new IllegalArgumentException("offset should be non negative");
        }

        this.setOffset(offset);
        this.setSourceToken(sourceToken);
    }

    public OffsetContinuationToken(String serializedCompositeToken) {
        super(serializedCompositeToken);
        this.getOffset();
        this.getSourceToken();
    }

    public static boolean tryParse(String serializedOffsetContinuationToken,
                                   Utils.ValueHolder<OffsetContinuationToken> outOffsetContinuationToken) {
        if (StringUtils.isEmpty(serializedOffsetContinuationToken)) {
            return false;
        }

        boolean parsed;
        try {
            outOffsetContinuationToken.v = new OffsetContinuationToken(serializedOffsetContinuationToken);
            parsed = true;
        } catch (Exception ex) {
            logger.debug("Received exception {} when trying to parse: {}",
                ex.getMessage(),
                serializedOffsetContinuationToken);
            parsed = false;
            outOffsetContinuationToken.v = null;
        }

        return parsed;
    }

    public String getSourceToken() {
        return super.getString(TOKEN_PROPERTY_NAME);
    }

    private void setSourceToken(String sourceToken) {
        BridgeInternal.setProperty(this, TOKEN_PROPERTY_NAME, sourceToken);
    }

    public int getOffset() {
        return super.getInt(OFFSET_PROPERTY_NAME);
    }

    private void setOffset(int offset) {
        BridgeInternal.setProperty(this, OFFSET_PROPERTY_NAME, offset);
    }
}

