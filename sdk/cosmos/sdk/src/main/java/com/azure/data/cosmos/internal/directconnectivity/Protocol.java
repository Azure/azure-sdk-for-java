// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import org.apache.commons.text.WordUtils;

public enum Protocol {
    HTTPS, TCP;

    String scheme() {
        switch (this) {
            case HTTPS:
                return "https";
            case TCP:
                return "rntbd";
            default:
                throw new IllegalStateException();
        }
    }
    
    @Override
    public String toString() {
        return WordUtils.capitalizeFully(this.name());        
    }    
}
