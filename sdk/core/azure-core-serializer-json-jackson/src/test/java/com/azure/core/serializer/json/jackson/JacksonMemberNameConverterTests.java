// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.serializer.MemberNameConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests {@link JacksonJsonSerializer JacksonJsonSerializer's} {@link MemberNameConverter} functionality.
 */
public class JacksonMemberNameConverterTests {
    private static final String EXPECT_VALUE_IN_FIELD = "expectFieldName";
    private static final String EXPECT_VALUE_IN_METHOD = "expectMethodName";
    private static MemberNameConverter memberNameConverter;

    @BeforeAll
    public static void setup() {
        memberNameConverter = new JacksonJsonSerializerProvider().createInstance();
    }

    @Test
    public void testPropertyNameOnFieldName() throws NoSuchFieldException {
        class LocalHotel {
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals("hotelName", memberNameConverter.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnIgnoredFieldName() throws NoSuchFieldException {
        class LocalHotel {
            @JsonIgnore
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertNull(memberNameConverter.convertMemberName(f));
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
        assertNull(memberNameConverter.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotation() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty(value = EXPECT_VALUE_IN_FIELD)
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals(EXPECT_VALUE_IN_FIELD, memberNameConverter.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithEmptyValue() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty(value = "")
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");

        assertEquals("hotelName", memberNameConverter.convertMemberName(f));
    }

    @Test
    public void testPropertyNameOnFieldAnnotationWithNullValue() throws NoSuchFieldException {
        class LocalHotel {
            @JsonProperty()
            String hotelName;
        }
        Field f = LocalHotel.class.getDeclaredField("hotelName");
        assertEquals("hotelName", memberNameConverter.convertMemberName(f));
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
        assertEquals("hotelName", memberNameConverter.convertMemberName(getterM));
        Method setterM = LocalHotel.class.getDeclaredMethod("setHotelName", String.class);
        assertNull(memberNameConverter.convertMemberName(setterM));
        Method getterWithIs = LocalHotel.class.getDeclaredMethod("isFlag");
        assertEquals("flag", memberNameConverter.convertMemberName(getterWithIs));
        Method setterWithIs = LocalHotel.class.getDeclaredMethod("setFlag", boolean.class);
        assertNull(memberNameConverter.convertMemberName(setterWithIs));
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
        assertNull(memberNameConverter.convertMemberName(getterM));
        Method setterM = LocalHotel.class.getDeclaredMethod("hotelName2", String.class);
        assertNull(memberNameConverter.convertMemberName(setterM));
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

        assertNull(memberNameConverter.convertMemberName(getterM1));
        assertEquals("hotelGetId", memberNameConverter.convertMemberName(getterM2));
        assertNull(memberNameConverter.convertMemberName(getterM3));
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
        assertNull(memberNameConverter.convertMemberName(m));
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
        assertEquals(EXPECT_VALUE_IN_METHOD, memberNameConverter.convertMemberName(m));
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
        assertEquals("hotelName", memberNameConverter.convertMemberName(m));
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

        assertEquals("hotelName", memberNameConverter.convertMemberName(m));
    }

    @Test
    public void testPropertyNameOnConstructor() {
        Constructor<?>[] constructors = Hotel.class.getConstructors();
        assertEquals(1, constructors.length);

        assertNull(memberNameConverter.convertMemberName(constructors[0]));
    }

    @Test
    public void classWithPublicProperties() {
        Set<String> expected = new HashSet<>();
        expected.add("age");
        expected.add("name");

        Set<String> actual = getAllDeclaredMembers(NoAnnotationsPublicFields.class)
            .map(memberNameConverter::convertMemberName)
            .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @Test
    public void classWithGettersWithoutAnnotations() {
        Set<String> expected = new HashSet<>();
        expected.add("age");
        expected.add("name");

        Set<String> actual = getAllDeclaredMembers(NoAnnotationsGetters.class)
            .map(memberNameConverter::convertMemberName)
            .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    @Test
    public void classWithGettersWithAnnotations() {
        Set<String> expected = new HashSet<>();
        expected.add("_age");
        expected.add("_name");

        Set<String> actual = getAllDeclaredMembers(GettersWithAnnotations.class)
            .map(memberNameConverter::convertMemberName)
            .collect(Collectors.toSet());

        assertEquals(expected, actual);
    }

    private static Stream<Member> getAllDeclaredMembers(Class<?> clazz) {
        List<Member> members = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        members.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        members.addAll(Arrays.asList(clazz.getDeclaredConstructors()));

        return members.stream();
    }

    private static final class NoAnnotationsPublicFields {
        public int age;
        public String name;

        public NoAnnotationsPublicFields(int age, String name) {
            this.age = age;
            this.name = name;
        }
    }

    private static final class NoAnnotationsGetters {
        private int age;
        private String name;

        public int getAge() {
            return age;
        }

        public String getName() {
            return name;
        }

        public NoAnnotationsGetters(int age, String name) {
            this.age = age;
            this.name = name;
        }
    }

    private static final class GettersWithAnnotations {
        private int age;
        private String name;

        @JsonProperty("_age")
        public int getAge() {
            return age;
        }

        @JsonProperty("_name")
        public String getName() {
            return name;
        }

        public GettersWithAnnotations(int age, String name) {
            this.age = age;
            this.name = name;
        }
    }
}
