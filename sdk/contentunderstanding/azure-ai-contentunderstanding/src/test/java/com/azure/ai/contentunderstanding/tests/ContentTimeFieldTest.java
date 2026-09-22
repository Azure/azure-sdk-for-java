// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentunderstanding.tests;

import com.azure.ai.contentunderstanding.models.ContentField;
import com.azure.ai.contentunderstanding.models.ContentTimeField;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ContentTimeFieldTest {
    @Test
    public void getValuePreservesReleasedStringAndExposesLocalTimeHelper() {
        ContentTimeField field
            = BinaryData.fromString("{\"type\":\"time\",\"valueTime\":\"14:30:45\"}").toObject(ContentTimeField.class);

        assertEquals("14:30:45", field.getValue());
        assertEquals("14:30:45", ((ContentField) field).getValue());
        assertEquals(LocalTime.of(14, 30, 45), field.getTimeValue());
    }

    @Test
    public void getTimeValuePreservesFractionalSeconds() {
        ContentTimeField field = BinaryData.fromString("{\"type\":\"time\",\"valueTime\":\"14:30:45.123456789\"}")
            .toObject(ContentTimeField.class);

        assertEquals("14:30:45.123456789", field.getValue());
        assertEquals(LocalTime.of(14, 30, 45, 123456789), field.getTimeValue());
    }

    @Test
    public void getValueReturnsNullWhenWireValueIsAbsent() {
        ContentTimeField field = BinaryData.fromString("{\"type\":\"time\"}").toObject(ContentTimeField.class);

        assertNull(field.getValue());
        assertNull(field.getTimeValue());
    }

    @Test
    public void serializationKeepsPlainTimeWireFormat() {
        ContentTimeField field
            = BinaryData.fromString("{\"type\":\"time\",\"valueTime\":\"14:30:45\"}").toObject(ContentTimeField.class);

        String serialized = BinaryData.fromObject(field).toString();

        assertTrue(serialized.contains("\"valueTime\":\"14:30:45\""));
    }
}
