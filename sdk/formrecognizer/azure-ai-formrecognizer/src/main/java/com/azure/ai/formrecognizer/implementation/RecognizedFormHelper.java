// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.ai.formrecognizer.models.RecognizedForm;

/**
 * The helper class to set the non-public properties of an {@link RecognizedForm} instance.
 */
public final class RecognizedFormHelper {
    private static RecognizedFormAccessor accessor;

    private RecognizedFormHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link RecognizedForm} instance.
     */
    public interface RecognizedFormAccessor {
        void setFormTypeConfidence(RecognizedForm form, Float formTypeConfidence);
        void setModelId(RecognizedForm form, String modelId);
    }

    /**
     * The method called from {@link RecognizedForm} to set it's accessor.
     *
     * @param formAccessor The accessor.
     */
    public static void setAccessor(final RecognizedFormAccessor formAccessor) {
        accessor = formAccessor;
    }

    public static void setFormTypeConfidence(RecognizedForm form, Float formTypeConfidence) {
        accessor.setFormTypeConfidence(form, formTypeConfidence);
    }

    public static void setModelId(RecognizedForm form, String modelId) {
        accessor.setModelId(form, modelId);
    }
}
