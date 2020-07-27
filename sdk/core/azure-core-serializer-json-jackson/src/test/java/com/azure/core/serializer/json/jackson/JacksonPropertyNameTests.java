// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonPropertyNameTests {
    private static final String expectValueInField = "expectFieldName";
    private static final String expectValueInMethod = "expectMethodName";
    private static JacksonPropertyNameSerializer serializer;

    @BeforeAll
    public static void setup() {
        serializer = new JacksonPropertyNameSerializer();
    }

    @Test
    public void testPropertyNameOnFieldName() throws NoSuchFieldException {
        class Hotel {
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");

        StepVerifier.create(serializer.getSerializerMemberName(f))
            .assertNext(actual -> assertEquals("hotelName", actual))
            .verifyComplete();

    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class Hotel {
            @JsonProperty(value = expectValueInField)
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");

        StepVerifier.create(serializer.getSerializerMemberName(f))
            .assertNext(actual -> assertEquals(expectValueInField, actual))
            .verifyComplete();

    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() throws NoSuchFieldException {
        class Hotel {
            @JsonProperty(value = "")
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");

        StepVerifier.create(serializer.getSerializerMemberName(f))
            .assertNext(actual -> assertEquals("hotelName", actual))
            .verifyComplete();
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithNullValue() throws NoSuchFieldException {
        class Hotel {
            @JsonProperty()
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");

        StepVerifier.create(serializer.getSerializerMemberName(f))
            .assertNext(actual -> assertEquals("hotelName", actual))
            .verifyComplete();

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

        StepVerifier.create(serializer.getSerializerMemberName(m))
            .assertNext(actual -> assertEquals("getHotelName", actual))
            .verifyComplete();
    }

    @Test
    public void testPropertyNameOnMethodAnnotation() throws NoSuchMethodException {
        class Hotel {
            String hotelName;

            @JsonProperty(value = expectValueInMethod)
            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = Hotel.class.getDeclaredMethod("getHotelName");

        StepVerifier.create(serializer.getSerializerMemberName(m))
            .assertNext(actual -> assertEquals(expectValueInMethod, actual))
            .verifyComplete();
    }


    @Test
    public void testPropertyNameOnMethodAnnotationWithEmptyValue() throws NoSuchMethodException {
        class Hotel {
            String hotelName;

            @JsonProperty(value = "")
            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = Hotel.class.getDeclaredMethod("getHotelName");
        StepVerifier.create(serializer.getSerializerMemberName(m))
            .assertNext(actual -> assertEquals("getHotelName", actual))
            .verifyComplete();
    }

    @Test
    public void testPropertyNameOnMethodAnnotationWithNullValue() throws NoSuchMethodException {
        class Hotel {
            String hotelName;

            @JsonProperty()
            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = Hotel.class.getDeclaredMethod("getHotelName");
        StepVerifier.create(serializer.getSerializerMemberName(m))
            .assertNext(actual -> assertEquals("getHotelName", actual))
            .verifyComplete();
    }
}
