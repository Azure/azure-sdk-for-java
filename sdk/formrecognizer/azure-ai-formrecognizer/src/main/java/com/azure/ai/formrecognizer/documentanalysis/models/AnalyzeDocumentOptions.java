// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.models;

import com.azure.core.annotation.Fluent;

import java.util.Arrays;
import java.util.List;

/**
 * The configurable options to be passed when using analyze document API on Document Analysis client.
 */
@Fluent
public final class AnalyzeDocumentOptions {
    private List<String> pages;
    private String locale;
    private List<DocumentAnalysisFeature> documentAnalysisFeatures;

    /**
     * Get the custom page numbers for multipage documents(PDF/TIFF). Input the number of the
     * pages you want to get the recognized result for.
     * <p>For a range of pages, use a hyphen, ex - ["1-3"]. Separate each page or a page
     * range with a comma, ex - ["1-3", 4].</p>
     *
     * @return the list of custom page numbers for a multipage document.
     */
    public List<String> getPages() {
        return pages;
    }

    /**
     * Set the custom page numbers for multipage documents(PDF/TIFF). Input the number of the
     * pages you want to get the recognized result for.
     * <p>For a range of pages, use a hyphen, ex - ["1-3"]. Separate each page or a page
     * range with a comma, ex - ["1-3", 4].</p>
     *
     * @param pages the custom page numbers value to set.
     * @return the updated {@code AnalyzeDocumentOptions} value.
     */
    public AnalyzeDocumentOptions setPages(List<String> pages) {
        this.pages = pages;
        return this;
    }

    /**
     * Get the locale hint for text recognition and document analysis.
     * Value may contain only the language code (ex. \"en\", \"fr\") or BCP 47 language tag (ex. \"en-US\").
     *
     * @return the locale value.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Set the locale value.
     * Supported locales include: en-AU, en-CA, en-GB, en-IN, en-US.
     *
     * @param locale the locale value to set.
     * @return the updated {@code AnalyzeDocumentOptions} value.
     */
    public AnalyzeDocumentOptions setLocale(String locale) {
        this.locale = locale;
        return this;
    }

    /**
     * Get the list of optional analysis features.
     * @return List of optional analysis features.
     */
    public List<DocumentAnalysisFeature> getDocumentAnalysisFeatures() {
        return documentAnalysisFeatures;
    }

    /**
     * Set the list of optional analysis features.
     * @param documentAnalysisFeatures List of optional analysis features.
     * @return the updated {@code AnalyzeDocumentOptions} value.
     */
    public AnalyzeDocumentOptions setDocumentAnalysisFeatures(List<DocumentAnalysisFeature> documentAnalysisFeatures) {
        this.documentAnalysisFeatures = documentAnalysisFeatures;
        return this;
    }

    /**
     * Set optional analysis features.
     * @param documentAnalysisFeatures List of optional analysis features.
     * @return the updated {@code AnalyzeDocumentOptions} value.
     */
    public AnalyzeDocumentOptions setDocumentAnalysisFeatures(DocumentAnalysisFeature... documentAnalysisFeatures) {
        if (documentAnalysisFeatures != null) {
            this.documentAnalysisFeatures = Arrays.asList(documentAnalysisFeatures);
        }
        return this;
    }
}
