// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JacksonPropertyNameTests {
    private static final String EXPECT_VALUE_IN_FIELD = "expectFieldName";
    private static final String EXPECT_VALUE_IN_METHOD = "expectMethodName";
    private static JacksonJsonSerializer serializer;

    @BeforeAll
    public static void setup() {
        serializer = new JacksonJsonSerializerProvider().createInstance();
    }

    @Test
    public void testPropertyNameOnFieldName() throws NoSuchFieldException {
        class LocalHotel {
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals("hotelName", serializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnIgnoredFieldName() throws NoSuchFieldException {
        class LocalHotel {
            @JsonIgnore
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertNull(serializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnTransientFieldName() throws NoSuchFieldException {
        class LocalHotel {
            transient String hotelName;

            public String getHotelName() {
                return hotelName;
            }
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertNull(serializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty(value = EXPECT_VALUE_IN_FIELD)
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals(EXPECT_VALUE_IN_FIELD, serializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty(value = "")
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");

        assertEquals("hotelName", serializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithNullValue() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty()
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals("hotelName", serializer.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnMethodName() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;
            boolean flag;

            public String getHotelName() {
                return hotelName;
            }
            public void setHotelName(String hotelName) {
                this.hotelName = hotelName;
            }

            public boolean isFlag() {
                return flag;
            }
            public void setFlag(boolean flag) {
                this.flag = flag;
            }
        }

        Method getterM = LocalHotel.class.getDeclaredMethod("getHotelName");
        assertEquals("hotelName", serializer.convertMemberName(getterM));
        Method setterM = LocalHotel.class.getDeclaredMethod("setHotelName", String.class);
        assertNull(serializer.convertMemberName(setterM));
        Method getterWithIs = LocalHotel.class.getDeclaredMethod("isFlag");
        assertEquals("flag", serializer.convertMemberName(getterWithIs));
        Method setterWithIs = LocalHotel.class.getDeclaredMethod("setFlag", boolean.class);
        assertNull(serializer.convertMemberName(setterWithIs));
    }

    @Test
    public void testPropertyNameOnMethodNameWithoutGetSet() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            public String hotelName1() {
                return hotelName;
            }

            public void hotelName2(String hotelName) {
                this.hotelName = hotelName;
            }
        }

        Method getterM = LocalHotel.class.getDeclaredMethod("hotelName1");
        assertNull(serializer.convertMemberName(getterM));
        Method setterM = LocalHotel.class.getDeclaredMethod("hotelName2", String.class);
        assertNull(serializer.convertMemberName(setterM));
    }

    @Test
    public void testPropertyNameOnMethodNameWithGetInMiddle() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;
            String hotelId;
            String flag;

            public String hotelgetName() {
                return hotelName;
            }

            public String getHotelGetId() {
                return hotelId;
            }

            public String isFlag() {
                return flag;
            }
        }

        Method getterM1 = LocalHotel.class.getDeclaredMethod("hotelgetName");
        Method getterM2 = LocalHotel.class.getDeclaredMethod("getHotelGetId");
        Method getterM3 = LocalHotel.class.getDeclaredMethod("isFlag");

        assertNull(serializer.convertMemberName(getterM1));
        assertEquals("hotelGetId", serializer.convertMemberName(getterM2));
        assertNull(serializer.convertMemberName(getterM3));
    }
    @Test
    public void testPropertyNameOnIgnoredMethodName() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            @JsonIgnore
            public String getHotelName() {
                return hotelName;
            }
        }
        Method m = LocalHotel.class.getDeclaredMethod("getHotelName");
        assertNull(serializer.convertMemberName(m));
    }

    @Test
    public void testPropertyNameOnMethodAnnotation() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            @JsonProperty(value = EXPECT_VALUE_IN_METHOD)
            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = LocalHotel.class.getDeclaredMethod("getHotelName");
        assertEquals(EXPECT_VALUE_IN_METHOD, serializer.convertMemberName(m));
    }


    @Test
    public void testPropertyNameOnMethodAnnotationWithEmptyValue() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            @JsonProperty(value = "")
            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = LocalHotel.class.getDeclaredMethod("getHotelName");
        assertEquals("hotelName", serializer.convertMemberName(m));
    }

    @Test
    public void testPropertyNameOnMethodAnnotationWithNullValue() throws NoSuchMethodException {
        class LocalHotel {
            String hotelName;

            @JsonProperty()
            public String getHotelName() {
                return hotelName;
            }
        }

        Method m = LocalHotel.class.getDeclaredMethod("getHotelName");

        assertEquals("hotelName", serializer.convertMemberName(m));
    }

    @Test
    public void testPropertyNameOnConstructor() throws NoSuchMethodException {
        Constructor<?>[] constructors = Hotel.class.getConstructors();
        assertEquals(1, constructors.length);

        assertNull(serializer.convertMemberName(constructors[0]));
    }
}
