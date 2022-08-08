// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

/**
 * Cloud audiences available for Search.
 */
public final class SearchAudience {
    private static final ClientLogger LOGGER = new ClientLogger(SearchAudience.class);

    private final String audience;

    private SearchAudience(String audience) {
        if (CoreUtils.isNullOrEmpty(audience)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'audience' cannot be null or empty."));
        }

        this.audience = audience;
    }

    /**
     * The {@link SearchAudience} for Azure China cloud.
     */
    public static final SearchAudience AZURE_CHINA = new SearchAudience("https://search.azure.cn");

    /**
     * The {@link SearchAudience} for Azure US Government cloud.
     */
    public static final SearchAudience AZURE_GOVERNMENT = new SearchAudience("https://search.azure.us");

    /**
     * The {@link SearchAudience} for the Azure Public cloud.
     */
    public static final SearchAudience AZURE_PUBLIC = new SearchAudience("https://search.azure.com");

    /**
     * Gets the Azure Active Directory (AAD) audience to use when forming authorization scopes.
     * <p>
     * This value corresponds to a URL that identifies the Azure cloud where the resource is located.
     *
     * @return The Azure Active Directory (AAD) audience to use when forming authorization scopes.
     */
    public String getAudience() {
        return audience;
    }
}
