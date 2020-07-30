// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

import com.azure.core.experimental.serializer.PropertyNameSerializer;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GsonPropertyNameTests {
    private static final String EXPECT_VALUE_IN_FIELD = "expectFieldName";
    private static PropertyNameSerializer serializer;

    @BeforeAll
    public static void setup() {
        serializer = new GsonJsonSerializerProvider().createInstance();
    }

    @Test
    public void testPropertyNameOnFieldName() throws NoSuchFieldException {
        class Hotel {
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");

        assertEquals(serializer.getSerializerMemberName(f), "hotelName");
    }

    @Test
    public void testPropertyNameOnTransientIgnoredFieldName() throws NoSuchFieldException {
        class Hotel {
            transient String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");
        assertNull(serializer.getSerializerMemberName(f));
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

        PropertyNameSerializer serializerWithSetting = new GsonJsonSerializerBuilder()
            .serializer(new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()).build();
        assertNull(serializerWithSetting.getSerializerMemberName(f1));
        assertEquals(serializer.getSerializerMemberName(f2), "hotelId");
    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class Hotel {
            @SerializedName(value = EXPECT_VALUE_IN_FIELD)
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");
        assertEquals(serializer.getSerializerMemberName(f), EXPECT_VALUE_IN_FIELD);
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() throws NoSuchFieldException {
        class Hotel {
            @SerializedName(value = "")
            String hotelName;
        }
        Field f = Hotel.class.getDeclaredField("hotelName");

        assertEquals(serializer.getSerializerMemberName(f), "hotelName");
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

        assertEquals(serializer.getSerializerMemberName(m), "getHotelName");
    }

    @Test
    public void testPropertyNameOnConstructor() {
        Constructor[] constructors = Hotel.class.getConstructors();
        assertEquals(1, constructors.length);

        assertEquals(serializer.getSerializerMemberName(constructors[0]),
            "com.azure.core.serializer.json.gson.Hotel");
    }
}
