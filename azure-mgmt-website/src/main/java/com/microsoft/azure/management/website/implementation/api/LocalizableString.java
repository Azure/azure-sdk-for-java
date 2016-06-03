/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;


/**
 * LocalizableString object containing the name and a localized value.
 */
public class LocalizableString {
    /**
     * Non localized name.
     */
    private String value;

    /**
     * Localized name.
     */
    private String localizedValue;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the LocalizableString object itself.
     */
    public LocalizableString withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Get the localizedValue value.
     *
     * @return the localizedValue value
     */
    public String localizedValue() {
        return this.localizedValue;
    }

    /**
     * Set the localizedValue value.
     *
     * @param localizedValue the localizedValue value to set
     * @return the LocalizableString object itself.
     */
    public LocalizableString withLocalizedValue(String localizedValue) {
        this.localizedValue = localizedValue;
        return this;
    }

}
