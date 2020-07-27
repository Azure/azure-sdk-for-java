// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.google.gson.annotations.SerializedName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GsonPropertyNameTests {
    private static final String expectValueInField = "expectFieldName";
    private static GsonPropertyNameSerializer serializer;

    @BeforeAll
    public static void setup() {
        serializer = new GsonPropertyNameSerializer();
    }

    @Test
    public void testPropertyNameOnFieldName() throws NoSuchFieldException {
        class Hotel {
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");

        assertMemberValue(f, "hotelName");
    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class Hotel {
            @SerializedName(value = expectValueInField)
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");
        assertMemberValue(f, expectValueInField);

    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() throws NoSuchFieldException {
        class Hotel {
            @SerializedName(value = "")
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");

        assertMemberValue(f, "hotelName");
    }

    @Test
    public void testPropertyNameOnMethodName() throws NoSuchMethodException {
        class Hotel {
            String hotelName;

            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = Hotel.class.getDeclaredMethod("getHotelName");

        assertMemberValue(m, "getHotelName");
    }

    public void assertMemberValue(Member m, String expectValue) {
        StepVerifier.create(serializer.getSerializerMemberName(m))
            .assertNext(actual -> assertEquals(expectValue, actual))
            .verifyComplete();
    }
}
