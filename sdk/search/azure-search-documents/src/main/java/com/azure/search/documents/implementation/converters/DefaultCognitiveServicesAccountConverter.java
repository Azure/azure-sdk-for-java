// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.models.DefaultCognitiveServicesAccount;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.DefaultCognitiveServicesAccount} and
 * {@link DefaultCognitiveServicesAccount}.
 */
public final class DefaultCognitiveServicesAccountConverter {
    private static final ClientLogger LOGGER = new ClientLogger(DefaultCognitiveServicesAccountConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.DefaultCognitiveServicesAccount} to
     * {@link DefaultCognitiveServicesAccount}.
     */
    public static DefaultCognitiveServicesAccount map(com.azure.search.documents.implementation.models.DefaultCognitiveServicesAccount obj) {
        if (obj == null) {
            return null;
        }
        DefaultCognitiveServicesAccount defaultCognitiveServicesAccount = new DefaultCognitiveServicesAccount();

        String _description = obj.getDescription();
        defaultCognitiveServicesAccount.setDescription(_description);
        return defaultCognitiveServicesAccount;
    }

    /**
     * Maps from {@link DefaultCognitiveServicesAccount} to
     * {@link com.azure.search.documents.implementation.models.DefaultCognitiveServicesAccount}.
     */
    public static com.azure.search.documents.implementation.models.DefaultCognitiveServicesAccount map(DefaultCognitiveServicesAccount obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.DefaultCognitiveServicesAccount defaultCognitiveServicesAccount = new com.azure.search.documents.implementation.models.DefaultCognitiveServicesAccount();

        String _description = obj.getDescription();
        defaultCognitiveServicesAccount.setDescription(_description);
        return defaultCognitiveServicesAccount;
    }
}
