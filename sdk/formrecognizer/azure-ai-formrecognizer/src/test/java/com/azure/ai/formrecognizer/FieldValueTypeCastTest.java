// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormField;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FieldValueTypeCastTest {

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to Date.
     */
    @Test
    public void toDateFromDate() {
        LocalDate inputDate = LocalDate.of(2006, 6, 6);
        LocalDate actualDate = FieldValueType.DATE.cast(new FormField<>(0, null, null,
            inputDate, null, FieldValueType.DATE));
        assertEquals(inputDate, actualDate);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to Date from String.
     */
    @Test
    public void toDateFromString() {
        String inputDateString = "2006/06/06";
        LocalDate inputDate = LocalDate.parse(inputDateString, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        LocalDate actualDate = FieldValueType.DATE.cast(new FormField<>(0, null, null,
            inputDateString, null, FieldValueType.STRING));
        assertEquals(inputDate, actualDate);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to Date from null field value.
     */
    @Test
    public void toDateFromNull() {
        assertNull(FieldValueType.DATE.cast(new FormField<>(0, null, null,
            null, null, FieldValueType.STRING)));
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to Date from any other
     * FieldValueType except for String.
     */
    @Test
    public void toDateFromPhoneNumber() {
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                FieldValueType.DATE.cast(new FormField<>(0, null, null,
            "19876543210", null, FieldValueType.PHONE_NUMBER)));
        assertEquals(unsupportedOperationException.getMessage(), "Cannot cast from field value of "
            + "type PHONE_NUMBER to type DATE");
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to TIME.
     */
    @Test
    public void toTimeFromTime() {
        LocalTime inputTime = LocalTime.parse("13:59:00", DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime actualTime = FieldValueType.TIME.cast(new FormField<>(0, null, null,
            inputTime, null, FieldValueType.TIME));
        assertEquals(inputTime, actualTime);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to TIME from String.
     */
    @Test
    public void toTimeFromString() {
        String inputTimeString = "13:59:00";
        LocalTime inputTime = LocalTime.parse(inputTimeString, DateTimeFormatter.ofPattern("HH:mm:ss"));
        LocalTime actualTime = FieldValueType.TIME.cast(new FormField<>(0, null, null,
            inputTimeString, null, FieldValueType.STRING));
        assertEquals(inputTime, actualTime);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to TIME from null field value.
     */
    @Test
    public void toTimeFromNull() {
        assertNull(FieldValueType.TIME.cast(new FormField<>(0, null, null,
            null, null, FieldValueType.TIME)));
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to TIME from any other
     * FieldValueType except for String.
     */
    @Test
    public void toTimeFromPhoneNumber() {
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                FieldValueType.TIME.cast(new FormField<>(0, null, null,
                    "19876543210", null, FieldValueType.PHONE_NUMBER)));
        assertEquals(unsupportedOperationException.getMessage(), "Cannot cast from field value of "
            + "type PHONE_NUMBER to type TIME");
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to list.
     */
    @Test
    public void toListFromList() {
        List<String> inputList = new ArrayList<>(Arrays.asList("1"));
        List<String> actualList = FieldValueType.LIST.cast(new FormField<>(0, null, null,
            inputList, null, FieldValueType.LIST));
        assertEquals(inputList, actualList);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to list from String.
     */
    @Test
    public void toListFromString() {
        String listString = "1";
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                FieldValueType.LIST.cast(new FormField<>(0, null, null,
                    listString, null, FieldValueType.STRING)));
        assertEquals(unsupportedOperationException.getMessage(), "Cannot cast from field value of "
            + "type STRING to type LIST");
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to list from null field value.
     */
    @Test
    public void toListFromNull() {
        assertNull(FieldValueType.LIST.cast(new FormField<>(0, null, null,
            null, null, FieldValueType.LIST)));
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to list from any other
     * FieldValueType except for String.
     */
    @Test
    public void toListFromTime() {
        LocalTime inputTime = LocalTime.parse("13:59:00", DateTimeFormatter.ofPattern("HH:mm:ss"));
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                FieldValueType.LIST.cast(new FormField<>(0, null, null,
                    inputTime, null, FieldValueType.TIME)));
        assertEquals(unsupportedOperationException.getMessage(), "Cannot cast from field value of "
            + "type TIME to type LIST");
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to phone number.
     */
    @Test
    public void toPhoneNumberFromPhoneNumber() {
        String phoneNumber = "19876543210";
        String actualPhoneNumber = FieldValueType.PHONE_NUMBER.cast(new FormField<>(0, null, null,
            phoneNumber, null, FieldValueType.PHONE_NUMBER));
        assertEquals(phoneNumber, actualPhoneNumber);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to phone number from String.
     */
    @Test
    public void toPhoneNumberFromString() {
        String phoneNumber = "19876543210";
        String actualPhoneNumber = FieldValueType.PHONE_NUMBER.cast(new FormField<>(0, null, null,
            phoneNumber, null, FieldValueType.STRING));
        assertEquals(phoneNumber, actualPhoneNumber);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to phone number from null field value.
     */
    @Test
    public void toPhoneNumberFromNull() {
        assertNull(FieldValueType.PHONE_NUMBER.cast(new FormField<>(0, null, null,
            null, null, FieldValueType.PHONE_NUMBER)));
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to phone number from any other
     * FieldValueType except for String.
     */
    @Test
    public void toPhoneNumberFromTime() {
        LocalTime inputTime = LocalTime.parse("13:59:00", DateTimeFormatter.ofPattern("HH:mm:ss"));
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                FieldValueType.PHONE_NUMBER.cast(new FormField<>(0, null, null,
                    inputTime, null, FieldValueType.TIME)));
        assertEquals(unsupportedOperationException.getMessage(), "Cannot cast from field value of "
            + "type TIME to type PHONE_NUMBER");
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to map.
     */
    @Test
    public void toMapFromMap() {
        Map<String, String> inputMap = Collections.singletonMap("key", "value");
        Map<String, String> actualList = FieldValueType.MAP.cast(new FormField<>(0, null, null,
            inputMap, null, FieldValueType.MAP));
        assertEquals(inputMap, actualList);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to map from String.
     */
    @Test
    public void toMapFromString() {
        String listString = "1";
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                FieldValueType.MAP.cast(new FormField<>(0, null, null,
                    listString, null, FieldValueType.STRING)));
        assertEquals(unsupportedOperationException.getMessage(), "Cannot cast from field value of "
            + "type STRING to type MAP");
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to map from null field value.
     */
    @Test
    public void toMapFromNull() {
        assertNull(FieldValueType.MAP.cast(new FormField<>(0, null, null,
            null, null, FieldValueType.MAP)));
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to map from any other
     * FieldValueType except for String.
     */
    @Test
    public void toMapFromTime() {
        LocalTime inputTime = LocalTime.parse("13:59:00", DateTimeFormatter.ofPattern("HH:mm:ss"));
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                FieldValueType.MAP.cast(new FormField<>(0, null, null,
                    inputTime, null, FieldValueType.TIME)));
        assertEquals(unsupportedOperationException.getMessage(), "Cannot cast from field value of "
            + "type TIME to type MAP");
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to double.
     */
    @Test
    public void toDoubleFromDouble() {
        Double inputDouble = 2.2;
        Double actualDoubleValue = FieldValueType.DOUBLE.cast(new FormField<>(0, null, null,
            inputDouble, null, FieldValueType.DOUBLE));
        assertEquals(inputDouble, actualDoubleValue);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to double from String.
     */
    @Test
    public void toDoubleFromString() {
        String doubleString = "2.2";
        Double actualDouble = FieldValueType.DOUBLE.cast(new FormField<>(0, null, null,
            doubleString, null, FieldValueType.STRING));
        assertEquals(Double.valueOf(doubleString), actualDouble);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to double from null field value.
     */
    @Test
    public void toDoubleFromNull() {
        assertNull(FieldValueType.DOUBLE.cast(new FormField<>(0, null, null,
            null, null, FieldValueType.DOUBLE)));
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to double from any other
     * FieldValueType except for String.
     */
    @Test
    public void toDoubleFromTime() {
        LocalTime inputTime = LocalTime.parse("13:59:00", DateTimeFormatter.ofPattern("HH:mm:ss"));
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                FieldValueType.DOUBLE.cast(new FormField<>(0, null, null,
                    inputTime, null, FieldValueType.TIME)));
        assertEquals(unsupportedOperationException.getMessage(), "Cannot cast from field value of "
            + "type TIME to type DOUBLE");
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to long.
     */
    @Test
    public void toLongFromLong() {
        long inputDouble = 22;
        Long actualLongValue = FieldValueType.LONG.cast(new FormField<>(0, null, null,
            inputDouble, null, FieldValueType.LONG));
        assertEquals(inputDouble, actualLongValue);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to long from String.
     */
    @Test
    public void toLongFromString() {
        String inputDoubleString = "22";
        Long actualLongValue = FieldValueType.LONG.cast(new FormField<>(0, null, null,
            inputDoubleString, null, FieldValueType.STRING));
        assertEquals(Long.valueOf(inputDoubleString), actualLongValue);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to long from null field value.
     */
    @Test
    public void toLongFromNull() {
        assertNull(FieldValueType.LONG.cast(new FormField<>(0, null, null,
            null, null, FieldValueType.LONG)));
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to long from any other
     * FieldValueType except for String.
     */
    @Test
    public void toLongFromTime() {
        LocalTime inputTime = LocalTime.parse("13:59:00", DateTimeFormatter.ofPattern("HH:mm:ss"));
        final UnsupportedOperationException unsupportedOperationException =
            assertThrows(UnsupportedOperationException.class, () ->
                FieldValueType.LONG.cast(new FormField<>(0, null, null,
                    inputTime, null, FieldValueType.TIME)));
        assertEquals(unsupportedOperationException.getMessage(), "Cannot cast from field value of "
            + "type TIME to type LONG");
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to String from TIME.
     */
    @Test
    public void toStringFromTime() {
        LocalTime inputTime = LocalTime.parse("13:59:00", DateTimeFormatter.ofPattern("HH:mm:ss"));
        String localTimeString = FieldValueType.STRING.cast(new FormField<>(0, null, null,
            inputTime, null, FieldValueType.TIME));
        assertEquals(inputTime.toString(), localTimeString);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to String from String.
     */
    @Test
    public void toStringFromString() {
        String stringValue = "String value";
        String actualStringValue = FieldValueType.STRING.cast(new FormField<>(0, null, null,
            stringValue, null, FieldValueType.STRING));
        assertEquals(stringValue, actualStringValue);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to String from double.
     */
    @Test
    public void toStringFromDouble() {
        Double doubleValue = 2.2;
        String actualDouble = FieldValueType.STRING.cast(new FormField<>(0, null, null,
            doubleValue, null, FieldValueType.DOUBLE));
        assertEquals(String.valueOf(doubleValue), actualDouble);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to long from String.
     */
    @Test
    public void toStringFromLong() {
        Long inputLong = 22L;
        String actualLongValue = FieldValueType.STRING.cast(new FormField<>(0, null, null,
            inputLong, null, FieldValueType.LONG));
        assertEquals(String.valueOf(inputLong), actualLongValue);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to String from Map.
     */
    @Test
    public void toStringFromMap() {
        Map<String, String> inputMap = Collections.singletonMap("key", "value");
        String stringMap = FieldValueType.STRING.cast(new FormField<>(0, null, null,
                    inputMap, null, FieldValueType.MAP));
        assertEquals(inputMap.toString(), stringMap);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to String from Phone number.
     */
    @Test
    public void toStringFromPhoneNumber() {
        String phoneNumber = "19876543210";
        String actualPhoneNumber = FieldValueType.STRING.cast(new FormField<>(0, null, null,
            phoneNumber, null, FieldValueType.PHONE_NUMBER));
        assertEquals(phoneNumber, actualPhoneNumber);
    }

    /**
     * Test for {@link com.azure.ai.formrecognizer.models.FieldValueType#cast(FormField)} to String from List.
     */
    @Test
    public void toStringFromList() {
        List<String> inputList = Collections.singletonList("1");
        String actualStringList = FieldValueType.STRING.cast(new FormField<>(0, null, null,
            inputList, null, FieldValueType.LIST));
        assertEquals(inputList.toString(), actualStringList);
    }
}
