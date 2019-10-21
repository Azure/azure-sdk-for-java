// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.credentials;

import com.azure.storage.common.implementation.credentials.SasTokenCredential;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Code snippets for {@link SasTokenCredential}.
 */
public final class SASTokenCredentialJavaDocCodeSnippets {
    private final URL url = new URL("https://www.example.com?queryString");

    /**
     * @throws MalformedURLException ignored
     */
    public SASTokenCredentialJavaDocCodeSnippets() throws MalformedURLException {
    }

    /**
     * Code sample for {@link SasTokenCredential#fromSasTokenString(String)}.
     */
    public void fromSASTokenString() {
        String preformattedSASToken = "sasToken";
        // BEGIN: com.azure.storage.common.credentials.SasTokenCredential.fromSasTokenString#String
        SasTokenCredential credential = SasTokenCredential.fromSasTokenString(preformattedSASToken);
        // END: com.azure.storage.common.credentials.SasTokenCredential.fromSasTokenString#String
    }

    /**
     * Code sample for {@link SasTokenCredential#fromQueryParameters(Map)}.
     */
    public void fromQueryParameters() {
        // BEGIN: com.azure.storage.common.credentials.SasTokenCredential.fromQueryParameters#Map
        Map<String, String> queryMap = new HashMap<String, String>() {{
                put("queryPara1", "queryValue1");
                put("queryPara2", "queryValue2");
            }};
        SasTokenCredential credential = SasTokenCredential
            .fromQueryParameters(queryMap);
        // END: com.azure.storage.common.credentials.SasTokenCredential.fromQueryParameters#Map
    }
}
