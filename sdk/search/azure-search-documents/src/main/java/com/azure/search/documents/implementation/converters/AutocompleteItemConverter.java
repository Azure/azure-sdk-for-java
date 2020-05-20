// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.AutocompleteItem;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.AutocompleteItem} and
 * {@link AutocompleteItem}.
 */
public final class AutocompleteItemConverter {
    private static final ClientLogger LOGGER = new ClientLogger(AutocompleteItemConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.AutocompleteItem} to {@link AutocompleteItem}.
     */
    public static AutocompleteItem map(com.azure.search.documents.implementation.models.AutocompleteItem obj) {
        if (obj == null) {
            return null;
        }
        AutocompleteItem autocompleteItem = new AutocompleteItem();

        String _text = obj.getText();
        PrivateFieldAccessHelper.set(autocompleteItem, "text", _text);

        String _queryPlusText = obj.getQueryPlusText();
        PrivateFieldAccessHelper.set(autocompleteItem, "queryPlusText", _queryPlusText);
        return autocompleteItem;
    }

    /**
     * Maps from {@link AutocompleteItem} to {@link com.azure.search.documents.implementation.models.AutocompleteItem}.
     */
    public static com.azure.search.documents.implementation.models.AutocompleteItem map(AutocompleteItem obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.AutocompleteItem autocompleteItem =
            new com.azure.search.documents.implementation.models.AutocompleteItem();

        String _text = obj.getText();
        PrivateFieldAccessHelper.set(autocompleteItem, "text", _text);

        String _queryPlusText = obj.getQueryPlusText();
        PrivateFieldAccessHelper.set(autocompleteItem, "queryPlusText", _queryPlusText);
        return autocompleteItem;
    }
}
