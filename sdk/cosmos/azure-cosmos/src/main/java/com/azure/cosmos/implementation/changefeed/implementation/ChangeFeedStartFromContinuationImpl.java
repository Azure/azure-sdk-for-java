// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.Strings;

class ChangeFeedStartFromContinuationImpl extends ChangeFeedStartFromInternal {
    private final String continuation;

    public ChangeFeedStartFromContinuationImpl(String continuation) {
        super();

        if (continuation == null) {
            throw new NullPointerException("continuation");
        }

        if (Strings.isNullOrWhiteSpace(continuation)) {
            throw new IllegalArgumentException(
                "Continuation token must not be empty.");
        }

        this.continuation = continuation;
    }

    public String getContinuation() {
        return this.continuation;
    }

    @Override
    void accept(ChangeFeedStartFromVisitor visitor) {
        visitor.Visit(this);
    }
}
