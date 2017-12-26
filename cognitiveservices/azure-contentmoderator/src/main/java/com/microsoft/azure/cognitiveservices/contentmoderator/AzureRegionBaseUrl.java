/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.microsoft.rest.ExpandableStringEnum;

/**
 * Defines values for AzureRegionBaseUrl.
 */
public final class AzureRegionBaseUrl extends ExpandableStringEnum<AzureRegionBaseUrl> {
    /** Static value westus.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl WESTUSAPICOGNITIVEMICROSOFTCOM = fromString("westus.api.cognitive.microsoft.com");

    /** Static value westus2.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl WESTUS2APICOGNITIVEMICROSOFTCOM = fromString("westus2.api.cognitive.microsoft.com");

    /** Static value eastus.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl EASTUSAPICOGNITIVEMICROSOFTCOM = fromString("eastus.api.cognitive.microsoft.com");

    /** Static value eastus2.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl EASTUS2APICOGNITIVEMICROSOFTCOM = fromString("eastus2.api.cognitive.microsoft.com");

    /** Static value westcentralus.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl WESTCENTRALUSAPICOGNITIVEMICROSOFTCOM = fromString("westcentralus.api.cognitive.microsoft.com");

    /** Static value southcentralus.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl SOUTHCENTRALUSAPICOGNITIVEMICROSOFTCOM = fromString("southcentralus.api.cognitive.microsoft.com");

    /** Static value westeurope.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl WESTEUROPEAPICOGNITIVEMICROSOFTCOM = fromString("westeurope.api.cognitive.microsoft.com");

    /** Static value northeurope.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl NORTHEUROPEAPICOGNITIVEMICROSOFTCOM = fromString("northeurope.api.cognitive.microsoft.com");

    /** Static value southeastasia.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl SOUTHEASTASIAAPICOGNITIVEMICROSOFTCOM = fromString("southeastasia.api.cognitive.microsoft.com");

    /** Static value eastasia.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl EASTASIAAPICOGNITIVEMICROSOFTCOM = fromString("eastasia.api.cognitive.microsoft.com");

    /** Static value australiaeast.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl AUSTRALIAEASTAPICOGNITIVEMICROSOFTCOM = fromString("australiaeast.api.cognitive.microsoft.com");

    /** Static value brazilsouth.api.cognitive.microsoft.com for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl BRAZILSOUTHAPICOGNITIVEMICROSOFTCOM = fromString("brazilsouth.api.cognitive.microsoft.com");

    /** Static value contentmoderatortest.azure-api.net for AzureRegionBaseUrl. */
    public static final AzureRegionBaseUrl CONTENTMODERATORTESTAZURE_APINET = fromString("contentmoderatortest.azure-api.net");

    /**
     * Creates or finds a AzureRegionBaseUrl from its string representation.
     * @param name a name to look for
     * @return the corresponding AzureRegionBaseUrl
     */
    @JsonCreator
    public static AzureRegionBaseUrl fromString(String name) {
        return fromString(name, AzureRegionBaseUrl.class);
    }

    /**
     * @return known AzureRegionBaseUrl values
     */
    public static Collection<AzureRegionBaseUrl> values() {
        return values(AzureRegionBaseUrl.class);
    }
}
