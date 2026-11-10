package com.azure.tools.apiview.processor.model.traits;

import com.azure.tools.apiview.processor.model.ReviewLine;

import java.util.List;

public interface Parent {
    ReviewLine addChildLine();
    ReviewLine addChildLine(final String lineId);
    ReviewLine addChildLine(ReviewLine child);

    List<ReviewLine> getChildren();
}
