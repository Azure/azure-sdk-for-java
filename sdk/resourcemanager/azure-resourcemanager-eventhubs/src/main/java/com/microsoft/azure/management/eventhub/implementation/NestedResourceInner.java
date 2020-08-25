/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.eventhub.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.azure.management.apigeneration.Beta;

/**
 * A type contains subset of ARM envelop properties {@link com.microsoft.azure.Resource} id, name and type.
 */
@Beta(Beta.SinceVersion.V1_7_0)
public class NestedResourceInner {
    @JsonProperty(
            access = JsonProperty.Access.WRITE_ONLY
    )
    private String id;
    @JsonProperty(
            access = JsonProperty.Access.WRITE_ONLY
    )
    private String name;
    @JsonProperty(
            access = JsonProperty.Access.WRITE_ONLY
    )
    private String type;

    /**
     * @return ARM resource id
     */
    public String id() {
        return this.id;
    }
    /**
     * @return the resource name
     */
    public String name() {
        return this.name;
    }
    /**
     * @return the type name
     */
    public String type() {
        return this.type;
    }
}
