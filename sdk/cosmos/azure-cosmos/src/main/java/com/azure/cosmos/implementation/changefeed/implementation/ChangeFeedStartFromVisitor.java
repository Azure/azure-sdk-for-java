// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

abstract class ChangeFeedStartFromVisitor {
    public abstract void Visit(ChangeFeedStartFromNowImpl startFromNow);
    public abstract void Visit(ChangeFeedStartFromPointInTimeImpl startFromTime);
    public abstract void Visit(ChangeFeedStartFromContinuationImpl startFromContinuation);
    public abstract void Visit(ChangeFeedStartFromEtagAndFeedRangeImpl startFromEtagAndFeedRange);
    public abstract void Visit(ChangeFeedStartFromBeginningImpl startFromBeginning);
}