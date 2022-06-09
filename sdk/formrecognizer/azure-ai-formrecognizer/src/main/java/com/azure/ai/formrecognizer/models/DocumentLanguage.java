// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.ai.formrecognizer.implementation.util.DocumentLanguageHelper;

import java.util.List;

/** An object representing the detected language for a given text span. */
public final class DocumentLanguage {
    /*
     * Detected language.  Value may an ISO 639-1 language code (ex. "en",
     * "fr") or BCP 47 language tag (ex. "zh-Hans").
     */
    private String locale;

    /*
     * Location of the text elements in the concatenated content the language
     * applies to.
     */
    private List<DocumentSpan> spans;

    /*
     * Confidence of correctly identifying the language.
     */
    private float confidence;

    /**
     * Get the Detected language code. Value may an ISO 639-1 language code (ex. "en", "fr") or BCP 47
     * language tag (ex. "zh-Hans").
     *
     * @return the code value.
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Set the Detected language code. Value may an ISO 639-1 language code (ex. "en", "fr") or BCP 47
     * language tag (ex. "zh-Hans").
     *
     * @param locale the code value to set.
     */
    void setLocale(String locale) {
        this.locale = locale;
    }

    /**
     * Get the spans property: Location of the text elements in the concatenated content the language applies to.
     *
     * @return the spans value.
     */
    public List<DocumentSpan> getSpans() {
        return this.spans;
    }

    /**
     * Set the spans property: Location of the text elements in the concatenated content the language applies to.
     *
     * @param spans the spans value to set.
     */
    void setSpans(List<DocumentSpan> spans) {
        this.spans = spans;
    }

    /**
     * Get the confidence property: Confidence of correctly identifying the language.
     *
     * @return the confidence value.
     */
    public float getConfidence() {
        return this.confidence;
    }

    /**
     * Set the confidence property: Confidence of correctly identifying the language.
     *
     * @param confidence the confidence value to set.
     */
    void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    static {
        DocumentLanguageHelper.setAccessor(new DocumentLanguageHelper.DocumentLanguageAccessor() {
            @Override
            public void setLocale(DocumentLanguage documentLanguage, String locale) {
                documentLanguage.setLocale(locale);
            }

            @Override
            public void setSpans(DocumentLanguage documentLanguage, List<DocumentSpan> spans) {
                documentLanguage.setSpans(spans);
            }

            @Override
            public void setConfidence(DocumentLanguage documentLanguage, Float confidence) {
                documentLanguage.setConfidence(confidence);
            }
        });
    }
}
