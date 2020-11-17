// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

class ChangeFeedStartFromBeginningImpl extends ChangeFeedStartFromInternal {
    public ChangeFeedStartFromBeginningImpl() {
        super();
    }

    @Override
    void accept(ChangeFeedStartFromVisitor visitor) {
        visitor.Visit(this);
    }
}