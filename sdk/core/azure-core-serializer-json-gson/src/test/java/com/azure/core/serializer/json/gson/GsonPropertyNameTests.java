// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.gson;

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
    private static GsonJsonSerializer serializer;

    @BeforeAll
    public static void setup() {
        serializer = new GsonJsonSerializerProvider().createInstance();
    }

    @Test
    public void testPropertyNameOnFieldName() throws NoSuchFieldException {
        class LocalHotel {
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");

        assertEquals(serializer.convertMemberName(f), "hotelName");
    }

    @Test
    public void testPropertyNameOnTransientIgnoredFieldName() throws NoSuchFieldException {
        class LocalHotel {
            transient String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertNull(serializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class LocalHotel {
            @SerializedName(value = EXPECT_VALUE_IN_FIELD)
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals(serializer.convertMemberName(f), EXPECT_VALUE_IN_FIELD);
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() throws NoSuchFieldException {
        class LocalHotel {
            @SerializedName(value = "")
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");

        assertEquals("", serializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnMethodName() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = LocalHotel.class.getDeclaredMethod("getHotelName");

        assertNull(serializer.convertMemberName(m));
    }

    @Test
    public void testPropertyNameOnConstructor() {
        Constructor<?>[] constructors = Hotel.class.getConstructors();
        assertEquals(1, constructors.length);

        assertNull(serializer.convertMemberName(constructors[0]));
    }
}
