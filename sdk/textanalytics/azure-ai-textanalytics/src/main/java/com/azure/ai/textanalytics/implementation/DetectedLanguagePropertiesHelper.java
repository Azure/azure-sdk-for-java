// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.implementation;

import com.azure.ai.textanalytics.models.DetectedLanguage;
import com.azure.ai.textanalytics.models.ScriptKind;

/**
 * The helper class to set the non-public properties of an {@link DetectedLanguage} instance.
 */
public final class DetectedLanguagePropertiesHelper {
    private static DetectedLanguageAccessor accessor;

    private DetectedLanguagePropertiesHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link DetectedLanguage}
     * instance.
     */
    public interface DetectedLanguageAccessor {
        void setScript(DetectedLanguage detectedLanguage, ScriptKind script);
    }

    /**
     * The method called from {@link DetectedLanguage} to set it's accessor.
     *
     * @param detectedLanguageAccessor The accessor.
     */
    public static void setAccessor(
        final DetectedLanguageAccessor detectedLanguageAccessor) {
        accessor = detectedLanguageAccessor;
    }

    public static void setScript(DetectedLanguage detectedLanguage, ScriptKind script) {
        accessor.setScript(detectedLanguage, script);
    }
}
