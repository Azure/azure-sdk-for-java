// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.implementation.feedranges.FeedRangeInternal;
import com.azure.cosmos.util.Beta;

import java.io.IOException;

@Beta(value = Beta.SinceVersion.V4_9_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public interface FeedRange {
    /**
     * Creates a range from a previously obtained string representation.
     *
     * @param json A string representation of a feed range
     * @return A feed range
     */
    public static FeedRange fromJsonString(String json) {
        return FeedRangeInternal.fromJsonString(json);
    }

    public String toJsonString();
}