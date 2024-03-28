// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.common;

import com.azure.cosmos.CosmosItemSerializer;
import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;

class ChangeFeedStartFromBeginningImpl extends ChangeFeedStartFromInternal {
    public ChangeFeedStartFromBeginningImpl() {
        super();
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();

        synchronized(this) {
            this.set(
                Constants.Properties.CHANGE_FEED_START_FROM_TYPE,
                ChangeFeedStartFromTypes.BEGINNING,
                CosmosItemSerializer.DEFAULT_SERIALIZER);
        }
    }

    @Override
    public void populateRequest(RxDocumentServiceRequest request, ChangeFeedMode changeFeedMode) {
        // We don't need to set any headers to start from the beginning
    }

    @Override
    public boolean supportsFullFidelityRetention() {
        return false;
    }
}
