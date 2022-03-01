// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.serializer;

import com.azure.core.http.HttpHeaders;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Integration tests for {@link JacksonAdapter}.
 */
public class JacksonAdapterIT {
    @Test
    public void stronglyTypedHeadersClassIsLazilyDeserialized() throws IOException {
        final String expectedValue = "the-string-value";

        HttpHeaders rawHeaders = new HttpHeaders().set("the-string", "the-string-value");

        StronglyTypedHeaders stronglyTypedHeaders = JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(rawHeaders, StronglyTypedHeaders.class);

        assertNull(stronglyTypedHeaders.theString);
        assertEquals(expectedValue, stronglyTypedHeaders.getTheString());
    }

    private static class StronglyTypedHeaders {
        private final HttpHeaders rawHeaders;

        boolean hasTheStringBeenDeserialized;

        String theString;

        StronglyTypedHeaders(HttpHeaders rawHeaders) {
            this.rawHeaders = rawHeaders;
        }

        String getTheString() {
            if (hasTheStringBeenDeserialized) {
                return theString;
            }

            theString = rawHeaders.getValue("the-string");
            hasTheStringBeenDeserialized = true;

            return theString;
        }
    }
}
