// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.experimental.serializer.PropertyNameSerializer;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GsonPropertyNameTests {
    private static final String EXPECT_VALUE_IN_FIELD = "expectFieldName";
    private static PropertyNameSerializer serializer;

    @BeforeAll
    public static void setup() {
        serializer = new GsonPropertyNameSerializerProvider().createInstance();
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
    public void testPropertyNameOnTransientIgnoredFieldName() throws NoSuchFieldException {
        class Hotel {
            transient String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");
        assertMemberNull(f);
    }

    @Test
    public void testPropertyNameOnExposeIgnoredFieldName() throws NoSuchFieldException {
        class Hotel {
            String hotelName;

            @Expose
            String hotelId;
        }
        Field f1 = Hotel.class.getDeclaredField("hotelName");
        Field f2 = Hotel.class.getDeclaredField("hotelId");

        PropertyNameSerializer serializerWithSetting = new GsonPropertyNameSerializerBuilder()
            .serializer(new GsonBuilder().excludeFieldsWithoutExposeAnnotation()).build();
        StepVerifier.create(serializerWithSetting.getSerializerMemberName(f1))
            .verifyComplete();
        assertMemberValue(f2, "hotelId");
    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class Hotel {
            @SerializedName(value = EXPECT_VALUE_IN_FIELD)
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");
        assertMemberValue(f, EXPECT_VALUE_IN_FIELD);

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

    public void assertMemberNull(Member m) {
        StepVerifier.create(serializer.getSerializerMemberName(m))
            .verifyComplete();
    }
}
