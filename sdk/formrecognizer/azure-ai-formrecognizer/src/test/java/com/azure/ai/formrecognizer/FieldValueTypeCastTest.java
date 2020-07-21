// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValue;
import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldValueTypeCastTest {

    /**
     * Test for {@link FieldValue#asDate()} to Date.
     */
    @Test
    public void toDateFromDate() {
        LocalDate inputDate = LocalDate.of(2006, 6, 6);
        FormField<?> formField = new FormField<>(null, null, null, new FieldValue(FieldValueType.DATE)
            .setFormFieldDate(inputDate), 0);
        LocalDate actualDate = formField.getValue().asDate();
        assertEquals(inputDate, actualDate);
    }

    /**
     * Test for {@link FieldValue#asDate()} to Date from String.
     */
    @Test
    public void toDateFromString() {
        String inputDateString = "2006/06/06";
        FormField<?> formField = new FormField<>(null, null, null,
            new FieldValue(FieldValueType.STRING).setFormFieldString(inputDateString), 0);
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                formField.getValue().asDate());
        assertEquals(unsupportedOperationException.getMessage(), "Cannot get field as DATE from field value "
            + "of type STRING");
    }

    /**
     * Test for {@link FieldValue#asDate()} to Date from null field value.
     */
    @Test
    public void toDateFromNull() {
        FormField<?> formField = new FormField<>(null, null, null, new FieldValue(FieldValueType.DATE)
            .setFormFieldDate(null), 0);
        assertNull(formField.getValue().asDate());
    }


    /**
     * Test for {@link FieldValue#asTime()} to TIME.
     */
    @Test
    public void toTimeFromTime() {
        LocalTime inputTime = LocalTime.parse("13:59:00", DateTimeFormatter.ofPattern("HH:mm:ss"));
        FormField<?> formField = new FormField<>(null, null, null,
            new FieldValue(FieldValueType.TIME).setFormFieldTime(inputTime), 0);
        LocalTime actualTime = formField.getValue().asTime();
        assertEquals(inputTime, actualTime);
    }

    /**
     * Test for {@link FieldValue#asTime()} to TIME from String.
     */
    @Test
    public void toTimeFromString() {
        String inputTimeString = "13:59:00";
        FormField<?> formField = new FormField<>(null, null, null,
            new FieldValue(FieldValueType.STRING).setFormFieldString(inputTimeString), 0);
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                formField.getValue().asTime());
        assertEquals(unsupportedOperationException.getMessage(), "Cannot get field as TIME from field"
            + " value of type STRING");
    }

    /**
     * Test for {@link FieldValue#asTime()} to TIME from null field value.
     */
    @Test
    public void toTimeFromNull() {
        assertNull(new FormField<>(null, null, null,
            new FieldValue(FieldValueType.TIME), 0).getValue().asTime());
    }


    /**
     * Test for {@link FieldValue#asList()} to list.
     */
    @Test
    public void toListFromList() {
        List<FormField<?>> inputList = new ArrayList<>(Arrays.asList(new FormField<>(null, null, null, null, 0)));
        FormField<?> formField = new FormField<>(null, null, null,
            new FieldValue(FieldValueType.LIST).setFormFieldList(inputList), 0);
        List<FormField<?>> actualList = formField.getValue().asList();
        assertEquals(inputList, actualList);
    }

    /**
     * Test for {@link FieldValue#asList()} to list from String.
     */
    @Test
    public void toListFromString() {
        String test = "testString";
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                new FormField<>(null, null, null,
                    new FieldValue(FieldValueType.STRING).setFormFieldString(test), 0).getValue().asList());
        assertEquals(unsupportedOperationException.getMessage(), "Cannot get field as a LIST from field value "
            + "of type STRING");
    }

    /**
     * Test for {@link FieldValue#asList()}  to list from null field value.
     */
    @Test
    public void toListFromNull() {
        assertNull(new FormField<>(null, null, null, new FieldValue(FieldValueType.LIST), 0).getValue().asList());
    }

    /**
     * Test for {@link FieldValue#asPhoneNumber()} to phone number.
     */
    @Test
    public void toPhoneNumberFromPhoneNumber() {
        String phoneNumber = "19876543210";
        String actualPhoneNumber = new FormField<>(null, null, null,
            new FieldValue(FieldValueType.PHONE_NUMBER).setFormFieldPhoneNumber(phoneNumber), 0)
            .getValue().asPhoneNumber();
        assertEquals(phoneNumber, actualPhoneNumber);
    }

    /**
     * Test for {@link FieldValue#asPhoneNumber()} to phone number from String.
     */
    @Test
    public void toPhoneNumberFromString() {
        String phoneNumber = "19876543210";
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                new FormField<>(null, null, null,
                    new FieldValue(FieldValueType.STRING).setFormFieldString(phoneNumber), 0)
                    .getValue().asPhoneNumber());
        assertEquals(unsupportedOperationException.getMessage(), "Cannot get field as aPHONE_NUMBER "
            + "from field value of type STRING");
    }

    /**
     * Test for {@link FieldValue#asPhoneNumber()} to phone number from null field value.
     */
    @Test
    public void toPhoneNumberFromNull() {
        assertNull(new FormField<>(null, null, null,
            new FieldValue(FieldValueType.PHONE_NUMBER), 0).getValue().asPhoneNumber());
    }

    /**
     * Test for {@link FieldValue#asMap()} to map.
     */
    @Test
    public void toMapFromMap() {
        Map<String, FormField<?>> inputMap = new HashMap<>() {
            {
                put("key", new FormField<>(null, null, null, null, 0));
            }
        };
        Map<String, FormField<?>> actualList = new FormField<>(null, null, null,
            new FieldValue(FieldValueType.MAP).setFormFieldMap(inputMap), 0).getValue().asMap();
        assertEquals(inputMap, actualList);
    }

    /**
     * Test for {@link FieldValue#asMap()} to map from String.
     */
    @Test
    public void toMapFromString() {
        String str = "1";
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                new FormField<>(null, null, null,
                    new FieldValue(FieldValueType.STRING).setFormFieldString(str), 0).getValue().asMap());
        assertEquals(unsupportedOperationException.getMessage(), "Cannot get field as a MAP from field "
            + "value of type STRING");
    }

    /**
     * Test for {@link FieldValue#asMap()} to map from null field value.
     */
    @Test
    public void toMapFromNull() {
        assertNull(new FormField<>(null, null, null,
            new FieldValue(FieldValueType.MAP), 0).getValue().asMap());
    }

    /**
     * Test for {@link FieldValue#asDouble()} to double.
     */
    @Test
    public void toDoubleFromDouble() {
        Double inputDouble = 2.2;
        Double actualDoubleValue = new FormField<>(null, null, null,
            new FieldValue(FieldValueType.DOUBLE).setFormFieldDouble(inputDouble), 0).getValue().asDouble();
        assertEquals(inputDouble, actualDoubleValue);
    }

    /**
     * Test for {@link FieldValue#asDouble()} to double from String.
     */
    @Test
    public void toDoubleFromString() {
        String doubleString = "2.2";
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                new FormField<>(null, null, null,
                    new FieldValue(FieldValueType.STRING).setFormFieldString(doubleString), 0).getValue().asDouble());
        assertEquals(unsupportedOperationException.getMessage(), "Cannot get field as DOUBLE from "
            + "field value of type STRING");
    }

    /**
     * Test for {@link FieldValue#asDouble()} to double from null field value.
     */
    @Test
    public void toDoubleFromNull() {
        assertNull(new FormField<>(null, null, null,
            new FieldValue(FieldValueType.DOUBLE), 0).getValue().asDouble());
    }

    /**
     * Test for {@link FieldValue#asLong()} to long.
     */
    @Test
    public void toLongFromLong() {
        long inputLong = 22;
        Long actualLongValue = new FormField<>(null, null, null,
            new FieldValue(FieldValueType.LONG).setFormFieldLong(inputLong), 0).getValue().asLong();
        assertEquals(inputLong, actualLongValue);
    }

    /**
     * Test for {@link FieldValue#asLong()} to long from String.
     */
    @Test
    public void toLongFromString() {
        String inputLongString = "22";
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                new FormField<>(null, null, null,
                    new FieldValue(FieldValueType.STRING).setFormFieldString(inputLongString), 0).getValue().asLong());
        assertEquals(unsupportedOperationException.getMessage(), "Cannot get field as LONG from field value of "
            + "type STRING");
    }

    /**
     * Test for {@link FieldValue#asLong()} to long from null field value.
     */
    @Test
    public void toLongFromNull() {
        assertNull(new FormField<>(null, null, null,
            new FieldValue(FieldValueType.LONG), 0).getValue().asLong());
    }

    /**
     * Test for {@link FieldValue#asString()} to String from TIME.
     */
    @Test
    public void toStringFromTime() {
        LocalTime inputTime = LocalTime.parse("13:59:00", DateTimeFormatter.ofPattern("HH:mm:ss"));
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                new FormField<>(null, null, null,
                    new FieldValue(FieldValueType.TIME).setFormFieldTime(inputTime), 0).getValue().asString());
        assertEquals(unsupportedOperationException.getMessage(), "Cannot get field as STRING from field "
            + "value of type TIME");
    }

    /**
     * Test for {@link FieldValue#asString()} to String from String.
     */
    @Test
    public void toStringFromString() {
        String stringValue = "String value";
        String actualStringValue = new FormField<>(null, null, null,
            new FieldValue(FieldValueType.STRING).setFormFieldString(stringValue), 0).getValue().asString();
        assertEquals(stringValue, actualStringValue);
    }
}
