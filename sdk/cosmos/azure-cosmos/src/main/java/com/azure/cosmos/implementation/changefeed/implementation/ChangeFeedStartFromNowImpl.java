// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;

import static com.azure.cosmos.BridgeInternal.setProperty;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChangeFeedStartFromNowImpl extends ChangeFeedStartFromInternal {
    public ChangeFeedStartFromNowImpl() {
        super();
    }

    @Override
    public void populatePropertyBag() {
        
            super.populatePropertyBag();

        synchronized(this) {
            setProperty(
                this,
                Constants.Properties.CHANGE_FEED_START_FROM_TYPE,
                ChangeFeedStartFromTypes.NOW);
        }
    }

    @Override
    public boolean supportsFullFidelityRetention() {
        return true;
    }

    @Override
    public void populateRequest(RxDocumentServiceRequest request) {
        checkNotNull(request, "Argument 'request' must not be null.");

        request.getHeaders().put(
            HttpConstants.HttpHeaders.IF_NONE_MATCH,
            HttpConstants.HeaderValues.IF_NONE_MATCH_ALL);
    }
}