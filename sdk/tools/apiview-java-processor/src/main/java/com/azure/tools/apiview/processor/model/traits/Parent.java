// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model.traits;

import com.azure.tools.apiview.processor.model.ReviewLine;

import java.util.List;

public interface Parent {
    ReviewLine addChildLine();

    ReviewLine addChildLine(final String lineId);

    ReviewLine addChildLine(ReviewLine child);

    List<ReviewLine> getChildren();
}
