// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.implementation.Option;

import java.util.NoSuchElementException;

/**
 * Code snippets for {@link Option}
 */
public class OptionJavaDocCodeSnippets {
    /**
     * Code snippets for using {@link Option}.
     */
    public void optionUsage() {
        // BEGIN: com.azure.core.util.Option
        // An Option with non-null-value.
        Option<String> skuOption = Option.of("basic");
        if (skuOption.isInitialized()) {
            // Option.isInitialized() returns true because option is initialized with a non-null value.
            System.out.println(skuOption.getValue()); // print: "basic"
        }

        // An Option with null-value.
        Option<String> descriptionOption = Option.of(null);
        if (descriptionOption.isInitialized()) {
            // Option.isInitialized() returns true because option is initialized with an explicit null-value.
            System.out.println(skuOption.getValue()); // print: null
        }

        // An Option with no-value.
        Option<String> uninitializedOption = Option.uninitialized();
        if (!uninitializedOption.isInitialized()) {
            // Option.isInitialized() returns false because option is uninitialized.
            System.out.println("not initialized");
        }

        // Attempting to access the value when an option has no-value will throw 'NoSuchElementException'
        try {
            uninitializedOption.getValue();
        } catch (NoSuchElementException exception) {
            System.out.println(exception.getMessage()); // print: 'No value initialized'
        }
        // END: com.azure.core.util.Option
    }
}
