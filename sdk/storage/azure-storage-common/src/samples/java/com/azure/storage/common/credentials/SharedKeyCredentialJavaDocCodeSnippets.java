// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.credentials;

/**
 * Code snippets for {@link SharedKeyCredential}.
 */
public final class SharedKeyCredentialJavaDocCodeSnippets {
    private final String connectionString = "AccountName=accountname;AccountKey=accountkey;additionalproperties";
    /**
     * Code snippets for {@link SharedKeyCredential#fromConnectionString(String)}.
     */
    public void fromConnectionString() {
        // BEGIN: com.azure.storage.common.credentials.SharedKeyCredential.fromConnectionString#String
        SharedKeyCredential credential = SharedKeyCredential.fromConnectionString(connectionString);
        // END: com.azure.storage.common.credentials.SharedKeyCredential.fromConnectionString#String
    }
}
