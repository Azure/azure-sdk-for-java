// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Fluent;

import java.util.List;

/**
 * Options that may be passed when using analyze document API on Document Analysis client.
 */
@Fluent
public final class AnalyzeDocumentOptions {
    private List<String> pages;
    private String locale;

    /**
     * Get the custom page numbers for multi-page documents(PDF/TIFF). Input the number of the
     * pages you want to get the recognized result for.
     * <p>For a range of pages, use a hyphen, ex - ["1-3"]. Separate each page or a page
     * range with a comma, ex - ["1-3", 4].</p>
     *
     * @return the list of custom page numbers for a multi page document.
     */
    public List<String> getPages() {
        return pages;
    }

    /**
     * Set the custom page numbers for multi-page documents(PDF/TIFF). Input the number of the
     * pages you want to get the recognized result for.
     * <p>For a range of pages, use a hyphen, ex - ["1-3"]. Separate each page or a page
     * range with a comma, ex - ["1-3", 4].</p>
     *
     * @param pages the custom page numbers value to set.
     * @return the updated {@code RecognizeContentOptions} value.
     */
    public AnalyzeDocumentOptions setPages(List<String> pages) {
        this.pages = pages;
        return this;
    }

    /**
     * Get the locale value.
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
     * @return the locale value.
     */
    public AnalyzeDocumentOptions setLocale(String locale) {
        this.locale = locale;
        return this;
    }
}
