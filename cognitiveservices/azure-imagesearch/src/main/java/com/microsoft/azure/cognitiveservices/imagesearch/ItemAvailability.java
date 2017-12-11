/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for ItemAvailability.
 */
public final class ItemAvailability extends ExpandableStringEnum<ItemAvailability> {
    /** Static value Discontinued for ItemAvailability. */
    public static final ItemAvailability DISCONTINUED = fromString("Discontinued");

    /** Static value InStock for ItemAvailability. */
    public static final ItemAvailability IN_STOCK = fromString("InStock");

    /** Static value InStoreOnly for ItemAvailability. */
    public static final ItemAvailability IN_STORE_ONLY = fromString("InStoreOnly");

    /** Static value LimitedAvailability for ItemAvailability. */
    public static final ItemAvailability LIMITED_AVAILABILITY = fromString("LimitedAvailability");

    /** Static value OnlineOnly for ItemAvailability. */
    public static final ItemAvailability ONLINE_ONLY = fromString("OnlineOnly");

    /** Static value OutOfStock for ItemAvailability. */
    public static final ItemAvailability OUT_OF_STOCK = fromString("OutOfStock");

    /** Static value PreOrder for ItemAvailability. */
    public static final ItemAvailability PRE_ORDER = fromString("PreOrder");

    /** Static value SoldOut for ItemAvailability. */
    public static final ItemAvailability SOLD_OUT = fromString("SoldOut");

    /**
     * Creates or finds a ItemAvailability from its string representation.
     * @param name a name to look for
     * @return the corresponding ItemAvailability
     */
    @JsonCreator
    public static ItemAvailability fromString(String name) {
        return fromString(name, ItemAvailability.class);
    }

    /**
     * @return known ItemAvailability values
     */
    public static Collection<ItemAvailability> values() {
        return values(ItemAvailability.class);
    }
}
