// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.implementation;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;

abstract class ChangeFeedStartFromVisitor {
    public abstract void visit(
        ChangeFeedStartFromNowImpl startFromNow,
        RxDocumentServiceRequest request);

    public abstract void visit(
        ChangeFeedStartFromPointInTimeImpl startFromTime,
        RxDocumentServiceRequest request);

    public abstract void visit(
        ChangeFeedStartFromETagAndFeedRangeImpl startFromETagAndFeedRange,
        RxDocumentServiceRequest request);

    public abstract void visit(
        ChangeFeedStartFromBeginningImpl startFromBeginning,
        RxDocumentServiceRequest request);
}