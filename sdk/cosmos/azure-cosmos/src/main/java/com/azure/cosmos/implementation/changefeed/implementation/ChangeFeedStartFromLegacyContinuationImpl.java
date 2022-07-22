// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.azure.cosmos.BridgeInternal.setProperty;

class ChangeFeedStartFromLegacyContinuationImpl extends ChangeFeedStartFromInternal {
    private final Logger logger = LoggerFactory.getLogger(ChangeFeedStartFromLegacyContinuationImpl.class);

    public ChangeFeedStartFromLegacyContinuationImpl() {
        super();
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();

        synchronized (this) {
            setProperty(
                this,
                Constants.Properties.CHANGE_FEED_START_FROM_TYPE,
                ChangeFeedStartFromTypes.LEGACY_CHECKPOINT);
        }
    }

    @Override
    public void populateRequest(RxDocumentServiceRequest request) {
        logger.info("Populate request called in file with req headers {}", request.getHeaders());
        // We don't need to set any headers to start from the beginning
    }

    @Override
    public boolean supportsFullFidelityRetention() {
        return false;
    }
}
