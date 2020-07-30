// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

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
        assertMemberValue(f, "hotelName");
    }

    @Test
    public void testPropertyNameOnIgnoredFieldName() throws NoSuchFieldException {
        class LocalHotel {
            @JsonIgnore
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertMemberNull(f);
    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty(value = EXPECT_VALUE_IN_FIELD)
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertMemberValue(f, EXPECT_VALUE_IN_FIELD);
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty(value = "")
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");

        assertMemberValue(f, "hotelName");
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithNullValue() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty()
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertMemberValue(f, "hotelName");
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
        assertMemberValue(m, "getHotelName");
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
        assertMemberNull(m);
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
        assertMemberValue(m, EXPECT_VALUE_IN_METHOD);
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
        assertMemberValue(m, "getHotelName");
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
        assertMemberValue(m, "getHotelName");
    }

    public void assertMemberValue(Member m, String expectValue) {
        assertEquals(expectValue, serializer.getSerializerMemberName(m));
    }

    public void assertMemberNull(Member m) {
        assertNull(serializer.getSerializerMemberName(m));
    }


    @Test
    public void testPropertyNameOnConstructor() {
        Constructor<?>[] constructors = Hotel.class.getConstructors();
        assertEquals(1, constructors.length);

        assertEquals(serializer.getSerializerMemberName(constructors[0]),
            "com.azure.core.serializer.json.jackson.Hotel");
    }

    @Test
    public void compareSerializedNameWithJsonSerializer() throws NoSuchFieldException {
        Map<String, String> valueMapping = new HashMap<>() {
            {
                put("hotelId", "id");
                put("hotelName", "hotelName");
                put("description", "description");
                put("tags", "tags");
            }
        };

        JacksonJsonObject node = (JacksonJsonObject) serializer.toTree(
            new NameTestingHotel()
                .setHotelName("name")
                .setId("1")
                .setAddress("address")
                .setDescription("good")
                .setReviews("nice")
                .setTags("free parking"));
        assertEquals(6, node.fieldNames().count());
        Field f = NameTestingHotel.class.getDeclaredField("price");
        assertNull(serializer.getSerializerMemberName(f));
        node.fieldNames().forEach(name -> {
            Member m = null;
            try {
                if ("hotelReviews".equals(name)) {
                    m = NameTestingHotel.class.getDeclaredMethod("getReviews");
                } else if ("hotelAddress".equals(name)) {
                    m = NameTestingHotel.class.getDeclaredMethod("setAddress", String.class);
                } else {
                    m = NameTestingHotel.class.getDeclaredField(valueMapping.get(name));
                }
            } catch (NoSuchFieldException | NoSuchMethodException e) {
                fail();
            }
            String actualValue = serializer.getSerializerMemberName(m);
            assertEquals(name, actualValue, String.format(
                "The expect field name %s does not the same as actual field name %s.", name, actualValue));
        });
    }
}
