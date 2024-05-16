// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.version.tests;

import com.azure.core.serializer.json.jackson.JacksonJsonSerializer;
import com.azure.core.serializer.json.jackson.JacksonJsonSerializerBuilder;
import com.azure.core.version.tests.models.GettersWithAnnotations;
import com.azure.core.version.tests.models.Hotel;
import com.azure.core.version.tests.models.NoAnnotationsGetters;
import com.azure.core.version.tests.models.NoAnnotationsPublicFields;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MemberNameConverterTests {
    private static final String EXPECT_VALUE_IN_FIELD = "expectFieldName";
    private static final String EXPECT_VALUE_IN_METHOD = "expectMethodName";

    private static final JacksonJsonSerializer JACKSON_JSON_SERIALIZER;

    static {
        ObjectMapper mapper = new ObjectMapper();

        // Configure the mapper with non-private field serialization.
        mapper.setVisibility(mapper.getVisibilityChecker().withFieldVisibility(JsonAutoDetect.Visibility.NON_PRIVATE));

        JACKSON_JSON_SERIALIZER = new JacksonJsonSerializerBuilder().serializer(mapper).build();
    }

    @Test
    public void testPropertyNameOnFieldName() throws NoSuchFieldException {
        class LocalHotel {
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals("hotelName", JACKSON_JSON_SERIALIZER.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnIgnoredFieldName() throws NoSuchFieldException {
        class LocalHotel {
            @JsonIgnore
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertNull(JACKSON_JSON_SERIALIZER.convertMemberName(f));
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
        assertNull(JACKSON_JSON_SERIALIZER.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty(value = EXPECT_VALUE_IN_FIELD)
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals(EXPECT_VALUE_IN_FIELD, JACKSON_JSON_SERIALIZER.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty(value = "")
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");

        assertEquals("hotelName", JACKSON_JSON_SERIALIZER.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithNullValue() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals("hotelName", JACKSON_JSON_SERIALIZER.convertMemberName(f));
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
        assertEquals("hotelName", JACKSON_JSON_SERIALIZER.convertMemberName(getterM));
        Method setterM = LocalHotel.class.getDeclaredMethod("setHotelName", String.class);
        assertNull(JACKSON_JSON_SERIALIZER.convertMemberName(setterM));
        Method getterWithIs = LocalHotel.class.getDeclaredMethod("isFlag");
        assertEquals("flag", JACKSON_JSON_SERIALIZER.convertMemberName(getterWithIs));
        Method setterWithIs = LocalHotel.class.getDeclaredMethod("setFlag", boolean.class);
        assertNull(JACKSON_JSON_SERIALIZER.convertMemberName(setterWithIs));
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
        assertNull(JACKSON_JSON_SERIALIZER.convertMemberName(getterM));
        Method setterM = LocalHotel.class.getDeclaredMethod("hotelName2", String.class);
        assertNull(JACKSON_JSON_SERIALIZER.convertMemberName(setterM));
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

        assertNull(JACKSON_JSON_SERIALIZER.convertMemberName(getterM1));
        assertEquals("hotelGetId", JACKSON_JSON_SERIALIZER.convertMemberName(getterM2));
        assertNull(JACKSON_JSON_SERIALIZER.convertMemberName(getterM3));
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
        assertNull(JACKSON_JSON_SERIALIZER.convertMemberName(m));
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
        assertEquals(EXPECT_VALUE_IN_METHOD, JACKSON_JSON_SERIALIZER.convertMemberName(m));
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
        assertEquals("hotelName", JACKSON_JSON_SERIALIZER.convertMemberName(m));
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

        assertEquals("hotelName", JACKSON_JSON_SERIALIZER.convertMemberName(m));
    }

    @Test
    public void testPropertyNameOnConstructor() {
        Constructor<?>[] constructors = Hotel.class.getConstructors();
        assertEquals(1, constructors.length);

        assertNull(JACKSON_JSON_SERIALIZER.convertMemberName(constructors[0]));
    }

    @ParameterizedTest
    @MethodSource("classConversionSupplier")
    public <T> void classConversion(T object, JacksonJsonSerializer converter, Set<String> expected) {
        Set<String> actual = getAllDeclaredMembers(object.getClass()).map(converter::convertMemberName)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> classConversionSupplier() {
        return Stream.of(
            Arguments.of(new NoAnnotationsPublicFields(50, "John Doe"), JACKSON_JSON_SERIALIZER,
                new HashSet<>(Arrays.asList("age", "name"))),

            Arguments.of(new NoAnnotationsGetters(50, "John Doe"), JACKSON_JSON_SERIALIZER,
                new HashSet<>(Arrays.asList("age", "name"))),

            Arguments.of(new GettersWithAnnotations().setAge(50).setName("John Doe"), JACKSON_JSON_SERIALIZER,
                new HashSet<>(Arrays.asList("_age", "_name"))));
    }

    private static Stream<Member> getAllDeclaredMembers(Class<?> clazz) {
        List<Member> members = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        members.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        members.addAll(Arrays.asList(clazz.getDeclaredConstructors()));

        return members.stream();
    }
}
