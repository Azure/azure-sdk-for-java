// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.Constants;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;

import static com.azure.cosmos.BridgeInternal.setProperty;

class ChangeFeedStartFromBeginningImpl extends ChangeFeedStartFromInternal {
    public ChangeFeedStartFromBeginningImpl() {
        super();
    }

    @Override
    void accept(ChangeFeedStartFromVisitor visitor, RxDocumentServiceRequest request) {
        visitor.visit(this, request);
    }

    @Override
    public void populatePropertyBag() {
        super.populatePropertyBag();

        setProperty(
            this,
            Constants.Properties.CHANGE_FEED_START_FROM_TYPE,
            ChangeFeedStartFromTypes.BEGINNING);
    }
}