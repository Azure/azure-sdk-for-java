// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.AutocompleteItem;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.AutocompleteItem} and
 * {@link AutocompleteItem}.
 */
public final class AutocompleteItemConverter {
    /**
     * Maps from {@link com.azure.search.documents.implementation.models.AutocompleteItem} to {@link AutocompleteItem}.
     */
    public static AutocompleteItem map(com.azure.search.documents.implementation.models.AutocompleteItem obj) {
        if (obj == null) {
            return null;
        }
        AutocompleteItem autocompleteItem = new AutocompleteItem();

        String text = obj.getText();
        PrivateFieldAccessHelper.set(autocompleteItem, "text", text);

        String queryPlusText = obj.getQueryPlusText();
        PrivateFieldAccessHelper.set(autocompleteItem, "queryPlusText", queryPlusText);
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

        String text = obj.getText();
        PrivateFieldAccessHelper.set(autocompleteItem, "text", text);

        String queryPlusText = obj.getQueryPlusText();
        PrivateFieldAccessHelper.set(autocompleteItem, "queryPlusText", queryPlusText);
        return autocompleteItem;
    }

    private AutocompleteItemConverter() {
    }
}
