// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.credentials;

import com.azure.storage.common.Utility;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Code snippets for {@link SASTokenCredential}.
 */
public final class SASTokenCredentialJavaDocCodeSnippets {
    private final String preformattedSASToken = "sasToken";
    private final URL url = new URL("https://www.example.com?queryString");

    private SASTokenCredentialJavaDocCodeSnippets() throws MalformedURLException {
    }

    /**
     * Code sample for {@link SASTokenCredential#fromSASTokenString(String)}.
     */
    public void fromSASTokenString() {
        // BEGIN: com.azure.storage.common.credentials.SASTokenCredential.fromSASTokenString#String
        SASTokenCredential credential = SASTokenCredential.fromSASTokenString(preformattedSASToken);
        // END: com.azure.storage.common.credentials.SASTokenCredential.fromSASTokenString#String
    }

    /**
     * Code sample for {@link SASTokenCredential#fromQueryParameters(Map)}.
     */
    public void fromQueryParameters() {
        // BEGIN: com.azure.storage.common.credentials.SASTokenCredential.fromQueryParameters#Map
        SASTokenCredential credential = SASTokenCredential
            .fromQueryParameters(Utility.parseQueryString(url.getQuery()));
        // END: com.azure.storage.common.credentials.SASTokenCredential.fromQueryParameters#Map
    }
}
