// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.implementation;

import com.azure.search.documents.models.AutocompleteOptions;
import org.junit.jupiter.api.Test;

public class ConverterTest {
    @Test
    public void test() {
        AutocompleteOptions options = new AutocompleteOptions();
        options.setFilter("filter");
        System.out.println(AutocompleteOptionsConverter.convert(options).getFilter());
    }
}
