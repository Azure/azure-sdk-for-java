// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.credentials;

import com.azure.core.implementation.util.ImplUtils;
import com.azure.storage.blob.SASQueryParameters;

/**
 * Holds a SAS token used for authenticating requests.
 */
public final class SASTokenCredential {
    private final String sasToken;

    /**
     * Creates a SAS token credential from the passed SAS token.
     *
     * @param sasToken SAS token used to authenticate requests with the service.
     */
    private SASTokenCredential(String sasToken) {
        this.sasToken = sasToken;
    }

    /**
     * @return the SAS token
     */
    public String sasToken() {
        return sasToken;
    }

    /**
     * Creates a SAS token credential from the passed SAS token.
     *
     * @param sasToken SAS token
     * @return a SAS token credential if {@code sasToken} is not {@code null} or empty, otherwise null.
     */
    public static SASTokenCredential fromSASTokenString(String sasToken) {
        if (ImplUtils.isNullOrEmpty(sasToken)) {
            return null;
        }

        return new SASTokenCredential(sasToken);
    }

    /**
     * Creates a SAS token credential from the passed {@link SASQueryParameters}.
     *
     * @param queryParameters SAS token query parameters object
     * @return a SAS token credential if {@code queryParameters} is not {@code null} and has
     * {@link SASQueryParameters#signature() signature} set, otherwise returns {@code null}.
     */
    public static SASTokenCredential fromQueryParameters(SASQueryParameters queryParameters) {
        if (queryParameters == null || ImplUtils.isNullOrEmpty(queryParameters.signature())) {
            return null;
        }

        return new SASTokenCredential(queryParameters.encode());
    }
}
