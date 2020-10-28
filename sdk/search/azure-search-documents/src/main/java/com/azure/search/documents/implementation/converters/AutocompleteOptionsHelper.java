// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation.converters;

import com.azure.search.documents.models.AutocompleteOptions;
import java.util.List;

/**
 * The helper class to set the non-public properties of an {@link AutocompleteOptions} instance.
 */
public final class AutocompleteOptionsHelper {
    private static AutocompleteOptionsAccessor accessor;

    private AutocompleteOptionsHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link AutocompleteOptions} instance.
     */
    public interface AutocompleteOptionsAccessor {
        void setSearchFields(AutocompleteOptions autocompleteOptions, List<String> searchFields);
    }

    /**
     * The method called from {@link AutocompleteOptions} to set it's accessor.
     *
     * @param optionsAccessor The accessor.
     */
    public static void setAccessor(final AutocompleteOptionsAccessor optionsAccessor) {
        accessor = optionsAccessor;
    }

    static void setSearchFields(AutocompleteOptions autocompleteOptions, List<String> searchFields) {
        accessor.setSearchFields(autocompleteOptions, searchFields);
    }
}
