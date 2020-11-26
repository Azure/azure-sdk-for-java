// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;

abstract class ChangeFeedStartFromVisitor {
    public abstract void Visit(
        ChangeFeedStartFromNowImpl startFromNow,
        RxDocumentServiceRequest request);
    public abstract void Visit(
        ChangeFeedStartFromPointInTimeImpl startFromTime,
        RxDocumentServiceRequest request);
    public abstract void Visit(
        ChangeFeedStartFromEtagAndFeedRangeImpl startFromEtagAndFeedRange,
        RxDocumentServiceRequest request);
    public abstract void Visit(
        ChangeFeedStartFromBeginningImpl startFromBeginning,
        RxDocumentServiceRequest request);
}