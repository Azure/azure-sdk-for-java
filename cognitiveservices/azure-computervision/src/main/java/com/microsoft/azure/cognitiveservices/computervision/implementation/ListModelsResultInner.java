/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.computervision.ModelDescription;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of the List Domain Models operation.
 */
public class ListModelsResultInner {
    /**
     * An array of supported models.
     */
    @JsonProperty(value = "models", access = JsonProperty.Access.WRITE_ONLY)
    private List<ModelDescription> modelsProperty;

    /**
     * Get the modelsProperty value.
     *
     * @return the modelsProperty value
     */
    public List<ModelDescription> modelsProperty() {
        return this.modelsProperty;
    }

}
