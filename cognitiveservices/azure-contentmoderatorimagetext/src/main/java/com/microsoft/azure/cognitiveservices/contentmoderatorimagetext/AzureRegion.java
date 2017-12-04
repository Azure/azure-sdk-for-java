/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for AzureRegion.
 */
public final class AzureRegion extends ExpandableStringEnum<AzureRegion> {
    /** Static value westus for AzureRegion. */
    public static final AzureRegion WESTUS = fromString("westus");

    /** Static value westeurope for AzureRegion. */
    public static final AzureRegion WESTEUROPE = fromString("westeurope");

    /** Static value southeastasia for AzureRegion. */
    public static final AzureRegion SOUTHEASTASIA = fromString("southeastasia");

    /** Static value eastus2 for AzureRegion. */
    public static final AzureRegion EASTUS2 = fromString("eastus2");

    /** Static value westcentralus for AzureRegion. */
    public static final AzureRegion WESTCENTRALUS = fromString("westcentralus");

    /**
     * Creates or finds a AzureRegion from its string representation.
     * @param name a name to look for
     * @return the corresponding AzureRegion
     */
    @JsonCreator
    public static AzureRegion fromString(String name) {
        return fromString(name, AzureRegion.class);
    }

    /**
     * @return known AzureRegion values
     */
    public static Collection<AzureRegion> values() {
        return values(AzureRegion.class);
    }
}
