// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

/**
 * Collection of useful URL escapers.
 */
final class UrlEscapers {

    private static final String UNRESERVED_SYMBOLS = "-._~";
    private static final String SUB_DELIMS = "!$&'()*+,;=";

    /** An escaper for escaping path parameters. */
    public static final PercentEscaper PATH_ESCAPER = new PercentEscaper(UNRESERVED_SYMBOLS + SUB_DELIMS + ":@", false);
    /** An escaper for escaping query parameters. */
    public static final PercentEscaper QUERY_ESCAPER = new PercentEscaper(UNRESERVED_SYMBOLS + "/?", false);
    /** An escaper for escaping form parameters. */
    public static final PercentEscaper FORM_ESCAPER = new PercentEscaper(UNRESERVED_SYMBOLS, true);

}
