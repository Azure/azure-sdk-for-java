// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JacksonPropertyNameTests {
    private static final String EXPECT_VALUE_IN_FIELD = "expectFieldName";
    private static final String EXPECT_VALUE_IN_METHOD = "expectMethodName";
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
        assertMemberValue(f, "hotelName");
    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class Hotel {
            @JsonProperty(value = EXPECT_VALUE_IN_FIELD)
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");
        assertMemberValue(f, EXPECT_VALUE_IN_FIELD);
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() throws NoSuchFieldException {
        class Hotel {
            @JsonProperty(value = "")
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");

        assertMemberValue(f, "hotelName");
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithNullValue() throws NoSuchFieldException {
        class Hotel {
            @JsonProperty()
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

    @Test
    public void testPropertyNameOnMethodAnnotation() throws NoSuchMethodException {
        class Hotel {
            String hotelName;

            @JsonProperty(value = EXPECT_VALUE_IN_METHOD)
            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = Hotel.class.getDeclaredMethod("getHotelName");
        assertMemberValue(m, EXPECT_VALUE_IN_METHOD);
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
        assertMemberValue(m, "getHotelName");
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
        assertMemberValue(m, "getHotelName");
    }

    public void assertMemberValue(Member m, String expectValue) {
        StepVerifier.create(serializer.getSerializerMemberName(m))
            .assertNext(actual -> assertEquals(expectValue, actual))
            .verifyComplete();
    }
}
