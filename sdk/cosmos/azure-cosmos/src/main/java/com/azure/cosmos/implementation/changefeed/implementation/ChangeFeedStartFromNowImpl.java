// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

class ChangeFeedStartFromNowImpl extends ChangeFeedStartFromInternal {
    public ChangeFeedStartFromNowImpl() {
        super();
    }

    @Override
    void accept(ChangeFeedStartFromVisitor visitor) {
        visitor.Visit(this);
    }
}