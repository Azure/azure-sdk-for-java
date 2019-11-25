// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.util.IterableStream;

import java.util.List;

/**
 * The DetectedLanguageResult model.
 */
@Fluent
public final class DetectedLanguageResult extends DocumentResult {
    private DetectedLanguage primaryLanguage;
    private IterableStream<DetectedLanguage> items;

    public DetectedLanguage getPrimaryLanguage() {
        return primaryLanguage;
    }

    public IterableStream<DetectedLanguage> getItems() {
        return items;
    }
}
