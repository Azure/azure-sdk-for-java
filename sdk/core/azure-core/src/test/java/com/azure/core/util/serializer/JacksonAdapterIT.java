// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util.serializer;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.DateTimeRfc1123;
import com.fasterxml.jackson.core.JacksonException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for {@link JacksonAdapter}.
 */
public class JacksonAdapterIT {
    @Test
    public void stronglyTypedHeadersClassIsDeserialized() throws IOException {
        final String expectedDate = DateTimeRfc1123.toRfc1123String(OffsetDateTime.now());

        HttpHeaders rawHeaders = new HttpHeaders().set("Date", expectedDate);

        StronglyTypedHeaders stronglyTypedHeaders = JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(rawHeaders, StronglyTypedHeaders.class);

        assertEquals(expectedDate, DateTimeRfc1123.toRfc1123String(stronglyTypedHeaders.getDate()));
    }

    @Test
    public void stronglyTypedHeadersClassThrowsEagerly() {
        HttpHeaders rawHeaders = new HttpHeaders().set("Date", "invalid-rfc1123-date");

        assertThrows(DateTimeParseException.class, () -> JacksonAdapter.createDefaultSerializerAdapter()
            .deserialize(rawHeaders, StronglyTypedHeaders.class));
    }

    @Test
    public void invalidStronglyTypedHeadersClassThrowsCorrectException() throws IOException {
        try {
            JacksonAdapter.createDefaultSerializerAdapter().deserialize(new HttpHeaders(),
                InvalidStronglyTypedHeaders.class);

            fail("An exception should have been thrown.");
        } catch (RuntimeException ex) {
            assertTrue(ex.getCause() instanceof JacksonException, "Exception cause type was "
                + ex.getCause().getClass().getName() + " instead of the expected JacksonException type.");
        }
    }

    public static final class StronglyTypedHeaders {
        private final DateTimeRfc1123 date;

        public StronglyTypedHeaders(HttpHeaders rawHeaders) {
            String dateString = rawHeaders.getValue("Date");
            this.date = (dateString == null) ? null : new DateTimeRfc1123(dateString);
        }

        OffsetDateTime getDate() {
            return (date == null) ? null : date.getDateTime();
        }
    }

    public static final class InvalidStronglyTypedHeaders {
        public InvalidStronglyTypedHeaders(HttpHeaders httpHeaders) throws Exception {
            throw new Exception();
        }
    }
}
