// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.changefeed.fullfidelity;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *  An optional helper class for serializing Full Fidelity Change Feed responses. Users are welcome to create their own custom helper class.
 */
public class ChangeFeedItemChanges {
    @JsonProperty(value = "current", access = JsonProperty.Access.WRITE_ONLY)
    private final Object current;
    @JsonProperty(value = "metadata", access = JsonProperty.Access.WRITE_ONLY)
    private final ChangeFeedMetadata changeFeedMetadata;
    @JsonProperty(value = "previous", access = JsonProperty.Access.WRITE_ONLY)
    private final Object previous;

    public ChangeFeedItemChanges(Object current, ChangeFeedMetadata changeFeedMetadata, Object previous) {
        this.current = current;
        this.changeFeedMetadata = changeFeedMetadata;
        this.previous = previous;
    }

    public Object getCurrent() {
        return current;
    }

    public ChangeFeedMetadata getChangeFeedMetadata() {
        return changeFeedMetadata;
    }

    public Object getPrevious() {
        return previous;
    }
}
